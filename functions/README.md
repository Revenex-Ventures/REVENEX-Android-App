# functions/

Firebase Cloud Functions backend for REVENEX School ERP. Built with Node.js 20, Firebase Functions v2 (HTTP), and Express.js.

## Structure

```
functions/
├── index.js          — Main Express app + exported Cloud Function entry point
├── package.json      — Node.js dependencies and scripts
├── src/              — Supporting modules (claims, config, logging, monitoring, provisioning)
└── test/             — Backend test helpers (rules tests, debug token)
```

## API Endpoints (`index.js`)

All endpoints are mounted on a single `api` HTTP Cloud Function behind Express with CORS and a fixed-window rate limiter (120 requests/min per IP + route).

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/health` | None | Health check — returns environment info, region, schoolId |
| `POST` | `/provisionUsers` | Admin token | Bulk-create Firebase Auth users with custom claims |
| `POST` | `/migrateSessionIds` | Admin token | Backfill `sessionId` field across all Firestore collections |
| `POST` | `/rollbackSessionIds` | Admin token | Remove `sessionId` fields (migration rollback) |
| `POST` | `/promoteStudents` | Admin token | Year-end promotion — creates new session records and fee entries |
| `POST` | `/createOrder` | User JWT | Create a Razorpay order from the server-verified fee balance (mocked if no key configured) |
| `POST` | `/razorpayWebhook` | HMAC signature | Verifies Razorpay webhook, updates fee records via Firestore transaction on `payment.captured` |
| `POST` | `/recordOfflinePayment` | User JWT | Record a cash or cheque payment via Firestore transaction |
| `POST` | `/registerDevice` | User JWT | Register or rotate an FCM device token in the caller's school and session |
| `POST` | `/publishNotification` | Staff JWT | Store an auditable, role-targeted notification and deliver it through FCM |

## Notification delivery

`/registerDevice` takes `{ fcmToken, platform? }`. The server derives the user,
school, session, and role from the verified Firebase token; a client cannot
register a token for another tenant or person.

`/publishNotification` takes `{ title, body, category?, targetRoles? }`. It
persists an in-app record, writes an audit entry, sends to matching devices in
batches of 500, and removes tokens Firebase reports as invalid. A temporary
FCM failure does not discard the durable in-app notification record.

`/createOrder` takes `studentId` and an optional partial `amount` in paise.
The server rejects unrelated students and amounts above the active fee-ledger
balance, then creates its own Razorpay receipt and order notes.

## Local development

```bash
# Install dependencies
cd functions && npm install

# Run emulator (from project root)
firebase emulators:start --only functions,firestore
```

## Deployment

```bash
firebase deploy --only functions
```

Requires `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `SCHOOL_ID`, and optionally `SENTRY_DSN` set as Firebase environment secrets or `.env`.
