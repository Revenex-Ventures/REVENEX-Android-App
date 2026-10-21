package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.academics.*
import com.example.ui.screens.attendance.*
import com.example.ui.screens.communication.*
import com.example.ui.screens.fees.*
import com.example.ui.screens.operations.*
import com.example.ui.screens.reports.*
import com.example.ui.screens.search.*
import com.example.ui.screens.students.*
import com.example.ui.screens.teachers.*
import com.example.ui.screens.ai.*
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
@Config(sdk=[34], qualifiers="w393dp-h852dp-xhdpi")
class SecondaryScreensScreenshotTest {
  @get:Rule val compose = createComposeRule()
  private data class Sample(val name: String, val render: @Composable () -> Unit)
  @Test fun reachableSecondaryScreensRenderInBothPalettes() {
    val repository = ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    repository.switchRole(UserRole.PRINCIPAL)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val studentId = repository.students.value.first().id
    val teacherId = repository.teachers.value.first().id
    val samples = listOf(
      Sample("students") { StudentListScreen(repository,{},{}) },
      Sample("student_detail") { StudentDetailScreen(studentId,repository,{}, {_,_->}) },
      Sample("teachers") { TeacherListScreen(repository,{},{}) },
      Sample("teacher_detail") { TeacherDetailScreen(teacherId,repository,{}) },
      Sample("teacher_profile") { TeacherProfileScreen(repository) },
      Sample("attendance") { AttendanceScreen(repository,onBack={}) },
      Sample("principal_attendance") { PrincipalAttendanceScreen(repository,{}) },
      Sample("timetable") { TimetableScreen(repository) },
      Sample("assignments") { HomeworkScreen(repository,{}) },
      Sample("examinations") { ExamsScreen(repository) },
      Sample("results") { ReportCardScreen(repository) },
      Sample("study_material") { StudyMaterialScreen(repository) },
      Sample("fees") { FeeManagementScreen(repository,{_,_->}) },
      Sample("notices") { NoticesScreen(repository,{}) },
      Sample("notifications") { NotificationsScreen(repository,{}) },
      Sample("modules") { OperationsHubScreen(repository,{},{},{},{},{}) },
      Sample("leave") { LeaveManagementScreen(repository,{}) },
      Sample("transport") { TransportScreen(repository) },
      Sample("library") { LibraryScreen(repository) },
      Sample("inventory") { InventoryScreen(repository) },
      Sample("reports") { ReportsAnalyticsScreen(repository) },
      Sample("search") { GlobalSearchScreen(repository,{},{}) },
      Sample("assistant") { AiAssistantScreen(repository,{}) }
    )
    var selected by mutableIntStateOf(0)
    var dark by mutableStateOf(false)
    compose.setContent { ScholaTheme(dark) {
      Surface(Modifier.fillMaxSize()) { key(selected,dark) { samples[selected].render() } }
    } }
    File("build/reports/redesign/secondary").mkdirs()
    for (appearance in listOf(false,true)) samples.forEachIndexed { index, sample ->
      compose.runOnIdle { selected=index; dark=appearance }
      compose.onRoot().captureRoboImage("build/reports/redesign/secondary/${sample.name}_${if(appearance) "dark" else "light"}.png")
    }
  }
}
