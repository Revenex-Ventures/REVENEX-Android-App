// HTTP Admin SDK calls bypass Firestore Rules: authorize before every handler.
const ADMIN_ROLES = ['PRINCIPAL', 'ADMIN'];
const STAFF_ROLES = [...ADMIN_ROLES, 'ACCOUNTANT', 'TEACHER', 'CLASS_TEACHER'];
const ALL_ROLES = [...STAFF_ROLES, 'PARENT', 'STUDENT', 'TRANSPORT_MANAGER', 'LIBRARIAN'];
const ROUTES = Object.freeze({
  '/provisionusers': ADMIN_ROLES,
  '/migratesessionids': ADMIN_ROLES,
  '/rollbacksessionids': ADMIN_ROLES,
  '/promotestudents': ADMIN_ROLES,
  '/recordofflinepayment': [...ADMIN_ROLES, 'ACCOUNTANT'],
  '/createorder': [...ADMIN_ROLES, 'ACCOUNTANT', 'PARENT', 'STUDENT'],
  '/registerdevice': ALL_ROLES,
  '/publishnotification': STAFF_ROLES,
});

function authorize(verifyIdToken) {
  return async (req, res, next) => {
    // Express routes are case insensitive and accept a trailing slash by default.
    const path = req.path.toLowerCase().replace(/\/+$/, '') || '/';
    if ((req.method === 'GET' && path === '/health') ||
        (req.method === 'POST' && path === '/razorpaywebhook')) return next();
    const match = /^Bearer\s+(\S+)$/i.exec(req.headers.authorization || '');
    if (!match) return res.status(401).json({ status: 'ERROR', message: 'Authentication required' });
    let claims;
    try {
      claims = await verifyIdToken(match[1], true);
    } catch {
      return res.status(401).json({ status: 'ERROR', message: 'Invalid or expired authentication' });
    }
    const allowed = ROUTES[path];
    if (req.method !== 'POST' || !allowed || !allowed.includes(claims.role) ||
        !claims.uid || typeof claims.schoolId !== 'string' || !claims.schoolId ||
        typeof claims.sessionId !== 'string' || !claims.sessionId) {
      return res.status(403).json({ status: 'ERROR', message: 'Insufficient permissions' });
    }
    const body = req.body || {};
    if ((body.schoolId !== undefined && body.schoolId !== claims.schoolId) ||
        (body.sessionId !== undefined && body.sessionId !== claims.sessionId)) {
      return res.status(403).json({ status: 'ERROR', message: 'School or session mismatch' });
    }
    req.user = claims;
    req.body = { ...body, schoolId: claims.schoolId, sessionId: claims.sessionId };
    return next();
  };
}
module.exports = { authorize };
