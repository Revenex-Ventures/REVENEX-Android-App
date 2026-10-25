package com.example
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.ScholaTheme
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h820dp-xhdpi")
class PrincipalNavigationSpecTest {
 @get:Rule val compose=createComposeRule()
 @Test fun homeReturnsToDashboardAfterOpeningAndReturningFromARecord() {
  val repository=ErpDataRepository.getInstance();repository.setDataMode(DataMode.DEMO);repository.logout()
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  compose.setContent {ScholaTheme(false,principal=true){ScholaErpApp(false,false,"",{_,_->},{})}}
  compose.onNodeWithTag("try_demo").performScrollTo().performClick()
  compose.onNodeWithTag("role_select_principal").performScrollTo().performClick()
  compose.onNodeWithTag("submit_login_button").performScrollTo().performClick()
  compose.waitUntil(10000){compose.onAllNodesWithTag("principal_dashboard_list").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("principal_module_students").performScrollTo().performClick()
  compose.onNodeWithTag("principal_students").assertExists()
  val student=repository.students.value.first {it.status=="ACTIVE"}
  compose.onNodeWithTag("student_row_${student.id}").performScrollTo().performClick()
  compose.onNodeWithText("Student profile").assertExists()
  compose.onAllNodesWithContentDescription("Back").onFirst().performClick()
  compose.onNodeWithTag("principal_students").assertExists()
  compose.onNodeWithContentDescription("Home").performClick()
  compose.onNodeWithTag("principal_dashboard_list").assertExists()
  compose.onNodeWithTag("principal_students").assertDoesNotExist()
 }
}
