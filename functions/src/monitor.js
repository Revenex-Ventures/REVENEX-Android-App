const log = require('./logger');

// Error monitoring: Sentry is bootstrapped ONLY when SENTRY_DSN is provided.
// Because the DSN is a secret, it must come from environment configuration —
// never from source. Absent a DSN we still get structured logs via
// firebase-functions/logger (which surfaces in Cloud Logging + Error Reporting).
let Sentry = null;

function initMonitor(dsn, env) {
  if (!dsn) {
    log.info('Error monitoring: Sentry disabled (SENTRY_DSN not configured); using Cloud Logging');
    return;
  }
  try {
    // eslint-disable-next-line global-require
    Sentry = require('@sentry/node');
    Sentry.init({ dsn, environment: env, tracesSampleRate: 0.05 });
    log.info('Error monitoring: Sentry enabled');
  } catch (err) {
    log.warn('Sentry initialization skipped', { reason: err.message });
  }
}

/** Send a request-scoped error to Sentry (if configured) + structured log. */
function captureAndLog({ message, error, request, event }) {
  if (Sentry) {
    const scope = Sentry.getCurrentScope();
    if (request) {
      scope.setExtra('method', request.method);
      scope.setExtra('path', request.path);
      scope.setExtra('requestId', request.headers && request.headers['x-request-id']);
      if (request.headers) scope.setExtra('userAgent', request.headers['user-agent']);
    }
    if (event) scope.setExtra('event', event.type);
    Sentry.captureException(error);
  }
  log.error(message, {
    errorMessage: error && error.message,
    errorCode: error && error.code,
    name: error && error.name,
    requestMethod: request && request.method,
    requestPath: request && request.path,
  });
}

module.exports = { initMonitor, captureAndLog };