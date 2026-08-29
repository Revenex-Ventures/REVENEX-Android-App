package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeStatus
import com.example.data.model.LeaveStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenexTopBar(
  title: String,
  subtitle: String? = null,
  currentRole: UserRole,
  currentUserName: String = "",
  unreadCount: Int = 0,
  onNotificationClick: () -> Unit,
  onSearchClick: () -> Unit,
  onProfileClick: () -> Unit,
  onLogoutClick: () -> Unit,
  navigationIcon: @Composable (() -> Unit)? = null
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shadowElevation = 1.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (navigationIcon != null) {
          navigationIcon()
          Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          if (subtitle != null) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Profile Avatar Menu (My Profile + Logout) — replaces the former role switcher
        var showProfileMenu by remember { mutableStateOf(false) }
        Box {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = when (currentRole) {
              UserRole.PRINCIPAL -> RevenexPrimaryContainer
              UserRole.TEACHER -> Color(0xFFEDE9FE)
              else -> Color(0xFFDCFCE7)
            },
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { showProfileMenu = true }
              .testTag("profile_menu_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .background(
                    when (currentRole) {
                      UserRole.PRINCIPAL -> RevenexBlue
                      UserRole.TEACHER -> Color(0xFF6D28D9)
                      else -> Color(0xFF15803D)
                    },
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUserName.take(1).uppercase().ifBlank { "U" },
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(
                text = when (currentRole) {
                  UserRole.PRINCIPAL -> "Principal"
                  UserRole.TEACHER -> "Teacher"
                  else -> "Student Portal"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when (currentRole) {
                  UserRole.PRINCIPAL -> RevenexBlue
                  UserRole.TEACHER -> Color(0xFF6D28D9)
                  else -> Color(0xFF15803D)
                }
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open Profile Menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          DropdownMenu(
            expanded = showProfileMenu,
            onDismissRequest = { showProfileMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("My Profile") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
              onClick = {
                showProfileMenu = false
                onProfileClick()
              },
              modifier = Modifier.testTag("menu_my_profile")
            )
            DropdownMenuItem(
              text = { Text("Logout") },
              leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
              onClick = {
                showProfileMenu = false
                onLogoutClick()
              },
              modifier = Modifier.testTag("menu_logout")
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Search Action
        IconButton(
          onClick = onSearchClick,
          modifier = Modifier
            .size(38.dp)
            .testTag("global_search_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        // Notification Bell with Badge
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onNotificationClick)
            .testTag("notification_bell_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onSurface
          )
          if (unreadCount > 0) {
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .size(16.dp)
                .background(StatusError, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  sublabel: String,
  icon: ImageVector,
  iconTint: Color,
  iconBackground: Color,
  modifier: Modifier = Modifier,
  trendText: String? = null,
  isPositiveTrend: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(iconBackground, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }

        if (trendText != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isPositiveTrend) StatusSuccessContainer else StatusErrorContainer
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isPositiveTrend) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isPositiveTrend) StatusSuccessText else StatusErrorText,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = trendText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositiveTrend) StatusSuccessText else StatusErrorText,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
      )

      if (sublabel.isNotBlank()) {
        Text(
          text = sublabel,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    if (actionText != null && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = actionText,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
fun StatusBadge(
  status: String,
  type: String = "general" // attendance, fee, leave, general
) {
  val (bgColor, textColor) = when (status.uppercase()) {
    "PRESENT", "PAID", "APPROVED", "OPERATIONAL", "SUBMITTED" -> StatusSuccessContainer to StatusSuccessText
    "ABSENT", "OVERDUE", "REJECTED", "NEEDS REPLACEMENT" -> StatusErrorContainer to StatusErrorText
    "LATE", "PARTIAL", "PARTIALLY PAID", "PENDING", "UNDER MAINTENANCE" -> StatusWarningContainer to StatusWarningText
    "ON LEAVE", "CIRCULAR", "UPCOMING" -> StatusInfoContainer to StatusInfoText
    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor
  ) {
    Text(
      text = status,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  iconTint: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .background(backgroundColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun SimpleBarChart(
  title: String,
  data: List<Pair<String, Float>>, // Label to value (0..100)
  modifier: Modifier = Modifier,
  barColor: Color = RevenexPrimary,
  onBarClick: ((String) -> Unit)? = null
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        data.forEach { (label, value) ->
          var startAnim by remember { mutableStateOf(false) }
          LaunchedEffect(value) {
            startAnim = true
          }
          val animatedValue by animateFloatAsState(
            targetValue = if (startAnim) value else 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
          )
          val fraction = (animatedValue / 100f).coerceIn(0.05f, 1.0f)
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .clickable(enabled = onBarClick != null) { onBarClick?.invoke(label) }
          ) {
            Text(
              text = "${animatedValue.toInt()}%",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .width(22.dp)
                .fillMaxHeight(fraction)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                  Brush.verticalGradient(
                    listOf(
                      barColor,
                      barColor.copy(alpha = 0.7f)
                    )
                  )
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
fun SimpleProgressRing(
  percentage: Double,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  ringColor: Color = StatusSuccess
) {
  var startAnim by remember { mutableStateOf(false) }
  LaunchedEffect(percentage) {
    startAnim = true
  }
  val animatedProgress by animateFloatAsState(
    targetValue = if (startAnim) (percentage / 100.0).toFloat() else 0f,
    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier.size(72.dp),
          color = ringColor,
          strokeWidth = 7.dp,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
          text = String.format("%.1f%%", animatedProgress * 100.0),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  actionButtonText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    if (actionButtonText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onActionClick) {
        Text(actionButtonText)
      }
    }
  }
}

@Composable
fun AnimatedFadeIn(
  delayMillis: Int = 0,
  content: @Composable () -> Unit
) {
  var visible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (delayMillis > 0) {
      kotlinx.coroutines.delay(delayMillis.toLong())
    }
    visible = true
  }
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)) + slideInVertically(
      initialOffsetY = { 40 },
      animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
    )
  ) {
    content()
  }
}

@Composable
fun RevenexLogoCrest(
  modifier: Modifier = Modifier,
  tint: Color = RevenexGoldLight
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      drawCircle(
        color = tint.copy(alpha = 0.25f),
        radius = w / 2f,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
      )
      drawCircle(
        color = tint.copy(alpha = 0.15f),
        radius = w / 2.4f,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
      )
    }
    Icon(
      imageVector = Icons.Default.School,
      contentDescription = "REVENEX Crest",
      tint = tint,
      modifier = Modifier.fillMaxSize(0.55f)
    )
  }
}


