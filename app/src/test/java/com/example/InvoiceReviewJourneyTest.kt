package com.example
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.fees.FeeManagementScreen
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class InvoiceReviewJourneyTest {
 @get:Rule val compose=createComposeRule()
 @Test fun invoiceReviewPreservesItsIdentifierAndOutstandingAmountForCheckout() {
  val repository=ErpDataRepository.getInstance();repository.setDataMode(DataMode.DEMO);repository.switchRole(UserRole.PARENT)
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  var opened="";var amount=0L
  compose.setContent {ScholaTheme(false){FeeManagementScreen(repository,{_,_->},onPayRazorpay={id,_,due->opened=id;amount=due})}}
  compose.waitUntil(10000) {
    repository.invoices.value.any {it.pendingAmount>0 && it.studentId==repository.selectedStudentId.value}
  }
  val selected=repository.selectedStudentId.value
  val invoice=repository.invoices.value.first {it.pendingAmount>0 && it.studentId==selected}
  val count=repository.invoices.value.count {it.pendingAmount>0 && it.studentId==selected}
  compose.onNodeWithText("Pending ($count)").assertExists()
  compose.onNodeWithTag("invoice_row_${invoice.id}").performScrollTo().performClick()
  compose.onNodeWithText(invoice.invoiceNumber).performScrollTo().assertExists()
  compose.onNodeWithTag("pay_invoice_${invoice.id}").performScrollTo().performClick()
  assertEquals(invoice.id,opened);assertEquals(invoice.pendingAmount,amount)
 }
}
