/**
 * PHASE 0 ACCEPTANCE — Firestore Security Rules test.
 * Runs against the local Firestore emulator:
 *   npm run test:rules   (in functions/)
 *
 * Proves, with a real rules engine:
 *  (a) a client SDK attempting to write fees/payments/attendance/marks directly
 *      is REJECTED for every role (backend-only writes);
 *  (b) tenant isolation + role scoping hold (student vs parent vs staff vs
 *      cross-school);
 *  (c) the backend (admin context, i.e. Cloud Functions) CAN write those docs.
 */
const fs = require('node:fs');
const path = require('node:path');
const { initializeTestEnvironment } = require('@firebase/rules-unit-testing');

const PROJECT = 'demo-revenex';
const RULES_FILE = path.resolve(__dirname, '..', '..', 'firestore.rules');

const results = [];
let count = 0;

async function expect(promise, shouldSucceed, label) {
  count += 1;
  const p = promise().then(
    () => true,
    () => false
  );
  const ok = (await p) === shouldSucceed;
  const outcome = shouldSucceed ? 'ALLOW' : 'DENY';
  results.push({ ok, label, outcome });
  if (!ok) {
    const actual = shouldSucceed ? 'DENY' : 'ALLOW';
    console.error(`[FAIL] expected ${outcome}, got ${actual}: ${label}`);
  }
}

async function main() {
  const rules = fs.readFileSync(RULES_FILE, 'utf8');

  const env = await initializeTestEnvironment({
    projectId: PROJECT,
    firestore: { host: '127.0.0.1', port: 8085, rules },
  });

  const anon = env.unauthenticatedContext().firestore();

  const claims = (role, schoolId, extra = {}) => ({
    role, schoolId, sessionId: '2026-2027', ...extra,
  });
  const student1 = env.authenticatedContext('student1', claims('STUDENT', 's1')).firestore();
  const student2 = env.authenticatedContext('student2', claims('STUDENT', 's1')).firestore();
  const parent = env.authenticatedContext('parent1', claims('PARENT', 's1', { studentIds: ['student1', 'student2'] })).firestore();
  const teacher = env.authenticatedContext('teacher1', claims('TEACHER', 's1')).firestore();
  const principal = env.authenticatedContext('principal1', claims('PRINCIPAL', 's1')).firestore();
  const crossSchool = env.authenticatedContext('teacher2', claims('TEACHER', 's2')).firestore();

  // ---- Seed canonical docs "as the backend" (rules bypassed, Admin-SDK model) ----
  const seed = {
    'schools/s1': { name: 'School One' },
    'schools/s2': { name: 'School Two' },
    'schools/s1/students/student1': { schoolId: 's1', sessionId: '2026-2027', name: 'Student One', admissionNumber: 'S001' },
    'schools/s1/students/student2': { schoolId: 's1', sessionId: '2026-2027', name: 'Student Two', admissionNumber: 'S002' },
    'schools/s1/teachers/teacher1': { schoolId: 's1', name: 'Teacher One', employeeId: 'T001' },
    'schools/s1/fees/fee1': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student1', amountPaise: 50000, paidPaise: 20000, status: 'PARTIAL' },
    'schools/s1/fees/fee2': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student2', amountPaise: 50000, paidPaise: 0, status: 'PENDING' },
    'schools/s1/payments/p1': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student1', amountPaise: 20000, mode: 'ONLINE', status: 'SUCCESS' },
    'schools/s1/attendance/att1': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student1', date: '2026-08-01', present: true },
    'schools/s1/marks/m1': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student1', subject: 'MATH', marksObtained: 80, maxMarks: 100 },
    'schools/s1/report_cards/rc1': { schoolId: 's1', sessionId: '2026-2027', studentId: 'student1', grade: 'A' },
    'schools/s1/notices/n1': { schoolId: 's1', sessionId: '2026-2027', title: 'Assembly', audience: 'all' },
    'schools/s1/notifications/notif1': { schoolId: 's1', studentId: 'student1', recipientId: 'student1', body: 'Fee due' },
    'schools/s1/sessions/sess2026': { schoolId: 's1', name: '2026-2027', active: true },
    'schools/s1/classes/c10a': { schoolId: 's1', sessionId: '2026-2027', grade: '10', section: 'A' },
    'schools/s1/audit_log/log1': { schoolId: 's1', actor: 'backend', action: 'payment.create', amountPaise: 20000 },
    'schools/s1/users/student1': { schoolId: 's1', fullName: 'Student One' },
    'schools/s1/messages/msg1': { schoolId: 's1', participantIds: ['parent1', 'teacher1'], senderId: 'parent1', body: 'Hi' },
  };
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await Promise.all(Object.entries(seed).map(([docPath, data]) => db.doc(docPath).set(data)));
  });

  // ================= 1. Unauthenticated — everything denied =================
  await expect(() => anon.doc('schools/s1/fees/fee1').get(), false, 'anon: read fee');
  await expect(() => anon.doc('schools/s1/payments/pX').set({ schoolId: 's1', studentId: 'student1', amountPaise: 1 }), false, 'anon: create payment');

  // ========== 2. Client can NEVER write money/marks/attendance ==============
  const sensitiveWrites = [
    ['schools/s1/fees/feeX', { schoolId: 's1', studentId: 'student1', amountPaise: 1 }],
    ['schools/s1/payments/pX', { schoolId: 's1', studentId: 'student1', amountPaise: 1 }],
    ['schools/s1/attendance/attX', { schoolId: 's1', studentId: 'student1', date: '2026-08-02', present: true }],
    ['schools/s1/marks/mX', { schoolId: 's1', studentId: 'student1', subject: 'SCI', marksObtained: 90, maxMarks: 100 }],
    ['schools/s1/report_cards/rcX', { schoolId: 's1', studentId: 'student1', grade: 'B' }],
  ];
  for (const [docPath, data] of sensitiveWrites) {
    for (const [who, ctx] of [['student', student1], ['teacher', teacher], ['principal', principal], ['parent', parent]]) {
      await expect(() => ctx.doc(docPath).set(data), false, `${who}: direct create ${docPath.split('/').pop()}`);
    }
  }
  await expect(() => student1.doc('schools/s1/fees/fee1').update({ paidPaise: 90000 }), false, 'student: update payment amounts on fee');
  await expect(() => teacher.doc('schools/s1/fees/fee1').update({ paidPaise: 90000 }), false, 'teacher: update payment amounts on fee');
  await expect(() => principal.doc('schools/s1/fees/fee1').update({ paidPaise: 90000 }), false, 'principal: update payment amounts on fee');
  await expect(() => parent.doc('schools/s1/payments/p1').delete(), false, 'parent: delete payment');
  await expect(() => student1.doc('schools/s1/attendance/att1').delete(), false, 'student: delete attendance');

  // =============== 3. Backend (Admin SDK) CAN write those ===================
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await expect(() => db.doc('schools/s1/payments/backendPayment').set({ schoolId: 's1', studentId: 'student1', amountPaise: 1000 }), true, 'backend: create payment');
    await expect(() => db.doc('schools/s1/attendance/backendAtt').set({ schoolId: 's1', studentId: 'student1', date: '2026-08-03', present: true }), true, 'backend: create attendance');
    await expect(() => db.doc('schools/s1/marks/backendMark').set({ schoolId: 's1', studentId: 'student1', subject: 'ENG', marksObtained: 88, maxMarks: 100 }), true, 'backend: create marks');
  });

  // ========== 4. Tenant isolation + role-scoped reads (audit F#2) ============
  await expect(() => student1.doc('schools/s1/fees/fee1').get(), true, 'student1: read own fee');
  await expect(() => student1.doc('schools/s1/fees/fee2').get(), false, 'student1: read another student fee');
  await expect(() => student1.doc('schools/s1/teachers/teacher1').get(), false, 'student: read teacher data (audit F#2)');
  await expect(() => student1.doc('schools/s1/attendance/att1').get(), true, 'student1: read own attendance');
  await expect(() => student1.doc('schools/s1/marks/m1').get(), true, 'student1: read own marks');
  await expect(() => parent.doc('schools/s1/fees/fee2').get(), true, 'parent: read linked child fee (multi-child)');
  await expect(() => parent.doc('schools/s1/payments/p1').get(), true, 'parent: read linked child payment');
  await expect(() => parent.doc('schools/s1/report_cards/rc1').get(), true, 'parent: read linked child report card');
  await expect(() => teacher.doc('schools/s1/fees/fee1').get(), true, 'teacher: staff read fee');
  await expect(() => crossSchool.doc('schools/s1/fees/fee1').get(), false, 'cross-school teacher: denied');
  await expect(() => crossSchool.doc('schools/s1/notices/n1').get(), false, 'cross-school teacher: denied notice read');

  // ========== 5. Writes that ARE allowed for staff, with scoping ============
  await expect(() => principal.doc('schools/s1/classes/c11a').set({ schoolId: 's1', sessionId: '2026-2027', grade: '11', section: 'A' }), true, 'principal: create session-scoped class');
  await expect(() => teacher.doc('schools/s1/notices/n2').set({ schoolId: 's1', sessionId: '2026-2027', title: 'PTM', audience: 'class' }), true, 'teacher: create session-scoped notice');
  await expect(() => teacher.doc('schools/s1/notices/nBad').set({ schoolId: 's1', title: 'No session' }), false, 'teacher: notice missing sessionId');
  await expect(() => teacher.doc('schools/s1/notices/nBad2').set({ schoolId: 's1', sessionId: '1999-2000', title: 'Old session' }), false, 'teacher: notice wrong sessionId');
  await expect(() => student1.doc('schools/s1/notices/n3').set({ schoolId: 's1', sessionId: '2026-2027', title: 'x' }), false, 'student: cannot create notice');
  await expect(() => principal.doc('schools/s1/notifications/nX').set({ schoolId: 's1', recipientId: 'student1', body: 'hi' }), false, 'principal: cannot create notification (backend-only)');
  await expect(() => student1.doc('schools/s1/notifications/notif1').get(), true, 'student: read own notification');

  // ========== 6. Legacy top-level (pre-audit) paths are DEFAULT-DENY =========
  await expect(() => student1.doc('students/old').set({ schoolId: 's1', name: 'x' }), false, 'student: legacy top-level students write');
  await expect(() => teacher.doc('fees/old').set({ schoolId: 's1', studentId: 'student1', amountPaise: 1 }), false, 'teacher: legacy top-level fees write');
  await expect(() => student1.doc('fees/fee1').get(), false, 'student: legacy top-level fees read');

  // ========== 7. Self profile update limited to allowed keys =================
  await expect(() => student1.doc('schools/s1/users/student1').update({ fullName: 'Updated Name' }), true, 'student: update own profile name');
  await expect(() => student1.doc('schools/s1/users/student1').update({ role: 'PRINCIPAL' }), false, 'student: cannot self-elevate role');

  // ========== 8. Messages / complaints =======================================
  await expect(() => parent.doc('schools/s1/complaints/c1').set({ schoolId: 's1', studentId: 'student1', subject: 'bus' }), true, 'parent: file complaint');
  await expect(() => parent.doc('schools/s1/messages/m2').set({ schoolId: 's1', participantIds: ['parent1', 'teacher1'], senderId: 'parent1', body: 'Hi' }), true, 'parent: send message to teacher');
  await expect(() => teacher.doc('schools/s1/messages/msg1').get(), true, 'teacher: read class-teacher message');

  // ---- Report ---------------------------------------------------------------
  const passed = results.filter((r) => r.ok).length;
  console.log(`\n=== Rules test summary: ${passed}/${results.length} passed ===`);
  if (passed !== results.length) {
    console.error(`${results.length - passed} FAILURES — see [FAIL] lines above`);
    process.exit(1);
  }

  await env.cleanup();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});