package com.example.ui.screens.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.ErpDataRepository
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun TeacherHome(repository:ErpDataRepository,navigate:(String)->Unit,create:()->Unit,apply:()->Unit) {
 val actor by repository.currentUser.collectAsState();val timetable by repository.timetable.collectAsState();val notices by repository.notices.collectAsState();val teacher=teacherAllocation(repository)
 val submissions by repository.assignmentSubmissions.collectAsState();val mode by repository.dataMode.collectAsState()
 val capabilities by repository.backendCapabilities.collectAsState()
 val submissionBackendReady=mode==com.example.data.datasource.DataMode.SUPABASE_DEMO&&capabilities.loaded&&capabilities.submissions
 val pendingReview=submissions.count{it.reviewStatus=="SUBMITTED"}
 val today=LocalDate.now(ZoneId.of("Asia/Kolkata"));val now=LocalTime.now(ZoneId.of("Asia/Kolkata"))
 val lessons=timetable.filter{it.teacherName.equals(actor.name,true)&&"${it.classGrade}-${it.division}" in teacher?.assignedClasses.orEmpty()&&it.dayOfWeek.equals(today.dayOfWeek.name,true)}.sortedBy{it.periodNumber}
 fun time(value:String)=listOf("h:mm a","hh:mm a","HH:mm").firstNotNullOfOrNull{pattern->runCatching{LocalTime.parse(value.uppercase(Locale.US),DateTimeFormatter.ofPattern(pattern,Locale.US))}.getOrNull()}
 val next=lessons.firstOrNull{time(it.endTime)?.isAfter(now)==true}
 var context by remember{mutableStateOf(false)}
 val year=teacher?.sessionId.orEmpty().let{Regex("(20\\d{2})[^0-9]+(20\\d{2}|\\d{2})").find(it)?.let{match->"${match.groupValues[1]}–${match.groupValues[2].takeLast(2)}"}?:"Current session"}
 if(context)AlertDialog(onDismissRequest={context=false},title={Text("Teaching context")},text={Text("${teacher?.subjects.orEmpty().joinToString()}\n${teacher?.assignedClasses.orEmpty().joinToString()}\n$year\nYour verified school allocation controls available records.")},confirmButton={TextButton(onClick={context=false}){Text("Close")}})
 val firstName=actor.name.split(' ').filter{it.isNotBlank()&&it.lowercase() !in listOf("prof.","dr.","mr.","mrs.","ms.")}.firstOrNull().orEmpty()
 LazyColumn(Modifier.fillMaxSize().testTag("teacher_dashboard_list"),contentPadding=PaddingValues(TeacherLayout.margin),verticalArrangement=Arrangement.spacedBy(TeacherLayout.section)) {
  item{Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Text("${if(now.hour<12)"Good morning" else if(now.hour<17)"Good afternoon" else "Good evening"}, $firstName",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){Text("Teacher",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);OutlinedButton(onClick={context=true}){Text(year);Icon(Icons.Outlined.ExpandMore,null)}}}}
  item{TeacherCard(color=TeacherColors.peach){Column(Modifier.padding(TeacherLayout.gap)){Text("Next class",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);TeacherRow(next?.subject?:"No upcoming class",next?.let{"${it.classGrade}-${it.division} · ${it.startTime}"}?:"No remaining scheduled lesson today",Icons.Outlined.Schedule,3){navigate(Screen.Timetable.route)}}}}
  item {TeacherHomeMetrics(teacher?.assignedClasses?.size?:0,notices.size,if(submissionBackendReady)submissions.size.toString()else "N/A")}
  item {
    TeacherTitle("My workspace")
    Spacer(Modifier.height(Spacing.sm8))
    TeacherModules(teacher?.assignedClasses?.firstOrNull().orEmpty(), navigate)
  }
  item{TeacherTitle("Needs attention");TeacherCard{TeacherRow(if(submissionBackendReady)"$pendingReview to review"else "School submission inbox",if(submissionBackendReady)"Confirmed submissions awaiting feedback"else if(mode==com.example.data.datasource.DataMode.SUPABASE_DEMO)"Submission setup or reconciliation required."else "Sign in to review shared school submissions.",Icons.Outlined.Description,3){navigate(Screen.Homework.route)}}}
  item{TeacherTitle("Today","View all"){navigate(Screen.Timetable.route)};TeacherCard{
   if(lessons.isEmpty())Text("No lessons scheduled today.",Modifier.padding(TeacherLayout.gap),style=MaterialTheme.typography.bodyMedium)
   lessons.forEachIndexed{i,slot->TeacherRow(slot.subject,"${slot.startTime} – ${slot.endTime} · Class ${slot.classGrade}-${slot.division}",Icons.Outlined.Schedule,2){navigate(Screen.TeacherClass.createRoute("${slot.classGrade}-${slot.division}"))};if(i<lessons.lastIndex)HorizontalDivider()}
  }}
  item{Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){OutlinedButton(onClick=create,Modifier.weight(1f)){Text("Add assignment")};OutlinedButton(onClick=apply,Modifier.weight(1f)){Text("Request leave")}}}
 }
}

@Composable private fun TeacherModules(cohort:String,navigate:(String)->Unit) {
 val modules=listOf(
   Triple("My classes",Icons.Outlined.Groups,0),
   Triple("Attendance",Icons.Outlined.EventAvailable,1),
   Triple("Assignments",Icons.Outlined.Description,3),
   Triple("Marks",Icons.Outlined.BarChart,2),
   Triple("Timetable",Icons.Outlined.CalendarMonth,0),
   Triple("Leave",Icons.Outlined.Eco,1)
 )
 Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  val columns=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<400&&androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f)2 else 3
  modules.chunked(columns).forEach { row ->
   Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
    row.forEach { (label,icon,tone) ->
     val route=when(label) {
      "My classes"->Screen.TeacherClasses.route
      "Attendance"->if(cohort.isBlank())Screen.Attendance.route else Screen.Attendance.createRoute(cohort.substringBeforeLast('-'),cohort.substringAfterLast('-'))
      "Assignments"->Screen.Homework.route
      "Marks"->Screen.ReportCard.route
      "Leave"->Screen.Leaves.route
      else->Screen.Timetable.route
     }
     Surface(onClick={navigate(route)},modifier=Modifier.weight(1f).heightIn(min=TeacherLayout.moduleHeight).testTag("teacher_module_${label.lowercase().replace(' ','_')}"),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
      Column(Modifier.padding(vertical=TeacherLayout.gap,horizontal=Spacing.xs4),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
       TeacherIcon(icon,tone)
       Text(label,style=MaterialTheme.typography.bodySmall)
      }
     }
    }
   }
  }
 }
}

@Composable private fun TeacherHomeMetrics(classCount:Int,noticeCount:Int,submissionCount:String) {
 val large=androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<400&&androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f
 val metrics=listOf(Triple(classCount.toString(),"Classes",Icons.Outlined.Groups),Triple(submissionCount,"Submissions",Icons.Outlined.Description),Triple(noticeCount.toString(),"Notices",Icons.Outlined.Campaign))
 val card:@Composable (Int,Modifier)->Unit={index,modifier->
  val metric=metrics[index]
  TeacherCard(modifier) {
   Column(Modifier.fillMaxWidth().heightIn(min=80.dp).padding(Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
     TeacherIcon(metric.third,if(index==0)0 else if(index==1)1 else 2,Spacing.xxl32)
     Text(metric.first,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    }
    Text(metric.second,style=MaterialTheme.typography.bodySmall)
   }
  }
 }
 if(large)Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){metrics.indices.forEach{card(it,Modifier.fillMaxWidth())}}
 else Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){metrics.indices.forEach{card(it,Modifier.weight(1f))}}
}
