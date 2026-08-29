package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.AttendanceStatus
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit
) {
  val students by repository.students.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val studyMaterials by repository.studyMaterials.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val reportCards by repository.reportCards.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()

  // Use selected student ID (set at login), fall back to first student
  val currentStudent = remember(students, selectedStudentId) {
    students.firstOrNull { it.id == selectedStudentId.ifBlank { "stu_1001" } }
      ?: students.firstOrNull()
  } ?: return

  // Derive initials from student name
  val initials = remember(currentStudent.name) {
    currentStudent.name.split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.first().uppercase() }
      .ifBlank { "ST" }
  }

  val studentReport = reportCards.firstOrNull { it.studentId == currentStudent.id }
  val studentAssignments = assignments.filter { it.classGrade == currentStudent.classGrade }
  val todayClasses = timetable.filter { it.dayOfWeek.equals("Monday", ignoreCase = true) }

  val todayAttendance = remember(classAttendanceMap, currentStudent.id) {
    repository.getStudentTodayAttendance(currentStudent.id)
  }
  val isPresent = todayAttendance == AttendanceStatus.PRESENT || todayAttendance == AttendanceStatus.LATE

  val listState = rememberLazyListState()
  LaunchedEffect(key1 = Unit) {
    listState.scrollToItem(0)
  }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("student_dashboard_list"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Student Identity Card
    item {
      AnimatedFadeIn(delayMillis = 0) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = RevenexPrimary)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(54.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentStudent.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "Class ${currentStudent.fullClass} • Roll #${currentStudent.rollNumber} • Adm #${currentStudent.admissionNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f)
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = RevenexGold
              ) {
                Text(
                  text = "Rank #${currentStudent.rank}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance & GPA highlights
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("ATTENDANCE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${currentStudent.attendancePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("CUMULATIVE GPA", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${currentStudent.gpa} / 10.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("TODAY'S STATUS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(todayAttendance.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isPresent) StatusSuccessContainer else StatusErrorContainer)
              }
            }
          }
        }
      }
    }

    // Quick Portal Navigation
    item {
      AnimatedFadeIn(delayMillis = 100) {
        Column {
          SectionHeader(title = "Student Learning Desk")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              QuickActionButton(
                title = "Class Timetable",
                icon = Icons.Default.CalendarMonth,
                iconTint = RevenexBlue,
                backgroundColor = RevenexPrimaryContainer,
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.Timetable.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Homework & HW",
                icon = Icons.Default.Assignment,
                iconTint = Color(0xFF15803D),
                backgroundColor = Color(0xFFDCFCE7),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.Homework.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Term Marks",
                icon = Icons.Default.Grade,
                iconTint = Color(0xFF6D28D9),
                backgroundColor = Color(0xFFEDE9FE),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.ReportCard.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Study Notes",
                icon = Icons.Default.MenuBook,
                iconTint = Color(0xFFB45309),
                backgroundColor = Color(0xFFFEF3C7),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.StudyMaterial.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Digital Library",
                icon = Icons.Default.LocalLibrary,
                iconTint = Color(0xFF0284C7),
                backgroundColor = Color(0xFFE0F2FE),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.Library.route) }
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }

    // Attendance Performance Ring
    item {
      SectionHeader(title = "Overall Attendance Health")
      SimpleProgressRing(
        percentage = currentStudent.attendancePercent,
        title = "Eligible for Term Board Examinations",
        subtitle = "Current attendance is ${currentStudent.attendancePercent}% (CBSE requirement is min 75%). Registered in Class ${currentStudent.fullClass}.",
        modifier = Modifier.padding(horizontal = 16.dp),
        ringColor = if (currentStudent.attendancePercent >= 75.0) StatusSuccess else StatusError
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Active Homework Tasks with live Toggle
    item {
      SectionHeader(
        title = "Pending Homework & Tasks",
        actionText = "All Tasks (${studentAssignments.size})",
        onActionClick = { onNavigateTo(Screen.Homework.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        studentAssignments.take(3).forEach { hw ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = hw.isCompletedByStudent,
                onCheckedChange = { repository.toggleAssignmentCompletion(hw.id) }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = hw.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${hw.subject} • Due ${hw.dueDate} • ${hw.teacherName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              StatusBadge(status = hw.submissionStatus)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Today's Class Timetable
    item {
      SectionHeader(
        title = "Today's Periods (Monday)",
        actionText = "Full Schedule",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        todayClasses.take(3).forEach { slot ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${slot.startTime}\n${slot.endTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = slot.subject,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${slot.teacherName} • ${slot.roomNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // My Family / Parent & Guardian Section
    item {
      SectionHeader(title = "My Family / Parent & Guardian")
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.FamilyRestroom,
              contentDescription = null,
              tint = Color(0xFFB45309),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Parent & Guardian Information",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Father / Guardian Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(currentStudent.parentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Contact Phone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(currentStudent.parentPhone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Guardian Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(currentStudent.parentEmail, style = MaterialTheme.typography.bodyMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Emergency Contact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(currentStudent.parentPhone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text("Residential Address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(currentStudent.address, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
}
