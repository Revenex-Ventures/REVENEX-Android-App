package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

// ============================================================================
// 1. FEATURE HERO CARD (Dark Onyx Surface, 22dp corners, 1px border)
// ============================================================================
@Composable
fun FeatureHeroCard(
  cycleTitle: String,
  categoryBadge: String,
  primaryStatistic: String,
  statisticLabel: String,
  secondaryKeyMetric: String,
  actionButtonText: String,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier,
  badgeBgColor: Color = ScholaGoldContainer,
  badgeTextColor: Color = ScholaGoldText,
  heroHighlightColor: Color = ScholaGoldLight,
  secondaryMetricLabel: String = "DAILY ATTENDANCE RATE"
) {
  val shape = RoundedCornerShape(Radius.hero)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(ScholaOnyxSurface, ScholaOnyx)
        )
      )
      .border(1.dp, ScholaOnyxBorder, shape)
      .padding(Spacing.cardPaddingLarge)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top header: Cycle title + Category badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = cycleTitle.uppercase(Locale.getDefault()),
          color = ScholaOnyxMuted,
          style = MaterialTheme.typography.labelSmall,
          letterSpacing = TypeTokens.trackingMicroLabel,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(Spacing.s2))
        Surface(
          shape = RoundedCornerShape(Radius.pill),
          color = badgeBgColor
        ) {
          Text(
            text = categoryBadge.uppercase(Locale.getDefault()),
            color = badgeTextColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = Spacing.s2, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // Main Content: Primary Statistic & Secondary Metric
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = statisticLabel.uppercase(Locale.getDefault()),
            color = ScholaOnyxMuted,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = primaryStatistic,
            color = ScholaOnyxText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = secondaryMetricLabel.uppercase(Locale.getDefault()),
            color = ScholaOnyxMuted,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = secondaryKeyMetric,
            color = heroHighlightColor,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // Pill Action Button with Trailing Arrow
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = ScholaTerracotta,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .clickable(onClick = onActionClick)
          .testTag("hero_action_pill")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s4, vertical = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = actionButtonText,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(Spacing.s2))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

// ============================================================================
// 2. COMPACT STAT TILES (White surface card, 20dp corners, 1px border)
// ============================================================================
@Composable
fun CompactStatTile(
  categoryLabel: String,
  statCounter: String,
  icon: ImageVector,
  iconTint: Color = ScholaTerracotta,
  iconContainerColor: Color = ScholaTerracottaContainer,
  deltaText: String? = null,
  isPositiveDelta: Boolean = true,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(Radius.tile)
  val baseModifier = if (onClick != null) {
    modifier.clickable(onClick = onClick)
  } else {
    modifier
  }

  Box(
    modifier = baseModifier
      .clip(shape)
      .background(ScholaSurface)
      .border(1.dp, ScholaBorder, shape)
      .padding(Spacing.cardPadding)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top row: 38dp circular icon container + delta percentage chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(iconContainerColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }

        if (deltaText != null) {
          Surface(
            shape = RoundedCornerShape(Radius.pill),
            color = if (isPositiveDelta) StatusSuccessBg else StatusDangerBg
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isPositiveDelta) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isPositiveDelta) StatusSuccessText else StatusDangerText,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = deltaText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositiveDelta) StatusSuccessText else StatusDangerText,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s3))

      // Category uppercase tracked label
      Text(
        text = categoryLabel.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelSmall,
        color = ScholaMuted,
        letterSpacing = TypeTokens.trackingMicroLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(2.dp))

      // Bold stat counter
      Text(
        text = statCounter,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = ScholaTextPrimary
      )
    }
  }
}

// ============================================================================
// 3. FLOATING DOCK (Mobile: 64dp height, 10dp elevation, dark onyx pill)
// ============================================================================
@Composable
fun ScholaFloatingDock(
  items: List<ScholaDockItem>,
  currentRoute: String?,
  onItemSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val dockShape = RoundedCornerShape(Radius.pill)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
      .navigationBarsPadding(),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .height(64.dp)
        .shadow(Elev.floatingDock, dockShape, clip = false)
        .clip(dockShape)
        .border(1.dp, ScholaOnyxBorder, dockShape),
      color = ScholaOnyx,
      shape = dockShape
    ) {
      Row(
        modifier = Modifier
          .fillMaxHeight()
          .padding(horizontal = Spacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        items.forEach { item ->
          val isSelected = currentRoute == item.route

          val animatedBgColor by animateColorAsState(
            targetValue = if (isSelected) ScholaTerracotta else Color.Transparent,
            animationSpec = tween(Motion.normal),
            label = "dockActiveBg"
          )
          val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) Color.White else ScholaOnyxMuted,
            animationSpec = tween(Motion.normal),
            label = "dockActiveContent"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(0.82f)
              .clip(RoundedCornerShape(Radius.pill))
              .background(animatedBgColor)
              .clickable { onItemSelected(item.route) }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("dock_tab_${item.route}"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = animatedContentColor,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = item.label,
                color = animatedContentColor,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }
}

data class ScholaDockItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

// ============================================================================
// 4. FORM & INPUT FIELDS (14-16dp corners, 1px border, leading icon, clear btn)
// ============================================================================
@Composable
fun ScholaInputField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "",
  placeholder: String = "",
  leadingIcon: ImageVector? = null,
  keyboardType: KeyboardType = KeyboardType.Text,
  singleLine: Boolean = true,
  supportingText: String = "",
  onClear: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(Radius.input)

  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    label = if (label.isNotEmpty()) { { Text(label) } } else null,
    placeholder = if (placeholder.isNotEmpty()) {
      { Text(placeholder, color = ScholaMuted) }
    } else null,
    leadingIcon = leadingIcon?.let {
      { Icon(it, contentDescription = null, tint = ScholaMuted, modifier = Modifier.size(20.dp)) }
    },
    trailingIcon = {
      if (value.isNotEmpty() && onClear != null) {
        IconButton(onClick = onClear) {
          Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = ScholaMuted, modifier = Modifier.size(18.dp))
        }
      }
    },
    singleLine = singleLine,
    supportingText = if (supportingText.isNotEmpty()) { { Text(supportingText) } } else null,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = shape,
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = ScholaSurface,
      unfocusedContainerColor = ScholaSurface,
      focusedBorderColor = ScholaTerracotta,
      unfocusedBorderColor = ScholaBorder,
      focusedLabelColor = ScholaTerracotta,
      unfocusedLabelColor = ScholaMuted,
      focusedTextColor = ScholaTextPrimary,
      unfocusedTextColor = ScholaTextPrimary
    )
  )
}

// ============================================================================
// 5. STATUS PILL BADGES (High-Contrast Background & Text Pairs)
// ============================================================================
@Composable
fun ScholaPillBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (status.uppercase(Locale.getDefault()).trim()) {
    "PRESENT", "PAID", "APPROVED", "OPERATIONAL", "ACTIVE", "SUCCESS", "SUBMITTED" ->
      StatusSuccessBg to StatusSuccessText
    "LATE", "PARTIAL", "PARTIALLY PAID", "PENDING", "UNDER MAINTENANCE", "WARNING" ->
      StatusWarningBg to StatusWarningText
    "ABSENT", "OVERDUE", "REJECTED", "DANGER", "ERROR", "NEEDS REPLACEMENT" ->
      StatusDangerBg to StatusDangerText
    "EXCUSED", "SCHEDULED", "ON LEAVE", "NEUTRAL", "CIRCULAR", "UPCOMING", "INFO" ->
      StatusNeutralBg to StatusNeutralText
    else -> ScholaSlateContainer to ScholaSlateNavy
  }

  Surface(
    shape = RoundedCornerShape(Radius.pill),
    color = bgColor,
    modifier = modifier
  ) {
    Text(
      text = status.uppercase(Locale.getDefault()),
      color = textColor,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.6.sp,
      modifier = Modifier.padding(horizontal = Spacing.s2, vertical = 3.dp)
    )
  }
}

// ============================================================================
// 6. ANIMATED VITAL RING & PROGRESS RING
// ============================================================================
enum class VitalRingSize(val dpSize: Dp, val strokeDp: Dp) {
  SMALL(48.dp, 4.dp),
  MEDIUM(64.dp, 6.dp),
  LARGE(88.dp, 8.dp),
  HERO(120.dp, 10.dp)
}

@Composable
fun VitalRing(
  value: Float, // 0..100
  label: String = "ATTENDANCE",
  size: VitalRingSize = VitalRingSize.MEDIUM,
  gradientStart: Color = ScholaTerracotta,
  gradientEnd: Color = ScholaGold,
  trackColor: Color = ScholaBorder,
  textColor: Color = Color.White,
  modifier: Modifier = Modifier
) {
  val progress = remember { Animatable(0f) }
  LaunchedEffect(value) {
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = (value / 100f).coerceIn(0f, 1f),
      animationSpec = tween(900, easing = FastOutSlowInEasing)
    )
  }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      progress = { progress.value },
      modifier = Modifier.size(size.dpSize),
      color = gradientStart,
      trackColor = trackColor,
      strokeWidth = size.strokeDp
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "${value.toInt()}%",
        style = if (size == VitalRingSize.HERO) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
        color = textColor,
        fontWeight = FontWeight.Bold
      )
      if (label.isNotBlank() && size != VitalRingSize.SMALL) {
        Text(
          text = label.uppercase(Locale.getDefault()),
          color = textColor.copy(alpha = 0.7f),
          style = MaterialTheme.typography.labelSmall,
          fontSize = 8.sp,
          letterSpacing = 0.4.sp
        )
      }
    }
  }
}

@Composable
fun SimpleProgressRing(
  progress: Float,
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  strokeWidth: Dp = 5.dp,
  color: Color = ScholaTerracotta,
  trackColor: Color = ScholaBorder
) {
  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(progress) {
    animProgress.animateTo(progress.coerceIn(0f, 1f), tween(800, easing = FastOutSlowInEasing))
  }
  CircularProgressIndicator(
    progress = { animProgress.value },
    modifier = modifier.size(size),
    color = color,
    trackColor = trackColor,
    strokeWidth = strokeWidth
  )
}

// ============================================================================
// 7. COUNT-UP NUMBER & CURRENCY
// ============================================================================
@Composable
fun CountUpNumber(
  value: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.titleLarge,
  color: Color = ScholaTextPrimary
) {
  val parsed = remember(value) {
    value.replace(",", "").toDoubleOrNull()?.toFloat()
      ?: Regex("""\d+(\.\d+)?""").find(value)?.value?.toFloatOrNull()
  }
  var display by remember { mutableStateOf(value) }

  LaunchedEffect(value) {
    if (parsed != null) {
      val anim = Animatable(0f)
      anim.animateTo(parsed, tween(800, easing = FastOutSlowInEasing)) {
        display = if (this.value % 1.0f == 0f) {
          this.value.toInt().toString()
        } else {
          String.format(Locale.US, "%.1f", this.value)
        }
      }
    } else {
      display = value
    }
  }

  Text(
    text = display,
    style = style,
    color = color,
    fontWeight = FontWeight.Bold,
    modifier = modifier
  )
}

@Composable
fun CountUpCurrency(
  amountPaise: Long,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.titleLarge,
  color: Color = ScholaTextPrimary,
  currencySymbol: String = "$"
) {
  val dollars = amountPaise / 100.0
  var display by remember { mutableStateOf("${currencySymbol}0") }

  LaunchedEffect(amountPaise) {
    val anim = Animatable(0f)
    anim.animateTo(dollars.toFloat(), tween(800, easing = FastOutSlowInEasing)) {
      display = "$currencySymbol${String.format(Locale.US, "%,.0f", this.value)}"
    }
  }

  Text(
    text = display,
    style = style,
    color = color,
    fontWeight = FontWeight.Bold,
    modifier = modifier
  )
}

@Composable
fun RevenexLogoCrest(
  modifier: Modifier = Modifier,
  size: Dp = 64.dp,
  tint: Color = ScholaGold,
  backgroundColor: Color = ScholaSlateNavy
) {
  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(Radius.md))
      .background(backgroundColor),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Default.AccountBalance,
      contentDescription = "Schola Crest",
      tint = tint,
      modifier = Modifier.size(size * 0.6f)
    )
  }
}

@Composable
fun ScholaLogoCrest(
  modifier: Modifier = Modifier,
  size: Dp = 64.dp,
  tint: Color = ScholaGold,
  backgroundColor: Color = ScholaSlateNavy
) {
  RevenexLogoCrest(modifier = modifier, size = size, tint = tint, backgroundColor = backgroundColor)
}


// ============================================================================
// 8. APP BUTTON, CARD, AVATAR, & SKELETON
// ============================================================================
@Composable
fun AppButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  containerColor: Color = ScholaTerracotta,
  contentColor: Color = Color.White,
  minHeight: Dp = 48.dp,
  leadingIcon: ImageVector? = null,
  fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
  fontWeight: FontWeight = FontWeight.Bold
) {
  val haptics = LocalHapticFeedback.current
  var pressed by remember { mutableStateOf(false) }

  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.97f else 1f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
    label = "btnScale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(Radius.lg))
      .background(if (enabled) containerColor else ScholaSlateContainer)
      .pointerInput(enabled) {
        detectTapGestures(
          onPress = {
            if (enabled) pressed = true
            tryAwaitRelease()
            pressed = false
          },
          onTap = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
          }
        )
      }
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = minHeight)
        .padding(horizontal = Spacing.xl20),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (leadingIcon != null) {
        Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(Spacing.s2))
      }
      Text(
        text = text,
        color = if (enabled) contentColor else ScholaMuted,
        fontSize = fontSize,
        fontWeight = fontWeight
      )
    }
  }
}

@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  containerColor: Color = ScholaSurface,
  elevation: Dp = Elev.e0,
  shape: RoundedCornerShape = RoundedCornerShape(Radius.lg),
  content: @Composable ColumnScope.() -> Unit
) {
  val base = Modifier
    .clip(shape)
    .then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
  Box(
    modifier = modifier
      .then(base)
      .shadow(elevation, shape, clip = false)
      .clip(shape)
      .background(containerColor)
      .border(1.dp, ScholaBorder, shape)
      .padding(Spacing.cardPadding)
  ) {
    Column(modifier = Modifier.fillMaxWidth(), content = content)
  }
}

@Composable
fun InfoCard(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null
) {
  AppCard(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
          Icon(icon, contentDescription = null, tint = ScholaTerracotta, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(Spacing.s2))
        }
        Text(title, style = MaterialTheme.typography.bodyMedium, color = ScholaMuted)
      }
      Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
    }
  }
}

@Composable
fun Avatar(
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  background: Color = ScholaTerracotta,
  textColor: Color = Color.White
) {
  val initials = name.trim().split(" ").filter { it.isNotBlank() }
    .mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase(Locale.getDefault())
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(background),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials.ifBlank { "?" },
      color = textColor,
      fontSize = (size.value * 0.36f).sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun SkeletonShimmer(
  modifier: Modifier = Modifier,
  baseColor: Color = ScholaBorder,
  highlightColor: Color = ScholaSurfaceWarm,
  cornerRadius: Dp = Radius.md
) {
  val transition = rememberInfiniteTransition(label = "skeleton")
  val offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
    label = "skeletonOffset"
  )
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(cornerRadius))
      .background(
        Brush.linearGradient(
          colors = listOf(baseColor, highlightColor, baseColor),
          start = androidx.compose.ui.geometry.Offset(offset - 500f, 0f),
          end = androidx.compose.ui.geometry.Offset(offset + 500f, 0f)
        )
      )
  )
}

@Composable
fun AnimatedFadeIn(
  delayMillis: Int = 0,
  content: @Composable () -> Unit
) {
  var visible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(delayMillis.toLong())
    visible = true
  }
  androidx.compose.animation.AnimatedVisibility(
    visible = visible,
    enter = androidx.compose.animation.fadeIn(tween(300)) + androidx.compose.animation.slideInVertically(
      initialOffsetY = { 20 },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  ) {
    content()
  }
}

@Composable
fun AnimatedEmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  retryText: String? = null,
  onRetry: (() -> Unit)? = null,
  accent: Color = ScholaTerracotta
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(Spacing.xxl32),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(accent.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(36.dp))
    }
    Spacer(modifier = Modifier.height(Spacing.s4))
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(Spacing.s2))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = ScholaMuted,
      textAlign = TextAlign.Center
    )
    if (retryText != null && onRetry != null) {
      Spacer(modifier = Modifier.height(Spacing.s4))
      AppButton(text = retryText, onClick = onRetry)
    }
  }
}

// Backward compatibility alias for Badge -> ScholaPillBadge
@Composable
fun Badge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = ScholaTerracotta
) {
  ScholaPillBadge(status = text, modifier = modifier)
}

@Composable
fun rememberReducedMotion(): Boolean = false