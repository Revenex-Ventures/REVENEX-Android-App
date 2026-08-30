const { onRequest } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const crypto = require('node:crypto');

const { config, validate } = require('./src/config');
const log = require('./src/logger');
const { initMonitor, captureAndLog } = require('./src/monitor');
const { provisionUsers } = require('./src/provision');

validate();
admin.initializeApp();

const app = express();
app.use(cors({ origin: true }));
app.use(express.json({ limit: '1mb' }));

// --- Request ID + structured logging -----------------------------------------
app.use((req, res, next) => {
  const id = req.headers['x-request-id'] || crypto.randomUUID();
  req.id = id;
  res.setHeader('x-request-id', id);
  log.withRequestId(id);
  next();
});

// --- Simple fixed-window rate limiter (per IP + route) ------------------------
const buckets = new Map();
const WINDOW_MS = Number(process.env.RATE_LIMIT_WINDOW_MS || 60_000);
const MAX_REQ = Number(process.env.RATE_LIMIT_MAX || 120);
app.use((req, res, next) => {
  const key = `${req.ip}|${req.method} ${req.path}`;
  const now = Date.now();
  const b = buckets.get(key);
  if (!b || b.window + WINDOW_MS < now) {
    buckets.set(key, { window: now, count: 1 });
    return next();
  }
  b.count += 1;
  if (b.count > MAX_REQ) {
    log.warn('rate limit exceeded', { method: req.method, path: req.path });
    return res.status(429).json({ status: 'ERROR', message: 'Too many requests' });
  }
  return next();
});

// --- Health check --------------------------------------------------------------
app.get('/health', (req, res) => {
  res.json({
    status: 'HEALTHY',
    env: config.env,
    region: config.region,
    schoolId: config.schoolId,
    timestamp: new Date().toISOString(),
    requestId: req.id,
  });
});

// --- Bulk provision users + custom claims ---------------------------------------
app.post('/provisionUsers', async (req, res, next) => {
  try {
    const db = admin.firestore();
    const auth = admin.auth();
    const results = await provisionUsers({ db, auth, config, ...(req.body || {}) });
    log.info('provisionUsers completed', {
      teachers: results.teachers,
      students: results.students,
      parents: results.parents,
      errors: results.errors.length,
    });
    res.json({ status: 'SUCCESS', results });
  } catch (err) {
    next(err);
  }
});

// --- Migrate sessionId backfill endpoint -------------------------------------------
app.post('/migrateSessionIds', async (req, res, next) => {
  try {
    const db = admin.firestore();
    const sid = req.body.schoolId || config.schoolId;
    const targetSession = req.body.sessionId || config.sessionId;

    const schoolRef = db.collection('schools').doc(sid);
    const collections = ['students', 'teachers', 'attendance', 'fees', 'exams', 'report_cards', 'assignments', 'timetable'];
    const results = {};

    for (const collName of collections) {
      const snap = await schoolRef.collection(collName).get();
      let updated = 0;
      const batch = db.batch();

      snap.forEach(doc => {
        batch.update(doc.ref, { sessionId: targetSession });
        updated++;
      });

      if (updated > 0) {
        await batch.commit();
      }
      results[collName] = updated;
    }

    res.json({
      status: 'SUCCESS',
      message: `Successfully backfilled sessionId = ${targetSession} across database collections.`,
      results
    });
  } catch (err) {
    next(err);
  }
});

// --- Rollback sessionId field endpoint -------------------------------------------
app.post('/rollbackSessionIds', async (req, res, next) => {
  try {
    const db = admin.firestore();
    const sid = req.body.schoolId || config.schoolId;

    const schoolRef = db.collection('schools').doc(sid);
    const collections = ['students', 'teachers', 'attendance', 'fees', 'exams', 'report_cards', 'assignments', 'timetable'];
    const results = {};

    for (const collName of collections) {
      const snap = await schoolRef.collection(collName).get();
      let updated = 0;
      const batch = db.batch();

      snap.forEach(doc => {
        batch.update(doc.ref, { sessionId: admin.firestore.FieldValue.delete() });
        updated++;
      });

      if (updated > 0) {
        await batch.commit();
      }
      results[collName] = updated;
    }

    res.json({
      status: 'SUCCESS',
      message: 'Successfully rolled back and deleted sessionId field across collections.',
      results
    });
  } catch (err) {
    next(err);
  }
});

// --- Promote Students Rollover endpoint -------------------------------------------
app.post('/promoteStudents', async (req, res, next) => {
  try {
    const db = admin.firestore();
    const sid = req.body.schoolId || config.schoolId;
    const { targetClass, targetDivision, targetSession, studentIds } = req.body;

    if (!targetClass || !targetSession || !Array.isArray(studentIds) || studentIds.length === 0) {
      return res.status(400).json({ status: 'ERROR', message: 'Missing targetClass, targetSession, or studentIds array.' });
    }

    const schoolRef = db.collection('schools').doc(sid);
    const results = { promoted: 0, errors: [] };

    for (const studentId of studentIds) {
      try {
        const studentDoc = await schoolRef.collection('students').doc(studentId).get();
        if (!studentDoc.exists) {
          results.errors.push(`Student ID ${studentId} not found.`);
          continue;
        }

        const student = studentDoc.data();
        const newStudentId = `${studentId}_${targetSession}`;

        // Create the new session student record
        const promotedStudent = {
          ...student,
          id: newStudentId,
          classGrade: targetClass,
          division: targetDivision || student.division || 'A',
          sessionId: targetSession,
          attendancePercent: 100.0, // reset for new session
          feeStatus: 'PENDING',
          feePendingAmount: 26000.0
        };

        await schoolRef.collection('students').doc(newStudentId).set(promotedStudent);

        // Auto-generate new session Fee Record for this student
        const newFeeId = `fee_${newStudentId}`;
        const newFeeRecord = {
          id: newFeeId,
          studentId: newStudentId,
          studentName: student.name,
          classGrade: targetClass,
          division: targetDivision || student.division || 'A',
          tuitionFee: 20000.0,
          examFee: 2000.0,
          transportFee: 3000.0,
          labLibraryFee: 1000.0,
          discountScholarship: 0.0,
          totalFee: 26000.0,
          paidAmount: 0.0,
          status: 'PENDING',
          dueDate: `10/06/${targetSession.split('_').slice(-1)[0]}`, // e.g. 10/06/2027
          transactions: [],
          schoolId: sid,
          sessionId: targetSession
        };

        await schoolRef.collection('fees').doc(newFeeId).set(newFeeRecord);
        results.promoted++;
      } catch (err) {
        results.errors.push(`Error promoting Student ID ${studentId}: ${err.message}`);
      }
    }

    res.json({
      status: 'SUCCESS',
      message: `Successfully completed promotion rollover of ${results.promoted} students to Session ${targetSession}.`,
      results
    });
  } catch (err) {
    next(err);
  }
});



// --- Global error handler --------------------------------------------------------
app.use((err, req, res, next) => { // eslint-disable-line no-unused-vars
  captureAndLog({ message: 'Unhandled serverless exception', error: err, request: req });
  res.status(500).json({
    status: 'ERROR',
    message: 'Internal Server Error',
    details: config.isEmulator ? err.message : undefined,
  });
});

exports.api = onRequest({ cors: true, maxInstances: 10, region: config.region }, app);