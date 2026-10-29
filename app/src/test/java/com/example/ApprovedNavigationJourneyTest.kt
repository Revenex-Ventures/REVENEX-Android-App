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
 @Test fun principalSharedPeopleAcademicsAndAttendanceRemainReachable() {
  val repository=ErpDataRepository.getInstance();repository.setDataMode(DataMode.DEMO);repository.logout()
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  compose.setContent {ScholaTheme(false){ScholaErpApp(false,false,"",{_,_->},{})}}
  compose.onNodeWithTag("portal_admin").assertDoesNotExist()
  compose.onNodeWithTag("try_demo").performScrollTo().performClick()
  compose.onNodeWithTag("role_select_admin").assertDoesNotExist()
  compose.onNodeWithTag("role_select_principal").performScrollTo().performClick()
  compose.onNodeWithTag("submit_login_button").performScrollTo().performClick()
  compose.waitUntil(10000){compose.onAllNodesWithTag("principal_dashboard_list").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("principal_module_students").performScrollTo().performClick()
  compose.onNodeWithTag("principal_students").assertExists()
  compose.onNodeWithTag("fab_admit_student").assertExists()
  compose.onNodeWithContentDescription("Home").performClick()
  compose.onNodeWithTag("principal_module_staff").performScrollTo().performClick()
  compose.onNodeWithTag("principal_staff").assertExists()
  compose.onNodeWithTag("fab_add_faculty").assertExists()
  compose.onNodeWithContentDescription("Home").performClick()
  compose.onNodeWithTag("principal_module_academics").performScrollTo().performClick()
  compose.onNodeWithTag("principal_academics").assertExists()
  compose.onNodeWithContentDescription("Home").performClick()
  compose.onNodeWithTag("principal_module_attendance").performScrollTo().performClick()
  compose.onNodeWithTag("principal_attendance_overview").assertExists()
  compose.onNodeWithTag("admin_dashboard_list").assertDoesNotExist()
 }
}
