# Backend health check — 27 October 2026

Source checkpoint: `99fd130795acc95c7c59df4e12089dbda196b6e4`.
Checked at 2026-10-27T18:23:37.371125+05:30.

## Confirmed locally

- 140 PostgreSQL schema/RLS checks passed against an isolated PGlite test database.
- 36 Node handler tests passed, using mocked external providers and adapters.
- 47 focused Android backend tests passed; Gradle completed successfully.

Coverage includes school/session boundaries, teacher subject/class allocations,
linked-parent access, private submissions/files, feedback, draft-mark privacy,
publication permissions, attendance persistence/version conflicts, recipient-only
inboxes and read flags, account-switch invalidation, notice targeting, leave
decisions and duplicate-safe payment settlement. Tests confirmed invalid signatures
and uncaptured/incorrect amounts cannot credit fees. Callback/webhook replay does
not double-credit the test ledger. These are local tests, not real transactions.

## Hosted read-only checks

| Endpoint | HTTP | Interpretation |
| --- | --- | --- |
| Auth health | 200 | Auth service responds |
| Students | 401 | Unsigned access rejected; signed-in reads unverified |
| Fee records | 401 | Unsigned access rejected |
| Razorpay orders | 401 | Unsigned access rejected |
| Razorpay payments function | 401 | Endpoint responds and rejects unsigned callers; secrets/order creation unverified |
| Revenex AI function | 404 | Hosted AI endpoint unavailable |

## Remaining live acceptance

This check cannot certify the whole hosted backend. Four-account Auth/profile
provisioning, actual private file upload/download, two-account Realtime/reconnect,
and a real Razorpay TEST capture with receipt, balance and provider webhook still
need signed-in hosted/device evidence. Anonymous 401 responses do not prove the
deployed schema, policies, payment secrets or migrations match the local tests.

The Android source reconciles authorized data every 30 seconds alongside Realtime
and clears account-scoped state on logout. The inbox is database-backed in-app
notification delivery; background device push is not configured.

The current cloud session policy is memory-only. Historical attendance corrections,
library circulation and protected profile corrections remain unavailable or
operator-managed, as documented in [known limitations](KNOWN_LIMITATIONS.md).

No hosted database reset, seed, migration, function deployment, payment or private
credential entry occurred during this audit. Use the local hidden-password
[hosted read-only checker](../supabase/verify_hosted.py) for the existing synthetic
accounts; never paste their passwords or tokens into chat.

Machine-readable evidence: `app/build/reports/backend-health-2026-10-27.json`.
