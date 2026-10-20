const { test } = require('node:test');
const assert = require('node:assert/strict');
const {
  chunk,
  deviceId,
  normalizeDeviceToken,
  normalizeNotificationPayload,
} = require('../src/notifications');

test('notification payload is bounded and defaults to every supported role', () => {
  const payload = normalizeNotificationPayload({ title: '  Exam update ', body: ' Timetable published. ' });
  assert.equal(payload.title, 'Exam update');
  assert.equal(payload.body, 'Timetable published.');
  assert.equal(payload.category, 'GENERAL');
  assert.ok(payload.targetRoles.includes('STUDENT'));
  assert.ok(payload.targetRoles.includes('ADMIN'));
});

test('notification publisher rejects forged audiences and oversized content', () => {
  assert.throws(() => normalizeNotificationPayload({ title: 'Notice', body: 'x', targetRoles: ['ROOT'] }));
  assert.throws(() => normalizeNotificationPayload({ title: 'x'.repeat(121), body: 'Body' }));
});

test('device tokens have a stable opaque id and reject control characters', () => {
  assert.equal(deviceId('user-a', 'token-a'), deviceId('user-a', 'token-a'));
  assert.notEqual(deviceId('user-a', 'token-a'), deviceId('user-b', 'token-a'));
  assert.equal(normalizeDeviceToken('token-a'), 'token-a');
  assert.throws(() => normalizeDeviceToken('bad\nvalue'));
});

test('multicast batches never exceed Firebase messaging limits', () => {
  const values = Array.from({ length: 1_001 }, (_, index) => index);
  assert.deepEqual(chunk(values).map((batch) => batch.length), [500, 500, 1]);
});
