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
fun TeacherDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowCreateAssignment: () -> Unit,
  onShowApplyLeave: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()

  val matchedTeacher = remember(teachers, currentUser) {
    teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) || it.name.equals(currentUser.name, ignoreCase = true) }
  }

  val mentorClass = matchedTeacher?.assignedClasses?.firstOrNull() ?: "10-A"
  val mentorClassGrade = mentorClass.substringBefore("-")
  val mentorClassDiv = mentorClass.substringAfter("-")

  val classStudents = remember(students, mentorClassGrade, mentorClassDiv) {
    students.filter { it.classGrade == mentorClassGrade && it.division == mentorClassDiv }
  }

  val todayDayOfWeek = remember {
    val sdf = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
    sdf.format(java.util.Date())
  }

  val todaySlots = remember(timetable, currentUser, todayDayOfWeek) {
    timetable.filter {
      it.teacherName.equals(currentUser.name, ignoreCase = true) &&
      it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true)
    }
  }

  val listState = rememberLazyListState()
  LaunchedEffect(key1 = Unit) {
    listState.scrollToItem(0)
  }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("teacher_dashboard_list"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Teacher Profile & Class Teacher Banner
    item {
      AnimatedFadeIn(delayMillis = 0) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentUser.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                val designation = matchedTeacher?.designation ?: currentUser.designation
                val department = matchedTeacher?.department ?: "Academic Department"
                Text(
                  text = "$designation • $department • Class $mentorClass Mentor",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF7C3AED)
              ) {
                Text(
                  text = "FACULTY",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("MY CLASS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("$mentorClass (${classStudents.size} Std)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("WEEKLY PERIODS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${matchedTeacher?.weeklyPeriods ?: 22} Periods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("ATTENDANCE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${matchedTeacher?.attendancePercent ?: 98.4}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusSuccessContainer)
              }
            }
          }
        }
      }
    }

    // Quick Actions
    item {
      AnimatedFadeIn(delayMillis = 100) {
        Column {
          SectionHeader(title = "Faculty Quick Actions")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              QuickActionButton(
                title = "Mark Attendance",
                icon = Icons.Default.FactCheck,
                iconTint = Color(0xFF15803D),
                backgroundColor = Color(0xFFDCFCE7),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.Attendance.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Add Homework",
                icon = Icons.Default.Assignment,
                iconTint = RevenexBlue,
                backgroundColor = RevenexPrimaryContainer,
                modifier = Modifier.width(115.dp),
                onClick = onShowCreateAssignment
              )
            }
            item {
              QuickActionButton(
                title = "Upload Notes",
                icon = Icons.Default.CloudUpload,
                iconTint = Color(0xFF6D28D9),
                backgroundColor = Color(0xFFEDE9FE),
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.StudyMaterial.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Staff Leave",
                icon = Icons.Default.EventBusy,
                iconTint = Color(0xFFB45309),
                backgroundColor = Color(0xFFFEF3C7),
                modifier = Modifier.width(115.dp),
                onClick = onShowApplyLeave
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }

    // Today's Teaching Schedule
    item {
      SectionHeader(
        title = "Today's Teaching Schedule ($todayDayOfWeek)",
        actionText = "Full Week",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        todaySlots.forEach { slot ->
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
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = RevenexPrimaryContainer,
                modifier = Modifier.size(48.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = "P${slot.periodNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevenexBlue
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = slot.subject,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${slot.startTime} - ${slot.endTime} • Class ${slot.classGrade}-${slot.division} (${slot.roomNumber})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StatusSuccessContainer
              ) {
                Text(
                  text = "Room ${slot.roomNumber.takeLast(3)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessText,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Class 10-A Student Overview & Attendance Quick Check
    item {
      SectionHeader(
        title = "Class 10-A Student Strength (${classStudents.size})",
        actionText = "Student Directory",
        onActionClick = { onNavigateTo(Screen.Students.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        classStudents.take(4).forEach { st ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateTo(Screen.StudentDetail.createRoute(st.id)) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = Color(st.avatarColorHex),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = st.rollNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = st.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Adm: ${st.admissionNumber} • Roll: #${st.rollNumber} • Rank #${st.rank}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StatusSuccessContainer
              ) {
                Text(
                  text = "${st.attendancePercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessText,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
