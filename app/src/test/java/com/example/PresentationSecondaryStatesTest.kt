package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.teachers.TeacherProfileScreen
import com.example.ui.screens.attendance.PrincipalAttendanceScreen
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class PresentationSecondaryStatesTest {
  @get:Rule val compose=createComposeRule()
  private fun setup(role: UserRole): ErpDataRepository {
    val repo=ErpDataRepository.getInstance();repo.setDataMode(DataMode.DEMO);repo.switchRole(role)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil {repo.currentUser.value.role==role}
    return repo
  }
  private fun capture(name: String) {
    compose.waitForIdle();File("build/reports/presentation-rebuild/secondary").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/presentation-rebuild/secondary/$name.png")
  }
  @Test fun ownFacultyProfileAndEditFormRenderInBothPalettes() {
    val repo=setup(UserRole.TEACHER)
    var dark by mutableStateOf(false)
    compose.setContent {ScholaTheme(dark){Surface(Modifier.fillMaxSize()){TeacherProfileScreen(repo)}}}
    capture("faculty_profile_light")
    compose.onNodeWithText("Edit").performScrollTo().performClick()
    compose.onNodeWithText("Full name").assertExists()
    capture("faculty_edit_light")
    compose.runOnIdle {dark=true};capture("faculty_edit_dark")
  }
  @Test fun principalCanOpenClassThenRecordedStudentHistory() {
    val repo=setup(UserRole.PRINCIPAL)
    compose.setContent {ScholaTheme(false){PrincipalAttendanceScreen(repo,{})}}
    capture("attendance_overview_light")
    val cohort=repo.students.value.map {it.fullClass}.distinct().sorted().first()
    compose.onNodeWithTag("principal_attendance_overview").performScrollToNode(hasText("Class $cohort"))
    compose.onNodeWithText("Class $cohort").performClick()
    val student=repo.students.value.filter {it.fullClass==cohort}.minBy {it.rollNumber}
    compose.onNodeWithTag("student_att_${student.id}").performScrollTo().performClick()
    compose.onNodeWithText("Close").assertExists()
    capture("attendance_history_light")
  }
}
