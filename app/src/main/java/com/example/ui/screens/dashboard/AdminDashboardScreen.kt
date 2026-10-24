package com.example.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.LeaveStatus
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.attendance.attendanceDateMatches
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun AdminDashboardScreen(repository: ErpDataRepository, onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit, onShowAddTeacher: () -> Unit, onShowCreateNotice: () -> Unit) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val invoices by repository.invoices.collectAsState()
  val leaves by repository.leaveRequests.collectAsState()
  val records by repository.classAttendance.collectAsState()
  val audit by repository.auditLogs.collectAsState()
  val today = LocalDate.now()
  val registers = records.values.filter { attendanceDateMatches(it.date,today.toString()) }
  LazyColumn(Modifier.fillMaxSize().testTag("admin_dashboard_list"), contentPadding = PaddingValues(Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xl24)) {
    item {
      Text("School operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(today.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(Modifier.height(Spacing.md12))
      Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SchoolMetric(students.size.toString(),"Students",Icons.Default.Groups,Modifier.weight(1f)) { onNavigateTo(Screen.Students.route) }
        SchoolMetric(teachers.size.toString(),"Faculty",Icons.Default.School,Modifier.weight(1f)) { onNavigateTo(Screen.Teachers.route) }
        SchoolMetric(invoices.count { it.pendingAmount>0 }.toString(),"Unpaid",Icons.Default.ReceiptLong,Modifier.weight(1f),SchoolMetricTone.FINANCE) { onNavigateTo(Screen.Fees.route) }
      }
    }
    item { SchoolSection("Needs attention", "All modules", { onNavigateTo(Screen.OperationsHub.route) }) { SchoolGroup {
      SchoolRow("Leave requests", "Awaiting a decision",Icons.Default.EventBusy,{onNavigateTo(Screen.Leaves.route)},leaves.count {it.status==LeaveStatus.PENDING}.toString())
      HorizontalDivider()
      SchoolRow("Attendance review", "${registers.size} saved registers today",Icons.Default.FactCheck,{onNavigateTo(Screen.PrincipalAttendance.route)},registers.sumOf {it.absentCount+it.lateCount}.toString()+" exceptions")
      HorizontalDivider()
      SchoolRow("Fee accounts", "Review balances, payments and receipts",Icons.Default.AccountBalanceWallet,{onNavigateTo(Screen.Fees.route)})
    } } }
    item { SchoolSection("Quick actions") { SchoolGroup {
      SchoolRow("Enrol student","Admissions and class allocation",Icons.Default.PersonAdd,onShowAddStudent)
      HorizontalDivider()
      SchoolRow("Add faculty","Staff setup",Icons.Default.PersonAddAlt,onShowAddTeacher)
      HorizontalDivider()
      SchoolRow("Publish notice","School communication",Icons.Default.Campaign,onShowCreateNotice)
    } } }
    item { SchoolSection("School workspaces") { SchoolGroup {
      SchoolRow("People & admissions","Student and faculty records",Icons.Default.Groups,{onNavigateTo(Screen.Students.route)})
      SchoolRow("Academic schedule","Classes and periods",Icons.Default.CalendarMonth,{onNavigateTo(Screen.Timetable.route)})
      SchoolRow("Reports","Operational summaries",Icons.Default.BarChart,{onNavigateTo(Screen.Reports.route)})
      SchoolRow("All modules","Search grouped school tools",Icons.Default.Apps,{onNavigateTo(Screen.OperationsHub.route)})
    } } }
    item { SchoolSection("Recent activity","View all",{onNavigateTo(Screen.Notices.route)}) { SchoolGroup {
      if(audit.isEmpty()) Text("No recorded activity.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      audit.take(3).forEach { SchoolRow(it.actionSummary,"${it.actorName} · ${it.timestamp}",Icons.Default.History,{onNavigateTo(Screen.Notices.route)}) }
    } } }
  }
}
