const MAX_PAYMENT_PAISE = 10_000_000_00;

function normalizeStudentId(value) {
  if (typeof value !== 'string' || !/^[A-Za-z0-9_-]{1,128}$/.test(value)) {
    throw new Error('studentId is invalid');
  }
  return value;
}

function parsePaise(value, field) {
  const amount = typeof value === 'number' ? value : Number(value);
  if (!Number.isSafeInteger(amount) || amount <= 0 || amount > MAX_PAYMENT_PAISE) {
    throw new Error(`${field} must be a positive whole number of paise`);
  }
  return amount;
}

function callerMayPayForStudent(user, studentId) {
  if (['PRINCIPAL', 'ADMIN', 'ACCOUNTANT'].includes(user.role)) return true;
  return ['PARENT', 'STUDENT'].includes(user.role)
    && Array.isArray(user.studentIds)
    && user.studentIds.includes(studentId);
}

function payableAmount({ requestedAmount, pendingAmount }) {
  const pending = parsePaise(pendingAmount, 'Outstanding fee balance');
  if (requestedAmount === undefined || requestedAmount === null) return pending;
  const requested = parsePaise(requestedAmount, 'amount');
  if (requested > pending) throw new Error('amount exceeds the outstanding fee balance');
  return requested;
}

module.exports = { callerMayPayForStudent, normalizeStudentId, payableAmount };
