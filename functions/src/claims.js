// Canonical custom-claim vocabulary. These constants are the ONLY source of
// truth for role names and must stay in sync with firestore.rules.
const ROLE = Object.freeze({
  PRINCIPAL: 'PRINCIPAL',
  ADMIN: 'ADMIN',
  ACCOUNTANT: 'ACCOUNTANT',
  TEACHER: 'TEACHER',
  CLASS_TEACHER: 'CLASS_TEACHER',
  PARENT: 'PARENT',
  STUDENT: 'STUDENT',
  TRANSPORT_MANAGER: 'TRANSPORT_MANAGER',
  LIBRARIAN: 'LIBRARIAN',
});

const ROLES = Object.values(ROLE);

/**
 * Build the custom claims object for a provisioned user.
 * @param {{role: string, schoolId: string, sessionId: string,
 *          employeeId?: string, studentIds?: string[]}} spec
 */
function buildClaims({ role, schoolId, sessionId, employeeId, studentIds }) {
  const claims = { role, schoolId, sessionId };
  if (employeeId) claims.employeeId = employeeId;
  if (Array.isArray(studentIds) && studentIds.length > 0) claims.studentIds = studentIds;
  return claims;
}

module.exports = { ROLE, ROLES, buildClaims };