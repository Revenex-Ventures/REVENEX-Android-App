> Historical change log: the black/orange, missing-profile and payment-404 observations below describe earlier builds. Read [source/evidence reconciliation](SOURCE_RECONCILIATION.md) and [current delivery evidence](IVORY_DELIVERY_VERIFICATION.md) first. Do not treat historical test counts or setup instructions as current acceptance.

> Server AI/CI update: supplied prompt stored only under `supabase/functions/revenex-ai/`; Android sends bounded text/history and Auth session only. Backend verifies user/profile, derives school/session, parent/teacher student scope, stored language and read-only tool permissions; rejects client authorization fields. Migration 8 adds language settings and request limits. See `docs/SERVER_AI_AND_CICD.md` and `docs/SERVER_AI_VERIFICATION.json`. Local checks: 55 Android tests, 99 DB checks, 31 Edge tests, 32 retained-backend tests, Deno type/lint, actionlint, credential/history and APK boundary scans passed. No paid model calls were made. Live AI, deployed SQL and CI/CD secret activation still require server configuration; never put keys in Android.

> Latest connected-workflow update: migration 7 adds principal/admin enrollment with optional initial invoice and server inbox events, principal/family payment success and verified-failure messages, deduplicated failure attempts and full-payment removal from the default Pending list. Auth failures identify Auth/profile/table stages. Local validation: 52 Android tests, 92 SQL/RLS checks, 15 Edge tests; lint 0 errors/67 warnings. Hosted preflight found payment table/function 404; user screenshot showed 0 demo profiles. User then ran SQL successfully, but post-seed profile count and hosted flows are not independently confirmed. See `supabase/ACTION_WORKFLOW_SETUP.md` and `docs/ACTION_WORKFLOW_VERIFICATION.json`. No Supabase deployment credential is available; do not claim cloud checkout was verified. Preserve black/orange UI.

> Final restored build: assembleDebug, 50 Android tests and lint passed (0 errors, 67 warnings). Database 81 checks; Edge handler 11 tests. See `docs/BLACK_ORANGE_BUILD_VERIFICATION.json` and `supabase/RAZORPAY_SETUP.md`. Final APK uses black/orange; hosted Razorpay activation is still operator work.

> Latest update — 20 October 2026: The original black/orange palette and carbon/ember shared components have been restored from commit `6efa23f`, while retaining Supabase and system-inset fixes. Optional Razorpay TEST payment integration is implemented in Android and `supabase/functions/razorpay-payments/`, with migration 6 and server-only secrets. See `supabase/RAZORPAY_SETUP.md`. Hosted deployment and real test capture are not yet verified. Local validation: 81 SQL/RLS checks and 11 payment handler tests pass. Older verification results below describe earlier APKs, not the restored build.

# Revenex ERP — project changes and current status

Updated: 20 October 2026. Working migration; this is not a claim that the hosted backend is ready.

## What the project was when this migration began

The existing project is a Kotlin / Jetpack Compose Android school ERP (not Flutter). It already had role-aware dashboards for principal, school administrator, teacher, student and parent; student/faculty records; attendance; assignments; exams/marks/report cards; fee records; notices/inbox; and operations screens. A local in-memory demo supplied realistic sample data. A Firebase datasource, Firebase Authentication, Firestore, Storage and a messaging service were present, as were Razorpay checkout references. Build configuration applied Google Services and a secrets plugin that read `.env`.

Many earlier edits were already uncommitted before this task, including UI/theme changes, admin screens, tests and Firebase Functions work. This migration must not be described as the author of all those changes. The previous request to restore an earlier UI was blocked at the time by disk exhaustion; no successful restoration was verified. No further visual redesign is part of this migration.

## What is changing now

| Area | Before this migration | Current implementation |
| --- | --- | --- |
| Demo modes | Local demo plus Firebase production selector | Local demo preserved; explicit Supabase demo login added; production selection rejected in this demo build |
| Configuration | Google Services and `.env` secrets plugin applied | Those plugins no longer applied in app module; only URL and publishable key read from ignored `supabase.local.properties` |
| Login | Local role selection; legacy Firebase credentials | Supabase email/password sign-in; protected database profile supplies role, school and session |
| Data | Local samples or legacy Firebase datasource | Additive Supabase datasource maps PostgreSQL rows into current models; no sample fallback in Supabase mode |
| Access | Existing Firebase rules | New PostgreSQL migrations with tenant/session foreign keys, role/class/child RLS; no anonymous ERP reads |
| Notifications | Local inbox plus Firebase messaging code | Recipient-owned database inbox; database triggers; Realtime subscriptions; push not enabled |
| Files | Firebase upload path | Private Supabase bucket, metadata-controlled upload paths, limited MIME/size, temporary signed links |
| Fees | Local simulation / checkout references | Supabase fee reads only; demo checkout blocked; no payment integration added |
| Legacy code | Firebase implementations | Preserved, not deleted; made lazy/unreachable from demo; Firebase startup provider removed and messaging service disabled |
| UI | Existing appearance | Kept; login, cloud status, honest summaries and missing-record states adjusted |

## Files provided

- `supabase.local.properties.example`: public configuration template. Copy locally; do not add secrets.
- `supabase/PLAN.md`: phased migration and security boundaries.
- `supabase/migrations/202610200001_schema_rls.sql`: ERP schema, roles, foreign keys, grants and RLS.
- `supabase/migrations/202610200002_events_storage.sql`: audit/inbox triggers, Realtime publication, private bucket policies.
- `supabase/migrations/202610200003_document_access.sql`: durable document references and scoped class/student grants.
- `supabase/migrations/202610200004_integrity.sql`: stronger foreign keys, separate same-day roll-call sessions, leave identity validation.
- `supabase/migrations/202610200005_student_privacy.sql`: removes duplicated financial fields from student payloads and prevents reintroduction.
- `supabase/verify_hosted.py`: local hidden-password prompts for real account/read checks; no private credentials saved.
- `supabase/seed.sql`: one synthetic school; requires four Auth accounts created separately.
- `supabase/tests/rls.test.mjs`: executable PostgreSQL/RLS tests using pinned PGlite; Auth/storage API and websocket services are not simulated as verified services.
- `app/src/main/java/com/example/data/datasource/SupabaseProvider.kt`: validated public-only client, memory-only Auth session.
- `SupabaseDataSource.kt` and `SupabaseFileStore.kt` in the same directory: cloud adapter and storage implementation.
- `supabase/README.md`: operator setup guide for SQL Editor migrations, demo Auth account provisioning, seeding, and Android public properties.
- `docs/NEXT_AGENT_PROMPT.md`: handoff prompt and verification priorities.

## Verification completed on 20 October 2026

Latest configured build: the supplied public Supabase values are saved in ignored `supabase.local.properties` and verified in generated Android configuration. No private keys were added. The configured APK passed the full build, all 47 tests and lint (zero errors, 66 warnings) in 6m 40s. Log: `app/build/supabase-configured-verification.log`. Installed successfully on the isolated API 34 phone, cold-started in 4.281 seconds, and visually verified the Supabase login screen. No real account credentials were used by this run.

- Android: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` **BUILD SUCCESSFUL in 2m 27s**. **47 tests passed**, zero failures, errors or skipped test cases. Lint: **zero errors, 66 warnings**. Log: `app/build/supabase-completion-verification.log`.
- Database: `npm test --prefix supabase/tests` **64 PostgreSQL/RLS checks passed**. Log: `app/build/supabase-sql-verification.log`. PGlite uses mock Auth/Storage schemas; this does not verify hosted services or websocket transport.
- Notification interaction: a failing UI test reproduced the missing read action. Unread rows now invoke the existing repository operation. A normal Compose click plus explicit Robolectric main-looper synchronization verifies that the repository read state changes. No timeout or assertion was weakened.
- Student summaries: three regression tests verify unavailable fee data is hidden, authorized fee rows alone supply balances, and attendance comes from structured records. Migration 5 removes duplicated fee fields from student payloads on existing and future writes.
- Earlier PDF cleanup failed once during a rerun; its two tests passed unchanged in isolation and the final full suite. This intermittent failure is recorded, not claimed to have a proven root-cause fix.
- Earlier API 36 emulator inspection covered role selection, Supabase login, missing-local-configuration feedback, local administrator dashboard and local inbox. Final APK: installed successfully on an isolated Android 34 x86_64 Pixel 6 AVD, cold-started in 5.006 seconds, and visually verified login with readable mode-switch text, local administrator dashboard and inbox. A real tap marked the first notification read and changed the header badge from 2 unread to 1 unread, confirmed in both UI hierarchy and screenshot. Evidence is under `app/build/tmp/revenex-final-auth.png`, `revenex-final-admin.png`, and `revenex-final-inbox-after.png`. The user's pre-existing phone AVD had a missing configuration folder and was left untouched; the isolated QA AVD was stopped after verification.
- Roborazzi finalization and instrumentation tasks are skipped by the configured build. Do not describe this as an instrumentation or screenshot-comparison pass.
- Latest hosted preflight: Auth health HTTP 200; email authentication enabled; students, profiles, notifications and fee_records deny anonymous requests with HTTP 401 / PostgreSQL 42501. These endpoints no longer return the earlier table-not-found response. The user reports completing hosted setup, but account login, seeded profiles, authenticated RLS and live transport have not been independently verified. Report: `app/build/reports/supabase-public-preflight.json`.

## Build artifact

`app/build/outputs/apk/debug/Revenex-Supabase-Configured-debug.apk`

SHA-256: `CDBFD763230E95EA03E77385DAAF2DB571F8CC6DC4AC0FBE1EE25B4173C5980F`

This debug APK contains the supplied public Supabase project configuration. Open it, tap Use Supabase Demo, and enter an account created in Supabase Auth and linked by the seed/profile setup. Local demo remains available. The earlier Revenex-Supabase-Demo-debug.apk is the old unconfigured artifact. No additional private key is needed.

## Remaining limitations

1. The user reports completing hosted setup. Confirm all five migrations and the seed/profile links are present when doing account checks; do not rerun already-applied one-time migrations blindly. The public client configuration is now installed locally. No passwords or private keys are needed in chat.
2. Run `python supabase/verify_hosted.py` locally, then the two-device hosted checklist. Real Auth, Realtime delivery/reconnect, Storage upload/sign/download, role walkthroughs and writes remain unverified.
3. Documents use durable references, private bucket policies and database-managed class/student grants. Already-issued signed links remain usable for up to one hour; failed uploads can leave metadata requiring operator cleanup.
4. Fees are read-only. Payments, push notifications and Edge Functions are intentionally not implemented. Four demo accounts are seeded; SQL supports nine roles, while Android supports its existing five dashboard roles.
5. Enrollment/account provisioning, identity changes, circulation and leave approval remain operator-managed or explicitly unsupported in cloud mode. Legacy Firebase code remains, with demo startup paths disabled.
6. Auth credentials and mode are memory-only. Process restart returns to local demo; there is no offline cloud write queue or import of legacy Firebase records.
7. JSONB payloads preserve current model contracts. Full-table snapshots suit demo scale, not paginated production reporting or a normalized production accounting ledger. These local results do not establish production readiness.
