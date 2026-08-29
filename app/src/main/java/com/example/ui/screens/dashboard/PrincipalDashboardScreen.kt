package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PrincipalDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit,
  onShowAddTeacher: () -> Unit,
  onShowCreateNotice: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val leaveRequests by repository.leaveRequests.collectAsState()
  val notices by repository.notices.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()
  val examSchedules by repository.examSchedules.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val events by repository.events.collectAsState()

  val totalStudents = students.size
  val totalFaculty = teachers.size
  val pendingLeaves = leaveRequests.filter { it.status == LeaveStatus.PENDING }

  val totalFeeDues = feeRecords.sumOf { it.totalFee }.coerceAtLeast(1.0)
  val totalCollected = feeRecords.sumOf { it.paidAmount }
  val totalPending = feeRecords.sumOf { it.pendingAmount }

  val overallAttendance = remember(classAttendanceMap, students) {
    repository.getOverallSchoolAttendanceRate()
  }

  val formattedAttendance = String.format(Locale.US, "%.1f", overallAttendance)

  val listState = rememberLazyListState()
  LaunchedEffect(key1 = Unit) {
    listState.scrollToItem(0)
  }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("principal_dashboard_list"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Executive Welcome Banner
    item {
      AnimatedFadeIn(delayMillis = 0) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = RevenexNavy)
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
                  text = "Welcome, ${currentUser.name}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "Revenex Public School • Term 1 Operations Hub",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = RevenexGold
              ) {
                Text(
                  text = "ADMIN",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Quick Snapshot in Hero (Dynamically calculated)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "TODAY'S ATTENDANCE",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 9.sp
                )
                Text(
                  text = "$formattedAttendance%",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessContainer
                )
              }
              Column {
                Text(
                  text = "FEES COLLECTED",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 9.sp
                )
                Text(
                  text = "₹${(totalCollected / 1000).toInt()}k",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              Column {
                Text(
                  text = "STAFF ON DUTY",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 9.sp
                )
                Text(
                  text = "$totalFaculty / $totalFaculty",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }
      }
    }

    // Quick Action Bar
    item {
      AnimatedFadeIn(delayMillis = 100) {
        Column {
          SectionHeader(title = "Administrative Actions")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              QuickActionButton(
                title = "Admit Student",
                icon = Icons.Default.PersonAdd,
                iconTint = RevenexBlue,
                backgroundColor = RevenexPrimaryContainer,
                modifier = Modifier.width(110.dp),
                onClick = onShowAddStudent
              )
            }
            item {
              QuickActionButton(
                title = "Add Faculty",
                icon = Icons.Default.School,
                iconTint = Color(0xFF6D28D9),
                backgroundColor = Color(0xFFEDE9FE),
                modifier = Modifier.width(110.dp),
                onClick = onShowAddTeacher
              )
            }
            item {
              QuickActionButton(
                title = "Mark Attendance",
                icon = Icons.Default.HowToReg,
                iconTint = Color(0xFF15803D),
                backgroundColor = Color(0xFFDCFCE7),
                modifier = Modifier.width(110.dp),
                onClick = { onNavigateTo(Screen.Attendance.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Publish Notice",
                icon = Icons.Default.Campaign,
                iconTint = Color(0xFFB45309),
                backgroundColor = Color(0xFFFEF3C7),
                modifier = Modifier.width(110.dp),
                onClick = onShowCreateNotice
              )
            }
            item {
              QuickActionButton(
                title = "AI Assistant",
                icon = Icons.Default.AutoAwesome,
                iconTint = Color(0xFF0284C7),
                backgroundColor = Color(0xFFE0F2FE),
                modifier = Modifier.width(110.dp),
                onClick = { onNavigateTo(Screen.AiAssistant.route) }
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }

    // High Level Metric Grid (Live calculated)
    item {
      AnimatedFadeIn(delayMillis = 200) {
        Column {
          SectionHeader(title = "Key Operating Metrics")
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StatCard(
              title = "Enrolled Students",
              value = "$totalStudents",
              sublabel = "Across Classes 6 - 12",
              icon = Icons.Default.Groups,
              iconTint = RevenexBlue,
              iconBackground = RevenexPrimaryContainer,
              trendText = "+4.2% YoY",
              isPositiveTrend = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Students.route) }
            )
            StatCard(
              title = "Active Faculty",
              value = "$totalFaculty",
              sublabel = "100% On Duty Today",
              icon = Icons.Default.Psychology,
              iconTint = Color(0xFF6D28D9),
              iconBackground = Color(0xFFEDE9FE),
              trendText = "98% Avg Att",
              isPositiveTrend = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Teachers.route) }
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            val collectionPercent = ((totalCollected / totalFeeDues) * 100).toInt()
            StatCard(
              title = "Fees Realized",
              value = "₹${(totalCollected / 1000).toInt()}k",
              sublabel = "Target: ₹${(totalFeeDues / 1000).toInt()}k",
              icon = Icons.Default.AccountBalanceWallet,
              iconTint = Color(0xFF15803D),
              iconBackground = Color(0xFFDCFCE7),
              trendText = "$collectionPercent% Collected",
              isPositiveTrend = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Fees.route) }
            )
            StatCard(
              title = "Pending Dues",
              value = "₹${(totalPending / 1000).toInt()}k",
              sublabel = "${feeRecords.count { it.status == FeeStatus.PENDING || it.status == FeeStatus.OVERDUE }} Students Pending",
              icon = Icons.Default.HourglassEmpty,
              iconTint = StatusErrorText,
              iconBackground = StatusErrorContainer,
              trendText = "Action Req.",
              isPositiveTrend = false,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Fees.route) }
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // Visual Analytics: Attendance Trends & Class Comparison
    item {
      AnimatedFadeIn(delayMillis = 300) {
        Column {
          SectionHeader(
            title = "Analytics & Trends",
            actionText = "Full Reports",
            onActionClick = { onNavigateTo(Screen.Reports.route) }
          )
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            SimpleBarChart(
              title = "Weekly Attendance Trend (Mon - Fri)",
              data = listOf(
                "Mon" to 95.8f,
                "Tue" to 97.2f,
                "Wed" to 96.4f,
                "Thu" to 94.1f,
                "Fri" to 96.0f
              ),
              barColor = RevenexPrimary
            )

            SimpleBarChart(
              title = "Class-wise Attendance Ratio Today",
              data = listOf(
                "10-A" to 96.4f,
                "10-B" to 95.3f,
                "9-A" to 94.0f,
                "8-A" to 97.4f,
                "6-B" to 96.5f
              ),
              barColor = Color(0xFF0D9488),
              onBarClick = { classLabel ->
                val grade = classLabel.substringBefore("-")
                val div = classLabel.substringAfter("-")
                onNavigateTo("attendance?classGrade=$grade&division=$div")
              }
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // Pending Approvals & Leave Requests with Live Approve/Reject Actions
    item {
      SectionHeader(
        title = "Pending Leave Approvals (${pendingLeaves.size})",
        actionText = "Manage All",
        onActionClick = { onNavigateTo(Screen.Leaves.route) }
      )
      if (pendingLeaves.isEmpty()) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircleOutline,
              contentDescription = null,
              tint = StatusSuccess
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "All staff and student leave applications are reviewed.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          pendingLeaves.forEach { leave ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder(),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = leave.applicantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  StatusBadge(status = leave.status.label, type = "leave")
                }
                Text(
                  text = "${leave.classOrDept} • ${leave.leaveType} (${leave.daysCount} days: ${leave.startDate} to ${leave.endDate})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "\"${leave.reason}\"",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedButton(
                    onClick = { repository.rejectLeave(leave.id) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                  ) {
                    Text("Reject", fontSize = 12.sp)
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    onClick = { repository.approveLeave(leave.id) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                  ) {
                    Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Latest Published Circulars & Notices
    item {
      SectionHeader(
        title = "Official School Circulars",
        actionText = "View All",
        onActionClick = { onNavigateTo(Screen.Notices.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        notices.take(3).forEach { notice ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateTo(Screen.Notices.route) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                StatusBadge(status = notice.category.label)
                Text(
                  text = notice.publishedDate,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = notice.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = notice.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Upcoming Exams
    item {
      SectionHeader(
        title = "Upcoming Examination Schedules",
        actionText = "View Calendar",
        onActionClick = { onNavigateTo(Screen.Exams.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val upcomingExamSubjects = examSchedules.flatMap { sched ->
          sched.subjects.map { subj -> sched to subj }
        }
        if (upcomingExamSubjects.isEmpty()) {
          Text("No exams scheduled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          upcomingExamSubjects.take(3).forEach { (sched, exam) ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder(),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEDE9FE), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFF6D28D9))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${exam.subjectName} (${sched.title})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Class ${sched.classGrade} • Room ${exam.room}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = exam.date,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevenexBlue
                  )
                  Text(
                    text = exam.time,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Academic & Homework Alerts
    item {
      SectionHeader(
        title = "Academic & Homework Alerts",
        actionText = "All Homework",
        onActionClick = { onNavigateTo(Screen.Homework.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (assignments.isEmpty()) {
          Text("No active homework tasks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          assignments.take(3).forEach { hw ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder(),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFFD97706))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = hw.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${hw.subject} • Class ${hw.classGrade}-${hw.division}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "Due: ${hw.dueDate}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                  )
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // School Events & Holidays
    item {
      SectionHeader(
        title = "School Events & Academic Calendar",
        actionText = "Full Calendar",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (events.isEmpty()) {
          Text("No upcoming events scheduled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          events.take(3).forEach { event ->
            val isHoliday = event.category.equals("Holiday", ignoreCase = true)
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder(),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(
                      if (isHoliday) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (isHoliday) Icons.Default.EventBusy else Icons.Default.Festival,
                    contentDescription = null,
                    tint = if (isHoliday) Color(0xFFEF4444) else Color(0xFF0284C7)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = event.date,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHoliday) Color(0xFFB91C1C) else Color(0xFF0369A1)
                  )
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
