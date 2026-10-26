# Revenex connected-backend checkpoint — 26 October 2026

This is an implementation/evidence checkpoint, not a certificate that every ERP workflow is finished. Approved portal presentation, Supabase records and the payment ledger are preserved. Older delivery reports describe older binaries.

## Implemented

- Authoritative reconciliation on foreground/resume, Realtime hints and a 30-second fallback. Protected scope is revalidated and account data cleared on logout/scope changes. In-app updates are not background device push.
- Private assignment drafts, versioned publication, student file/note submissions, subject-scoped teacher feedback, private document grants and recipient-specific inbox events.
- Version-checked marks drafts/review submission, leadership-only publication and student/guardian draft privacy.
- Atomic whole-class attendance, canonical India school dates, safe identical retries, stale-batch rollback and retained form edits on conflict. Historical correction approval remains unavailable.
- Targeted notices for school, teachers, students, guardians or a class. Backend records derive author and recipients; failed forms retain input.
- Confirmed leave requests with validated dates, server-derived identity/day count, stable retry IDs, leadership inbox and duplicate-safe decisions. The form closes only after confirmation.
- Interrupted Razorpay TEST order recovery deployed to the existing Edge Function. Exact server-receipt lookup validates amount/currency before recovering an order. Ambiguous outcomes remain unresolved; Android callbacks alone cannot settle invoices.

## Hosted state

Migrations **11–19 are applied** to `nhaxnuaxyqkgnlkwpqkw`. Migrations 12–14 and 15/17–19 followed explicit user approvals. Migration 16 only reports available capabilities; it grants no data access. Do not replay these migrations or seed this project.

Install the updated APK before testing writes. Older APKs that use direct attendance, marks, notices or leave writes may now be rejected intentionally; do not restore those grants to accommodate an old client.

HOSTED rollback acceptance passed for assignment privacy/publication/submission/review; attendance versions, stale-batch rollback and guardian boundaries; targeted notice inboxes; leave retries/decisions; and marks privacy/publication/read isolation. All temporary writes rolled back. These tests simulate protected authenticated roles in SQL; they do not prove Android Auth, Storage HTTP or two-device Realtime transport.

Proofs: `app/build/backend-completion/assignment-rollback-pass.png`, `connected-rollback-pass.png`, `versioned-marks-rollback-pass.png` and `payment-recovery-deployed.png`. Individual migration success screenshots are alongside them. Payment deployment metadata changed from three days old to a few seconds old. Authentication settings and secret values were not changed.

## Actual verification

| Check | Result / evidence |
|---|---|
| Latest Android build, full unit suite, lint and Firebase boundary | **185 tests / 50 suites, zero failures/errors/skips; lint 0 errors / 81 warnings; build and no-Firebase check passed**. `app/build/backend-final-android.log` |
| Isolated PostgreSQL/PGlite schema/RLS | **140 passed**. `app/build/backend-connected-rollback-policy.log` |
| Node payment/AI handlers | **36 passed**, including interrupted-order recovery, signature and idempotency cases. `app/build/backend-order-recovery-green.log` |
| Deno 2.9.7 | Function type checks/lint passed; earlier real local anonymous HTTP entrypoint passed. No hosted AI execution verified. |
| Native Compose forms and states | Confirmed notice/leave tests and Light/Dark nested renders passed. `app/build/backend-confirmed-forms-green.log`; `app/build/reports/redesign/nested/` |
| Actual installed emulator / Local Demo | Teacher draft creation/reopening, entry/persona flow, teacher home and confirmed synthetic leave history inspected. Final rebuilt APK installed in place and its corrected narrow-phone leave form visually inspected. `app/build/backend-completion/runtime/`. Not cloud verification. |
| Source/APK security boundaries | Release and Git-history secret scan passed; APK server-prompt/private-name scan passed. Neither is a complete security audit. |
| GitHub CI/CD | Workflow definition retained with Android/backend quality gates and gated function deployment. No remote run of this local checkpoint is verified; nothing was pushed. |

The existing API34 emulator was restarted without wiping data, updated in place and stopped after saving runtime screenshots. No physical Android device is connected. No app data was cleared. Earlier emulator ANR evidence is retained under `app/build/student-timeline-anr*`; its cause remains unconfirmed.

## Final layout regression and test recovery

Actual runtime inspection found Sports compressed into a vertical label in the leave form. A native text-layout regression reproduced six lines. The options now wrap through Compose FlowRow using shared tokens; the latest installed APK screenshot confirms readable rows. Guidance: [Android flow layouts](https://developer.android.com/develop/ui/compose/layouts/flow).

An initial full 185-test run had five Espresso idle-timeout failures, retained in `app/build/backend-full-idle-failures.log` and `app/build/backend-completion/idle-failure-xml/`. Native text measurement was separated from the existing interaction test into its own native-rendering class. The affected classes passed together; the subsequent complete suite passed. No assertions were removed, tests skipped or timeouts raised. Renderer isolation and stopping the competing emulator both changed the test conditions; the precise cause of the earlier timeout is not proven.

Older Light/Dark nested fixture screenshots predate this final chip-wrap fix. Use `app/build/backend-completion/runtime/final-fixed-teacher-leave-form-dark.png` for the corrected latest APK view.

## Remaining work and limits

- Historical attendance correction request/review is not implemented. Historical register writes remain denied.
- Exercise cloud login, private upload/download, two-account assignment/review/inbox, reconnect, allocation removal and account switching on the installed APK.
- Complete a real Razorpay **TEST** checkout on the user's device: verify server capture, receipt, balance, principal inbox and duplicate status/callback handling. No captured test transaction was verified in this run. Handler mocks are not capture evidence.
- Cloud AI last returned 404. Model/deployment configuration is unavailable. Identity provisioning, protected profile corrections and library circulation remain operator-managed/unavailable. Admin web is deferred; push/email/SMS are not configured.

## Artifact and device steps

Delivery: `app/build/backend-completion-delivery/apk/app-debug.apk`. Exact source checkpoint and APK SHA-256 are in that folder's `source.json` and `checks.json`. Normal checkout HEAD stays `ce80da2d73283bb62846449cb9b434ff8e800cff`; delivery uses a separate Git checkpoint, preserving existing work/index.

Install preserving data: `adb install -r app/build/backend-completion-delivery/apk/app-debug.apk`. Stop on a signing/package conflict; do not uninstall. Sign in as the synthetic student/guardian → Fees → Pending → Pay → confirm TEST mode → complete Razorpay's official mock Success flow yourself → return in the same account → Check payment status. Report only receipt/pending/error text, never credentials or OTPs.


## Historical implementation notes

The notes below are retained as incremental history. Their local-only/pending statements are superseded by the current evidence above. Never use these historical entries to decide whether to replay SQL.

# Backend completion — 26 October 2026

Active request: `47450a26-2450-4fcc-a639-c7059126b191/Pasted text.txt` (user attachment). Preserve approved UI; defer Admin web. No reset/reseed, paid services, live payments, secrets in Android, force-push or release.

## Source and baseline

- Checkout branch `codex/revenex-reference-redesign`, HEAD `ce80da2d73283bb62846449cb9b434ff8e800cff`, substantial preserved working changes. Normal Git index is not used for delivery snapshots.
- Previous Student source checkpoint `b5c72802465051652abf233b0ba3ba7c559d21ba`.
- Timetable-only checkpoint `07101e94067723320ac6a3639ed573813836aa97`, branch `codex/student-timeline-build`. APK: `app/build/student-timeline-delivery/apk/app-debug.apk`; SHA/checks in that folder. 170 Android tests passed, lint 0 errors/80 warnings, no Firebase dependencies. Light/Dark native renders and runtime Light rail/ticking IST clock available. Emulator ANR occurred during theme/resume automation; trace saved under `app/build/student-timeline-anr*`. Main thread was measuring Compose text with high emulator CPU pressure. Cause not conclusively isolated; do not call device/theme verification complete.
- Android emulator API34 `emulator-5554`; no physical device. Node24 available; no `supabase`, `psql` or Docker on PATH. Python runtime under user's Codex primary-runtime dependencies.
- Baseline LOCAL PGlite: 111 schema/RLS checks passed (`app/build/backend-baseline-policy.log`). Node handler tests: 31 passed when run with Node-supported files only (`backend-baseline-handlers-corrected.log`). `assistant-entrypoint.test.mjs` is a **Deno** test; initial Node invocation failed because `Deno` was undefined, not an assistant-handler failure. Run with Deno before claiming coverage.

## Hosted observations (read-only unless explicitly stated)

- Public probes: Auth health 200; anonymous students/fees/orders/payment function 401; AI function 404. These do not prove signed-in workflows or capture.
- Supabase browser session is authenticated. Created SQL Editor query `6adb1194-8c5d-4410-a684-9b07c06200f6` for this run; do not overwrite unrelated user snippets. Browser ID2, tab8. No credentials read or entered.
- Hosted schema has migrations 1–10 equivalents, four demo profiles, zero public tables without RLS. `assignment_submissions` has only seven original fields ending with `completed`; no file/review support.
- Deployed migration10 function bodies match local source **CRLF** hashes: `save_draft_mark` 29b1606166abab4163d4d33c4b523186; `set_result_publication` a225d57970d1dbfb651488ae9d02edda; `decide_leave` cba75a78c7e1ece4281307d4aa321484.

## Slice A/E — result publication boundary

- Found and reproduced: teacher could call publication RPC or set `marks.published` directly despite the hidden UI control. New negative test failed against migration10 (`backend-publication-red.log`).
- Added migration `202610260011_result_publication_boundary.sql`: staff-only publication/unpublication, immutable mark identity, locked published term values including direct-write bypass, shared transaction advisory lock. Existing RLS and rows preserved.
- Updated positive tests/rollback acceptance to use principal publication; retained teacher draft tests and added direct bypass denial tests. **113 LOCAL PGlite checks pass** (`backend-publication-green.log`).
- Repository and Supabase datasource now reject teacher publication before dispatch as a convenience; database remains authority.
- **Hosted migration11 applied successfully once through SQL Editor. Do not replay it.** Screenshot `app/build/backend-completion/migration11-success.png`. Hosted rollback-only synthetic acceptance also passed teacher denial, principal publication, draft privacy, publication replay and per-recipient read isolation (`result-rollback-pass.png`). All temporary changes rolled back; existing records retained.

## Subsequent work (current working tree; not yet final delivery)

- Full authoritative reconciliation replaces all cached tables, including submissions, protected class/parent allocations and subjects. Profile identity/role/school/session is checked before/after fetch; changed scope clears the session. Realtime hints now trigger fresh RLS reads. ON_STOP/ON_START performs foreground catch-up; cancelled sign-in clears partial state. Teacher class labels derive from protected allocation rows. Four new pure tests plus existing session/cache tests passed.
- Migrations **12–14 are prepared locally, NOT hosted**. An explicit action-time approval question is pending for private-file access. Do not apply them without that answer. Migration12 adds versioned private file/note submissions, review, recipient/event dedupe and unlinked staging cleanup. Migration13 adds private drafts, versioned assignment authoring/publication, subject ownership and draft-file privacy. Migration14 restricts private marks to protected subject allocations and checks whole-term locks outside teacher SELECT visibility.
- Android uses the new RPCs, exposes a cloud submission form with retained note/file/version, teacher received/missing lists and feedback, draft/edit/publish controls, stable private references and 5-minute signed links. Local Demo completion still works; local drafts remain hidden from family roles and upsert instead of duplicating. Approved colors/icons/layout system retained; only necessary functional controls added.
- Current LOCAL PGlite result: **122 passed** (`backend-mark-subject-green.log`). Includes stale/duplicate requests, peer/private-file denial, draft/attachment privacy, subject-scoped review and publication. Node handlers **31 passed**; Node dependency audit **0 vulnerabilities**.
- Downloaded official Deno **2.9.7** from GitHub (not available under the same npm wrapper version); release digest verified, details in `app/build/tools/deno-2.9.7/download.json`. Deno check, lint and the actual local anonymous HTTP entrypoint test **1 passed**. No hosted model execution verified; hosted AI still 404.
- Android `assembleDebug` plus **31 focused tests in five suites passed** (`backend-assignment-drafts-android.log`). This is not the full suite/lint after all backend changes. New APK installed with `adb install -r`, preserving data. Cold launch responsive; earlier ANR evidence retained, root cause not proved.
- ACTUAL EMULATOR / LOCAL DEMO: teacher created `Backend QA draft`, then opened Drafts and saw it (`backend-draft-local-runtime.log`, screenshots under `app/build/backend-completion/runtime`). This is not a Supabase write or cloud login. The assignment tabs were wrapping in the inspected phone screenshot; Add has now moved into the existing compact page-header action area to keep all three tab labels readable. Rebuild/re-render needed for that adjustment.
- Remaining: marks optimistic versions and submission-for-review lifecycle; consistent attendance definitions/confirmed save/corrections; leave replay/retry; notice recipient targeting; payment interrupted-order reconciliation; unsupported module review, full inventory, final build/lint/secret scan and source/APK evidence. No real Razorpay TEST capture performed.

### Marks version slice added after the above checkpoint

- Migration15 prepared, **not hosted**: expected-version draft RPC, submission-for-review, version invalidation on publication, protected mark-document RPC; legacy blind writes/old RPC denied. Android row editors capture the version when edits start and retain unconfirmed rows, with explicit loading of a newer saved value instead of silently overwriting it.
- Source method/record mapping updated; teacher can request review but cannot publish. Existing principal review/publication stays school-scoped.
- **126 LOCAL PGlite checks pass** (`backend-mark-versions-green.log`). Extra cases cover invisible-subject term locks, private marked-paper access, preservation of existing permitted document references and hidden legacy assignment adoption denial.
- `assembleDebug` and focused Android checks passed (`backend-versioned-marks-android.log`); exact per-run XML counts must be read before final reporting. No full post-change suite/lint yet.
- See `BACKEND_FEATURE_INVENTORY.md` for routed actions, implementation and honest gaps. Remaining independent slices: attendance canonical date/concurrency/corrections, leave request retry handling, targeted notices, interrupted payment creation recovery, protected school context metadata, final checks/rendering and artifact checkpoint.
- Pending asynchronous approval is specifically for **hosted migrations12–14 private-file access**. Do not interpret silence as approval. Continue independent work. Migration11 already deployed and verified; never replay it.

## Remaining implementation / next actions

1. Finish hosted migration11 confirmation and rollback acceptance; record screenshot/evidence.
2. Complete feature/action inventory against current routes, then implement remaining slices rather than writing status-only docs.
3. Auth/sync gaps: 30-second fallback refreshes only six tables, omitting submissions, teacher allocations and student scope. Need full authoritative reconciliation, profile-context revalidation, cancellation/account-race tests and resume hook. Sessions are intentionally memory-only; no password persistence.
4. Assignment gaps: no draft/publication state in model/schema; completion toggle only; teacher detail explicitly unavailable. Implement versioned draft/publish, private submission file/note/review, trusted ownership, safe retry/concurrency and recipient routing, preserving current approved presentation.
5. Attendance: existing atomic batch saves/RLS present. Inspect correction/locking/date definitions, confirm same-row cross-portal reads with positive/negative tests.
6. Marks: verify subject allocation policy (current table is class-level), publication/teacher draft submission semantics and hosted privacy. Do not invent subject authority.
7. Notices: existing server trigger currently broadcasts school-wide on insert; inspect explicit recipient selection, dedupe and destination metadata.
8. Fees: preserve existing server amount/order/signature/capture handlers. Real synthetic checkout remains device/credential dependent; unsigned probes are not capture evidence.
9. Library/profile/reports: existing honest operator/unavailable states should not be converted to fake success. Inspect supported data and business rules before enabling writes.
10. Build/test changed Android code, server/RLS checks, lint, secret/APK scan; record exact source snapshot and latest APK separately from the already tested timetable checkpoint.

Official guidance consulted: Supabase [RLS](https://supabase.com/docs/guides/database/postgres/row-level-security), [functions](https://supabase.com/docs/guides/database/functions), [Storage access](https://supabase.com/docs/guides/storage/security/access-control), [Postgres Changes](https://supabase.com/docs/guides/realtime/postgres-changes). Use verified backend profiles/RLS, never client-selected roles.
