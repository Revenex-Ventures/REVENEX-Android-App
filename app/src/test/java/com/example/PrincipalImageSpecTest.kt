package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.ScholaResponsiveScaffold
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.PrincipalDashboardScreen
import com.example.ui.screens.principal.*
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h820dp-xhdpi")
class PrincipalImageSpecTest {
 @get:Rule val compose=createComposeRule()
 @Test fun batchARendersProductionRoutesAndSixWorkingShortcuts() {
  val repo=ErpDataRepository.getInstance();repo.setDataMode(DataMode.DEMO);repo.switchRole(UserRole.PRINCIPAL)
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  var page by mutableIntStateOf(0);var dark by mutableStateOf(false);var route=""
  val routes=listOf(Screen.Dashboard.route,Screen.Students.route,Screen.Teachers.route,Screen.PrincipalAttendance.route,Screen.Academics.route,Screen.Fees.route,Screen.Reports.route)
  compose.setContent {val user by repo.currentUser.collectAsState();ScholaTheme(dark,principal=true){Box(Modifier.fillMaxWidth().height(if(page==0)820.dp else 640.dp).testTag("reference_viewport")){ScholaResponsiveScaffold(routes[page],user,2,{route=it},{},{},{},{},onBack={}){
   key(page,dark){when(page){
    0->PrincipalDashboardScreen(repo,{route=it},{},{},{})
    1->PrincipalStudents(repo,{route=Screen.StudentDetail.createRoute(it)},{})
    2->PrincipalStaff(repo,{route=Screen.TeacherDetail.createRoute(it)},{})
    3->PrincipalAttendanceOverview(repo,{c,d->route=Screen.PrincipalClass.createRoute(c,d)},{route=Screen.Reports.route})
    4->PrincipalAcademics(repo){route=it}
    5->PrincipalFees(repo){route=it}
    else->PrincipalReports(repo)
   }}
  }}}}
  File("build/reports/principal-spec/batch-a").mkdirs()
  for(palette in listOf(false,true))for(index in routes.indices){
   compose.runOnIdle {page=index;dark=palette};compose.waitForIdle()
   val name=listOf("home","students","staff","attendance","academics","fees","reports")[index]
   compose.onNodeWithTag("reference_viewport").captureRoboImage("build/reports/principal-spec/batch-a/${name}_${if(palette)"dark" else "light"}.png")
  }
  compose.runOnIdle {page=0;dark=false}
  listOf("students" to Screen.Students.route,"staff" to Screen.Teachers.route,"attendance" to Screen.PrincipalAttendance.route,"academics" to Screen.Academics.route,"fees" to Screen.Fees.route,"reports" to Screen.Reports.route).forEach {(label,target)->
   compose.onNodeWithTag("principal_module_$label").performScrollTo().performClick();assertEquals(target,route)
  }
  compose.onNodeWithTag("principal_dashboard_list").performScrollToNode(hasText("Upcoming"))
  compose.onNodeWithTag("reference_viewport").captureRoboImage("build/reports/principal-spec/batch-a/home_lower_light.png")
 }
}
