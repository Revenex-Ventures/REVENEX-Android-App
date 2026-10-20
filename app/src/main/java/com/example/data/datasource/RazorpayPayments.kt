package com.example.data.datasource

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class RazorpayCheckoutOrder(val reference:String,val order_id:String,val key_id:String,val amount:Long,val currency:String) {
  fun validate() {
    require(reference.matches(Regex("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) &&
      order_id.matches(Regex("order_[A-Za-z0-9]+")) && key_id.startsWith("rzp_test_") &&
      amount in 100L..1000000000L && currency=="INR") { "Invalid test checkout order from server." }
  }
}

@Serializable
data class RazorpayPaymentResult(
  val status:String,val reference:String="",val payment_id:String="",val receipt_no:String="",
  val amount:Long=0,val currency:String="INR",val message:String=""
) {
  val verifiedPaid:Boolean get()=status=="paid" && payment_id.startsWith("pay_") &&
    receipt_no.startsWith("RZP-TEST-") && amount>0 && currency=="INR"
}

/** Only public checkout data crosses this boundary. No Razorpay secret in Android. */
object RazorpayPayments {
  private fun requireSession():String {
    check(AppEnvironment.currentMode==DataMode.SUPABASE_DEMO) { "Use Supabase Demo for Razorpay test checkout." }
    return requireNotNull(SupabaseProvider.client.auth.currentUserOrNull()) { "Sign in before paying fees." }.id
  }
  private suspend inline fun <reified T> invoke(body:JsonObject):T {
    requireSession()
    try {
      val response=SupabaseProvider.client.functions.invoke("razorpay-payments",body=body,
        headers=Headers.build { append(HttpHeaders.ContentType,ContentType.Application.Json.toString()) })
      check(response.status.isSuccess())
      return SupabaseProvider.json.decodeFromString<T>(response.bodyAsText())
    } catch(e:Exception) {
      if(e is CancellationException) throw e
      throw IllegalStateException("Payment verification is unavailable. Check the server setup, sign-in and network. No fee credit was assumed.")
    }
  }
  suspend fun create(feeId:String):RazorpayCheckoutOrder = invoke<RazorpayCheckoutOrder>(buildJsonObject {
    put("action","create");put("fee_id",feeId)
  }).also { it.validate() }
  suspend fun verify(order:RazorpayCheckoutOrder,paymentId:String,providerOrderId:String,signature:String):RazorpayPaymentResult {
    order.validate()
    require(providerOrderId==order.order_id && paymentId.startsWith("pay_") && signature.matches(Regex("[0-9a-fA-F]{64}"))) { "Checkout did not return complete verification data." }
    return invoke(buildJsonObject {
      put("action","verify");put("reference",order.reference);put("payment_id",paymentId)
      put("provider_order_id",providerOrderId);put("signature",signature)
    })
  }
  @Serializable private data class Reference(val id:String)
  suspend fun checkFee(feeId:String):RazorpayPaymentResult? {
    val payer=requireSession()
    val latest=SupabaseProvider.client.from("razorpay_orders").select {
      filter { eq("fee_id",feeId);eq("payer_id",payer) }
      order("created_at",Order.DESCENDING);limit(1)
    }.decodeList<Reference>().firstOrNull() ?: return null
    return invoke(buildJsonObject { put("action","status");put("reference",latest.id) })
  }
}
