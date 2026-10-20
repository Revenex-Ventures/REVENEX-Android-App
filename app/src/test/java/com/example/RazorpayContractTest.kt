package com.example

import com.example.data.datasource.RazorpayCheckoutOrder
import com.example.data.datasource.RazorpayPaymentResult
import org.junit.Assert.*
import org.junit.Test

class RazorpayContractTest {
  private val order=RazorpayCheckoutOrder("00000000-0000-0000-0000-000000000001","order_123","rzp_test_public",1000,"INR")
  @Test fun onlyBoundTestOrdersInPaiseCanOpenCheckout() {
    order.validate()
    listOf(order.copy(key_id="rzp_live_private"),order.copy(currency="USD"),order.copy(amount=0),order.copy(order_id=""),order.copy(reference="")).forEach {
      assertTrue(runCatching { it.validate() }.isFailure)
    }
  }
  @Test fun checkoutCallbackAloneIsNeverAVerifiedPayment() {
    assertFalse(RazorpayPaymentResult(status="pending").verifiedPaid)
    assertFalse(RazorpayPaymentResult(status="paid",payment_id="pay_123").verifiedPaid)
    assertTrue(RazorpayPaymentResult("paid",order.reference,"pay_123","RZP-TEST-verified",1000,"INR").verifiedPaid)
    assertFalse(RazorpayPaymentResult("paid",order.reference,"pay_123","RZP-TEST-verified",1000,"USD").verifiedPaid)
  }
}
