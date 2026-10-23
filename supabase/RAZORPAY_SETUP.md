> Latest hosted status (2026-10-23): migrations 6–9 applied; payment function deployed; Auth health 200; payment relation and unsigned function requests rejected (401). No custom server secrets existed at inspection, so checkout/capture is not yet verified. See [current delivery evidence](../docs/IVORY_DELIVERY_VERIFICATION.md). Current redesign CI passed 81 Android tests; historical build details below are retained for context. Do not rerun already-applied migrations.

# Razorpay test payments — current Revenex app

This adds Razorpay **test mode only** to the existing Supabase demo. The Android app never receives a Razorpay secret or a Supabase service-role key. Local demo remains independent of payment services. No Firebase Functions are used.

## Activate the server

1. Ensure migrations 1–5 are already applied. Apply `migrations/202610200006_razorpay_test_payments.sql` once, then `migrations/202610200007_action_workflows.sql` once through the Supabase SQL Editor. Do not rerun the seed over real school data. Payments are restricted to the synthetic `revenex_demo` / `demo_2026` scope.
2. In **Supabase → Edge Functions → Secrets**, set:
   - `RAZORPAY_KEY_ID`: your `rzp_test_…` ID.
   - `RAZORPAY_KEY_SECRET`: its matching secret, entered directly in Supabase.
   - `RAZORPAY_WEBHOOK_SECRET`: a locally chosen strong random secret, also used for the Razorpay webhook.
   Never put these secrets in Android, `supabase.local.properties`, Git or chat. Supabase provides `SUPABASE_URL`, `SUPABASE_ANON_KEY` and `SUPABASE_SERVICE_ROLE_KEY` to the hosted function; do not copy the service-role key into the app.
3. Deploy both files in `functions/razorpay-payments/` together, preserving the relative `./core.mjs` import. With the official Supabase CLI, from the project root:
   ```powershell
   npx supabase login
   npx supabase functions deploy razorpay-payments --project-ref nhaxnuaxyqkgnlkwpqkw
   ```
   `supabase/config.toml` disables the gateway JWT check for this function because the webhook cannot supply a user JWT. **The handler still verifies every user request through Supabase Auth**; only the `/webhook` route instead requires a verified raw-body HMAC. Do not remove these checks.
4. In the **Razorpay test-mode dashboard**, enable automatic payment capture and create a webhook for `payment.captured` and `payment.failed`:
   ```text
   https://nhaxnuaxyqkgnlkwpqkw.supabase.co/functions/v1/razorpay-payments/webhook
   ```
   Use exactly the `RAZORPAY_WEBHOOK_SECRET` set in Supabase. Do not paste it into this file.

## App workflow

Sign in through **Use Supabase Demo**, open **Fees**, select an outstanding invoice and choose **Razorpay Test**. The server reads the authorized fee balance and creates the INR order in paise. Payment methods are selected in Razorpay's secure checkout.

The checkout callback is a request to verify, not proof of payment. The server verifies its HMAC using the **stored** order ID, fetches the Razorpay payment and checks its order, exact amount, currency and captured status. Only then does a database transaction create a receipt, credit the fee, record the audit event and create the in-app notification. Repeated callbacks/webhooks return the existing receipt without double-crediting.

Use **Check payment status** after an interrupted checkout or process restart. It retrieves the current account's existing order and reconciles a provider-confirmed captured payment. An authorized but uncaptured payment stays pending. There is no fake success fallback when keys, deployment or network are missing.

## Verify

```powershell
npm test --prefix supabase/tests
node --test supabase/tests/razorpay-edge.test.mjs
.\gradlew.bat '-Dorg.gradle.jvmargs=-Xmx2g' --no-daemon --no-configuration-cache --no-parallel --max-workers=1 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Then complete a Razorpay **test** checkout with a signed-in demo account. Verify the receipt/fee balance after signing out and back in, the parent/student inbox, callback plus webhook replay, cancellation, wrong signatures, and network loss. Confirm live keys are rejected. Use the payment methods/test data supported by Razorpay's current test dashboard; test mode does not guarantee every real UPI app flow is available.

## Limits

- Deployment and server-secret configuration require the operator's authenticated Supabase session. The Android publishable key cannot deploy code or set secrets.
- Checkout is full outstanding balance only; partial payments, refunds, disputes, live payments and production accounting are not implemented.
- One pending order per fee prevents parallel checkouts. A provider order created during a network failure before its ID is saved can leave a `creating` reservation. It fails closed and requires server reconciliation; do not delete reservations blindly after a possible capture.
- Callback/webhook reconciliation must be tested on the hosted project. Local PostgreSQL and mocked-provider tests do not establish actual Razorpay capture or webhook delivery.
- No SMS/email delivery is implied. Confirmations are in the database inbox.

## Design and sources

The user requested restoration of the existing black/orange design. The original palette and carbon/ember shared visuals come from Git commit `6efa23f`; Supabase, truthful data states, administrator access and inset fixes remain.

- [Razorpay Android integration, server orders and verification](https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/integration-steps/)
- [Supabase Kotlin Edge Function invocation](https://supabase.com/docs/reference/kotlin/functions-invoke)
- [Supabase server environment variables and secrets](https://supabase.com/docs/guides/functions/secrets)
- [Supabase Edge Function authentication and webhook boundaries](https://supabase.com/docs/guides/functions/auth)

## Local verification for this build

- Database: 81 schema/RLS/payment checks passed against all six migrations using PGlite.
- Edge handler: 11 Node tests passed, including request allowlists, callback HMAC, capture checks, webhook signature and replay-related database checks. The hosted Deno wrapper was not deployed or exercised.
- Android: 50 unit/Robolectric/Compose tests passed, zero failures/errors/skips. See `app/build/black-orange-final-verification.log` for the complete build/lint run.
- APK SHA-256: `8a574fdf68fcebcbed366576dc75aac570c0e02cbc154783856101de2fef2204`.
- This evidence does not establish a hosted Razorpay transaction. Complete the activation and operator checkout above before showcasing cloud payments.

## Connected action workflow update

The Fees screen defaults to Pending. Captured, server-verified fees disappear from that query and remain available under Paid/All Invoices; student records are never deleted. Success and provider-confirmed failure events notify the principal/admin/accountant and the relevant payer/student/parent in the same school/session. Failure events are deduplicated by provider payment ID and never credit a fee. Cancellation without a provider failure is shown locally, not forged into a permanent school event. Late capture takes precedence over failure when reconciling.

Enrollment now calls an authenticated principal/admin-only transaction. It inserts the student, an optional initial fee invoice, audit entry and school inbox messages atomically. Duplicate admission numbers and other-school sections are rejected. The form waits for confirmation before closing and reports failures. Enrollment does not invent passwords, create Auth accounts or silently link parents by email; account/guardian linking remains an operator step.

Hosted checks on this update found Auth health 200, students protected (401), **payment table 404 and payment function 404**. Therefore cloud checkout cannot work until migrations 6–7 and the Edge Function are deployed. The Android publishable key cannot perform that deployment. The read-only `diagnose_setup.sql` distinguishes missing Auth users, confirmation and demo profile provisioning; passwords stay local.
