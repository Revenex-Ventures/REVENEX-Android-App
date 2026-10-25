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
import com.example.ui.theme.*
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
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
@OptIn(ExperimentalCoroutinesApi::class)
class PrincipalImageSpecTest {
 private val dispatcher=StandardTestDispatcher()
 private var fixture:ErpDataRepository?=null
 @Before fun setup(){Dispatchers.setMain(dispatcher)}
 @After fun cleanup(){fixture?.let{(ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply{isAccessible=true}.get(it) as CoroutineScope).cancel()};dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}

 @get:Rule val compose=createComposeRule()
 @Test fun batchARendersProductionRoutesAndSixWorkingShortcuts() {
  com.example.data.datasource.AppEnvironment.setMode(DataMode.DEMO)
  val repo=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance();fixture=repo;repo.setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();repo.switchRole(UserRole.PRINCIPAL);dispatcher.scheduler.runCurrent()
  val request=repo.leaveRequests.value.first{it.status==com.example.data.model.LeaveStatus.PENDING}
  val preference=AppearancePreference(ApplicationProvider.getApplicationContext());preference.select(Appearance.LIGHT)
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  var page by mutableIntStateOf(0);var dark by mutableStateOf(false);var route=""
  val routes=listOf(Screen.Dashboard.route,Screen.Students.route,Screen.Teachers.route,Screen.PrincipalAttendance.route,Screen.Academics.route,Screen.Fees.route,Screen.Reports.route,Screen.Leaves.route,Screen.PrincipalApprovalDetail.route,Screen.Profile.route,Screen.OperationsHub.route)
  compose.setContent {val user by repo.currentUser.collectAsState();CompositionLocalProvider(LocalAppearance provides preference){ScholaTheme(dark,principal=true){Box(Modifier.fillMaxWidth().height(if(page==0)820.dp else 640.dp).testTag("reference_viewport")){ScholaResponsiveScaffold(routes[page],user,2,{route=it},{},{},{},{},onBack={}){
   key(page,dark){when(page){
    0->PrincipalDashboardScreen(repo,{route=it},{},{},{})
    1->PrincipalStudents(repo,{route=Screen.StudentDetail.createRoute(it)},{})
    2->PrincipalStaff(repo,{route=Screen.TeacherDetail.createRoute(it)},{})
    3->PrincipalAttendanceOverview(repo,{c,d->route=Screen.PrincipalClass.createRoute(c,d)},{route=Screen.Reports.route})
    4->PrincipalAcademics(repo){route=it}
    5->PrincipalFees(repo){route=it}
    6->PrincipalReports(repo)
    7->PrincipalApprovals(repo){route=it}
    8->PrincipalApprovalDetail(repo,request.id)
    9->PrincipalAccount(repo){route=it}
    else->PrincipalMore(repo,{route=it},{},{},{})
   }}
  }}}}}
  File("build/reports/principal-spec/batch-a").mkdirs()
  for(palette in listOf(false,true))for(index in routes.indices){
   compose.runOnIdle {page=index;dark=palette;preference.select(if(palette)Appearance.DARK else Appearance.LIGHT)};compose.waitForIdle()
   val name=listOf("home","students","staff","attendance","academics","fees","reports","approvals","approval_detail","account","more")[index]
   compose.onNodeWithTag("reference_viewport").captureRoboImage("build/reports/principal-spec/batch-a/${name}_${if(palette)"dark" else "light"}.png")
  }
  compose.runOnIdle {page=0;dark=false;preference.select(Appearance.LIGHT)}
  listOf("students" to Screen.Students.route,"staff" to Screen.Teachers.route,"attendance" to Screen.PrincipalAttendance.route,"academics" to Screen.Academics.route,"fees" to Screen.Fees.route,"reports" to Screen.Reports.route).forEach {(label,target)->
   compose.onNodeWithTag("principal_module_$label").performScrollTo().performClick();assertEquals(target,route)
  }
  compose.onNodeWithTag("principal_dashboard_list").performScrollToNode(hasText("Upcoming"))
  compose.onNodeWithTag("reference_viewport").captureRoboImage("build/reports/principal-spec/batch-a/home_lower_light.png")
 }
 private fun freshRepository():ErpDataRepository {
  com.example.data.datasource.AppEnvironment.setMode(DataMode.DEMO)
  val repo=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance()
  fixture=repo;repo.setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();repo.switchRole(UserRole.PRINCIPAL);dispatcher.scheduler.runCurrent()
  return repo
 }
 @Test @Config(sdk=[34],qualifiers="w320dp-h700dp-xhdpi")
 fun narrowLargeTextKeepsRegisterNavigationAndAppearanceReachable() {
  val repo=freshRepository();var page by mutableIntStateOf(0)
  compose.setContent {
   val user by repo.currentUser.collectAsState();val density=androidx.compose.ui.platform.LocalDensity.current
   CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,1.5f)) {
    ScholaTheme(false,principal=true){Box(Modifier.fillMaxSize().testTag("adaptive_viewport")) {
     ScholaResponsiveScaffold(if(page==2)Screen.Profile.route else Screen.Students.route,user,0,{},{},{},{},{},onBack={}) {
      key(page){when(page){0->PrincipalStudents(repo,{},{}) ;1->PrincipalAttendanceOverview(repo,{_,_->},{}) ;else->PrincipalAccount(repo){}}}
     }
    }}
   }
  }
  File("build/reports/principal-spec/adaptive").mkdirs()
  for(index in 0..2){compose.runOnIdle{page=index};compose.waitForIdle();compose.onNodeWithTag("adaptive_viewport").captureRoboImage("build/reports/principal-spec/adaptive/narrow_large_${index}.png")}
  compose.onNodeWithTag("principal_account").performScrollToNode(hasText("Appearance"));compose.onNodeWithText("System").assertIsDisplayed()
  compose.onNodeWithTag("principal_sign_out").performScrollTo().assertIsDisplayed()
  compose.onNodeWithTag("adaptive_viewport").captureRoboImage("build/reports/principal-spec/adaptive/narrow_account_lower.png")
 }
 @Test @Config(sdk=[34],qualifiers="w800dp-h1100dp-xhdpi")
 fun tabletUsesRailAndAdaptiveDirectory() {
  val repo=freshRepository();var students by mutableStateOf(false)
  compose.setContent {val user by repo.currentUser.collectAsState();ScholaTheme(false,principal=true){Box(Modifier.fillMaxSize().testTag("adaptive_viewport")) {
   ScholaResponsiveScaffold(if(students)Screen.Students.route else Screen.Dashboard.route,user,0,{},{},{},{},{},onBack={}) {
    if(students)PrincipalStudents(repo,{},{}) else PrincipalDashboardScreen(repo,{},{},{},{})
   }
  }}}
  File("build/reports/principal-spec/adaptive").mkdirs()
  compose.onNodeWithTag("adaptive_viewport").captureRoboImage("build/reports/principal-spec/adaptive/tablet_home.png")
  compose.runOnIdle{students=true};compose.waitForIdle()
  compose.onNodeWithTag("principal_students").assertExists()
  compose.onNodeWithTag("adaptive_viewport").captureRoboImage("build/reports/principal-spec/adaptive/tablet_students.png")
 }

}
