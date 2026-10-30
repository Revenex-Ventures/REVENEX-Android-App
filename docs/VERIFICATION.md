## Additional backend security check — 31 October 2026

Source changes and fresh hosted status probes are recorded in [backend security review](BACKEND_SECURITY_REVIEW.md). Stream deadline/cancellation tests and malformed AI quota validation reproduced the issues before their fixes. The prior exact-commit [CI run 41110750187](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/41110750187) passed backend and Android for `348769a`; that run predates these additional source changes. The subsequent [CI run 41114149537](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/41114149537) passed backend and Android for source-fix commit `a4a8ef10`; deployment stayed skipped. The action runtime maintenance warnings are recorded in the security review. Fresh probes at 00:43:44 IST returned Auth 200, protected data/payment 401, AI 404 and inspection 404; no authenticated write/model/capture/Realtime behavior was tested and no hosted changes were made.

# Backend/CI documentation follow-up — 31 October 2026

Scope: backend, documentation and CI only; no UI/frontend files or hosted records changed. The earlier dated checkpoint below remains historical.

- Remote working branch was checked through `git ls-remote`: `18c0c936398fd2cf08485af528e82a1b3d294877` matched local source at the start of this follow-up. Main is a separate branch; pushing this branch did not merge it into main.
- GitHub had no Actions runs or PR for `codex/project-review-2026-10-29`. Diagnosis: the workflow accepted only `main` pushes, PRs and dispatch. New source adds `codex/**` push coverage; deployment still requires main and explicit operator gates.
- Historical failed run [40123057312](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40123057312) failed Android quality checks on a different redesign commit. A later [40468041788](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40468041788) passed backend and Android for a different Dependabot commit. Neither is current-branch evidence.
- The four README files and security/testing/limitations/deployment guidance now include the prepared migrations, source changes, secure read-only role command, compatibility, operator backlog and evidence distinctions. Repository instructions require ongoing small-change README updates.
- Fresh local rerun passed: 142 PostgreSQL schema/RLS checks, 22 payment tests, 57 backend regressions, 16 AI tests and 3 Python verifier tests. Deno checked all three entrypoints and linted six function files; documentation links and credential/history scan passed. Logs: `app/build/readme-followup-{backend,ai}.log`. No new Android APK/device test was run locally.
- Exact-commit [run 41109276114](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/41109276114) verifies push-trigger coverage for `c99df934cb64e16a5935ff64fa7706a9e0a13505`. Backend completed successfully in 32 seconds. Android reported no steps while in progress, then completed successfully: the full build took 7m42s. Both quality jobs passed and deployment was correctly skipped. The temporary absence of step details was not a build failure; this later result supersedes the interim status recorded in the runner-pinning commit.
- All three jobs now select `ubuntu-24.04` explicitly during GitHub's documented Ubuntu 26.04 migration. This is an OS reproducibility change, not proof that image migration caused the stalled job. GitHub also had an Actions incident in monitoring; no causal claim is made. [Migration notice](https://github.com/actions/runner-images/issues/14748), [incident](https://www.githubstatus.com/incidents/pwxsjgnvrk6b).
- Full CI also passed the debug/review build, unit tests, lint, no-Firebase check, APK server/private-boundary checks and artifact upload; Node dependency audit reported zero vulnerabilities. Complete remote log retained locally at `app/build/readme-followup-ci-complete.log`. This is a CI build result, not hosted application acceptance.
- The runner follow-up starts a separate exact-commit run; inspect the latest [branch Actions](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions?query=branch%3Acodex%2Fproject-review-2026-10-29). Current source must not be called green until its backend and Android jobs complete; branch deployment must remain skipped.
- Repository metadata showed push but no admin permission, zero visible rulesets/environments, and HTTP 404 for main protection. Branch/environment enforcement is not verified and requires the repository owner's configuration.
- Hosted state is unchanged: prior role read evidence only, migrations 20–24 not applied here. No credentials, private keys, school-data reset or deployment is part of this work.

---

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


The project-structure cleanup preserves this verification history. Its new checks and source commit are recorded separately; old counts are not silently presented as new runs.
