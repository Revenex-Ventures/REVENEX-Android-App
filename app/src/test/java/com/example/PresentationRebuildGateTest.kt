package com.example

import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.ScholaResponsiveScaffold
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.*
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Same routed Composables and scaffold as MainActivity; synthetic data, 393dp / 1x font. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34], qualifiers="w393dp-h852dp-xhdpi")
class PresentationRebuildGateTest {
  @get:Rule val compose = createComposeRule()
  private fun capture(name: String) {
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.mainClock.advanceTimeBy(300)
    compose.waitForIdle()
    File("build/reports/presentation-rebuild").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/presentation-rebuild/$name.png")
  }
  private fun dashboard(role: UserRole, dark: Boolean) {
    val repo = ErpDataRepository.getInstance()
    repo.setDataMode(DataMode.DEMO); repo.switchRole(role)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil { repo.currentUser.value.role == role }
    var destination = ""
    compose.setContent {
      val user by repo.currentUser.collectAsState()
      ScholaTheme(dark,principal=role==UserRole.PRINCIPAL) {
        ScholaResponsiveScaffold(Screen.Dashboard.route,user,2,{ destination=it },{},{},{},{}) {
          if (role == UserRole.PRINCIPAL) PrincipalDashboardScreen(repo,{destination=it},{},{},{})
          else TeacherDashboardScreen(repo,{destination=it},{},{})
        }
      }
    }
    val name = "${role.name.lowercase()}_${if(dark) "dark" else "light"}"
    capture(name)
    if (role == UserRole.PRINCIPAL) {
      listOf("students","staff","attendance","academics","fees","reports").forEach {
        compose.onNodeWithTag("principal_module_$it").performScrollTo().assertExists()
      }
      compose.onNodeWithTag("principal_module_attendance").performClick()
    } else {
      compose.onNodeWithText("Mark attendance").performScrollTo().performClick()
    }
    if (role == UserRole.TEACHER) {
      val cohort = repo.teachers.value.firstOrNull { it.email.equals(repo.currentUser.value.email,true) }?.assignedClasses?.firstOrNull() ?: repo.currentUser.value.associatedClass
      assertEquals(Screen.Attendance.createRoute(cohort.substringBeforeLast("-"),cohort.substringAfterLast("-")),destination)
    } else assertEquals(Screen.PrincipalAttendance.route,destination)
    if (role == UserRole.PRINCIPAL) {
      compose.onNodeWithTag("principal_dashboard_list").performScrollToNode(hasText("Needs attention")); capture(name+"_attention")
      compose.onNodeWithTag("principal_dashboard_list").performScrollToNode(hasText("Upcoming")); capture(name+"_upcoming")
    } else {
      compose.onNodeWithTag("teacher_dashboard_list").performScrollToNode(hasText("Teaching tools")); capture(name+"_tools")
    }
  }
  @Test fun principalLight() = dashboard(UserRole.PRINCIPAL,false)
  @Test fun principalDark() = dashboard(UserRole.PRINCIPAL,true)
  @Test fun teacherLight() = dashboard(UserRole.TEACHER,false)
  @Test fun teacherDark() = dashboard(UserRole.TEACHER,true)
  @Test fun attendanceRegisterRowsRetainSelection() {
    val repo = ErpDataRepository.getInstance()
    repo.setDataMode(DataMode.DEMO);repo.switchRole(UserRole.TEACHER)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    var dark by mutableStateOf(false)
    compose.setContent {
      val user by repo.currentUser.collectAsState()
      ScholaTheme(dark) {
        ScholaResponsiveScaffold(Screen.Attendance.route,user,0,{},{},{},{},{}) {
          AttendanceScreen(repo,initialClassGrade="10",initialDivision="A",onBack={})
        }
      }
    }
    capture("attendance_light")
    compose.onNodeWithTag("attendance_matrix_view").performScrollToIndex(3)
    val firstClass = "Class 10-A"
    val first = repo.students.value.filter { "Class ${it.classGrade}-${it.division}" == firstClass }.minBy { it.rollNumber }
    compose.onNodeWithTag("btn_absent_${first.rollNumber}").performScrollTo().performClick()
    compose.onNodeWithTag("btn_absent_${first.rollNumber}").assertIsSelected()
    compose.onNodeWithTag("btn_present_${first.rollNumber}").assertIsNotSelected()
    compose.onNodeWithTag("submit_save_attendance").assertExists().assertIsEnabled()
    compose.runOnIdle { dark = true }
    capture("attendance_dark")
  }
}
