package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.StudentDashboardScreen
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class NestedWorkflowScreenshotTest {
  @get:Rule val compose = createComposeRule()
  private fun capture(name: String) {
    File("build/reports/redesign/nested").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/redesign/nested/$name.png")
  }

  @Test fun paymentAndPublishingStatesInBothPalettes() {
    val repository = ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    var state by mutableIntStateOf(0)
    var dark by mutableStateOf(false)
    val names = listOf("payment_review", "payment_pending", "payment_error", "payment_receipt", "notice_form", "assignment_form", "leave_form", "faculty_form")
    compose.setContent { ScholaTheme(dark) { Surface(Modifier.fillMaxSize()) {
      key(state, dark) {
        when (state) {
          in 0..3 -> FeePaymentDialog("Synthetic student",125000L,state == 1,state == 3,
            if(state == 3) "pay_synthetic_fixture" else "",{}, {},
            receiptNo = if(state == 3) "RZP-TEST-fixture" else "",
            statusMessage = when(state) { 1 -> "Verifying capture with the school payment server…"; 2 -> "Payment could not be verified. Check status before trying again."; else -> "" },
            onCheckStatus = {})
          4 -> CreateNoticeDialog({}, {})
          5 -> CreateAssignmentDialog("Demo teacher",repository,{}, {})
          6 -> ApplyLeaveDialog("Demo parent",UserRole.PARENT,{}, { _,_,_,_,_ -> })
          7 -> AddTeacherDialog(onDismiss = {}, onAddTeacher = {})
        }
      }
    } } }
    for (palette in listOf(false,true)) names.forEachIndexed { index,name ->
      compose.runOnIdle { state=index; dark=palette }
      compose.waitForIdle()
      val expected = when(index) {
        0 -> "Review test payment"
        1 -> "Verifying capture with the school payment server…"
        2 -> "Payment could not be verified. Check status before trying again."
        3 -> "Payment Received Successfully!"
        4 -> "Publish a notice"
        5 -> "New assignment"
        6 -> "Request leave"
        else -> "Add faculty member"
      }
      compose.onNodeWithText(expected).assertExists()
      compose.waitForIdle()
      if(index == 0) compose.onNodeWithText("Verified receipt").assertDoesNotExist()
      if(index == 1) compose.onNodeWithTag("submit_fee_payment_button").assertDoesNotExist()
      capture("${name}_${if(palette) "dark" else "light"}")
    }
  }

  @Test @Config(qualifiers = "w320dp-h640dp-xhdpi")
  fun narrowLargeTextNoticeKeepsCategoryAndPublishReachable() {
    compose.setContent {
      val density = LocalDensity.current
      CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
        ScholaTheme(false) { Surface(Modifier.fillMaxSize()) { CreateNoticeDialog({}, {}) } }
      }
    }
    compose.onNodeWithTag("input_notice_title").performTextInput("Class update")
    compose.onNodeWithTag("input_notice_content").performScrollTo().performTextInput("The class schedule has been updated.")
    compose.onNodeWithTag("submit_publish_notice_button").performScrollTo().assertIsEnabled()
    capture("notice_narrow_large_text")
  }

  @Test fun parentCanReachFeeAccountsDirectlyFromHome() {
    val repository = ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    repository.switchRole(UserRole.PARENT)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    var route = ""
    compose.setContent { ScholaTheme(false) { StudentDashboardScreen(repository,{ route=it }) } }
    compose.onNodeWithTag("family_fee_accounts").performScrollTo().performClick()
    compose.runOnIdle { assertEquals(Screen.Fees.route,route) }
  }
}
