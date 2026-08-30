const fs = require('node:fs');
const path = require('node:path');
const { ROLE, ROLES } = require('./claims');

// Tiny .env loader for local emulator runs (no dotenv dependency).
// Real runtime secrets stay in environment variables only.
function loadLocalEnv() {
  const file = path.join(__dirname, '..', '.env');
  if (!fs.existsSync(file)) return;
  for (const line of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
    const m = /^\s*([A-Za-z0-9_]+)\s*=\s*(.*?)\s*$/.exec(line);
    if (m && !(m[1] in process.env)) process.env[m[1]] = m[2];
  }
}
loadLocalEnv();

const isEmulator = process.env.FUNCTIONS_EMULATOR === 'true'
  || /emulator/i.test(process.env.GCLOUD_PROJECT || '')
  || !!process.env.FIREBASE_EMULATOR_ALL;

const env = process.env.NODE_ENV || (isEmulator ? 'development' : 'production');

/** Fail fast if a required secret is missing in a non-emulator environment. */
function requireVar(name, allowEmulatorDefault) {
  const value = process.env[name];
  if (value && String(value).trim() !== '') return value.trim();
  if (isEmulator && allowEmulatorDefault !== undefined) return allowEmulatorDefault;
  return undefined;
}

const config = {
  env,
  isEmulator,
  schoolId: requireVar('SCHOOL_ID', 'revenex_school_001'),
  sessionId: requireVar('SESSION_ID', '2026-2027'),
  region: process.env.FUNCTIONS_REGION || 'asia-south1',
  sentryDsn: process.env.SENTRY_DSN || undefined,
  // Razorpay secrets arrive in Phase 3; declared here so Phase 0 fail-fast is testable.
  razorpayKeyId: process.env.RAZORPAY_KEY_ID || undefined,
  razorpayKeySecret: process.env.RAZORPAY_KEY_SECRET || undefined,
};

function validate() {
  const problems = [];
  if (!ROLES.includes(ROLE.STUDENT)) problems.push('internal: claim vocabulary mismatch');
  if (env === 'production') {
    if (!config.schoolId) problems.push('SCHOOL_ID must be set for this school');
    if (!config.sessionId) problems.push('SESSION_ID must be set to the active academic session');
  }
  if (problems.length > 0) {
    throw new Error(`Invalid configuration: ${problems.join('; ')}`);
  }
  return config;
}

module.exports = { config, validate };