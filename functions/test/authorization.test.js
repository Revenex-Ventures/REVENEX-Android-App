const { test } = require('node:test');
const assert = require('node:assert/strict');
const { authorize } = require('../src/authorization');

async function invoke({ token, claims, body = {}, path = '/provisionUsers', method = 'POST', error } = {}) {
  let called = false;
  const req = { path, method, body, headers: token ? { authorization: `Bearer ${token}` } : {} };
  const res = { statusCode: 200, status(code) { this.statusCode = code; return this; }, json(value) { this.body = value; return this; } };
  const verify = async (value, revoked) => {
    assert.equal(value, token);
    assert.equal(revoked, true);
    if (error) throw error;
    return claims;
  };
  await authorize(verify)(req, res, () => { called = true; });
  return { req, res, called };
}
const admin = { uid: 'principal', role: 'PRINCIPAL', schoolId: 's1', sessionId: '2026-2027' };
test('anonymous mutation is rejected', async () => assert.equal((await invoke()).res.statusCode, 401));
test('invalid or revoked token is rejected', async () => assert.equal((await invoke({ token: 'bad', error: Error('revoked') })).res.statusCode, 401));
test('student cannot provision users', async () => assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'STUDENT' } })).res.statusCode, 403));
test('administrator cannot select a different school', async () => assert.equal((await invoke({ token: 'ok', claims: admin, body: { schoolId: 's2' } })).res.statusCode, 403));
test('claims establish school and session instead of global defaults', async () => {
  const result = await invoke({ token: 'ok', claims: admin });
  assert.equal(result.called, true);
  assert.equal(result.req.body.schoolId, 's1');
  assert.equal(result.req.body.sessionId, '2026-2027');
});
test('provisioning cannot override the active session', async () => assert.equal((await invoke({ token: 'ok', claims: admin, body: { sessionId: 'other' } })).res.statusCode, 403));
test('teacher cannot record cash payments', async () => assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'TEACHER' }, path: '/recordOfflinePayment' })).res.statusCode, 403));
test('accountant may record school-scoped offline payment', async () => assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'ACCOUNTANT' }, path: '/recordOfflinePayment' })).called, true));
test('all authenticated personas can only register their own scoped device', async () => assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'PARENT' }, path: '/registerDevice' })).called, true));
test('only staff may publish role-targeted notifications', async () => {
  assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'TEACHER' }, path: '/publishNotification' })).called, true);
  assert.equal((await invoke({ token: 'ok', claims: { ...admin, role: 'STUDENT' }, path: '/publishNotification' })).res.statusCode, 403);
});
test('incomplete tenant claims are denied', async () => assert.equal((await invoke({ token: 'ok', claims: { uid: 'x', role: 'ADMIN' } })).res.statusCode, 403));
test('health and webhook use their own trust boundary', async () => {
  assert.equal((await invoke({ path: '/health', method: 'GET' })).called, true);
  assert.equal((await invoke({ path: '/razorpayWebhook' })).called, true);
});
test('unknown route fails closed', async () => assert.equal((await invoke({ token: 'ok', claims: admin, path: '/newAdminRoute' })).res.statusCode, 403));
test('trailing slash cannot bypass route policy', async () => assert.equal((await invoke({ path: '/provisionUsers/' })).res.statusCode, 401));
