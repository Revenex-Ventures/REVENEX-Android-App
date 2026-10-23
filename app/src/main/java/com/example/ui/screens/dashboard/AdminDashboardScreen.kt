package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.AnimatedFadeIn
import com.example.ui.components.AppCard
import com.example.ui.components.CompactStatTile
import com.example.ui.components.FeatureHeroCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkflowQuickPill
import com.example.ui.navigation.Screen
import com.example.ui.theme.GlassBgTransparent
import com.example.ui.theme.ScholaGold

import com.example.ui.theme.MaterialTheme.colorScheme.onSurface

import com.example.ui.theme.SemanticColors
import com.example.ui.theme.Spacing



import java.util.Locale

private data class AdminModule(
  val title: String,
  val description: String,
  val icon: ImageVector,
  val route: String,
  val tint: Color,
  val container: Color
)

/**
 * Operational workspace for the school employee entrusted with day-to-day ERP
 * maintenance. It deliberately routes to real modules only; server-side role
 * claims remain the authority for every write.
 */
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
  val notices by repository.notices.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val auditLogs by repository.auditLogs.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()

  val pendingInvoices = invoices.count { it.pendingAmount > 0L }
  val overdueInvoices = invoices.count { it.status.name == "OVERDUE" }
  val modules = listOf(
    AdminModule("Admissions", "Student records and enrolment", Icons.Default.Groups, Screen.Students.route, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
    AdminModule("Faculty", "Staff directory and workloads", Icons.Default.School, Screen.Teachers.route, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.secondaryContainer),
    AdminModule("Academic setup", "Periods, classes and timetables", Icons.Default.CalendarMonth, Screen.Timetable.route, ScholaGold, SemanticColors.warningContainer),
    AdminModule("Attendance", "Registers and exception review", Icons.Default.FactCheck, Screen.Attendance.route, SemanticColors.onSuccess, SemanticColors.successContainer),
    AdminModule("Finance", "Dues, receipts and reconciliation", Icons.Default.AccountBalanceWallet, Screen.Fees.route, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer),
    AdminModule("Communication", "Notices and recipient delivery", Icons.Default.Campaign, Screen.Notices.route, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
    AdminModule("Assets", "Library, transport and inventory", Icons.Default.Inventory, Screen.Inventory.route, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.secondaryContainer),
    AdminModule("Reports", "Operational review and exports", Icons.Default.Analytics, Screen.Reports.route, ScholaGold, SemanticColors.warningContainer)
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .testTag("admin_dashboard_list"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s4)
  ) {
    item {
      AnimatedFadeIn {
        FeatureHeroCard(
          cycleTitle = "SCHOOL OPERATIONS • 2026–27",
          categoryBadge = "ADMIN CONTROL ROOM",
          primaryStatistic = students.size.toString(),
          statisticLabel = "Active student records",
          secondaryKeyMetric = pendingInvoices.toString(),
          secondaryMetricLabel = "Invoices needing follow-up",
          actionButtonText = "Open module directory",
          onActionClick = { onNavigateTo(Screen.OperationsHub.route) },
          badgeBgColor = MaterialTheme.colorScheme.primaryContainer,
          badgeTextColor = MaterialTheme.colorScheme.primary,
          heroHighlightColor = MaterialTheme.colorScheme.primary
        )
      }
    }

    item {
      AnimatedFadeIn(delayMillis = 80) {
        Column {
          SectionHeader(title = "Priority actions")
          LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
            item { WorkflowQuickPill("Add student", Icons.Default.Groups, onShowAddStudent, isHighlighted = true) }
            item { WorkflowQuickPill("Add faculty", Icons.Default.School, onShowAddTeacher) }
            item { WorkflowQuickPill("Publish notice", Icons.Default.Campaign, onShowCreateNotice) }
            item { WorkflowQuickPill("Review schedule", Icons.Default.CalendarMonth, onClick = { onNavigateTo(Screen.Timetable.route) }) }
          }
        }
      }
    }

    item {
      AnimatedFadeIn(delayMillis = 140) {
        Column {
          SectionHeader(title = "School operations at a glance")
          Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s3)) {
            CompactStatTile(
              categoryLabel = "Faculty profiles",
              statCounter = teachers.size.toString(),
              icon = Icons.Default.School,
              iconTint = MaterialTheme.colorScheme.onSurface,
              iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              deltaText = "Directory",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Teachers.route) }
            )
            CompactStatTile(
              categoryLabel = "Timetable slots",
              statCounter = timetable.size.toString(),
              icon = Icons.Default.CalendarMonth,
              iconTint = SemanticColors.onWarning,
              iconContainerColor = SemanticColors.warningContainer,
              deltaText = "Configured",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Timetable.route) }
            )
          }
          Spacer(modifier = Modifier.height(Spacing.s3))
          Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s3)) {
            CompactStatTile(
              categoryLabel = "Recorded registers",
              statCounter = classAttendanceMap.size.toString(),
              icon = Icons.Default.FactCheck,
              iconTint = SemanticColors.onSuccess,
              iconContainerColor = SemanticColors.successContainer,
              deltaText = "Recorded",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Attendance.route) }
            )
            CompactStatTile(
              categoryLabel = "Overdue accounts",
              statCounter = overdueInvoices.toString(),
              icon = Icons.Default.WarningAmber,
              iconTint = if (overdueInvoices == 0) SemanticColors.onSuccess else SemanticColors.onDanger,
              iconContainerColor = if (overdueInvoices == 0) SemanticColors.successContainer else SemanticColors.dangerContainer,
              deltaText = if (overdueInvoices == 0) "Clear" else "Needs review",
              isPositiveDelta = overdueInvoices == 0,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Fees.route) }
            )
          }
        }
      }
    }

    item {
      AnimatedFadeIn(delayMillis = 200) {
        Column {
          SectionHeader(title = "Manage the school")
          modules.chunked(2).forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
            ) {
              row.forEach { module ->
                AdminModuleCard(module, Modifier.weight(1f)) { onNavigateTo(module.route) }
              }
              if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(Spacing.s3))
          }
        }
      }
    }

    item {
      AnimatedFadeIn(delayMillis = 260) {
        Column {
          SectionHeader(title = "Access and change control")
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
              Spacer(modifier = Modifier.width(Spacing.s3))
              Column(modifier = Modifier.weight(1f)) {
                Text("Role grants are server controlled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                  "Supabase policies and backend authorization decide access. Every operational change should be recorded in the audit trail.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
              }
              StatusBadge("Policy enforced")
            }
          }
        }
      }
    }

    item {
      AnimatedFadeIn(delayMillis = 320) {
        Column {
          SectionHeader(title = "Recent operational activity", actionText = "View notices", onActionClick = { onNavigateTo(Screen.Notices.route) })
          if (auditLogs.isEmpty()) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
              Text("No recorded activity is available for this demo session.", style = MaterialTheme.typography.bodyMedium)
            }
          } else {
            auditLogs.take(3).forEach { log ->
              AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(log.actionSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(Spacing.xs4))
                Text(log.actorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
              }
              Spacer(modifier = Modifier.height(Spacing.s2))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminModuleCard(module: AdminModule, modifier: Modifier, onClick: () -> Unit) {
  AppCard(modifier = modifier.testTag("admin_module_${module.title.lowercase().replace(" ", "_")}"), onClick = onClick) {
    Icon(module.icon, contentDescription = null, tint = module.tint)
    Spacer(modifier = Modifier.height(Spacing.s3))
    Text(module.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(Spacing.xs4))
    Text(module.description, style = MaterialTheme.typography.bodySmall, color = module.tint.copy(alpha = 0.8f))
  }
}
