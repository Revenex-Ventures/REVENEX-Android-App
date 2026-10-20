const { onRequest } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const crypto = require('node:crypto');
const Razorpay = require('razorpay');

const { config, validate } = require('./src/config');
const log = require('./src/logger');
const { initMonitor, captureAndLog } = require('./src/monitor');
const { provisionUsers } = require('./src/provision');
const { authorize } = require('./src/authorization');
const { chunk, deviceId, normalizeDeviceToken, normalizeNotificationPayload } = require('./src/notifications');
const { callerMayPayForStudent, normalizeStudentId, payableAmount } = require('./src/payments');

validate();
admin.initializeApp();
initMonitor(config.sentryDsn, config.env);

let razorpay = null;
if (config.razorpayKeyId) {
  razorpay = new Razorpay({
    key_id: config.razorpayKeyId,
    key_secret: process.env.RAZORPAY_KEY_SECRET || 'dummy_secret',
  });
}

const app = express();
app.use(cors({ origin: true }));
app.use(express.json({
  limit: '1mb',
  verify: (req, res, buf) => {
    req.rawBody = buf;
  }
}));

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
app.use(authorize((token, checkRevoked) => admin.auth().verifyIdToken(token, checkRevoked)));

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

// --- FCM device registration -------------------------------------------------
// A device record is tenant-, session-, and user-scoped. The client never
// chooses those boundaries; they are taken from its verified custom claims.
app.post('/registerDevice', async (req, res, next) => {
  try {
    const fcmToken = normalizeDeviceToken(req.body.fcmToken);
    const platform = typeof req.body.platform === 'string'
      ? req.body.platform.trim().slice(0, 32)
      : 'android';
    const deviceRef = admin.firestore()
      .collection('schools').doc(req.user.schoolId)
      .collection('devices').doc(deviceId(req.user.uid, fcmToken));

    await deviceRef.set({
      uid: req.user.uid,
      token: fcmToken,
      role: req.user.role,
      sessionId: req.user.sessionId,
      platform,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, { merge: true });
    res.json({ status: 'SUCCESS' });
  } catch (err) {
    if (err.message && /^(fcmToken)/.test(err.message)) {
      return res.status(400).json({ status: 'ERROR', message: err.message });
    }
    return next(err);
  }
});

// --- Auditable, role-targeted notification delivery -------------------------
app.post('/publishNotification', async (req, res, next) => {
  try {
    const payload = normalizeNotificationPayload(req.body);
    const db = admin.firestore();
    const schoolRef = db.collection('schools').doc(req.user.schoolId);
    const notificationRef = schoolRef.collection('notifications').doc();
    const now = new Date().toISOString();

    await notificationRef.set({
      id: notificationRef.id,
      title: payload.title,
      message: payload.body,
      category: payload.category,
      targetRoles: payload.targetRoles,
      schoolId: req.user.schoolId,
      sessionId: req.user.sessionId,
      createdBy: req.user.uid,
      createdAt: now,
      isRead: false,
    });
    await schoolRef.collection('audit_log').add({
      actionType: 'NOTIFICATION_PUBLISHED',
      actionSummary: 'Published a role-targeted school notification',
      actorId: req.user.uid,
      actorRole: req.user.role,
      schoolId: req.user.schoolId,
      sessionId: req.user.sessionId,
      timestamp: now,
    });

    const devices = await schoolRef.collection('devices')
      .where('sessionId', '==', req.user.sessionId).get();
    const recipients = devices.docs.filter((doc) => payload.targetRoles.includes(doc.get('role')));
    let delivered = 0;
    let invalidTokens = 0;
    for (const group of chunk(recipients)) {
      let response;
      try {
        response = await admin.messaging().sendEachForMulticast({
          tokens: group.map((doc) => doc.get('token')),
          notification: { title: payload.title, body: payload.body },
          data: {
            notificationId: notificationRef.id,
            title: payload.title,
            body: payload.body,
            category: payload.category,
          },
          android: { priority: 'high', notification: { channelId: 'revenex_erp_default' } },
        });
      } catch (deliveryError) {
        // The notification document is durable and can be shown in-app even
        // when FCM is temporarily unavailable. A later worker can retry it.
        log.error('notification delivery batch failed', {
          notificationId: notificationRef.id,
          errorCode: deliveryError.code,
          errorMessage: deliveryError.message,
        });
        continue;
      }
      delivered += response.successCount;
      const deletes = [];
      response.responses.forEach((result, index) => {
        const code = result.error && result.error.code;
        if (code === 'messaging/registration-token-not-registered' || code === 'messaging/invalid-registration-token') {
          deletes.push(group[index].ref.delete());
          invalidTokens += 1;
        }
      });
      await Promise.all(deletes);
    }
    log.info('notification published', {
      notificationId: notificationRef.id,
      audienceRoles: payload.targetRoles,
      recipientCount: recipients.length,
      delivered,
      invalidTokens,
    });
    return res.status(201).json({ status: 'SUCCESS', notificationId: notificationRef.id, delivered });
  } catch (err) {
    if (err.message && /^(title|body|targetRoles)/.test(err.message)) {
      return res.status(400).json({ status: 'ERROR', message: err.message });
    }
    return next(err);
  }
});

// --- Bulk provision users + custom claims ---------------------------------------
app.post('/provisionUsers', async (req, res, next) => {
  try {
    const db = admin.firestore();
    const auth = admin.auth();
    const results = await provisionUsers({ db, auth, config,
      schoolId: req.body.schoolId, sessionId: req.body.sessionId });
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

// --- Create Razorpay Order endpoint -------------------------------------------
app.post('/createOrder', async (req, res, next) => {
  try {
    const studentId = normalizeStudentId(req.body.studentId);
    if (!callerMayPayForStudent(req.user, studentId)) {
      return res.status(403).json({ status: 'ERROR', message: 'You cannot make a payment for this student' });
    }
    const schoolRef = admin.firestore().collection('schools').doc(req.user.schoolId);
    const feeRef = schoolRef.collection('fees').doc(`fee_${studentId}`);
    const feeSnap = await feeRef.get();
    if (!feeSnap.exists || feeSnap.get('sessionId') !== req.user.sessionId) {
      return res.status(404).json({ status: 'ERROR', message: 'No active fee record found for this student' });
    }
    const amount = payableAmount({
      requestedAmount: req.body.amount,
      pendingAmount: feeSnap.get('feePendingAmount'),
    });
    const receipt = `fee_${req.user.sessionId}_${studentId}_${Date.now()}`;
    const orderRequest = {
      amount,
      currency: 'INR',
      receipt,
      notes: { studentId, schoolId: req.user.schoolId, sessionId: req.user.sessionId, feeId: feeRef.id },
    };
    if (!razorpay) {
      log.info('Mocking Razorpay order creation (no key configured)');
      return res.json({
        status: 'SUCCESS',
        order: {
          id: `order_mock_${crypto.randomBytes(8).toString('hex')}`,
          ...orderRequest,
        }
      });
    }

    const order = await razorpay.orders.create(orderRequest);

    log.info('Razorpay order created successfully', { orderId: order.id });
    res.json({ status: 'SUCCESS', order });
  } catch (err) {
    if (err.message && /^(studentId|amount|Outstanding fee balance)/.test(err.message)) {
      return res.status(400).json({ status: 'ERROR', message: err.message });
    }
    next(err);
  }
});

// --- Razorpay Webhook listener endpoint -----------------------------------------
app.post('/razorpayWebhook', async (req, res, next) => {
  try {
    const signature = req.headers['x-razorpay-signature'];
    const webhookSecret = process.env.RAZORPAY_WEBHOOK_SECRET || 'dummy_secret';

    let isValid = false;
    if (signature && req.rawBody) {
      const expectedSignature = crypto
        .createHmac('sha256', webhookSecret)
        .update(req.rawBody)
        .digest('hex');
      isValid = (signature === expectedSignature);
    }

    if (!isValid && config.isEmulator) {
      log.info('Allowing webhook signature validation fallback in emulator mode');
      isValid = true;
    }

    if (!isValid) {
      log.warn('Invalid Razorpay signature matching failed');
      return res.status(400).json({ status: 'ERROR', message: 'Invalid webhook signature' });
    }

    const event = req.body.event;
    log.info('Processing Razorpay webhook event', { event });

    if (event === 'payment.captured') {
      const payment = req.body.payload.payment.entity;
      const orderId = payment.order_id;
      const paymentId = payment.id;
      const amountPaid = payment.amount;
      const studentId = payment.notes ? payment.notes.studentId : null;
      const schoolId = payment.notes ? payment.notes.schoolId : config.schoolId;

      if (!studentId) {
        log.warn('Missing studentId in payment notes, skipping update');
        return res.json({ status: 'SUCCESS', message: 'Skipped: missing studentId' });
      }

      const db = admin.firestore();
      const schoolRef = db.collection('schools').doc(schoolId);

      const txnRef = schoolRef.collection('payments').doc(paymentId);
      const txnDoc = await txnRef.get();
      if (txnDoc.exists) {
        log.info('Payment transaction already processed', { paymentId });
        return res.json({ status: 'SUCCESS', message: 'Already processed' });
      }

      await db.runTransaction(async (transaction) => {
        transaction.set(txnRef, {
          id: paymentId,
          studentId,
          amountPaise: amountPaid,
          paymentMethod: 'Razorpay Online',
          feeHead: 'Term 2 Tuition & Activity Dues',
          razorpayPaymentId: paymentId,
          razorpayOrderId: orderId,
          status: 'SUCCESS',
          timestamp: new Date().toISOString(),
          schoolId,
          sessionId: config.sessionId
        });

        const feeId = `fee_${studentId}`;
        const feeRef = schoolRef.collection('fees').doc(feeId);
        const feeSnap = await transaction.get(feeRef);

        if (feeSnap.exists) {
          const feeData = feeSnap.data();
          const newPaid = (feeData.paidAmount || 0) + amountPaid;
          const totalFee = feeData.totalFee || 2600000;
          const newPending = Math.max(0, totalFee - newPaid);
          const newStatus = newPending === 0 ? 'PAID' : (newPaid > 0 ? 'PARTIAL' : 'PENDING');

          transaction.update(feeRef, {
            paidAmount: newPaid,
            feePendingAmount: newPending,
            status: newStatus,
            transactions: admin.firestore.FieldValue.arrayUnion(paymentId)
          });
        }
      });

      log.info('Razorpay payment processed successfully via webhook', { paymentId, studentId });
    }

    res.json({ status: 'SUCCESS' });
  } catch (err) {
    next(err);
  }
});

// --- Record Offline Cash/Cheque Payment endpoint -------------------------------
app.post('/recordOfflinePayment', async (req, res, next) => {
  try {
    const { studentId, amountPaid, paymentMethod, feeHead, receivedBy } = req.body;
    if (!studentId || !amountPaid) {
      return res.status(400).json({ status: 'ERROR', message: 'Missing studentId or amountPaid parameter' });
    }

    const db = admin.firestore();
    const sid = req.body.schoolId || config.schoolId;
    const schoolRef = db.collection('schools').doc(sid);

    const paymentId = `pay_offline_${crypto.randomBytes(8).toString('hex')}`;
    const receiptNumber = `REV-2026-${Math.floor(1000 + Math.random() * 9000)}`;

    await db.runTransaction(async (transaction) => {
      transaction.set(schoolRef.collection('payments').doc(paymentId), {
        id: paymentId,
        studentId,
        amountPaise: parseInt(amountPaid, 10),
        paymentMethod: paymentMethod || 'Cash',
        feeHead: feeHead || 'Term 2 Tuition & Activity Dues',
        status: 'SUCCESS',
        receiptNumber,
        receivedBy: req.user.uid,
        timestamp: new Date().toISOString(),
        schoolId: sid,
        sessionId: req.user.sessionId
      });

      const feeId = `fee_${studentId}`;
      const feeRef = schoolRef.collection('fees').doc(feeId);
      const feeSnap = await transaction.get(feeRef);

      if (feeSnap.exists) {
        const feeData = feeSnap.data();
        const newPaid = (feeData.paidAmount || 0) + parseInt(amountPaid, 10);
        const totalFee = feeData.totalFee || 2600000;
        const newPending = Math.max(0, totalFee - newPaid);
        const newStatus = newPending === 0 ? 'PAID' : (newPaid > 0 ? 'PARTIAL' : 'PENDING');

        transaction.update(feeRef, {
          paidAmount: newPaid,
          feePendingAmount: newPending,
          status: newStatus,
          transactions: admin.firestore.FieldValue.arrayUnion(paymentId)
        });
      }
    });

    log.info('Offline payment recorded successfully', { paymentId, receiptNumber });
    res.json({ status: 'SUCCESS', paymentId, receiptNumber });
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

exports.api = onRequest({
  cors: true,
  maxInstances: 10,
  region: config.region,
  secrets: ['RAZORPAY_KEY_ID', 'RAZORPAY_KEY_SECRET', 'RAZORPAY_WEBHOOK_SECRET', 'SENTRY_DSN'],
}, app);
