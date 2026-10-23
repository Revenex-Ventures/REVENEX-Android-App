package com.example.ui.screens.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*
import com.example.ui.navigation.Screen
import kotlinx.coroutines.launch

// ============================================================================
// AUDIT TRAIL & BROADCASTS WORKSPACE
// Segmented sub-tab: "System Audit Logs" ↔ "Parent Broadcasts"
// ============================================================================
@Composable
fun NoticesScreen(
  repository: ErpDataRepository,
  onShowCreateNotice: () -> Unit
) {
  val auditLogs by repository.auditLogs.collectAsState()
  val notices by repository.notices.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  var selectedSubTab by remember(currentUser.id) { mutableStateOf(1) } // 0 = System Audit Logs, 1 = Parent Broadcasts
  var auditFilter by remember { mutableStateOf("All") }
  val auditFilters = listOf("All", "ROLL_CALL", "FEE_PAYMENT", "GRADEBOOK", "ADMISSION", "BROADCAST")

  val filteredAuditLogs = remember(auditLogs, auditFilter) {
    if (auditFilter == "All") auditLogs
    else auditLogs.filter { it.actionType.equals(auditFilter, ignoreCase = true) }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("notices_screen"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      WorkspaceHeading(
        if (selectedSubTab == 0) "School activity" else "Notices & circulars",
        if (selectedSubTab == 0) "Recorded actions from your school workspace." else "Announcements, events and important school updates."
      ) {
        if (currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN)) {
          FilledTonalButton(onClick = onShowCreateNotice) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(Spacing.sm8))
            Text("Compose notice")
          }
        }
      }
    }

    // 2. SEGMENTED SUB-TAB TOGGLE ("System Audit Logs" ↔ "Parent Broadcasts")
    item {
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf("Audit log", "Notices").forEachIndexed { index, title ->
            if (index == 0 && currentUser.role !in setOf(UserRole.PRINCIPAL, UserRole.ADMIN)) return@forEachIndexed
            val isSelected = selectedSubTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.pill))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { selectedSubTab = index }
                .heightIn(min = 48.dp).padding(vertical = Spacing.sm8),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // 3. SUB-TAB CONTENT
    if (selectedSubTab == 0) {
      // TAB 1: SYSTEM AUDIT LOGS
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          items(auditFilters) { flt ->
            val isSelected = auditFilter == flt
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
                .clickable { auditFilter = flt }
            ) {
              Text(
                text = flt.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = Spacing.s3, vertical = Spacing.sm8)
              )
            }
          }
        }
      }

      if (filteredAuditLogs.isEmpty()) {
        item {
          EmptyStateView(title = "No Audit Logs", message = "No system audit logs found matching '$auditFilter'.")
        }
      } else {
        items(filteredAuditLogs, key = { it.id }) { log ->
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(
                    when (log.actionType) {
                      "ROLL_CALL" -> SemanticColors.successContainer
                      "FEE_PAYMENT" -> MaterialTheme.colorScheme.primaryContainer
                      "GRADEBOOK" -> Color(0xFFEDE9FE)
                      else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (log.actionType) {
                    "ROLL_CALL" -> Icons.Default.CheckCircle
                    "FEE_PAYMENT" -> Icons.Default.Payment
                    "GRADEBOOK" -> Icons.Default.Grade
                    "ADMISSION" -> Icons.Default.PersonAdd
                    else -> Icons.Default.Campaign
                  },
                  contentDescription = null,
                  tint = when (log.actionType) {
                    "ROLL_CALL" -> SemanticColors.onSuccess
                    "FEE_PAYMENT" -> MaterialTheme.colorScheme.primary
                    "GRADEBOOK" -> MaterialTheme.colorScheme.primary
                    else -> ScholaSlateNavy
                  },
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(Spacing.s3))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(log.actionSummary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                  Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${log.actorName} (${log.actorRole}) • Target: ${log.targetScholar}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    } else {
      // TAB 2: PARENT BROADCASTS
      item {
        SectionHeader(title = "Published Official Circulars (${notices.size})")
      }

      if (notices.isEmpty()) item {
        EmptyStateView(title = "No school notices", message = "Published announcements will appear here.")
      }
      items(notices, key = { it.id }) { notice ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            ScholaPillBadge(status = notice.category.label)
            Text(notice.publishedDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.height(Spacing.s2))
          Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(notice.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(Spacing.s2))
          Text(
            text = "Issued by ${notice.authorName} (${notice.authorRole}) • Target: ${notice.targetAudience}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

// Notifications Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onNavigateTo: (String) -> Unit = {}
) {
  val notifications by repository.notifications.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val scope = rememberCoroutineScope()
  var opening by remember(currentUser.id) { mutableStateOf<String?>(null) }
  var inboxError by remember(currentUser.id) { mutableStateOf<String?>(null) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = { Text("Notifications", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
      verticalArrangement = Arrangement.spacedBy(Spacing.s2)
    ) {
      item {
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Text("${notifications.count { !it.isRead }} unread · School inbox",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
          Text("Updates sync while the Supabase demo is open. Device push notifications are not enabled in this demo.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      inboxError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
      if (notifications.isEmpty()) {
        item { EmptyStateView(title = "No notifications", message = "School updates will appear here.") }
      }
      items(notifications, key = { it.id }) { notif ->
        AppCard(modifier = Modifier.fillMaxWidth().clickable(
          enabled = opening == null,
          onClickLabel = "Open notification"
        ) {
          opening = notif.id; inboxError = null
          scope.launch {
            try {
              if (!notif.isRead) repository.readNotificationConfirmed(notif.id)
              onNavigateTo(notificationDestination(notif.category))
            } catch (e: Exception) {
              if (e is kotlinx.coroutines.CancellationException) throw e
              inboxError = "Could not open this update. Read state is not confirmed; tap again to retry."
            } finally { opening = null }
          }
        }) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (notif.category) {
                  NotificationCategory.ATTENDANCE -> Icons.Default.HowToReg
                  NotificationCategory.FEES -> Icons.Default.AttachMoney
                  NotificationCategory.EXAMS -> Icons.Default.EventNote
                  else -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = if (!notif.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(Spacing.s3))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(notif.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (!notif.isRead) {
                  Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
              }
              Text("${notif.category.name.lowercase().replaceFirstChar { it.uppercase() }} · ${if (opening == notif.id) "Opening…" else if (!notif.isRead) "Unread" else "Read"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
              Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(2.dp))
              Text(notif.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

internal fun notificationDestination(category: NotificationCategory): String = when (category) {
  NotificationCategory.ATTENDANCE -> Screen.Attendance.route
  NotificationCategory.FEES -> Screen.Fees.route
  NotificationCategory.HOMEWORK -> Screen.Homework.route
  NotificationCategory.EXAMS -> Screen.ReportCard.route
  NotificationCategory.LEAVE -> Screen.Leaves.route
  NotificationCategory.ANNOUNCEMENTS -> Screen.Notices.route
  else -> Screen.OperationsHub.route
}
