package com.example

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.*
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.*
import com.example.ui.screens.teachers.*
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.communication.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
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
class TeacherImageSpecTest {
 @get:Rule val compose=createComposeRule()
 private val dispatcher=StandardTestDispatcher()
 private lateinit var repo:ErpDataRepository
 @Before fun setup(){Dispatchers.setMain(dispatcher);AppEnvironment.setMode(DataMode.DEMO);repo=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance();repo.setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();repo.switchRole(UserRole.TEACHER);dispatcher.scheduler.runCurrent()}
 @After fun cleanup(){(ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply{isAccessible=true}.get(repo)as CoroutineScope).cancel();dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}
 @Test fun teacherReferencePagesAndShortcuts() {
  var page by mutableIntStateOf(0);var dark by mutableStateOf(false);var destination=""
  val teacher=repo.teachers.value.first{it.email.equals(repo.currentUser.value.email,true)||it.name.equals(repo.currentUser.value.name,true)}
  val cohort=teacher.assignedClasses.first()
  val names=listOf("home","classes","class_detail","attendance","assignments","marks","timetable","leave","notifications","settings")
  val routes=listOf(Screen.Dashboard.route,Screen.TeacherClasses.route,Screen.TeacherClass.route,Screen.Attendance.route,Screen.Homework.route,Screen.ReportCard.route,Screen.Timetable.route,Screen.Leaves.route,Screen.Notifications.route,Screen.Profile.route)
  compose.setContent{val user by repo.currentUser.collectAsState();ScholaTheme(dark,teacher=true){Box(Modifier.fillMaxWidth().height(if(page==0)820.dp else 640.dp).testTag("teacher_viewport")){ScholaResponsiveScaffold(routes[page],user,2,{destination=it},{},{},{},{},onBack={}){
   key(page,dark){when(page){
    0->TeacherHome(repo,{destination=it},{},{})
    1->TeacherClasses(repo){destination=it}
    2->TeacherClassDetail(repo,cohort){destination=it}
    3->AttendanceScreen(repo,cohort.substringBeforeLast('-'),cohort.substringAfterLast('-'),{})
    4->TeacherAssignments(repo){}
    5->TeacherMarks(repo){}
    6->TeacherTimetable(repo){destination=it}
    7->TeacherLeave(repo){}
    8->NotificationsScreen(repo,{})
    else->ProfileScreen(repo,{},{})
   }}
  }}}}
  File("build/reports/teacher-spec").mkdirs()
  for(palette in listOf(false,true)){
   compose.runOnIdle{dark=palette;page=0};compose.waitForIdle()
   compose.onNodeWithTag("teacher_module_my_classes").performScrollTo().performClick();assertEquals(Screen.TeacherClasses.route,destination)
   compose.onNodeWithTag("teacher_module_marks").performScrollTo().performClick();assertEquals(Screen.ReportCard.route,destination)
   compose.onNodeWithTag("teacher_dashboard_list").performScrollToIndex(0)
   for(index in names.indices){compose.runOnIdle{page=index};compose.waitForIdle();compose.onNodeWithTag("teacher_viewport").captureRoboImage("build/reports/teacher-spec/${names[index]}_${if(palette)"dark"else "light"}.png")}
  }
 }
 @Test fun classesNeverShowUnallocatedStudentsAndOwnLeaveHasNoApprovals(){
  val teacher=repo.teachers.value.first{it.name.equals(repo.currentUser.value.name,true)||it.email.equals(repo.currentUser.value.email,true)}
  var detail by mutableStateOf(false)
  compose.setContent{ScholaTheme(false,teacher=true){if(detail)TeacherClassDetail(repo,"not-allocated"){}else TeacherLeave(repo){}}}
  compose.onNodeWithText("Approve").assertDoesNotExist();compose.onNodeWithText("New request").assertExists()
  compose.runOnIdle{detail=true};compose.onNodeWithText("Class unavailable").assertExists()
  assertFalse(teacher.assignedClasses.contains("not-allocated"))
 }
 @Test fun marksValidateRangeAndOnlyConfirmPersistedPrivateDraft(){
  val teacher=repo.teachers.value.first{it.email.equals(repo.currentUser.value.email,true)||it.name.equals(repo.currentUser.value.name,true)}
  val student=repo.students.value.filter{it.fullClass==teacher.assignedClasses.first()}.minBy{it.rollNumber}
  compose.setContent{ScholaTheme(false,teacher=true){TeacherMarks(repo){}}}
  compose.onNodeWithTag("marks_term").performScrollTo().performTextInput("Teacher UI verification")
  compose.onNodeWithTag("teacher_mark_${student.id}").performScrollTo().performTextInput("101")
  compose.onNodeWithTag("save_teacher_drafts").assertIsNotEnabled()
  compose.onNodeWithTag("teacher_mark_${student.id}").performTextClearance();compose.onNodeWithTag("teacher_mark_${student.id}").performTextInput("88")
  compose.onNodeWithTag("save_teacher_drafts").assertIsEnabled().performClick();dispatcher.scheduler.runCurrent();compose.waitForIdle()
  compose.onNodeWithText("1 draft row(s) confirmed.").assertExists()
  val report=repo.reportCards.value.first{it.studentId==student.id&&it.term=="Teacher UI verification"}
  assertFalse(report.published);assertEquals(88,report.scores.first{it.subjectName==teacher.subjects.first()}.obtainedMarks)
 }
 @Test fun individualTeacherEditorSavesDraftWithoutPublicationControls(){
  compose.setContent{ScholaTheme(false,teacher=true){com.example.ui.screens.academics.StaffMarksWorkspace(repo)}}
  compose.onNodeWithTag("marks_term").performScrollTo().performTextInput("Teacher private review")
  compose.onNodeWithTag("marks_subject").performScrollTo().performTextInput("Mathematics")
  compose.onNodeWithTag("marks_obtained").performScrollTo().performTextInput("72")
  compose.onNodeWithTag("save_draft_mark").performScrollTo().assertIsEnabled().performClick()
  dispatcher.scheduler.runCurrent();compose.waitForIdle()
  assertTrue(repo.reportCards.value.any{it.term=="Teacher private review"&&!it.published})
  compose.onNodeWithText("Review & publish").assertDoesNotExist()
  compose.onNodeWithText("Unpublish term").assertDoesNotExist()
 }
 @Test @Config(sdk=[34],qualifiers="w320dp-h740dp-xhdpi")
 fun narrowLargeTextTeacherLayouts(){
  var page by mutableIntStateOf(0)
  compose.setContent{val density=androidx.compose.ui.platform.LocalDensity.current;val user by repo.currentUser.collectAsState()
   CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,1.5f)){
    ScholaTheme(false,teacher=true){Box(Modifier.fillMaxSize().testTag("teacher_adaptive")){ScholaResponsiveScaffold(Screen.TeacherClasses.route,user,0,{},{},{},{},{},onBack={}){
     key(page){if(page==0)TeacherClasses(repo){}else TeacherMarks(repo){}}
    }}}
   }
  }
  File("build/reports/teacher-spec/adaptive").mkdirs()
  for(i in 0..1){compose.runOnIdle{page=i};compose.waitForIdle();compose.onNodeWithTag("teacher_adaptive").captureRoboImage("build/reports/teacher-spec/adaptive/narrow_large_$i.png")}
 }
 @Test @Config(sdk=[34],qualifiers="w800dp-h1000dp-xhdpi")
 fun tabletTeacherClassesUseAdaptiveShell(){
  compose.setContent{val user by repo.currentUser.collectAsState();ScholaTheme(false,teacher=true){Box(Modifier.fillMaxSize().testTag("teacher_tablet")){ScholaResponsiveScaffold(Screen.TeacherClasses.route,user,0,{},{},{},{},{},onBack={}){TeacherClasses(repo){}}}}}
  File("build/reports/teacher-spec/adaptive").mkdirs();compose.onNodeWithTag("teacher_tablet").captureRoboImage("build/reports/teacher-spec/adaptive/tablet_classes.png")
 }
}
