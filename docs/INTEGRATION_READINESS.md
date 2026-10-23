# Integration and page readiness — 2026-10-23

## Pipeline

Final redesign code passed run [39595174465](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39595174465): Android build, 82 tests (0 failures/skips), lint (0 errors, 70 warnings), APK boundary scan and server checks. Documentation revision de179ce also passed Android and server checks in run [39926187657](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39926187657). Deployment is separately gated to main, ENABLE_SUPABASE_DEPLOY=true and the supabase-demo environment. A successful build is not a deployment.

## Read-only hosted check

Checked the locally configured demo project, without login, database writes, payment creation or model requests:

| Endpoint | HTTP | Meaning |
|---|---|---|
| Auth health | 200 | Auth service reachable; account login not established |
| students | 401 | Anonymous access rejected; authenticated reads not verified |
| fee_records | 401 | Anonymous access rejected |
| razorpay_orders | 404 | Payment relation unavailable to this API request; inspect migration 6/schema cache |
| razorpay-payments function | 404 | Payment endpoint unavailable |
| revenex-ai function | 404 | AI endpoint unavailable |

Reproduce with `python scripts/check_integration_health.py`. It reads ignored public configuration and writes only endpoint status codes to `app/build/reports/integration-health.json`. 401 alone never proves a function is correctly configured.

## Activation blockers

1. Operator must inspect applied migrations and apply missing numbered migrations 1–9 once, in order. Do not rerun seeds or reset existing school data. Payment/enrollment need 6–7; server AI needs 8; result notifications need 9.
2. Deploy razorpay-payments and revenex-ai to the intended project using an authenticated Supabase operator/CI environment. No Supabase CLI or repository deployment secret was available in this check. Environment-scoped credentials were not inspected.
3. Configure Razorpay TEST credentials and webhook secret directly in Supabase server secrets, following [payment setup](../supabase/RAZORPAY_SETUP.md). Never reuse chat-exposed secrets; replace them directly at the provider and configure their replacements server-side. No private value belongs in Android or Git.
4. Complete authenticated two-account checks and a test checkout: server-confirmed capture, receipt, fee balance, pending-list removal, independent inbox read state, duplicate webhook/callback protection and failure handling. These are not established by the local tests.

No real payment was attempted. Razorpay merchant/account status cannot be determined from the app publishable key.

## Pages

All five implemented portals and screen groups use the shared redesign; see [full page/state inventory](REVENEX_REDESIGN_CHECKLIST.md). Real rendered coverage includes dashboards in both palettes, 23 secondary entry screens in both palettes, phone/tablet and large-font fixtures. This is not all-state acceptance.

Remaining page/workflow gaps: staff marks entry/publication has no reachable editor; cloud faculty/profile mutations, school-issued certificates, library circulation and leave approval are unsupported/operator-managed. Some 320dp/1.5x layouts wrap awkwardly. Authenticated hosted actions, Storage, reconnect, device push, all nested dialogs and accessibility traversal still need acceptance. No page is labelled production-ready merely because it renders.

## Rechecks in this audit

`node supabase/tests/rls.test.mjs`: 105 passed. `node --test supabase/tests/assistant-edge.test.mjs supabase/tests/razorpay-edge.test.mjs`: 31 passed. `python scripts/check_secrets.py --history`: passed. Python checker compilation and `git diff --check`: passed. No Android source changed in this follow-up; the green run above covers the current application code.
