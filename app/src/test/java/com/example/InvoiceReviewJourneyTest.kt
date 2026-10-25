package com.example
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.datasource.AppEnvironment
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.fees.FeeManagementScreen
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.After
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InvoiceReviewJourneyTest {
 @get:Rule val compose=createComposeRule()
 private val dispatcher=StandardTestDispatcher()
 private var fixture: ErpDataRepository?=null
 @Before fun setup(){Dispatchers.setMain(dispatcher)}
 @After fun cleanup(){
  fixture?.let { repository ->
    (ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply {isAccessible=true}.get(repository) as CoroutineScope).cancel()
  }
  dispatcher.scheduler.runCurrent();Dispatchers.resetMain()
 }
 @Test fun invoiceReviewPreservesItsIdentifierAndOutstandingAmountForCheckout() {
  // A separate in-memory fixture avoids inheriting a fee already settled by another test.
  // This never resets the application singleton or touches hosted records.
  AppEnvironment.setMode(DataMode.DEMO)
  val repository=ErpDataRepository::class.java.getDeclaredConstructor().apply {isAccessible=true}.newInstance()
  fixture=repository
  repository.setDataMode(DataMode.DEMO)
  dispatcher.scheduler.runCurrent()
  repository.switchRole(UserRole.PARENT)
  dispatcher.scheduler.runCurrent()
  assertEquals(UserRole.PARENT,repository.currentUser.value.role)
  assertTrue("Parent must have a selected linked child",repository.selectedStudentId.value.isNotBlank())
  val context = "mode=${repository.dataMode.value}; selected=${repository.selectedStudentId.value}; " +
    "fees=${repository.feeRecords.value.map {it.id to it.studentId}}; " +
    "invoices=${repository.invoices.value.map {it.id to it.studentId}}; state=${repository.backendState.value}"
  assertTrue(context, repository.invoices.value.any {it.pendingAmount>0 && it.studentId==repository.selectedStudentId.value})
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
