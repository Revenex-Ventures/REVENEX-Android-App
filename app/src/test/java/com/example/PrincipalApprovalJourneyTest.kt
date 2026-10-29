package com.example
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.*
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.principal.*
import com.example.ui.theme.ScholaTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h820dp-xhdpi")
@OptIn(ExperimentalCoroutinesApi::class)
class PrincipalApprovalJourneyTest {
 @get:Rule val compose=createComposeRule()
 private val dispatcher=StandardTestDispatcher()
 private lateinit var repository:ErpDataRepository
 @Before fun setup(){Dispatchers.setMain(dispatcher);AppEnvironment.setMode(DataMode.DEMO);repository=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance();repository.setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();repository.switchRole(UserRole.PRINCIPAL);dispatcher.scheduler.runCurrent()}
 @After fun cleanup(){(ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply{isAccessible=true}.get(repository) as CoroutineScope).cancel();dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}
 @Test fun decisionRequiresConfirmationAndRemovesActionsAfterConfirmedSave(){
  val request=repository.leaveRequests.value.first{it.status==LeaveStatus.PENDING}
  val before=repository.notifications.value.size
  compose.setContent{ScholaTheme(false,principal=true){PrincipalApprovalDetail(repository,request.id)}}
  compose.onNodeWithTag("principal_approval_comment").performScrollTo().performTextInput(" Reviewed ")
  compose.onNodeWithTag("principal_approve").performClick()
  assertEquals(LeaveStatus.PENDING,repository.leaveRequests.value.first{it.id==request.id}.status)
  compose.onNodeWithText("Confirm").performClick();dispatcher.scheduler.runCurrent();compose.waitForIdle()
  val updated=repository.leaveRequests.value.first{it.id==request.id}
  assertEquals(LeaveStatus.APPROVED,updated.status);assertEquals("Reviewed",updated.approverRemark)
  compose.onNodeWithTag("principal_approve").assertDoesNotExist()
  compose.onNodeWithTag("principal_decision_feedback").performScrollTo().assertTextEquals("Decision confirmed")
  assertEquals(before+1,repository.notifications.value.size)
 }
 @Test fun cancellingConfirmationLeavesRequestAndNotificationsUntouched(){
  val request=repository.leaveRequests.value.first{it.status==LeaveStatus.PENDING};val before=repository.notifications.value.size
  compose.setContent{ScholaTheme(false,principal=true){PrincipalApprovalDetail(repository,request.id)}}
  compose.onNodeWithText("Decline").performClick();compose.onNodeWithText("Cancel").performClick()
  dispatcher.scheduler.runCurrent();assertEquals(LeaveStatus.PENDING,repository.leaveRequests.value.first{it.id==request.id}.status);assertEquals(before,repository.notifications.value.size)
 }
 @Test fun unknownRequestHasNoDecisionControls(){
  compose.setContent{ScholaTheme(false,principal=true){PrincipalApprovalDetail(repository,"missing-request")}}
  compose.onNodeWithText("Request unavailable").assertExists();compose.onNodeWithTag("principal_approve").assertDoesNotExist()
 }
 @Test fun principalInvoiceReviewCannotLaunchParentCheckout(){
  val invoice=repository.invoices.value.first{it.pendingAmount>0}
  compose.setContent{ScholaTheme(false,principal=true){PrincipalFees(repository){fail("Local Principal review cannot fake a server receipt")}}}
  compose.onNodeWithTag("principal_invoice_${invoice.id}").performScrollTo().performClick()
  compose.onNodeWithText(invoice.invoiceNumber).assertExists();compose.onNodeWithText("Pay").assertDoesNotExist()
  compose.onNodeWithText("Done").performClick();assertEquals(invoice.pendingAmount,repository.invoices.value.first{it.id==invoice.id}.pendingAmount)
 }
}
