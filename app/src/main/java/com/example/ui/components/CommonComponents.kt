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
import androidx.compose.ui.draw.scale
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
import com.example.data.model.UserRole
import com.example.ui.theme.*
import java.util.Locale

// ============================================================================
// 1. SCHOLA TOP APP BAR (Institutional crest, ⌘K search trigger, unread badge)
// ============================================================================
@Composable
fun ScholaTopAppBar(
  title: String = "ScholaOS",
  subtitle: String? = "Institutional Operating System",
  currentRole: UserRole = UserRole.PRINCIPAL,
  currentUserName: String = "Dr. Arthur Pendelton",
  unreadCount: Int = 3,
  onSearchClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onProfileClick: () -> Unit = {},
  onLogoutClick: () -> Unit = {},
  navigationIcon: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = ScholaLinen,
    tonalElevation = Elev.e0
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = Spacing.s4, vertical = Spacing.s3),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (navigationIcon != null) {
          navigationIcon()
          Spacer(modifier = Modifier.width(Spacing.s2))
        }

        // Institutional Crest Emblem Icon
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(ScholaSlateNavy),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "ScholaOS Crest",
            tint = ScholaGold,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(Spacing.s3))

        // Institutional Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ScholaTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = ScholaTerracottaContainer
            ) {
              Text(
                text = "v2.6",
                style = MaterialTheme.typography.labelSmall,
                color = ScholaOnTerracottaContainer,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
          }
          if (subtitle != null) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = ScholaMuted,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Quick Search Trigger Pill (⌘K)
        Surface(
          shape = RoundedCornerShape(Radius.pill),
          color = ScholaSurface,
          modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .border(1.dp, ScholaBorder, RoundedCornerShape(Radius.pill))
            .clickable(onClick = onSearchClick)
            .testTag("command_palette_trigger_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = ScholaMuted,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "⌘K",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = ScholaTerracotta,
              fontSize = 11.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(Spacing.s2))

        // Notification Bell with Badge
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ScholaSurface)
            .border(1.dp, ScholaBorder, CircleShape)
            .clickable(onClick = onNotificationClick)
            .testTag("topbar_notifications_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = ScholaTextPrimary,
            modifier = Modifier.size(18.dp)
          )
          if (unreadCount > 0) {
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
                .size(14.dp)
                .background(StatusDangerText, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (unreadCount > 9) "9" else unreadCount.toString(),
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(Spacing.s2))

        // User Avatar Dropdown
        var showProfileMenu by remember { mutableStateOf(false) }
        Box {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(RoleAccent.of(currentRole))
              .clickable { showProfileMenu = true }
              .testTag("topbar_profile_avatar"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUserName.take(2).uppercase(Locale.getDefault()).ifBlank { "AD" },
              color = Color.White,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          DropdownMenu(
            expanded = showProfileMenu,
            onDismissRequest = { showProfileMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Profile (${currentRole.displayName})") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
              onClick = {
                showProfileMenu = false
                onProfileClick()
              }
            )
            DropdownMenuItem(
              text = { Text("Logout") },
              leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
              onClick = {
                showProfileMenu = false
                onLogoutClick()
              }
            )
          }
        }
      }

      // 1px bottom canvas border
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(ScholaBorder)
      )
    }
  }
}

// Backward compatibility alias for RevenexTopBar
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
  ScholaTopAppBar(
    title = title,
    subtitle = subtitle,
    currentRole = currentRole,
    currentUserName = currentUserName,
    unreadCount = unreadCount,
    onSearchClick = onSearchClick,
    onNotificationClick = onNotificationClick,
    onProfileClick = onProfileClick,
    onLogoutClick = onLogoutClick,
    navigationIcon = navigationIcon
  )
}

// ============================================================================
// 2. WORKFLOW QUICK ACTION PILL
// ============================================================================
@Composable
fun WorkflowQuickPill(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isHighlighted: Boolean = false
) {
  val shape = RoundedCornerShape(Radius.pill)
  Surface(
    shape = shape,
    color = if (isHighlighted) ScholaTerracotta else ScholaSurface,
    modifier = modifier
      .clip(shape)
      .border(1.dp, if (isHighlighted) ScholaTerracotta else ScholaBorder, shape)
      .clickable(onClick = onClick)
      .testTag("workflow_pill_${title.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = Spacing.s4, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isHighlighted) Color.White else ScholaTerracotta,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(Spacing.s2))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isHighlighted) Color.White else ScholaTextPrimary
      )
    }
  }
}

// ============================================================================
// 3. SECTION HEADER
// ============================================================================
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
      .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = ScholaTextPrimary
    )
    if (actionText != null && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        contentPadding = PaddingValues(horizontal = Spacing.s2, vertical = Spacing.s1)
      ) {
        Text(
          text = actionText,
          style = MaterialTheme.typography.labelMedium,
          color = ScholaTerracotta,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// ============================================================================
// 4. STATUS BADGE (Delegating to ScholaPillBadge)
// ============================================================================
@Composable
fun StatusBadge(
  status: String,
  type: String = "general",
  modifier: Modifier = Modifier
) {
  ScholaPillBadge(status = status, modifier = modifier)
}

// ============================================================================
// 5. QUICK ACTION BUTTON & STAT CARD WRAPPERS
// ============================================================================
@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  iconTint: Color = ScholaTerracotta,
  backgroundColor: Color = ScholaTerracottaContainer,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  WorkflowQuickPill(
    title = title,
    icon = icon,
    onClick = onClick,
    modifier = modifier
  )
}

@Composable
fun StatCard(
  title: String,
  value: String,
  sublabel: String,
  icon: ImageVector,
  iconTint: Color = ScholaTerracotta,
  iconBackground: Color = ScholaTerracottaContainer,
  modifier: Modifier = Modifier,
  trendText: String? = null,
  isPositiveTrend: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  CompactStatTile(
    categoryLabel = title,
    statCounter = value,
    icon = icon,
    iconTint = iconTint,
    iconContainerColor = iconBackground,
    deltaText = trendText,
    isPositiveDelta = isPositiveTrend,
    modifier = modifier,
    onClick = onClick
  )
}

// ============================================================================
// 6. ANIMATED BAR CHART & DONUT CHART
// ============================================================================
@Composable
fun SimpleBarChart(
  title: String,
  data: List<Pair<String, Float>>,
  modifier: Modifier = Modifier,
  barColor: Color = ScholaTerracotta,
  onBarClick: ((String) -> Unit)? = null
) {
  val progress = remember { Animatable(0f) }
  LaunchedEffect(data) {
    progress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
  }

  AppCard(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = ScholaTextPrimary
    )
    Spacer(modifier = Modifier.height(Spacing.s3))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(96.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.Bottom
    ) {
      data.forEach { (label, value) ->
        val barFraction = ((value / 100f).coerceIn(0f, 1f) * progress.value).coerceAtLeast(0.06f)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Bottom,
          modifier = Modifier
            .weight(1f)
            .then(if (onBarClick != null) Modifier.clickable { onBarClick(label) } else Modifier)
        ) {
          Text(
            text = "${value.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ScholaMuted,
            fontSize = 9.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Box(
            modifier = Modifier
              .width(20.dp)
              .fillMaxHeight(fraction = barFraction)
              .clip(RoundedCornerShape(topStart = Radius.xs, topEnd = Radius.xs))
              .background(barColor)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ScholaTextSecondary,
            fontSize = 10.sp,
            maxLines = 1
          )
        }
      }
    }
  }
}

@Composable
fun FeeCollectionDonutChart(
  collectedAmount: Long,
  totalAmount: Long,
  modifier: Modifier = Modifier
) {
  val fraction = if (totalAmount > 0) (collectedAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f) else 0.85f
  val animFraction = remember { Animatable(0f) }
  LaunchedEffect(fraction) {
    animFraction.animateTo(fraction, tween(900, easing = FastOutSlowInEasing))
  }

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "COLLECTION REALIZATION",
        color = ScholaOnyxMuted,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = TypeTokens.trackingMicroLabel
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${(fraction * 100).toInt()}% Realized",
        color = ScholaGold,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "$${collectedAmount / 100000L}k of $${totalAmount / 100000L}k target collected",
        color = ScholaOnyxMuted,
        style = MaterialTheme.typography.bodySmall
      )
    }

    Box(contentAlignment = Alignment.Center) {
      CircularProgressIndicator(
        progress = { animFraction.value },
        modifier = Modifier.size(64.dp),
        color = ScholaTerracotta,
        trackColor = ScholaOnyxBorder,
        strokeWidth = 7.dp
      )
      Text(
        text = "${(fraction * 100).toInt()}%",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

// ============================================================================
// 7. EMPTY STATE & SUCCESS CHECKMARK
// ============================================================================
@Composable
fun EmptyStateView(
  title: String,
  message: String = "",
  description: String = message,
  icon: ImageVector = Icons.Outlined.Inbox,
  actionLabel: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(Spacing.xxl32),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .background(ScholaBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = ScholaTerracotta,
        modifier = Modifier.size(28.dp)
      )
    }
    Spacer(modifier = Modifier.height(Spacing.s3))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = ScholaTextPrimary,
      textAlign = TextAlign.Center
    )
    if (description.isNotBlank()) {
      Spacer(modifier = Modifier.height(Spacing.s1))
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = ScholaMuted,
        textAlign = TextAlign.Center
      )
    }
    if (actionLabel != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(Spacing.s4))
      AppButton(text = actionLabel, onClick = onActionClick)
    }
  }
}

@Composable
fun SuccessCheckmarkView(
  message: String,
  modifier: Modifier = Modifier
) {
  val scale = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    scale.animateTo(1f, animationSpec = Motion.springSmooth())
  }

  Column(
    modifier = modifier.padding(Spacing.cardPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .scale(scale.value)
        .background(StatusSuccessBg, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = StatusSuccessText,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(Spacing.s3))
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = ScholaTextPrimary,
      textAlign = TextAlign.Center
    )
  }
}

// PDF Export Helper Stub
object PdfExportHelper {
  fun exportFeeReceiptPdf(
    context: android.content.Context,
    student: com.example.data.model.Student?,
    record: com.example.data.model.FeeRecord,
    transaction: com.example.data.model.FeePaymentTransaction?
  ) {
    android.widget.Toast.makeText(context, "Receipt exported for ${record.studentName} (ID: ${record.id})", android.widget.Toast.LENGTH_SHORT).show()
  }
}
