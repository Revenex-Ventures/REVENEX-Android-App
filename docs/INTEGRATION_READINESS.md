# Integration and page readiness — 2026-10-23

## Current evidence

Read [SOURCE_RECONCILIATION.md](SOURCE_RECONCILIATION.md) first. The starting source for the next implementation pass is b55a57c; the prior ivory APK was tested at b604edd with **81** Android tests (XML verified), lint 0 errors/69 warnings. New checks will be recorded separately.

Payment schema/RPCs and migrations 6–9 were applied without reseeding; the payment function is deployed. All three exact secret names are configured by the operator. Unsigned user POST returns handler 401; unsigned webhook returns 400 invalid signature. AI remains undeployed. Real capture/webhook/device verification remains pending.

Realtime subscriptions **and** 30-second reconciliation are implemented together. Marks data methods exist; the reachable Scores view is read-only and there is no staff marks editor. Parents route to the shared student/family Home, not the separate unused parent dashboard component.

## Historical pipeline evidence

Earlier redesign code passed run [39595174465](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39595174465): Android build, 82 tests (0 failures/skips), lint (0 errors, 70 warnings), APK boundary scan and server checks. Documentation revision de179ce also passed Android and server checks in run [39926187657](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/39926187657). Deployment is separately gated to main, ENABLE_SUPABASE_DEPLOY=true and the supabase-demo environment. A successful build is not a deployment.

## Historical pre-repair hosted check

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

## Remaining activation/acceptance work

Do not reset or reseed the database or blindly rerun deployed migrations. SQL Editor changes do not automatically populate CLI migration history. Complete the signed-in test payment and real cross-account checks. AI deployment/model configuration remains separate; device push is not configured. Payment configuration presence and unsigned rejection do not establish valid provider credentials or successful capture.

## Pages

All five implemented portals and screen groups use the shared redesign; see [full page/state inventory](REVENEX_REDESIGN_CHECKLIST.md). Real rendered coverage includes dashboards in both palettes, 23 secondary entry screens in both palettes, phone/tablet and large-font fixtures. This is not all-state acceptance.

Remaining page/workflow gaps: staff marks entry/publication has no reachable editor; cloud faculty/profile mutations, school-issued certificates, library circulation and leave approval are unsupported/operator-managed. Some 320dp/1.5x layouts wrap awkwardly. Authenticated hosted actions, Storage, reconnect, device push, all nested dialogs and accessibility traversal still need acceptance. No page is labelled production-ready merely because it renders.

## Rechecks in this audit

`node supabase/tests/rls.test.mjs`: 105 passed. `node --test supabase/tests/assistant-edge.test.mjs supabase/tests/razorpay-edge.test.mjs`: 31 passed. `python scripts/check_secrets.py --history`: passed. Python checker compilation and `git diff --check`: passed. No Android source changed in this follow-up; the green run above covers the current application code.

## Firebase retirement

Firebase BOM/Auth/Firestore/Storage/Messaging/AI/App Check SDKs, Google Services plugin catalog entries, the disabled FCM service, legacy login/storage/data adapters, Firebase rules/configuration and the unused Functions package were removed. Only DEMO and SUPABASE_DEMO remain. Shared upload metadata/MIME validation now lives in DocumentFiles. CI verifies the resolved debug runtime contains no com.google.firebase artifacts; an Android test checks Firebase components/classes are absent. The three retired FCM-service tests were removed with that service; Supabase workflows and their tests remain. Historical documents and secret-scanner signatures may still mention Firebase; these are not runtime dependencies. No remote Firebase project or existing Supabase data was deleted.
