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
  val actor by repository.currentUser.collectAsState()
  val cloud = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO
  val session = if(cloud) runCatching {repository.supabaseDataSource.sessionId()}.getOrDefault("") else "Local sample session"
  var contextOpen by remember {mutableStateOf(false)}
  if(contextOpen) AlertDialog(onDismissRequest={contextOpen=false},title={Text("School context")},text={Column {
    Text(if(cloud) "School ID: ${actor.schoolId}" else "Revenex Public School · sample")
    Text("Session: ${session.ifBlank { "Not loaded" }}")
  }},confirmButton={TextButton(onClick={contextOpen=false}){Text("Done")}})
  val today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))
  val registers = records.values.filter { attendanceDateMatches(it.date,today.toString()) }
  LazyColumn(Modifier.fillMaxSize().testTag("admin_dashboard_list"), contentPadding = PaddingValues(Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xl24)) {
    item {
      Text("School operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      TextButton(onClick={contextOpen=true},contentPadding=PaddingValues(Spacing.xs4)) {
        Text(if(cloud) "School ID …${actor.schoolId.takeLast(6)} · Session …${session.takeLast(6)}" else "Revenex Public School · Local sample session",style=MaterialTheme.typography.bodySmall)
      }
      Spacer(Modifier.height(Spacing.md12))
      Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SchoolMetric(students.size.toString(),"Students",Icons.Default.Groups,Modifier.weight(1f)) { onNavigateTo(Screen.Students.route) }
        SchoolMetric(teachers.size.toString(),"Faculty",Icons.Default.School,Modifier.weight(1f)) { onNavigateTo(Screen.Teachers.route) }
        SchoolMetric(invoices.count { it.pendingAmount>0 }.toString(),"Unpaid",Icons.Default.ReceiptLong,Modifier.weight(1f),SchoolMetricTone.FINANCE) { onNavigateTo(Screen.Fees.route) }
      }
    }
    item { SchoolSection("Needs attention", "View all", { onNavigateTo(Screen.OperationsHub.route) }) { SchoolGroup {
      SchoolRow("Leave requests", "Awaiting a decision",Icons.Default.EventBusy,{onNavigateTo(Screen.Leaves.route)},leaves.count {it.status==LeaveStatus.PENDING}.toString())
      HorizontalDivider()
      SchoolRow("Attendance", "${registers.size} registers saved today",Icons.Default.FactCheck,{onNavigateTo(Screen.PrincipalAttendance.route)},registers.sumOf {it.absentCount+it.lateCount}.toString())
      HorizontalDivider()
      SchoolRow("Fees", "Dues and collections",Icons.Default.AccountBalanceWallet,{onNavigateTo(Screen.Fees.route)})
    } } }
    item { SchoolSection("Quick actions") { SchoolGroup {
      SchoolRow("Add student","Admission and class",Icons.Default.PersonAdd,onShowAddStudent)
      HorizontalDivider()
      SchoolRow("Add faculty","Staff setup",Icons.Default.PersonAddAlt,onShowAddTeacher)
      HorizontalDivider()
      SchoolRow("Publish notice","School communication",Icons.Default.Campaign,onShowCreateNotice)
    } } }
    item { SchoolSection("Modules") { SchoolGroup {
      SchoolRow("People","Student and faculty records",Icons.Default.Groups,{onNavigateTo(Screen.Students.route)})
      SchoolRow("Academics","Classes and periods",Icons.Default.CalendarMonth,{onNavigateTo(Screen.Timetable.route)})
      SchoolRow("Reports","Operational summaries",Icons.Default.BarChart,{onNavigateTo(Screen.Reports.route)})
      SchoolRow("View all","Search all modules",Icons.Default.Apps,{onNavigateTo(Screen.OperationsHub.route)})
    } } }
    item { SchoolSection("Collections", "View all", {onNavigateTo(Screen.Fees.route)}) { SchoolGroup {
      val collected = invoices.filter {it.paidAmount > 0}.take(3)
      if(collected.isEmpty()) Text("No collections loaded.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      collected.forEach { invoice -> SchoolRow(invoice.invoiceNumber,
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(java.math.BigDecimal(invoice.paidAmount).movePointLeft(2)),
        Icons.Default.ReceiptLong,{onNavigateTo(Screen.Fees.route)}) }
    } } }
    item { SchoolSection("Recent activity","View all",{onNavigateTo(Screen.Audit.route)}) { SchoolGroup {
      if(audit.isEmpty()) Text("No recorded activity.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      audit.take(3).forEach { SchoolRow(it.actionSummary,"${it.actorName} · ${it.timestamp}",Icons.Default.History,{onNavigateTo(Screen.Audit.route)}) }
    } } }
  }
}
