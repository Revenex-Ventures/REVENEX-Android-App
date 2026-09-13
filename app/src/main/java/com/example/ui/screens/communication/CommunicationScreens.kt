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

  var selectedSubTab by remember { mutableStateOf(0) } // 0 = System Audit Logs, 1 = Parent Broadcasts
  var auditFilter by remember { mutableStateOf("All") }
  val auditFilters = listOf("All", "ROLL_CALL", "FEE_PAYMENT", "GRADEBOOK", "ADMISSION", "BROADCAST")

  val filteredAuditLogs = remember(auditLogs, auditFilter) {
    if (auditFilter == "All") auditLogs
    else auditLogs.filter { it.actionType.equals(auditFilter, ignoreCase = true) }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ScholaLinen)
      .testTag("notices_screen"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    // 1. HERO SURFACE (Dark Onyx, 22dp corners)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.hero))
          .background(ScholaOnyx)
          .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.hero))
          .padding(Spacing.cardPaddingLarge)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "COMMUNICATION & GOVERNANCE",
                color = ScholaOnyxMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = TypeTokens.trackingMicroLabel
              )
              Text(
                text = if (selectedSubTab == 0) "System Audit Trail" else "Parent Broadcasts & Circulars",
                color = ScholaOnyxText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
            }
            if (currentUser.role == UserRole.PRINCIPAL) {
              Button(
                onClick = onShowCreateNotice,
                colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracotta),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Compose", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // 2. SEGMENTED SUB-TAB TOGGLE ("System Audit Logs" ↔ "Parent Broadcasts")
    item {
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = ScholaSurface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .border(1.dp, ScholaBorder, RoundedCornerShape(Radius.pill))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf("System Audit Logs", "Parent Broadcasts").forEachIndexed { index, title ->
            val isSelected = selectedSubTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.pill))
                .background(if (isSelected) ScholaTerracotta else Color.Transparent)
                .clickable { selectedSubTab = index }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ScholaTextSecondary,
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
              color = if (isSelected) ScholaSlateNavy else ScholaSurface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) ScholaSlateNavy else ScholaBorder, RoundedCornerShape(Radius.pill))
                .clickable { auditFilter = flt }
            ) {
              Text(
                text = flt.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else ScholaTextPrimary,
                modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp)
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
                      "ROLL_CALL" -> StatusSuccessBg
                      "FEE_PAYMENT" -> ScholaTerracottaContainer
                      "GRADEBOOK" -> Color(0xFFEDE9FE)
                      else -> ScholaSlateContainer
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
                    "ROLL_CALL" -> StatusSuccessText
                    "FEE_PAYMENT" -> ScholaTerracotta
                    "GRADEBOOK" -> Color(0xFF6D28D9)
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
                  Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = ScholaMuted, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${log.actorName} (${log.actorRole}) • Target: ${log.targetScholar}",
                  style = MaterialTheme.typography.labelSmall,
                  color = ScholaTerracotta,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(log.details, style = MaterialTheme.typography.bodySmall, color = ScholaTextSecondary)
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

      items(notices, key = { it.id }) { notice ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            ScholaPillBadge(status = notice.category.label)
            Text(notice.publishedDate, style = MaterialTheme.typography.labelSmall, color = ScholaMuted)
          }
          Spacer(modifier = Modifier.height(Spacing.s2))
          Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(notice.content, style = MaterialTheme.typography.bodyMedium, color = ScholaTextSecondary)
          Spacer(modifier = Modifier.height(Spacing.s2))
          Text(
            text = "Issued by ${notice.authorName} (${notice.authorRole}) • Target: ${notice.targetAudience}",
            style = MaterialTheme.typography.labelSmall,
            color = ScholaMuted,
            fontSize = 10.sp
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
  onBack: () -> Unit
) {
  val notifications by repository.notifications.collectAsState()

  Scaffold(
    containerColor = ScholaLinen,
    topBar = {
      TopAppBar(
        title = { Text("Institutional Notifications", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ScholaLinen)
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
      items(notifications, key = { it.id }) { notif ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (!notif.isRead) ScholaTerracottaContainer else ScholaBorder),
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
                tint = if (!notif.isRead) ScholaTerracotta else ScholaMuted,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(Spacing.s3))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (!notif.isRead) {
                  Box(modifier = Modifier.size(8.dp).background(ScholaTerracotta, CircleShape))
                }
              }
              Text(notif.message, style = MaterialTheme.typography.bodySmall, color = ScholaTextSecondary)
              Spacer(modifier = Modifier.height(2.dp))
              Text(notif.timestamp, style = MaterialTheme.typography.labelSmall, color = ScholaMuted, fontSize = 9.sp)
            }
          }
        }
      }
    }
  }
}
