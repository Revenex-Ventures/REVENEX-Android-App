const { logger } = require('firebase-functions');

// Keys whose values are redacted from every log line.
const SENSITIVE_KEY = /(authorization|api[_-]?key|secret|password|token|access_key|private[_-]?key|otp|razorpay[_-]?signature|cookie)/i;

function redactValue(value) {
  if (typeof value === 'string') {
    if (/^\d{6}$/.test(value)) return '***';
    if (value.length > 12) return `${value.slice(0, 4)}…${value.slice(-4)}`;
    return '***';
  }
  return value;
}

function redact(input, depth = 0) {
  if (depth > 4) return '[max-depth]';
  if (input === null || input === undefined) return input;
  if (Array.isArray(input)) return input.map((v) => redact(v, depth + 1));
  if (typeof input !== 'object') return input;
  const out = {};
  for (const [k, v] of Object.entries(input)) {
    out[k] = SENSITIVE_KEY.test(k) ? redactValue(v) : redact(v, depth + 1);
  }
  return out;
}

let requestId = 'no-request';

function withRequestId(id) {
  requestId = id || 'no-request';
}

function log(level, message, fields = {}) {
  logger[level](message, { requestId, ...redact(fields) });
}

module.exports = {
  log,
  withRequestId,
  info: (m, f) => log('info', m, f),
  warn: (m, f) => log('warn', m, f),
  error: (m, f) => log('error', m, f),
};