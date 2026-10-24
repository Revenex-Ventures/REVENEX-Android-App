package com.example.ui.screens.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDetailScreen(teacherId: String, repository: ErpDataRepository, onBack: () -> Unit) {
  val teachers by repository.teachers.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val teacher=teachers.firstOrNull {it.id==teacherId}
  if(teacher==null){Column(Modifier.padding(Spacing.screenPadding)){TextButton(onClick=onBack){Text("Back")};EmptyStateView(title="Faculty unavailable",message="This record is not loaded or your account cannot access it.")};return}
  val slots=timetable.filter {it.teacherName.contains(teacher.name.split(" ").lastOrNull()?:"")}
  Scaffold(topBar={TopAppBar(title={Text("Faculty profile",style=MaterialTheme.typography.titleMedium)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}})}) {padding ->
    LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
      item {SchoolGroup {Row(Modifier.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
        Avatar(teacher.name,size=40.dp,background=SemanticColors.lavenderContainer,textColor=SemanticColors.onLavender)
        Column(Modifier.weight(1f)){Text(teacher.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("${teacher.department} · ${teacher.designation}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(teacher.employeeId,style=MaterialTheme.typography.labelSmall)}
      }}}
      item {SchoolSection("Contact & qualifications") {SchoolGroup {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        Text(teacher.email,style=MaterialTheme.typography.bodyMedium);Text(teacher.phone,style=MaterialTheme.typography.bodyMedium)
        Text(teacher.qualification,style=MaterialTheme.typography.bodyMedium)
        Text("${teacher.experienceYears} years experience · Joined ${teacher.joiningDate}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Classes: ${teacher.assignedClasses.joinToString(", ")}",style=MaterialTheme.typography.bodySmall)
        Text("Subjects: ${teacher.subjects.joinToString(", ")}",style=MaterialTheme.typography.bodySmall)
      }}}}
      item {SchoolSection("Weekly schedule") {if(slots.isEmpty()) Text("No timetable slots assigned.",style=MaterialTheme.typography.bodySmall)}}
      slots.groupBy {it.dayOfWeek}.forEach {(day,lessons) ->
        item {SchoolSection(day){SchoolGroup {lessons.sortedBy {it.periodNumber}.forEach {slot ->
          Row(Modifier.padding(Spacing.md12),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
            Column(Modifier.width(72.dp)){Text(slot.startTime,style=MaterialTheme.typography.labelLarge);Text(slot.endTime,style=MaterialTheme.typography.bodySmall)}
            Column(Modifier.weight(1f)){Text(slot.subject,style=MaterialTheme.typography.titleSmall);Text("Class ${slot.classGrade}-${slot.division} · ${slot.roomNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
          }
          HorizontalDivider()
        }}}}
      }
    }
  }
}
