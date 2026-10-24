package com.example
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.ScholaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class ApprovedNavigationJourneyTest {
 @get:Rule val compose=createComposeRule()
 @Test fun administratorAcademicsAndActivityOpenTheirActualWorkspaces() {
  val repository=ErpDataRepository.getInstance();repository.setDataMode(DataMode.DEMO);repository.logout()
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  compose.setContent {ScholaTheme(false){ScholaErpApp(false,false,"",{_,_->},{})}}
  compose.onNodeWithTag("try_demo").performScrollTo().performClick()
  compose.onNodeWithTag("role_select_admin").performScrollTo().performClick()
  compose.onNodeWithTag("submit_login_button").performScrollTo().performClick()
  compose.waitUntil(10000){compose.onAllNodesWithTag("admin_dashboard_list").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithContentDescription("People").performClick()
  compose.onNodeWithTag("fab_admit_student").assertExists()
  compose.onNodeWithText("Faculty").performClick()
  compose.onNodeWithTag("fab_add_faculty").assertExists()
  compose.onNodeWithText("Students").performClick()
  compose.onNodeWithTag("fab_admit_student").assertExists()
  compose.onNodeWithContentDescription("Academics").performClick()
  compose.waitUntil(10000){compose.onAllNodesWithTag("operations_hub_screen").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("workspace_homework").performScrollTo().performClick()
  compose.onNodeWithText("Assignments").assertExists()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick();compose.waitForIdle()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick();compose.waitForIdle()
  compose.onNodeWithTag("admin_dashboard_list").performScrollToNode(hasTestTag("section_action_recent_activity"))
  compose.onNodeWithTag("section_action_recent_activity").assertTouchHeightIsEqualTo(48.dp).performClick()
  compose.onNodeWithText("School activity").assertExists()
  compose.onNodeWithText("Audit log").assertExists()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick()
  compose.onNodeWithTag("admin_dashboard_list").assertExists()
  compose.onNodeWithContentDescription("More").performClick()
  compose.onNodeWithTag("operations_hub_screen").performScrollToNode(hasTestTag("workspace_principal_attendance"))
  compose.onNodeWithTag("workspace_principal_attendance").performClick()
  compose.onNodeWithTag("principal_attendance_overview").assertExists()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick()
  compose.onNodeWithContentDescription("Account and settings").performClick()
  compose.onNodeWithText("Settings").performClick()
  compose.onNodeWithText("Sign out").performScrollTo().performClick()
  compose.onNodeWithText("Welcome to Revenex").assertExists()
  compose.onNodeWithTag("admin_dashboard_list").assertDoesNotExist()
 }
}
