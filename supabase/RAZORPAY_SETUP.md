# Razorpay TEST payments

The active payment path is the Supabase Edge Function at [functions/razorpay-payments](functions/razorpay-payments). Android receives only a public key ID, server order ID and authorized amount. It never receives a payment secret or service-role credential.

## Operator configuration

1. Reconcile the existing schema before deployment. Payment schema begins with migration 6 and protected settlement/action functions are extended by later migrations. Do not rerun migrations or seed an existing project.
2. Configure matching TEST values directly in Supabase Edge Function Secrets: `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` and `RAZORPAY_WEBHOOK_SECRET`. Never put private values in Android, Git, local public properties or chat.
3. Deploy the entire function directory, preserving `index.ts` and its relative `core.mjs` import:

```powershell
supabase functions deploy razorpay-payments --project-ref <your-demo-project-ref>
```

[config.toml](config.toml) disables the legacy gateway JWT check so the webhook can reach the handler. The handler independently verifies signed-in requests with Supabase Auth and raw webhook HMAC. Do not remove those checks.

4. Configure the TEST provider webhook for `payment.captured` and `payment.failed` at `https://<project-ref>.supabase.co/functions/v1/razorpay-payments/webhook`, using the matching backend webhook secret. Automatic capture must be configured appropriately for the TEST workflow. Real webhook delivery remains to be accepted on a device/provider run.

## App and settlement flow

School sign-in as the synthetic Student/Guardian → Fees → Pending → invoice → Pay → confirm TEST mode. Local Demo does not prove or simulate verified provider settlement.

The server derives the invoice owner/payable balance and creates the INR order using integer paise. Android checkout receives matching public TEST credentials/order. A callback triggers server verification; it is not proof of payment. The backend validates HMAC and provider order/payment/amount/currency/capture, then atomically credits the fee, creates one receipt and records audit/inbox events. Duplicate callbacks/webhooks must not credit again.

After an interrupted checkout, keep the existing reference and use Check payment status in the same authorized account. Interrupted provider order creation is recovered only by an exact server receipt and validated amount/currency. Ambiguous or missing outcomes remain unresolved; never delete reservations or create replacement charges blindly after a possible capture.

## Acceptance and limits

Use the [device checklist](../docs/DEVICE_TESTING.md) for success, cancellation, failure, duplicate status checks, receipt/balance and principal inbox. Node/PGlite tests exercise mocked providers and isolated settlement rules; they do not establish real capture. The latest hosted handler recovery update is deployed, but a captured device TEST transaction and provider webhook delivery remain unverified.

No live keys, partial payments, refunds, disputes, production accounting or SMS/email delivery are enabled by this demo. No client-side manual fee credit is permitted. See [security](../docs/SECURITY.md) and [limitations](../docs/KNOWN_LIMITATIONS.md).

Official integration reference: [Razorpay Android integration](https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/integration-steps/).
