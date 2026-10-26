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
