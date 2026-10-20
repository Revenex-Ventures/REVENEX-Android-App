const crypto = require('node:crypto');
const { ROLES } = require('./claims');

const MAX_TITLE_LENGTH = 120;
const MAX_BODY_LENGTH = 1_000;
const MAX_TARGET_ROLES = ROLES.length;

function requiredText(value, field, maxLength) {
  if (typeof value !== 'string') throw new Error(`${field} is required`);
  const text = value.trim();
  if (!text || text.length > maxLength) throw new Error(`${field} must be between 1 and ${maxLength} characters`);
  return text;
}

function normalizeRoles(value) {
  if (value === undefined) return [...ROLES];
  if (!Array.isArray(value) || value.length === 0 || value.length > MAX_TARGET_ROLES) {
    throw new Error('targetRoles must be a non-empty role list');
  }
  const roles = [...new Set(value)];
  if (roles.some((role) => typeof role !== 'string' || !ROLES.includes(role))) {
    throw new Error('targetRoles contains an unsupported role');
  }
  return roles;
}

function normalizeNotificationPayload(body) {
  const payload = body || {};
  return {
    title: requiredText(payload.title, 'title', MAX_TITLE_LENGTH),
    body: requiredText(payload.body, 'body', MAX_BODY_LENGTH),
    category: typeof payload.category === 'string' && payload.category.trim()
      ? payload.category.trim().slice(0, 48)
      : 'GENERAL',
    targetRoles: normalizeRoles(payload.targetRoles),
  };
}

function deviceId(uid, token) {
  return `${uid}_${crypto.createHash('sha256').update(token).digest('hex').slice(0, 24)}`;
}

function normalizeDeviceToken(value) {
  const token = requiredText(value, 'fcmToken', 4_096);
  // FCM registration tokens are opaque. Reject control characters without
  // inventing a provider-specific format that could reject valid rotations.
  if (/[\u0000-\u001F\u007F]/.test(token)) throw new Error('fcmToken is invalid');
  return token;
}

function chunk(items, size = 500) {
  const result = [];
  for (let index = 0; index < items.length; index += size) result.push(items.slice(index, index + size));
  return result;
}

module.exports = {
  chunk,
  deviceId,
  normalizeDeviceToken,
  normalizeNotificationPayload,
};
