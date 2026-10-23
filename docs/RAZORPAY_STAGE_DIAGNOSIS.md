# Current Razorpay test integration trace

| Stage | Evidence / status |
|---|---|
| Pay button / guard | Fees calls onPayRazorpay → MainActivity.startRazorpayPayment. Local demo blocked intentionally; authenticated Supabase path enabled. |
| Authenticated order request | RazorpayPayments requires an Auth user, invokes razorpay-payments with action=create and fee_id only. No amount/role override sent. Hosted authenticated request untested without a test account session. |
| Deployment/configuration | Public endpoint returns 404; razorpay_orders relation also returns 404. Checkout cannot reach order creation. Missing migration/deployment must be resolved by the project operator. Server secrets cannot be inspected with a publishable key. |
| Ownership / amount | reserve_razorpay_order SQL validates school/session, payer/student links and payable balance; server reservation supplies integer paise. Local RLS/payment tests only. |
| Provider order | Server creates INR order with reserved amount and binds provider ID; mocked-provider tests pass. No real provider request tested. |
| Android checkout | Validates server reference/order, rzp_test key, amount bounds and INR; uses returned key/order/amount. Not launched against a hosted-created order. |
| Verification | Client callback calls verify; server checks stored order HMAC plus fetched captured provider payment. A callback alone never credits fees. |
| Webhook / persistence | Raw-body HMAC required; database settlement is atomic/idempotent with receipt and inbox writes. Local tests cover forged signatures, captured/failed/pending and replay; hosted delivery not tested. |

Implemented fix: sanitized client errors distinguish expired login, access denial, absent payment deployment, conflict/reconciliation and server unavailability where HTTP status is available. No server body/credential is surfaced. Existing secure guard remains.

Operator steps: apply missing migrations 6–9 in order (do not reset/reseed existing data), configure test credentials/webhook secret directly in Supabase secrets, deploy razorpay-payments to the configured project, configure test capture/failure webhook, then use a synthetic signed-in student to run success/cancel/failure/replay checks. See supabase/RAZORPAY_SETUP.md. No secrets should be sent in chat. Existing chat-exposed private keys should be replaced directly at the provider.

This is not a claim that Razorpay is active or that the full redesign has passed acceptance.
