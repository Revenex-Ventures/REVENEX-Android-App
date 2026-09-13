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
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit
) {
  val students by repository.students.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()

  val currentStudent = remember(students, selectedStudentId) {
    students.firstOrNull { it.id == selectedStudentId.ifBlank { "stu_1001" } }
      ?: students.firstOrNull()
  } ?: return

  val todayDayOfWeek = remember {
    SimpleDateFormat("EEEE", Locale.US).format(Date())
  }

  val todayClasses = remember(timetable, currentStudent, todayDayOfWeek) {
    timetable.filter {
      it.classGrade == currentStudent.classGrade &&
      it.division == currentStudent.division &&
      it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true)
    }
  }

  val activeAssignments = remember(assignments, currentStudent.classGrade) {
    assignments.filter { it.classGrade == currentStudent.classGrade }
  }

  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("student_dashboard_list"),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // 1. HERO STUDENT IDENTITY & ATTENDANCE RING
    item {
      AppCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(Spacing.screenPadding),
        containerColor = RevenexPrimary,
        elevation = Elev.e2
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Student Portal",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.7f),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(Spacing.s1))
            Text(
              text = currentStudent.name,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Class ${currentStudent.fullClass} • Roll #${currentStudent.rollNumber}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f)
            )
          }

          StatusBadge(status = "ACTIVE")
        }

        Spacer(modifier = Modifier.height(Spacing.s5))

        // Center Attendance Vital Signs Ring
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          VitalRing(
            value = currentStudent.attendancePercent.toFloat(),
            label = "Academic Attendance",
            size = VitalRingSize.HERO,
            gradientStart = VitalRingBlue,
            gradientEnd = VitalRingSky,
            trackColor = Color.White.copy(alpha = 0.15f)
          )
        }

        Spacer(modifier = Modifier.height(Spacing.s5))

        // Key stats row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(horizontalAlignment = Alignment.Start) {
            Text(
              text = "CLASS RANK",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.6f)
            )
            Text(
              text = "Rank #${currentStudent.rank}",
              style = MaterialTheme.typography.titleMedium,
              color = RevenexMeritGold,
              fontWeight = FontWeight.Bold
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "CUMULATIVE GPA",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.6f)
            )
            Text(
              text = "${currentStudent.gpa} / 10.0",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. STUDENT QUICK ACTIONS
    item {
      SectionHeader(title = "Learning Modules")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = Spacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
      ) {
        QuickActionButton(
          title = "Homework",
          icon = Icons.Default.MenuBook,
          iconTint = RevenexPrimary,
          backgroundColor = RevenexPrimaryContainer,
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTo(Screen.Homework.route) }
        )
        QuickActionButton(
          title = "Timetable",
          icon = Icons.Default.CalendarMonth,
          iconTint = RoleAccent.Teacher,
          backgroundColor = RoleAccent.softContainer(UserRole.TEACHER),
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTo(Screen.Timetable.route) }
        )
        QuickActionButton(
          title = "Report Card",
          icon = Icons.Default.Assessment,
          iconTint = RevenexMeritGold,
          backgroundColor = RevenexMeritGoldContainer,
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTo(Screen.ReportCard.route) }
        )
        QuickActionButton(
          title = "Materials",
          icon = Icons.Default.Folder,
          iconTint = RoleAccent.Parent,
          backgroundColor = RoleAccent.softContainer(UserRole.PARENT),
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTo(Screen.StudyMaterial.route) }
        )
      }
    }

    // 3. TODAY'S CLASSES
    item {
      Spacer(modifier = Modifier.height(Spacing.s5))
      SectionHeader(
        title = "Today's Schedule ($todayDayOfWeek)",
        actionText = "Full Schedule",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )

      if (todayClasses.isEmpty()) {
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Text(
            text = "No classes scheduled for today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding),
          verticalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          todayClasses.forEach { slot ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .background(RevenexPrimaryContainer, RoundedCornerShape(Radius.sm)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "P${slot.periodNumber}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = RevenexPrimary
                    )
                  }
                  Spacer(modifier = Modifier.width(Spacing.s3))
                  Column {
                    Text(
                      text = slot.subject,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${slot.teacherName} • Room ${slot.roomNumber}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Text(
                  text = "${slot.startTime} - ${slot.endTime}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
    }

    // 4. PENDING ASSIGNMENTS
    item {
      Spacer(modifier = Modifier.height(Spacing.s5))
      SectionHeader(
        title = "Homework & Projects",
        actionText = "View All",
        onActionClick = { onNavigateTo(Screen.Homework.route) }
      )

      if (activeAssignments.isEmpty()) {
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Text(
            text = "No pending homework assignments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding),
          verticalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          activeAssignments.take(3).forEach { hw ->
            AppCard(
              modifier = Modifier.fillMaxWidth(),
              onClick = { onNavigateTo(Screen.Homework.route) }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = hw.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${hw.subject} • Due: ${hw.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                StatusBadge(status = "PENDING")
              }
            }
          }
        }
      }
    }
  }
}
