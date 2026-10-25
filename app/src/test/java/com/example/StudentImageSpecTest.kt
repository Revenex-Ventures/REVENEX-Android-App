package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.*
import com.example.data.datasource.*
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.*
import com.example.ui.screens.students.portal.*
import com.example.ui.theme.*
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
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
class StudentImageSpecTest {
 @get:Rule val compose=createComposeRule()
 private val dispatcher=StandardTestDispatcher()
 private lateinit var repo:ErpDataRepository
 @Before fun setup(){Dispatchers.setMain(dispatcher);AppEnvironment.setMode(DataMode.DEMO);repo=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance();repo.setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();repo.switchRole(UserRole.STUDENT);dispatcher.scheduler.runCurrent()}
 @After fun cleanup(){(ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply{isAccessible=true}.get(repo)as CoroutineScope).cancel();dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}
 @Test fun allReferencePagesLightAndDark(){
  var page by mutableIntStateOf(0);var dark by mutableStateOf(false);var destination=""
  val student=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId}
  val assignment=repo.assignments.value.first{it.classGrade==student.classGrade&&it.division==student.division}
  val invoice=repo.invoices.value.first{it.studentId==student.id}
  val notice=repo.notices.value.first()
  val names=listOf("home","assignments","assignment","attendance","results","timetable","fees","invoice","library","inbox","notice","profile","settings","learn","more","resources","search","examinations")
  val routes=listOf(Screen.Dashboard.route,Screen.Homework.route,Screen.StudentAssignment.route,Screen.Attendance.route,Screen.ReportCard.route,Screen.Timetable.route,Screen.Fees.route,Screen.StudentInvoice.route,Screen.Library.route,Screen.Notifications.route,Screen.StudentNotice.route,Screen.StudentIdentity.route,Screen.Profile.route,Screen.StudentLearn.route,Screen.OperationsHub.route,Screen.StudyMaterial.route,Screen.GlobalSearch.route,Screen.Exams.route)
  compose.setContent{val user by repo.currentUser.collectAsState();ScholaTheme(dark,student=true){Box(Modifier.fillMaxWidth().height(if(page==0)820.dp else 640.dp).testTag("student_viewport")){ScholaResponsiveScaffold(routes[page],user,2,{destination=it},{},{},{},{},onBack={}){
   key(page,dark){when(page){
    0->StudentHome(repo){destination=it}
    1->StudentAssignments(repo){destination=it}
    2->StudentAssignment(repo,assignment.id)
    3->StudentAttendance(repo)
    4->StudentResults(repo)
    5->StudentTimetable(repo)
    6->StudentFees(repo){destination=it}
    7->StudentInvoice(repo,invoice.id,{_,_,_->},{})
    8->StudentLibrary(repo)
    9->StudentInbox(repo){destination=it}
    10->StudentNotices(repo,{destination=it},notice.id)
    11->StudentIdentity(repo)
    12->StudentSettings(repo,{destination=it},{})
    13->StudentLearn{destination=it}
    14->StudentMore{destination=it}
    15->StudentResources(repo)
    16->StudentSearch(repo){destination=it}
    else->StudentExaminations(repo)
   }}
  }}}}
  File("build/reports/student-spec").mkdirs()
  for(palette in listOf(false,true)){
   compose.runOnIdle{dark=palette;page=0};compose.waitForIdle()
   compose.onNodeWithTag("student_module_assignments").performScrollTo().performClick();assertEquals(Screen.Homework.route,destination)
   compose.onNodeWithTag("student_dashboard_list").performScrollToIndex(0)
   for(index in names.indices){compose.runOnIdle{page=index};compose.waitForIdle();compose.onNodeWithTag("student_viewport").captureRoboImage("build/reports/student-spec/${names[index]}_${if(palette)"dark"else "light"}.png")}
  }
 }
 @Test fun privateResultsAndOtherStudentsInvoicesStayUnavailable(){
  val own=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId};val other=repo.invoices.value.first{it.studentId!=own.id}
  var page by mutableIntStateOf(0)
  compose.setContent{ScholaTheme(false,student=true){when(page){0->StudentResults(repo);1->StudentInvoice(repo,other.id,{_,_,_->},{});else->StudentAssignment(repo,"unavailable")}}}
  val expected=studentPublishedReports(own,repo.reportCards.value)
  if(expected.isEmpty())compose.onNodeWithText("No published results").assertExists()
  compose.onNodeWithText("Review & publish").assertDoesNotExist()
  compose.runOnIdle{page=1};compose.onNodeWithText("Invoice unavailable").assertExists()
  compose.runOnIdle{page=2};compose.onNodeWithText("Assignment unavailable").assertExists()
 }
 @Test fun savedCompletionIsConfirmedAndCanBeUndone(){
  val own=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId}
  val work=repo.assignments.value.first{it.classGrade==own.classGrade&&it.division==own.division&&!it.isCompletedByStudent}
  compose.setContent{ScholaTheme(false,student=true){StudentAssignment(repo,work.id)}}
  compose.onNodeWithText("Mark submitted").performScrollTo().performClick();dispatcher.scheduler.runCurrent();compose.waitForIdle()
  assertTrue(repo.assignments.value.first{it.id==work.id}.isCompletedByStudent)
  compose.onNodeWithText("Completion status confirmed.").assertExists()
  compose.onNodeWithText("Mark as not submitted").performScrollTo().performClick();dispatcher.scheduler.runCurrent();compose.waitForIdle()
  assertFalse(repo.assignments.value.first{it.id==work.id}.isCompletedByStudent)
 }
 @Test @Config(sdk=[34],qualifiers="w320dp-h740dp-xhdpi") fun narrowLargeText(){
  var page by mutableIntStateOf(0)
  compose.setContent{val density=androidx.compose.ui.platform.LocalDensity.current;val user by repo.currentUser.collectAsState();CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,1.5f)){ScholaTheme(false,student=true){Box(Modifier.fillMaxSize().testTag("student_adaptive")){ScholaResponsiveScaffold(if(page==0)Screen.Dashboard.route else Screen.Profile.route,user,0,{},{},{},{},{},onBack={}){if(page==0)StudentHome(repo){}else StudentSettings(repo,{},{})}}}}}
  File("build/reports/student-spec/adaptive").mkdirs()
  for(i in 0..1){compose.runOnIdle{page=i};compose.waitForIdle();compose.onNodeWithTag("student_adaptive").captureRoboImage("build/reports/student-spec/adaptive/narrow_large_$i.png")}
 }
 @Test @Config(sdk=[34],qualifiers="w800dp-h1000dp-xhdpi") fun tabletShell(){
  compose.setContent{val user by repo.currentUser.collectAsState();ScholaTheme(false,student=true){Box(Modifier.fillMaxSize().testTag("student_tablet")){ScholaResponsiveScaffold(Screen.StudentLearn.route,user,0,{},{},{},{},{},onBack={}){StudentLearn{}}}}}
  File("build/reports/student-spec/adaptive").mkdirs();compose.onNodeWithTag("student_tablet").captureRoboImage("build/reports/student-spec/adaptive/tablet_learn.png")
 }
 @Test fun publishedReportScopeAndIntegerMoney(){
  val student=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId};val report=repo.reportCards.value.first().copy(studentId=student.id,schoolId=student.schoolId,sessionId=student.sessionId,published=true)
  assertEquals(1,studentPublishedReports(student,listOf(report,report.copy(id="draft",published=false),report.copy(id="other",studentId="other"),report.copy(id="school",schoolId="other"),report.copy(id="session",sessionId="other"))).size)
  assertEquals("₹45.01",studentMoney(4501));assertTrue(studentPublishedReports(null,listOf(report)).isEmpty())
 }
 @Test fun changingPresentationSelectionCannotChangeOwnStudent(){
  val own=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId};val other=repo.students.value.first{it.id!=own.id}
  repo.setSelectedStudentId(other.id)
  compose.setContent{ScholaTheme(false,student=true){StudentIdentity(repo)}}
  compose.onNodeWithText(own.name).assertExists();compose.onNodeWithText(other.name).assertDoesNotExist()
 }
 @Test fun futureAndOtherSessionAttendanceExcluded(){
  val own=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId};val base=repo.classAttendance.value.values.first()
  val today=java.time.LocalDate.of(2026,10,25)
  val record=base.copy(schoolId=own.schoolId,sessionId=own.sessionId,classGrade=own.classGrade,division=own.division,date="2026-10-24",studentList=listOf(StudentAttendance(own.id,own.name,own.rollNumber,AttendanceStatus.PRESENT)))
  val visible=studentAttendanceEntries(own,listOf(record,record.copy(date="2026-10-26"),record.copy(date="2026-10-23",sessionId="other"),record.copy(date="2026-10-22",schoolId="other")),today)
  assertEquals(1,visible.size);assertEquals(java.time.LocalDate.of(2026,10,24),visible.first().date)
 }
 @Test fun studentSearchDoesNotExposeOtherLoadedPeople(){
  val own=repo.currentUser.value.associatedStudentId;val other=repo.students.value.first{it.id!=own}
  compose.setContent{ScholaTheme(false,student=true){StudentSearch(repo){}}}
  compose.onNodeWithText("Search your school work").performTextInput(other.name)
  compose.onAllNodes(hasText(other.name) and !hasSetTextAction()).assertCountEquals(0)
  compose.onNodeWithText("No matching records").assertExists()
 }
 @Test fun studentPresentationPreferencesDefaultOffAndPersist(){
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val preference=StudentExperience(context)
  assertFalse(preference.sounds);assertFalse(preference.haptics);assertFalse(preference.reduced)
  preference.sound(true);preference.haptic(true);preference.reduce(true)
  val restored=StudentExperience(context)
  assertTrue(restored.sounds);assertTrue(restored.haptics);assertTrue(restored.reduced)
 }
 @Test fun badgeAndInboxUseSameRoleAndSchoolPresentationScope(){
  val actor=repo.currentUser.value
  val notification=repo.notifications.value.first().copy(id="own",schoolId=actor.schoolId,targetRole=UserRole.STUDENT,isRead=false)
  val entries=studentInboxEntries(actor,listOf(notification,notification.copy(id="general",targetRole=null,isRead=true),notification.copy(id="other-school",schoolId="other"),notification.copy(id="parent",targetRole=UserRole.PARENT)))
  assertEquals(listOf("own","general"),entries.map{it.id});assertEquals(1,entries.count{!it.isRead})
  assertTrue(studentInboxEntries(actor.copy(role=UserRole.PARENT),listOf(notification)).isEmpty())
 }
 @Test fun restoredStudentTabOpensLandingInsteadOfItsPreviousNotice(){
  lateinit var controller:androidx.navigation.NavHostController
  compose.setContent{ScholaTheme(false,student=true){controller=rememberNavController();NavHost(controller,Screen.Dashboard.route){
   composable(Screen.Dashboard.route){androidx.compose.material3.Text("Home landing")}
   composable(Screen.OperationsHub.route){androidx.compose.material3.Text("More landing")}
   composable(Screen.Notices.route){androidx.compose.material3.Text("Notice list")}
   composable(Screen.StudentNotice.route){androidx.compose.material3.Text("Notice detail")}
   composable(Screen.Notifications.route){androidx.compose.material3.Text("Inbox landing")}
  }}}
  compose.runOnIdle{controller.navigatePortalPrimary(Screen.OperationsHub.route,UserRole.STUDENT)}
  compose.runOnIdle{controller.navigate(Screen.Notices.route);controller.navigate(Screen.StudentNotice.createRoute("notice"))}
  compose.onNodeWithText("Notice detail").assertExists()
  compose.runOnIdle{controller.navigatePortalPrimary(Screen.Notifications.route,UserRole.STUDENT)}
  compose.onNodeWithText("Inbox landing").assertExists()
  compose.runOnIdle{controller.navigatePortalPrimary(Screen.OperationsHub.route,UserRole.STUDENT)}
  compose.onNodeWithText("More landing").assertExists();compose.onNodeWithText("Notice detail").assertDoesNotExist()
  assertEquals(Screen.OperationsHub.route,controller.currentDestination?.route)
 }
}
