# functions/src/

Supporting modules for the Cloud Functions backend. Each file is a focused, single-responsibility module imported by `index.js`.

## Files

### `claims.js`

Sets Firebase Auth custom claims on user accounts.

Custom claims written:

| Claim | Type | Description |
|---|---|---|
| `role` | string | User role: `PRINCIPAL`, `TEACHER`, `STUDENT`, `PARENT` |
| `schoolId` | string | Tenant school identifier (e.g. `revenex_school_001`) |
| `sessionId` | string | Active academic session (e.g. `session_2026_2027`) |
| `employeeId` | string | For teachers — staff ID |
| `studentId` | string | For students — admission number |
| `studentIds` | string[] | For parents — list of linked children's admission numbers |

Claims are read by Firestore security rules (`request.auth.token.role`) and by `ErpDataRepository.loginWithFirebase()` on the Android side.

---

### `config.js`

Reads and validates all required environment variables at startup.

Validated variables:

- `SCHOOL_ID` — tenant school identifier
- `FIREBASE_REGION` — Cloud Function deployment region
- `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` — payment gateway credentials
- `SENTRY_DSN` — error monitoring (optional)
- `SESSION_ID` — active academic session identifier
- `FUNCTIONS_EMULATOR` — set to `true` when running locally

Throws a startup error if any required variable is missing, preventing silent misconfigurations in production.

---

### `logger.js`

Structured JSON logging with request ID threading.

- All log entries include: `timestamp`, `level`, `requestId`, `schoolId`, `message`, and optional `data`.
- `requestId` is generated per-request and threaded through all log calls for that request, making it easy to trace a single request in Cloud Logging.
- Log levels: `info`, `warn`, `error`.

---

### `monitor.js`

Sentry integration for error capture and performance monitoring.

- Initializes the Sentry Node SDK with the `SENTRY_DSN` from config.
- Exports a `captureException(err, context)` helper used in catch blocks across `index.js`.
- If `SENTRY_DSN` is not configured, calls are no-ops (safe to run without Sentry in development).

---

### `provision.js`

Bulk user provisioning — creates Firebase Auth accounts and sets custom claims for an entire school cohort.

Used by the `POST /provisionUsers` endpoint. Accepts a JSON payload with arrays of:
- Teachers (with `employeeId`, `role`)
- Students (with `studentId`, `class`)
- Parents (with linked `studentIds`)

Creates accounts idempotently — skips users that already exist. Returns a summary of created, skipped, and failed accounts.
