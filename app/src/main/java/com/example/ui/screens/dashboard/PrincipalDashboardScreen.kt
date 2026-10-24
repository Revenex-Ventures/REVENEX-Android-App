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

@Composable
fun PrincipalDashboardScreen(repository: ErpDataRepository, onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit, onShowAddTeacher: () -> Unit, onShowCreateNotice: () -> Unit) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val invoices by repository.invoices.collectAsState()
  val records by repository.classAttendance.collectAsState()
  val leaves by repository.leaveRequests.collectAsState()
  val exams by repository.examSchedules.collectAsState()
  val logs by repository.auditLogs.collectAsState()
  val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
  val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
  val week = (0..5).map { offset ->
    val day = monday.plusDays(offset.toLong())
    val registers = records.values.filter { attendanceDateMatches(it.date, day.toString()) }
    val total = registers.sumOf { it.totalStudents }
    day to if (total == 0) null else registers.sumOf { it.presentCount + it.lateCount + it.excusedCount } * 100f / total
  }
  val attendance = week.mapNotNull { it.second }.takeIf { it.isNotEmpty() }?.average()
  val collected = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(java.math.BigDecimal(invoices.sumOf { it.paidAmount }).movePointLeft(2))
  LazyColumn(Modifier.fillMaxSize().testTag("principal_dashboard_list"),
    contentPadding = PaddingValues(Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xl24)) {
    item {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("School overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(today.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = { onNavigateTo(Screen.OperationsHub.route) }) { Text("School modules") }
      }
      Spacer(Modifier.height(Spacing.md12))
      Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SchoolMetric(students.size.toString(), "Students", Icons.Default.Groups, Modifier.weight(1f)) { onNavigateTo(Screen.Students.route) }
        SchoolMetric(attendance?.let { String.format(Locale.US,"%.0f%%",it) } ?: "—", "This week", Icons.Default.FactCheck, Modifier.weight(1f)) { onNavigateTo(Screen.PrincipalAttendance.route) }
        SchoolMetric(collected, "Collected", Icons.Default.CurrencyRupee, Modifier.weight(1f)) { onNavigateTo(Screen.Fees.route) }
      }
    }
    item {
      SchoolGroup {
        Column(Modifier.padding(Spacing.md12), verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          Text("Weekly attendance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text("Recorded registers · this week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
            week.forEach { (day, rate) ->
              Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(96.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                  if (rate != null) Box(Modifier.width(Spacing.xl24).fillMaxHeight((rate / 100f).coerceIn(0f,1f)).background(MaterialTheme.colorScheme.secondaryContainer))
                  Text(rate?.let { "${it.toInt()}%" } ?: "—", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(day.format(DateTimeFormatter.ofPattern("EEE")), style = MaterialTheme.typography.labelSmall)
              }
            }
          }
          if (attendance == null) Text("No attendance recorded this week.", style = MaterialTheme.typography.bodySmall)
        }
      }
    }
    item { SchoolSection("Today's actions", "All modules", { onNavigateTo(Screen.OperationsHub.route) }) {
      SchoolGroup {
        SchoolRow("Attendance register", "Record or review class attendance", Icons.Default.FactCheck, { onNavigateTo(Screen.Attendance.route) })
        HorizontalDivider()
        SchoolRow("Leave requests", "Awaiting a decision", Icons.Default.EventBusy, { onNavigateTo(Screen.Leaves.route) }, leaves.count { it.status == LeaveStatus.PENDING }.toString())
        HorizontalDivider()
        SchoolRow("Unpaid invoices", "Review outstanding balances", Icons.Default.ReceiptLong, { onNavigateTo(Screen.Fees.route) }, invoices.count { it.pendingAmount > 0 }.toString())
      }
    } }
    item { SchoolSection("Scheduled examinations", "View all", { onNavigateTo(Screen.Exams.route) }) {
      SchoolGroup {
        if (exams.isEmpty()) Text("No examinations scheduled.", Modifier.padding(Spacing.md12), style = MaterialTheme.typography.bodySmall)
        exams.take(2).forEach { exam -> SchoolRow(exam.title, "${exam.startDate} · Class ${exam.classGrade}", Icons.Default.CalendarMonth, { onNavigateTo(Screen.Exams.route) }) }
      }
    } }
    item { SchoolSection("Manage school") {
      SchoolGroup {
        SchoolRow("Enrol student", "Admissions and class allocation", Icons.Default.PersonAdd, onShowAddStudent)
        SchoolRow("Faculty · ${teachers.size}", "Directory and staff setup", Icons.Default.School, { onNavigateTo(Screen.Teachers.route) })
        SchoolRow("Add faculty", "Staff setup", Icons.Default.PersonAddAlt, onShowAddTeacher)
        SchoolRow("Publish notice", "School communication", Icons.Default.Campaign, onShowCreateNotice)
      }
    } }
    item { SchoolSection("Recent activity", "View all", { onNavigateTo(Screen.Notices.route) }) {
      SchoolGroup {
        if (logs.isEmpty()) Text("No recorded activity.", Modifier.padding(Spacing.md12), style = MaterialTheme.typography.bodySmall)
        logs.take(3).forEach { SchoolRow(it.actionSummary, "${it.actorName} · ${it.timestamp}", Icons.Default.History, { onNavigateTo(Screen.Notices.route) }) }
      }
    } }
  }
}
