package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
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
class StudentDarkSpecTest {
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
  val names=listOf("home","assignments","assignment","attendance","results","timetable","fees","invoice","library","inbox","notice","profile","settings","learn","more","resources","search","examinations","assistant","transport")
  val routes=listOf(Screen.Dashboard.route,Screen.Homework.route,Screen.StudentAssignment.route,Screen.Attendance.route,Screen.ReportCard.route,Screen.Timetable.route,Screen.Fees.route,Screen.StudentInvoice.route,Screen.Library.route,Screen.Notifications.route,Screen.StudentNotice.route,Screen.StudentIdentity.route,Screen.Profile.route,Screen.StudentLearn.route,Screen.OperationsHub.route,Screen.StudyMaterial.route,Screen.GlobalSearch.route,Screen.Exams.route,Screen.AiAssistant.route,Screen.Transport.route)
  compose.setContent{val user by repo.currentUser.collectAsState();val appearance=rememberAppearancePreference();SideEffect{appearance.select(if(dark)Appearance.DARK else Appearance.LIGHT)};CompositionLocalProvider(LocalAppearance provides appearance){ScholaTheme(dark,student=true){Box(Modifier.fillMaxWidth().height(if(page==0||dark)820.dp else 640.dp).testTag("student_viewport")){ScholaResponsiveScaffold(routes[page],user,2,{destination=it},{},{},{},{},onBack={}){
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
    18->com.example.ui.screens.ai.AiAssistantScreen(repo){}
    19->com.example.ui.screens.operations.TransportScreen(repo)
    else->StudentExaminations(repo)
   }}
  }}}}}
  File("build/reports/student-dark").mkdirs()
  for(palette in listOf(false,true)){
   compose.runOnIdle{dark=palette;page=0};compose.waitForIdle()
   compose.onNodeWithTag("student_module_assignments").performScrollTo().performClick();assertEquals(Screen.Homework.route,destination)
   compose.onNodeWithTag("student_dashboard_list").performScrollToIndex(0)
   for(index in names.indices){compose.runOnIdle{page=index};compose.waitForIdle();if(palette&&index==12){compose.onNodeWithText("Dark").performClick();compose.waitForIdle();compose.onNodeWithText("Dark").assertIsSelected()};compose.onNodeWithTag("student_viewport").captureRoboImage("build/reports/student-dark/${names[index]}_${if(palette)"dark"else "light"}.png")}
  }
 }

 @Test fun connectedBookResourceAndCorrectionViews(){
  var page by mutableIntStateOf(0)
  val actor=repo.currentUser.value
  val student=repo.students.value.first{it.id==actor.associatedStudentId}
  val book=repo.libraryBooks.value.first{it.schoolId==actor.schoolId}
  val material=repo.studyMaterials.value.first{it.schoolId==student.schoolId&&it.classGrade==student.classGrade}
  compose.setContent{ScholaTheme(true,student=true){Box(Modifier.fillMaxSize().testTag("student_nested")){ScholaResponsiveScaffold(if(page==0)Screen.Library.route else if(page==1)Screen.StudyMaterial.route else Screen.StudentIdentity.route,actor,0,{},{},{},{},{},onBack={}){when(page){0->StudentLibrary(repo);1->StudentResources(repo);else->StudentIdentity(repo)}}}}}
  File("build/reports/student-dark/nested").mkdirs()
  val cover=book.title.substringBefore(':').split(' ').take(2).joinToString(" ").uppercase()
  compose.onAllNodes(hasText(cover) and hasClickAction()).onFirst().performClick()
  compose.onNodeWithText("BOOK DETAILS").assertExists()
  compose.onNodeWithTag("student_nested").captureRoboImage("build/reports/student-dark/nested/book_detail.png")
  compose.onNodeWithContentDescription("Back").performClick()
  compose.onNodeWithText("Your reading shelf").performClick()
  compose.onNodeWithText("Borrowing details unavailable").assertExists()
  compose.onNodeWithTag("student_nested").captureRoboImage("build/reports/student-dark/nested/loans_unavailable.png")
  compose.runOnIdle{page=1}
  compose.onNodeWithText(material.title).performClick()
  compose.onNodeWithText("RESOURCE DETAIL").assertExists()
  compose.onNodeWithTag("student_nested").captureRoboImage("build/reports/student-dark/nested/resource_detail.png")
  compose.onNodeWithContentDescription("Back").performClick()
  compose.onNodeWithText("RESOURCES").assertExists()
  compose.runOnIdle{page=2}
  compose.onNodeWithText("Request correction").performScrollTo().performClick()
  compose.onNodeWithText("Request a correction").assertExists()
  compose.onRoot().captureRoboImage("build/reports/student-dark/nested/correction_unavailable.png")
 }
 @Test fun exactPaletteIsStudentDarkOnlyAndKeepsContrast(){
  var student by mutableStateOf(true);var teacher by mutableStateOf(false);var dark by mutableStateOf(true)
  var background=androidx.compose.ui.graphics.Color.Unspecified
  var studentDark=false
  var pairs=emptyList<Pair<androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color>>()
  compose.setContent{ScholaTheme(dark,teacher=teacher,student=student){
   studentDark=LocalStudentDark.current;background=androidx.compose.material3.MaterialTheme.colorScheme.background
   pairs=listOf(StudentDarkPalette.canvas to StudentDarkPalette.text,StudentDarkPalette.surface to StudentDarkPalette.muted,StudentDarkPalette.orange to StudentDarkPalette.ink,StudentDarkPalette.blue to StudentDarkPalette.ink,StudentDarkPalette.mint to StudentDarkPalette.ink)
  }}
  compose.runOnIdle{
   assertTrue(studentDark);assertEquals(androidx.compose.ui.graphics.Color(0xFF0B1220),background)
   pairs.forEach{(bg,fg)->
    val a=bg.luminance();val b=fg.luminance()
    assertTrue("Student dark text contrast",(maxOf(a,b)+.05f)/(minOf(a,b)+.05f)>=4.5f)
   }
   dark=false
  }
  compose.runOnIdle{assertFalse(studentDark);assertEquals(StudentLightPalette.canvas,background);dark=true;student=false;teacher=true}
  compose.runOnIdle{assertFalse(studentDark);assertEquals(androidx.compose.ui.graphics.Color(0xFF151B24),background)}
 }
 @Test @Config(sdk=[34],qualifiers="w320dp-h740dp-xhdpi") fun darkNarrowLargeText(){
  var page by mutableIntStateOf(0)
  compose.setContent{val density=androidx.compose.ui.platform.LocalDensity.current;val user by repo.currentUser.collectAsState();CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,1.5f)){ScholaTheme(true,student=true){Box(Modifier.fillMaxSize().testTag("student_adaptive")){ScholaResponsiveScaffold(if(page==0)Screen.Dashboard.route else Screen.Profile.route,user,0,{},{},{},{},{},onBack={}){if(page==0)StudentHome(repo){}else StudentSettings(repo,{},{})}}}}}
  File("build/reports/student-dark/adaptive").mkdirs()
  for(i in 0..1){compose.runOnIdle{page=i};compose.waitForIdle();if(i==1){compose.onNodeWithText("Dark").performClick();compose.waitForIdle()};compose.onNodeWithTag("student_adaptive").captureRoboImage("build/reports/student-dark/adaptive/narrow_large_$i.png")}
 }
 @Test @Config(sdk=[34],qualifiers="w800dp-h1000dp-xhdpi") fun darkTablet(){
  var destination=""
  compose.setContent{val user by repo.currentUser.collectAsState();ScholaTheme(true,student=true){Box(Modifier.fillMaxSize().testTag("student_tablet")){ScholaResponsiveScaffold(Screen.StudentLearn.route,user,0,{destination=it},{},{},{},{},onBack={}){StudentLearn{}}}}}
  File("build/reports/student-dark/adaptive").mkdirs();compose.onNodeWithTag("student_tablet").captureRoboImage("build/reports/student-dark/adaptive/tablet_learn.png")
  compose.onNodeWithText("You").performClick();compose.runOnIdle{assertEquals(Screen.Profile.route,destination)}
 }
}
