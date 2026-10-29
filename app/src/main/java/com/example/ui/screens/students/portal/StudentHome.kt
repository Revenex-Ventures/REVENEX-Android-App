package com.example.ui.screens.students.portal

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
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable fun StudentHome(repository:ErpDataRepository,navigate:(String)->Unit) {
 val dark=LocalStudentDark.current
 val student=ownStudent(repository)
 val work by repository.assignments.collectAsState();val slots by repository.timetable.collectAsState();val notices by repository.notices.collectAsState();val records by repository.classAttendance.collectAsState()
 if(student==null){EmptyStateView("Student link unavailable","Your school must link your signed-in account to your student record.");return}
 val today=studentToday();val ownWork=work.filter{it.classGrade==student.classGrade&&it.division==student.division&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId}
 val pending=ownWork.filter{!it.isCompletedByStudent}.sortedBy{studentDate(it.dueDate)}
 val next=slots.filter{it.classGrade==student.classGrade&&it.division==student.division&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.dayOfWeek.equals(today.dayOfWeek.name,true)}.sortedBy{it.periodNumber}.firstOrNull{scheduleStatus(true,currentMinutesOfDay(),it)!=ScheduleRowStatus.COMPLETED}
 val attendance=studentAttendanceEntries(student,records.values.toList())
 val rate=if(attendance.isEmpty())"N/A"else "${attendance.count{it.status in setOf(AttendanceStatus.PRESENT,AttendanceStatus.LATE,AttendanceStatus.EXCUSED)}*100/attendance.size}%"
 if(!dark){
  val notice=notices.firstOrNull{it.schoolId==student.schoolId}
  StudentLightHome(student,rate,pending.map{StudentLightTask(it.id,it.title,"${it.subject} · Due ${it.dueDate}",studentDate(it.dueDate))},next?.subject,next?.let{"${it.startTime} – ${it.endTime} · ${it.roomNumber}"},notice?.title,notice?.publishedDate,notice?.id,navigate)
  return
 }
 LazyColumn(Modifier.fillMaxSize().testTag("student_dashboard_list"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)) {
  if(dark)item{Text(today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM",java.util.Locale.US)).uppercase(),style=MaterialTheme.typography.labelSmall,color=StudentDarkPalette.muted)}
  item{Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Text(if(LocalStudentDark.current)"Make today count,\n${student.name.substringBefore(' ')}."else "Hi, ${student.name.substringBefore(' ')}",style=if(LocalStudentDark.current)MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Class ${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  item{StudentCard(tone=StudentTone.ORANGE,onClick={navigate(Screen.Timetable.route)}){Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
   Text("UP NEXT",style=MaterialTheme.typography.labelLarge,color=StudentColors.onOrange)
   if(androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f){
    Text(next?.subject?:"No upcoming lesson",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    Text(next?.let{"${it.startTime} – ${it.endTime} · ${it.roomNumber}"}?:"No remaining lesson scheduled today",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }else Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentIcon(StudentIcons.Book,StudentTone.ORANGE,48.dp);Column(Modifier.weight(1f)){Text(next?.subject?:"No upcoming lesson",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(next?.let{"${it.startTime} – ${it.endTime} · ${it.roomNumber}"}?:"No remaining lesson scheduled today",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};Icon(Icons.Outlined.ChevronRight,null)}
   if(LocalStudentDark.current)Surface(color=StudentDarkPalette.ink,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm)){Row(Modifier.fillMaxWidth().padding(Spacing.md12),horizontalArrangement=Arrangement.SpaceBetween){Text("View your timetable",color=StudentDarkPalette.text,style=MaterialTheme.typography.labelLarge);Icon(Icons.Outlined.NorthEast,null,tint=StudentDarkPalette.text)}}else Text("View schedule  →",style=MaterialTheme.typography.bodyMedium,color=StudentColors.onOrange)
  }}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
   StudentSummary(rate,"Attendance",StudentIcons.People,StudentTone.MINT,Modifier.weight(1f)){navigate(Screen.Attendance.route)}
   StudentSummary(pending.size.toString(),"Tasks due",StudentIcons.Document,StudentTone.BLUE,Modifier.weight(1f)){navigate(Screen.Homework.route)}
  }}
  item{StudentHeading("Your school");Spacer(Modifier.height(Spacing.sm8));StudentShortcuts(navigate)}
  item{StudentHeading("Due soon","View all"){navigate(Screen.Homework.route)};Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   if(pending.isEmpty())Text("No pending assignments.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   pending.take(2).forEachIndexed{i,assignment->StudentRow(assignment.title,"${assignment.subject} · ${assignment.dueDate}",StudentIcons.Document,if(i==0)StudentTone.BLUE else StudentTone.LAVENDER,{navigate(Screen.StudentAssignment.createRoute(assignment.id))},if(studentDate(assignment.dueDate)?.isBefore(today)==true)"Overdue"else "Due",statusTone=StudentTone.ORANGE)}
  }}
  item{
   val notice=notices.firstOrNull{it.schoolId==student.schoolId}
   if(notice==null)StudentCard{Text("No school notice available.",Modifier.padding(StudentLayout.gap),color=MaterialTheme.colorScheme.onSurfaceVariant)}
   else StudentRow(notice.title,notice.publishedDate,Icons.Outlined.Campaign,StudentTone.ORANGE,{navigate(Screen.StudentNotice.createRoute(notice.id))})
  }
 }
}
@Composable private fun StudentSummary(value:String,label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,tone:StudentTone,modifier:Modifier,onClick:()->Unit) {
 StudentCard(modifier,tone,onClick){
  if(LocalStudentDark.current)Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(value,style=MaterialTheme.typography.headlineLarge);Text(label,style=MaterialTheme.typography.bodySmall)}
  else if(androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f)Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){StudentIcon(icon,tone);Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  else Row(Modifier.padding(StudentLayout.gap).heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){StudentIcon(icon,tone);Column(Modifier.weight(1f)){Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
 }
}
@Composable internal fun StudentShortcuts(navigate:(String)->Unit) {
 val modules=listOf(
  Triple("Assignments",StudentIcons.Clipboard,StudentTone.ORANGE),Triple("Attendance",StudentIcons.Calendar,StudentTone.MINT),Triple("Results",StudentIcons.Results,StudentTone.LAVENDER),
  Triple("Timetable",StudentIcons.Clock,StudentTone.BLUE),Triple("Fees",StudentIcons.Wallet,StudentTone.ORANGE),Triple("Library",StudentIcons.Book,StudentTone.MINT))
 if(LocalStudentDark.current){
  val darkModules=listOf(
   Triple("Assignments",StudentIcons.Clipboard,Screen.Homework.route),Triple("Results",StudentIcons.Results,Screen.ReportCard.route),Triple("Library",StudentIcons.Book,Screen.Library.route),
   Triple("Timetable",StudentIcons.Clock,Screen.Timetable.route),Triple("Fees",StudentIcons.Wallet,Screen.Fees.route),Triple("Attendance",StudentIcons.Calendar,Screen.Attendance.route))
  Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){darkModules.chunked(3).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){row.forEach{(label,icon,route)->StudentCard(Modifier.weight(1f).testTag("student_module_${label.lowercase()}"),onClick={navigate(route)}){Column(Modifier.fillMaxWidth().padding(Spacing.sm8).heightIn(min=56.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Icon(icon,null,Modifier.size(22.dp),tint=StudentDarkPalette.muted);Text(label,style=MaterialTheme.typography.labelSmall)}}}}}};return
 }
 val columns=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<400&&androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f)2 else 3
 Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  modules.chunked(columns).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   row.forEach{(label,icon,tone)->
    val route=when(label){"Assignments"->Screen.Homework.route;"Attendance"->Screen.Attendance.route;"Results"->Screen.ReportCard.route;"Timetable"->Screen.Timetable.route;"Fees"->Screen.Fees.route;else->Screen.Library.route}
    StudentCard(Modifier.weight(1f).testTag(if(label=="Fees")"family_fee_accounts"else "student_module_${label.lowercase()}"),tone,{navigate(route)}){
     Column(Modifier.fillMaxWidth().heightIn(min=StudentLayout.moduleHeight).padding(StudentLayout.gap),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
      Icon(icon,null,Modifier.size(32.dp),tint=StudentColors.colors(tone).second);Text(label,style=MaterialTheme.typography.bodySmall,fontWeight=FontWeight.Medium)
     }
    }
   }
  }}
 }
}
