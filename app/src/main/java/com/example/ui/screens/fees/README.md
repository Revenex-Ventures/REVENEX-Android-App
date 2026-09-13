# screens/fees/

Fee management, ledger, and payment screen.

## Files

### `FeeManagementScreen.kt`

Financial ledger and payment interface.

**For Principal/Accountant:**
- List of all students with outstanding dues, sorted by overdue amount.
- Fee collection summary: total collected, total pending, overdue count.
- Record offline (cash/cheque) payments via the backend API.

**For Parent:**
- Scoped to the selected child's fee records only.
- Invoice list with due dates, amount, and status (PAID / PARTIAL / PENDING / OVERDUE).
- Tapping an invoice opens `FeePaymentDialog` which triggers a Razorpay payment flow.
- After a successful Razorpay callback in `MainActivity.kt`, `processFeePayment()` updates the ledger.
- Transaction receipt history per invoice.

Accessible to: Principal, Accountant (all students); Parent (own child only); Student (read-only view).
