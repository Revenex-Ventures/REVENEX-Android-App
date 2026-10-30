# Backend hardening implementation

This extends the backend remediation, preserving the approved UI and existing published records. No system can be guaranteed unhackable.

## Implemented source changes

- Migration 23 adds service-only transactional quotas derived from the authenticated actor's protected profile: payment create 5/minute, verify 15, status 30, document inspect 5; each operation also has a school/session ceiling of 200/minute. Denials create deduplicated, fixed-field security events readable only by scoped leadership. Payment requests fail closed before provider work if protection is unavailable. Signed payment webhooks retain their signature/replay controls.
- Migration 24 adds server-owned inspection evidence bound to Storage object UUID and update timestamp. New/changed assignment, material, marks and submission attachments require matching inspection evidence. Historical unchanged references and publication states remain intact; there is no backfill or deletion. Storage rows are locked during sharing checks.
- The inspect-document function authenticates with Auth, downloads with the caller's bearer token, enforces ownership, canonical paths, bounded streams and declared size, checks supported format signatures/structure, and records SHA-256 through service-only RPCs. Replacement during inspection is rejected. DOCX archives have entry, expansion, CRC and traversal checks. This is limited format inspection, **not antivirus, content disarm, or a guarantee that documents are safe to open**. PDF/JPEG checks do not fully parse/render those formats.
- The Android data uploader requires explicit successful server confirmation before returning a stable reference; failed inspection uses existing RLS-limited staging cleanup. No UI files change.
- CI runs the additional regressions and checks the new Edge Function. Automatic function deployment now additionally requires repository variable `BACKEND_HARDENING_SCHEMA_CONFIRMED=24`. Set this only after operator confirmation; the variable itself is not evidence of schema parity.
- `supabase/security_monitor_readonly.sql` supplies aggregated denial, pending-payment, receipt, staging-age and stale-inspection checks. It does not configure external alert delivery or delete anything.

## Deployment state and order

Migrations 20–24 are prepared locally only. None were applied hosted by this work. New payment code requires migration 23; the uploader and inspection handler require migration 24. Coordinate the release to avoid fail-closed upload/payment interruptions.

1. Confirm project `nhaxnuaxyqkgnlkwpqkw`, tenant `revenex_demo`, session `demo_2026`, synthetic marker and deployed definitions using `supabase/remediation_preflight_readonly.sql`. Compare migration history and function definitions with source; capability 19 alone is insufficient.
2. Review/apply only missing migrations in order using the authorized operator workflow after backup verification. Never reset, seed or blindly rerun migrations.
3. Deploy the inspection handler and payment handler, then release the compatible Android client. Keep historical published files available. Do not enable automated deployment until schema confirmation.
4. Use the disposable-record plan in `docs/BACKEND_REMEDIATION.md` for new unpublished synthetic records only. Include wrong-role/foreign-tenant attempts, quota denial, inspected/uninspected object sharing and replacement. Track created IDs; remove only disposable unlinked fixtures. Verify private delivery and two-device Realtime separately.

## Verification and limits

Node regression command: `npm test --prefix supabase/tests`. New suites cover quotas, tenant isolation, forged approval, stale/replaced objects, bounded archives, authorization and historical preservation. The quota burst fixture queues requests in one PGlite instance; it is not a multi-connection PostgreSQL load test.

Local verification for this patch:

- 142 PostgreSQL schema/RLS legacy and strict-publication upgrade checks.
- 22 payment handler tests; 57 attendance/publication/quota/document regressions.
- 16 AI handler security tests and 3 Python hosted-verifier tests.
- Focused Kotlin compilation of actual changed backend sources and 15 JUnit tests passed using cached dependencies (`app/build/remediation-direct/{compile.log,tests.log}`). This diagnostic is not a full Android build. The full Gradle build remains blocked by existing cache/process locks and the fresh-cache `settings.gradle.kts:15` unresolved `plugins`/`id` failure recorded in the remediation handoff; no APK was produced here.
- Deno type checks/lint for changed functions, documentation links, credential/history scan and diff whitespace checks passed.
- New SQL checks execute in isolated PGlite only. No remote function invocation, provider charge or hosted mutation is part of these tests.

Hosted evidence remains the previously supplied Principal console success and saved Teacher/Student read-only successes on schema capability 19. These do not verify migrations 20–24, new function deployment, writes, inspection, notifications or concurrency. No hosted claims are made for this patch.

Read-only account check from PowerShell:

```powershell
Set-Location -LiteralPath 'D:\VS Code\Revenex Android'
python .\supabase\verify_hosted.py --roles PRINCIPAL TEACHER STUDENT --required-schema 19
```

Requires the corresponding synthetic Principal, Teacher and Student accounts. Passwords are entered through hidden local prompts; never put credentials in arguments or chat. Checks Auth, scoped profiles/student reads and inbox without writes. Schema 19 is the currently evidenced read contract; use the SQL preflight for newer definition parity.

## Remaining operator work

- Confirm deployed definitions and authorize the coordinated migration/function/client rollout. No fresh operator session is available here.
- Define approved absence, exemption and reasoned published-result correction policy; the implementation does not invent academic rules or permit unrestricted overrides.
- Configure privileged-account MFA and recovery using the provider's protected administration workflow; no MFA requirement is falsely claimed enforced by this patch.
- Supply an approved alert destination and retention policy before scheduling monitor execution or notifications. Current SQL creates no scheduler or external messages.
- Obtain backup access and a separate disposable restore environment. Restore there only with authorization, verify migration history, RLS, tenant isolation, roster and published-record counts, receipt uniqueness/reconciliation and private Storage objects, then record recovery time and data-loss window. No backup restoration or recovery-time guarantee has been tested here.
- Approve an isolated concurrent-load target and budget. Exercise actual independent PostgreSQL connections across quota windows, publication conflicts and provider timeouts; do not load-test school production data.
