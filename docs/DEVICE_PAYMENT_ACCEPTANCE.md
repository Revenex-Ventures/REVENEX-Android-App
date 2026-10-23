# Device Razorpay TEST checkout and receipt verification

No real payment details, private keys, OTPs, tokens or passwords should be sent in chat. Use an existing synthetic Supabase parent/student account and an existing outstanding demo invoice. Do not reset/reseed or alter paid fees to repeat a test.

## What is established so far

- Operator saved all three exact server-secret names. Unsigned user/webhook probes reject with 401/400.
- User reports reaching Razorpay, with slow payment-method loading and no receipt.
- Read-only hosted audit on 23 October 2026: **1 order, 0 creating, 1 created, 0 paid, 0 receipts, 0 paid-without-receipt, 0 duplicate payments**. This establishes order persistence, not captured payment. Duplicate counts with no receipts do not prove hosted replay handling.
- The app had a separate display bug: Fees → Check payment status called reconciliation without reopening the dialog. Fixed in 6830b38. A confirmed result can now display its receipt even after checkout closes.
- Preload previously ran immediately before Checkout.open. It now starts for a signed-in cloud payment-capable account before the user pays. The pinned SDK version remains unchanged. This may reduce resource-loading delay; performance is not yet measured on the user's device.

## Steps requiring the device user

1. Install the latest verified APK listed in the delivery report; retain data if signatures match. Sign in via **Supabase Demo** with the synthetic student or parent account.
2. Home → **Fee accounts · invoices & receipts**. Older APK: Resources/Notes → Fee Accounts. Choose **Pending** and note the invoice balance locally.
3. Tap **Pay**. Confirm the review identifies TEST mode and the expected invoice amount. Payment-method details are handled by Razorpay, not Revenex.
4. Use only the provider's [official test details](https://github.com/razorpay/markdown-docs/blob/master/payments/payments/test-card-details.md). Complete the mock Success flow yourself. Do not use real card/UPI credentials. Available test methods depend on the provider's test checkout.
5. Return to Revenex in the **same account that started checkout**. Wait for server verification. Only a captured/verified server response may display the receipt. If pending/interrupted, use **Check payment status**; do not immediately start another charge. The current status lookup filters orders by the signed-in payer; switching to another linked family account does not transfer that order.
6. Confirm a receipt number, payment ID and amount are displayed. Confirm the balance changes and the invoice moves from Pending to Paid. Reopen Paid → View receipt/status, then sign out/in and confirm persistence.
7. Sign in as principal: check the matching student's payment inbox entry. Parent/student copies should have independent read states. Report only sanitized status/error text if something fails.

## Server/operator checks

Run `supabase/payment_acceptance_readonly.sql` in SQL Editor to inspect aggregate persisted counts. After a successful full payment, paid and receipt counts should increase together; paid-without-receipt and duplicate-payment counts should remain zero. This query does not verify the provider's signature/capture independently and does not settle fees.

In the **Razorpay test dashboard**, verify the payment is captured and the configured payment.captured/payment.failed webhook points to:

`https://nhaxnuaxyqkgnlkwpqkw.supabase.co/functions/v1/razorpay-payments/webhook`

The webhook secret must match the backend secret; enter it only through provider/Supabase secret interfaces. Actual webhook delivery has not been inspected. Once a captured test event exists, resend that same event through the provider dashboard, then rerun the read-only audit: receipt, fee credit and capture notification must not duplicate. Callback plus webhook replay is covered locally, not yet established in the hosted project.

Cancel/failure checks require another existing unpaid synthetic invoice. Cancellation must not fabricate a permanent failed-payment event; provider-confirmed failure may create a scoped inbox entry without crediting fees. Never delete an order to bypass uncertain capture or reconciliation.

## First failing stage guide

| Observed state | Stage to inspect |
|---|---|
| No Pay/review screen | UI role/navigation/guard |
| Sign-in/access/configuration error | Authenticated order request, ownership or deployment/secrets |
| Order stuck creating | Provider request/bind uncertainty; reconcile server-side before retrying |
| Created order, slow checkout methods | Checkout resource/provider/network stage; does not imply paid |
| Checkout success but no receipt | Callback matching, signature, captured provider status, settlement, then dialog display |
| Receipt exists but stale invoice | Authorized ledger refresh/reconnect |
| Receipt exists but no school inbox | Recipient-scoped notification transaction/delivery |

Never mark the fee paid solely from the SDK callback. Report exact sanitized errors, whether checkout returned, and whether Check payment status produces a receipt; retain all private values locally.

Provider preload guidance: [official Android sample](https://github.com/razorpay/razorpay-android-sample-app/blob/master/app/src/main/java/com/razorpay/sampleapp/java/PaymentActivity.java).
