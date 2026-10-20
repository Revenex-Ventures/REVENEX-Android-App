// Exercise the real exported HTTP handler without credentials or cloud writes.
process.env.FUNCTIONS_EMULATOR = 'true';
process.env.GCLOUD_PROJECT = 'demo-revenex';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const http = require('node:http');
const { api } = require('../index');

test('real HTTP handler protects every mutation route', async t => {
  const server = http.createServer(api);
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  t.after(async () => {
    server.closeAllConnections();
    await new Promise(resolve => server.close(resolve));
  });
  const base = `http://127.0.0.1:${server.address().port}`;
  await t.test('health remains public', async () => {
    const response = await fetch(`${base}/health`);
    assert.equal(response.status, 200);
    assert.equal((await response.json()).status, 'HEALTHY');
  });
  for (const route of ['provisionUsers', 'migrateSessionIds', 'rollbackSessionIds',
    'promoteStudents', 'recordOfflinePayment', 'createOrder', 'registerDevice', 'publishNotification', 'PROVISIONUSERS/']) {
    await t.test(`anonymous POST /${route} fails before mutation`, async () => {
      const response = await fetch(`${base}/${route}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}',
      });
      assert.equal(response.status, 401);
      assert.equal((await response.json()).message, 'Authentication required');
    });
  }
});
