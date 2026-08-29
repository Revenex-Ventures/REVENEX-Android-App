package com.example.ui.screens.communication

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoticeCategory
import com.example.data.model.NotificationCategory
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun NoticesScreen(
  repository: ErpDataRepository,
  onShowCreateNotice: () -> Unit
) {
  val notices by repository.notices.collectAsState()
  var selectedCategoryFilter by remember { mutableStateOf("All") }

  val categories = listOf("All", "Circular", "Event", "Exam", "Holiday")
  val filteredNotices = remember(notices, selectedCategoryFilter) {
    if (selectedCategoryFilter == "All") notices
    else notices.filter { it.category.label.equals(selectedCategoryFilter, ignoreCase = true) }
  }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onShowCreateNotice,
        icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
        text = { Text("Publish Notice", fontWeight = FontWeight.Bold) },
        containerColor = RevenexBlue,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_publish_notice")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("notices_screen_list"),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(categories) { cat ->
            FilterChip(
              selected = selectedCategoryFilter == cat,
              onClick = { selectedCategoryFilter = cat },
              label = { Text(cat) }
            )
          }
        }
      }

      items(filteredNotices, key = { it.id }) { notice ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = notice.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = notice.content,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (notice.attachmentName != null) {
              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = notice.attachmentName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                  )
                  Icon(Icons.Default.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Published by ${notice.authorName} (${notice.authorRole}) • Target: ${notice.targetAudience}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotificationsScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit
) {
  val notifications by repository.notifications.collectAsState()

  Scaffold(
    topBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
          Text(
            text = "Notifications & Alerts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )
          TextButton(onClick = { repository.markAllNotificationsAsRead() }) {
            Text("Mark all read", fontSize = 12.sp)
          }
        }
      }
    }
  ) { paddingValues ->
    if (notifications.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.NotificationsNone,
        title = "No New Notifications",
        description = "You're all caught up with school updates."
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .testTag("notifications_list_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(notifications, key = { it.id }) { notif ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { repository.markNotificationAsRead(notif.id) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else RevenexPrimaryContainer.copy(alpha = 0.4f)
            ),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(
                    when (notif.category) {
                      NotificationCategory.FEES -> StatusWarningContainer
                      NotificationCategory.ATTENDANCE -> StatusSuccessContainer
                      NotificationCategory.HOMEWORK -> RevenexPrimaryContainer
                      NotificationCategory.LEAVE -> Color(0xFFEDE9FE)
                      else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (notif.category) {
                    NotificationCategory.FEES -> Icons.Default.Payments
                    NotificationCategory.ATTENDANCE -> Icons.Default.FactCheck
                    NotificationCategory.HOMEWORK -> Icons.Default.Assignment
                    NotificationCategory.LEAVE -> Icons.Default.EventBusy
                    else -> Icons.Default.Notifications
                  },
                  contentDescription = null,
                  tint = when (notif.category) {
                    NotificationCategory.FEES -> Color(0xFFB45309)
                    NotificationCategory.ATTENDANCE -> StatusSuccessText
                    NotificationCategory.HOMEWORK -> RevenexBlue
                    NotificationCategory.LEAVE -> Color(0xFF6D28D9)
                    else -> MaterialTheme.colorScheme.primary
                  },
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = notif.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = if (notif.isRead) FontWeight.SemiBold else FontWeight.Bold
                )
                Text(
                  text = notif.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = notif.timestamp,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  fontSize = 10.sp
                )
              }

              if (!notif.isRead) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(RevenexBlue, CircleShape)
                )
              }
            }
          }
        }
      }
    }
  }
}
