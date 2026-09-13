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
| `POST` | `/createOrder` | User JWT | Create a Razorpay payment order (mocked if no key configured) |
| `POST` | `/razorpayWebhook` | HMAC signature | Verifies Razorpay webhook, updates fee records via Firestore transaction on `payment.captured` |
| `POST` | `/recordOfflinePayment` | User JWT | Record a cash or cheque payment via Firestore transaction |

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
