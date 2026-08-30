const admin = require('firebase-admin');
const { ROLE, buildClaims } = require('./claims');
const log = require('./logger');
const { captureAndLog } = require('./monitor');

/**
 * Bulk-provision Firebase Auth accounts + custom claims for every student,
 * teacher and parent in /schools/{schoolId}. Idempotent: existing accounts
 * are updated (claims refreshed), never duplicated.
 *
 * Route: POST /provisionUsers   (body: { schoolId?, sessionId? } optional)
 */
async function provisionUsers({ db, auth, config, schoolId, sessionId }) {
  const sid = schoolId || config.schoolId;
  const sess = sessionId || config.sessionId;

  const schoolRef = db.collection('schools').doc(sid);
  const studentsSnap = await schoolRef.collection('students').get();
  const teachersSnap = await schoolRef.collection('teachers').get();

  const results = { teachers: 0, students: 0, parents: 0, errors: [] };

  const domain = process.env.PROVISION_EMAIL_DOMAIN || 'revenex.edu.in';

  // --- 1. Teachers -----------------------------------------------------------
  for (const doc of teachersSnap.docs) {
    const teacher = doc.data();
    if (!teacher.employeeId) {
      results.errors.push(`Teacher ${teacher.name || doc.id}: missing employeeId, skipped`);
      continue;
    }
    const email = `${String(teacher.employeeId).toLowerCase().trim()}@${domain}`;
    const password = derivePassword(teacher.dob, 'teacher123');
    try {
      const user = await ensureUser(auth, email, password, teacher.name || doc.id);
      const role = String(teacher.role || '').toUpperCase() === ROLE.CLASS_TEACHER ? ROLE.CLASS_TEACHER : ROLE.TEACHER;
      await auth.setCustomUserClaims(user.uid, buildClaims({ role, schoolId: sid, sessionId: sess, employeeId: teacher.employeeId }));
      results.teachers += 1;
      log.info('provisioned teacher', { uid: user.uid, email });
    } catch (err) {
      results.errors.push(`Teacher ${teacher.name} (${email}): ${err.message}`);
    }
  }

  // --- 2. Build parent map for multi-child (sibling) linking -----------------
  const parentMap = new Map();
  for (const doc of studentsSnap.docs) {
    const student = doc.data();
    if (!student.admissionNumber) continue;
    if (!student.parentEmail || !String(student.parentEmail).trim()) continue;
    const pEmail = String(student.parentEmail).toLowerCase().trim();
    const entry = parentMap.get(pEmail) || {
      name: student.parentName,
      phone: student.parentPhone,
      studentIds: [],
    };
    entry.studentIds.push(doc.id);
    parentMap.set(pEmail, entry);
  }

  // --- 3. Students -----------------------------------------------------------
  for (const doc of studentsSnap.docs) {
    const student = doc.data();
    if (!student.admissionNumber) {
      results.errors.push(`Student ${student.name || doc.id}: missing admissionNumber, skipped`);
      continue;
    }
    const email = `${String(student.admissionNumber).toLowerCase().trim()}@${domain}`;
    const password = derivePassword(student.dob, 'student123');
    try {
      const user = await ensureUser(auth, email, password, student.name || doc.id);
      await auth.setCustomUserClaims(user.uid, buildClaims({
        role: ROLE.STUDENT,
        schoolId: sid,
        sessionId: sess,
        employeeId: undefined,
        studentIds: [doc.id],
      }));
      results.students += 1;
      log.info('provisioned student', { uid: user.uid, email });
    } catch (err) {
      results.errors.push(`Student ${student.name} (${email}): ${err.message}`);
    }
  }

  // --- 4. Parents (linked to all their children) -----------------------------
  for (const [email, info] of parentMap) {
    const phoneDigits = String(info.phone || '').replace(/[^0-9]/g, '');
    const password = phoneDigits.length >= 8 ? phoneDigits.slice(-8) : 'parent123';
    try {
      const user = await ensureUser(auth, email, password, info.name || 'Parent');
      await auth.setCustomUserClaims(user.uid, buildClaims({
        role: ROLE.PARENT,
        schoolId: sid,
        sessionId: sess,
        studentIds: [...new Set(info.studentIds)],
      }));
      results.parents += 1;
      log.info('provisioned parent', { uid: user.uid, email, linkedStudents: info.studentIds.length });
    } catch (err) {
      results.errors.push(`Parent ${info.name} (${email}): ${err.message}`);
    }
  }

  return results;
}

async function ensureUser(auth, email, password, displayName) {
  try {
    return await auth.getUserByEmail(email);
  } catch {
    return auth.createUser({ email, password, displayName });
  }
}

// DOB drives the initial password for staff/students; must be changed on first log-in.
function derivePassword(dob, fallback) {
  if (!dob) return fallback;
  const digits = String(dob).replace(/[^\d]/g, '');
  return digits.length >= 6 ? digits : fallback;
}

module.exports = { provisionUsers, ensureUser, derivePassword };