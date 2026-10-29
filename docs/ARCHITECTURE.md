# Architecture

## Android

The Compose UI consumes repository StateFlows and confirmed repository operations. The data source is explicitly either Local Demo or Supabase. Local personas and synthetic records are isolated from cloud account authorization. Login does not trust the chosen portal: the backend profile determines role, school, academic session and linked students. Cloud sessions are memory-only; process restart requires sign-in. No passwords are persisted.

Role-specific presentation shares theme/component tokens and navigation contracts. Light is the new-install default; Light/Dark/System is persisted using existing preferences. The Student timetable uses an explicit Asia/Kolkata clock. Time indicators do not write attendance.

## Supabase

Auth establishes identity. PostgreSQL protected profiles, composite school/session boundaries, RLS and scoped RPCs enforce access. Private Storage uses file metadata and database-managed document grants. Android never receives a service-role credential.

Mutation RPCs derive identity and scope, validate fields, use transactions and version checks, and generate audit/inbox events. Confirmed assignment, mark, attendance, notice and leave operations retain input or report conflicts when confirmation fails. Published results are a leadership operation; private drafts stay hidden from students/guardians.

## Reconciliation and inbox

Realtime events are hints to reload authoritative RLS-filtered state. Foreground/resume catch-up and a 30-second reconciliation fallback recover missed events. Snapshot replacement revalidates the protected profile and school/session scope. Account changes/logout clear account-scoped data; old requests cannot repopulate a new account.

Notifications are database rows with immutable recipients, per-recipient read state and deduplicated event keys. In-app updates are distinct from Android background push, which is not configured. A SQL acceptance test does not prove websocket or device delivery.

## Payments

The authenticated payment Edge Function obtains the payable invoice amount from protected server records and creates an integer-paise Razorpay TEST order. Android opens provider checkout using the returned public key/order. Callback HMAC, provider payment/order/amount/currency/capture and webhook HMAC are checked on the server. Settlement is transactional/idempotent. Interrupted order creation uses exact server-receipt recovery and fails closed when ambiguous. A client callback alone never credits an invoice.

## AI

The prompt and optional model credential live only in the server function. The server verifies the actor and derives tool permissions, permitted student IDs, school, session and language from protected records. Tools are bounded reads; chat cannot mutate ERP data. The hosted function remains unavailable until configured and deployed. See [deployment](DEPLOYMENT.md).

See [security](SECURITY.md), [screen/workflow inventory](SCREENS_AND_WORKFLOWS.md) and [limitations](KNOWN_LIMITATIONS.md). No architecture statement here certifies production scale or complete device acceptance.
