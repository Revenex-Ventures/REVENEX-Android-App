package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.data.datasource.*
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.students.portal.*
import com.example.ui.theme.*
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File
import java.time.*

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h820dp-xhdpi")
@OptIn(ExperimentalCoroutinesApi::class)
class StudentTimelineRenderTest {
 @get:Rule val compose=createComposeRule()
 private val dispatcher=StandardTestDispatcher()
 private lateinit var repo:ErpDataRepository
 @Before fun setup(){Dispatchers.setMain(dispatcher);AppEnvironment.setMode(DataMode.DEMO);repo=ErpDataRepository::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance();dispatcher.scheduler.runCurrent();repo.switchRole(UserRole.STUDENT);dispatcher.scheduler.runCurrent()}
 @After fun close(){(ErpDataRepository::class.java.getDeclaredField("repositoryScope").apply{isAccessible=true}.get(repo)as CoroutineScope).cancel();dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}
 @Test fun railRendersCurrentEndedUpcomingAndGapWithoutAttendanceWrites(){
  val student=repo.students.value.first{it.id==repo.currentUser.value.associatedStudentId}
  val original=repo.timetable.value.filter{it.classGrade==student.classGrade&&it.division==student.division}.take(3)
  val ranges=listOf("08:30" to "09:15","09:15" to "10:00","10:15" to "11:00")
  val fixture=original.mapIndexed{i,slot->slot.copy(dayOfWeek="Monday",periodNumber=i+1,startTime=ranges[i].first,endTime=ranges[i].second)}
  @Suppress("UNCHECKED_CAST") val state=ErpDataRepository::class.java.getDeclaredField("_timetable").apply{isAccessible=true}.get(repo)as MutableStateFlow<List<TimetableSlot>>
  state.value=fixture
  val attendance=repo.classAttendance.value.toMap()
  var clock by mutableStateOf<Clock>(Clock.fixed(Instant.parse("2026-10-26T04:07:30Z"),ZoneOffset.UTC))
  var dark by mutableStateOf(false)
  compose.setContent{val actor by repo.currentUser.collectAsState();ScholaTheme(dark,student=true){Box(Modifier.fillMaxWidth().height(820.dp).testTag("timeline_viewport")){ScholaResponsiveScaffold(Screen.Timetable.route,actor,0,{},{},{},{},{},onBack={}){StudentTimetable(repo,clock)}}}}
  File("build/reports/student-timeline").mkdirs()
  compose.onNodeWithText("India time · 09:37:30 IST").assertExists()
  compose.onNodeWithTag("student_period_${fixture[1].id}").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Now"))
  for(palette in listOf(false,true)){compose.runOnIdle{dark=palette};compose.waitForIdle();compose.onNodeWithTag("timeline_viewport").captureRoboImage("build/reports/student-timeline/active_${if(palette)"dark"else "light"}.png")}
  compose.runOnIdle{dark=false;clock=Clock.fixed(Instant.parse("2026-10-26T04:37:30Z"),ZoneOffset.UTC)}
  dispatcher.scheduler.runCurrent()
  compose.waitForIdle();compose.onNodeWithText("India time · 10:07:30 IST").assertExists()
  compose.onNodeWithTag("student_gap_${fixture[1].id}").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Now"))
  compose.onNodeWithTag("timeline_viewport").captureRoboImage("build/reports/student-timeline/active_gap_light.png")
  assertEquals(attendance,repo.classAttendance.value)
 }
}
