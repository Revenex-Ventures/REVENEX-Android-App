package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaveStatus
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.CompactStatTile
import com.example.ui.components.SectionHeader
import com.example.ui.components.WorkflowQuickPill
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Counts are scoped repository data, never inferred operational success. */
@Composable
fun AdminDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit,
  onShowAddTeacher: () -> Unit,
  onShowCreateNotice: () -> Unit
) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val invoices by repository.invoices.collectAsState()
  val leaves by repository.leaveRequests.collectAsState()
  val notices by repository.notices.collectAsState()
  val auditLogs by repository.auditLogs.collectAsState()
  val registers by repository.classAttendance.collectAsState()
  val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
  val todayRegisters = registers.values.filter { it.date == today }
  val unpaid = invoices.count { it.pendingAmount > 0L }
  val pendingLeave = leaves.count { it.status == LeaveStatus.PENDING }
  val exceptions = todayRegisters.sumOf { it.absentCount + it.lateCount }
  val wide = LocalConfiguration.current.screenWidthDp >= 600 && LocalConfiguration.current.fontScale <= 1.3f

  LazyColumn(
    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).testTag("admin_dashboard_list"),
    contentPadding = PaddingValues(Spacing.screenPadding),
    verticalArrangement = Arrangement.spacedBy(Spacing.xl24)
  ) {
    item {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("School operations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
          Text(today, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { onNavigateTo(Screen.OperationsHub.route) }) { Icon(Icons.Default.Apps, "Open module directory") }
      }
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
        CompactStatTile("Students", students.size.toString(), Icons.Default.Groups,
          iconTint = SemanticColors.onPeach, iconContainerColor = SemanticColors.peachContainer,
          deltaText = "Records", modifier = Modifier.weight(1f), onClick = { onNavigateTo(Screen.Students.route) })
        CompactStatTile("Faculty", teachers.size.toString(), Icons.Default.School,
          iconTint = SemanticColors.onLavender, iconContainerColor = SemanticColors.lavenderContainer,
          deltaText = "Profiles", modifier = Modifier.weight(1f), onClick = { onNavigateTo(Screen.Teachers.route) })
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SectionHeader("Quick actions")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          item { WorkflowQuickPill("Enrol student", Icons.Default.PersonAdd, onShowAddStudent, isHighlighted = true) }
          item { WorkflowQuickPill("Add faculty", Icons.Default.School, onShowAddTeacher) }
          item { WorkflowQuickPill("Publish notice", Icons.Default.Campaign, onShowCreateNotice) }
        }
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SectionHeader("Needs attention")
        AdminWorkRow("Unpaid invoices", "$unpaid invoices with an outstanding balance", Icons.Default.AccountBalanceWallet,
          onClick = { onNavigateTo(Screen.Fees.route) })
        AdminWorkRow("Leave requests", "$pendingLeave awaiting a decision", Icons.Default.EventBusy,
          onClick = { onNavigateTo(Screen.Leaves.route) })
        AdminWorkRow("Attendance review", if (todayRegisters.isEmpty()) "No registers recorded today" else "$exceptions absent or late entries Â· ${todayRegisters.size} registers today",
          Icons.Default.FactCheck, onClick = { onNavigateTo(Screen.PrincipalAttendance.route) })
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SectionHeader("Manage the school", actionText = "All modules", onActionClick = { onNavigateTo(Screen.OperationsHub.route) })
        val shortcuts = listOf(
          Triple("People & admissions", "Student and faculty records", Screen.Students.route),
          Triple("Academic schedule", "Classes and periods", Screen.Timetable.route),
          Triple("Communication", "Notices and audit history", Screen.Notices.route),
          Triple("Operational reports", "Scoped school summaries", Screen.Reports.route)
        )
        shortcuts.chunked(if (wide) 2 else 1).forEach { row ->
          Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
            row.forEach { (title, detail, route) ->
              AdminWorkRow(title, detail, Icons.Default.ChevronRight, Modifier.weight(1f)) { onNavigateTo(route) }
            }
          }
        }
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        SectionHeader("Recent activity", actionText = "Audit history", onActionClick = { onNavigateTo(Screen.Notices.route) })
        if (auditLogs.isEmpty()) Text("No recorded activity is available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        auditLogs.take(3).forEach { log ->
          Column(Modifier.fillMaxWidth().padding(vertical = Spacing.sm8), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
            Text(log.actionSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("${log.actorName} Â· ${log.timestamp}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(top = Spacing.sm8), color = MaterialTheme.colorScheme.outlineVariant)
          }
        }
        TextButton(onClick = { onNavigateTo(Screen.Notices.route) }) { Text("Notices & circulars Â· ${notices.size}") }
      }
    }
  }
}

@Composable
private fun AdminWorkRow(title: String, detail: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
  OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.md),
    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Row(Modifier.fillMaxWidth().padding(Spacing.md12), verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
      Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
