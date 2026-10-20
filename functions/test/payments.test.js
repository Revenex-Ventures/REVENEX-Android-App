const { test } = require('node:test');
const assert = require('node:assert/strict');
const { callerMayPayForStudent, normalizeStudentId, payableAmount } = require('../src/payments');

test('only finance staff or linked families can pay a student fee', () => {
  assert.equal(callerMayPayForStudent({ role: 'ADMIN' }, 'stu_1'), true);
  assert.equal(callerMayPayForStudent({ role: 'PARENT', studentIds: ['stu_1'] }, 'stu_1'), true);
  assert.equal(callerMayPayForStudent({ role: 'STUDENT', studentIds: ['stu_1'] }, 'stu_2'), false);
  assert.equal(callerMayPayForStudent({ role: 'TEACHER' }, 'stu_1'), false);
});

test('payment amount is bounded by the ledger balance', () => {
  assert.equal(payableAmount({ pendingAmount: 12500 }), 12500);
  assert.equal(payableAmount({ pendingAmount: 12500, requestedAmount: 5000 }), 5000);
  assert.throws(() => payableAmount({ pendingAmount: 12500, requestedAmount: 12501 }));
  assert.throws(() => payableAmount({ pendingAmount: 0 }));
});

test('student identifiers cannot escape the school document namespace', () => {
  assert.equal(normalizeStudentId('stu_2026-1042'), 'stu_2026-1042');
  assert.throws(() => normalizeStudentId('../another-school'));
});
