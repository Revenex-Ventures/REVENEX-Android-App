package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.screens.attendance.attendanceDateMatches
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherDashboardScreen(repository: ErpDataRepository, onNavigateTo: (String) -> Unit,
  onShowCreateAssignment: () -> Unit, onShowApplyLeave: () -> Unit) {
  val user by repository.currentUser.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val students by repository.students.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val records by repository.classAttendance.collectAsState()
  val teacher = teachers.firstOrNull { it.email.equals(user.email,true) || it.name.equals(user.name,true) }
  val cohort = teacher?.assignedClasses?.firstOrNull() ?: user.associatedClass
  val pupils = students.filter { "${it.classGrade}-${it.division}" == cohort }
  val day = LocalDate.now(ZoneId.of("Asia/Kolkata"))
  val slots = timetable.filter { it.teacherName.equals(user.name,true) && it.dayOfWeek.equals(day.dayOfWeek.name,true) }.sortedBy { it.periodNumber }
  val nextListedDay=if(slots.isEmpty())(1..7).map{day.plusDays(it)}.firstOrNull{candidate->
    timetable.any{it.teacherName.equals(user.name,true)&&it.dayOfWeek.equals(candidate.dayOfWeek.name,true)}
  } else null
  val shownSlots=if(slots.isNotEmpty())slots else timetable.filter{it.teacherName.equals(user.name,true)&&it.dayOfWeek.equals(nextListedDay?.dayOfWeek?.name,true)}.sortedBy{it.periodNumber}
  val register = records.values.lastOrNull { "${it.classGrade}-${it.division}" == cohort && attendanceDateMatches(it.date,day.toString()) }
  LazyColumn(Modifier.fillMaxSize().testTag("teacher_dashboard_list"), contentPadding = PaddingValues(Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xl24)) {
    item { SchoolGroup {
      Row(Modifier.padding(Spacing.md12), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
        Avatar(user.name, size = 40.dp)
        Column(Modifier.weight(1f)) {
          Text("Hello, ${user.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text(teacher?.designation ?: user.designation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = { onNavigateTo(Screen.TeacherProfile.route) }) { Text(cohort.ifBlank { "Profile" }) }
      }
    } }
    item { SchoolSection(if(slots.isEmpty()&&shownSlots.isNotEmpty())"Next listed timetable" else "Today's schedule", "View all", { onNavigateTo(Screen.Timetable.route) }) {
      if(slots.isEmpty())Text(if(shownSlots.isEmpty())"No lessons scheduled for today." else "No lessons scheduled today. Weekly timetable: ${nextListedDay?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL,Locale.getDefault())}.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      shownSlots.take(3).forEach { slot ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
          Column(Modifier.width(72.dp)) {
            Text(slot.startTime, style = MaterialTheme.typography.labelMedium)
            Text(slot.endTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Circle, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
            Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
          }
          Column(Modifier.weight(1f)) {
            Text(slot.subject, style = MaterialTheme.typography.titleSmall)
            Text("Class ${slot.classGrade}-${slot.division} · ${slot.roomNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
      FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        OutlinedButton(onClick = { onNavigateTo(Screen.Attendance.route) }) { Icon(Icons.Default.CalendarMonth,null,Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.xs4)); Text("Mark attendance") }
        OutlinedButton(onClick = onShowCreateAssignment) { Icon(Icons.Default.NoteAdd,null,Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.xs4)); Text("Add assignment") }
      }
    } }
    item { SchoolSection(if (cohort.isBlank()) "Class overview" else "Class $cohort overview", "View class", { onNavigateTo(Screen.Students.route) }) {
      Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SchoolCompactMetric(register?.presentCount?.toString() ?: "—", "Present", Icons.Default.CheckCircle, Modifier.weight(1f), SchoolMetricTone.POSITIVE) { onNavigateTo(Screen.Attendance.route) }
        SchoolCompactMetric(register?.absentCount?.toString() ?: "—", "Absent", Icons.Default.PersonOff, Modifier.weight(1f), SchoolMetricTone.DANGER) { onNavigateTo(Screen.Attendance.route) }
        SchoolCompactMetric(pupils.size.toString(), "Students", Icons.Default.Groups, Modifier.weight(1f)) { onNavigateTo(Screen.Students.route) }
      }
      if (register == null) Text("Today's register has not been saved.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
    item { SchoolSection("Assignment activity", "View all", { onNavigateTo(Screen.Homework.route) }) {
      SchoolGroup {
        val work = assignments.filter { it.teacherName.equals(user.name,true) }
        if (work.isEmpty()) Text("No assignment activity available.", Modifier.padding(Spacing.md12), style = MaterialTheme.typography.bodySmall)
        work.take(3).forEach { SchoolRow(it.title, "${it.subject} · Due ${it.dueDate}", Icons.Default.Assignment, { onNavigateTo(Screen.Homework.route) }, it.submissionStatus) }
      }
    } }
    item { SchoolSection("Teaching tools") { SchoolGroup {
      SchoolRow("Learning resources", "Notes and documents", Icons.Default.Folder, { onNavigateTo(Screen.StudyMaterial.route) })
      SchoolRow("Results & marks", "Drafts, review and publication", Icons.Default.Grade, { onNavigateTo(Screen.ReportCard.route) })
      SchoolRow("Apply for leave", "Request time away", Icons.Default.EventBusy, onShowApplyLeave)
    } } }
  }
}
