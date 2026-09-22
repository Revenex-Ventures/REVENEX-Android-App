package com.example.ui.components

import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================================
// 0. GLASS MORPHISM UTILITIES
// ============================================================================

fun Modifier.glassEffect(
  cornerRadius: Dp = 24.dp,
  borderAlpha: Float = 0.45f,
  dark: Boolean = false
): Modifier = this.then(Modifier)
  .background(
    color = if (dark) InkBlack.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.62f),
    shape = RoundedCornerShape(cornerRadius)
  )
  .border(
    width = 0.8.dp,
    color = Color.White.copy(alpha = if (dark) 0.18f else borderAlpha),
    shape = RoundedCornerShape(cornerRadius)
  )
  .shadow(
    elevation = 10.dp,
    shape = RoundedCornerShape(cornerRadius),
    clip = false,
    ambientColor = InkBlack.copy(alpha = 0.10f),
    spotColor = InkBlack.copy(alpha = 0.08f)
  )

@Composable
fun GlassBackground(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier.fillMaxSize()) {
    // Clean flat canvas — no gradient blobs, no glass artifacts
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(ScholaLinen)
    )
    content()
  }
}

val ScholaBgBrush = Brush.linearGradient(
  colors = listOf(
    ScholaTerracotta.copy(alpha = 0.05f),
    ScholaGoldLight.copy(alpha = 0.04f),
    Color.White
  )
)

// ============================================================================
// 1. FEATURE HERO CARD (Glass surface, 24dp corners, frosted border)
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
  badgeBgColor: Color = ScholaSlateContainer,
  badgeTextColor: Color = ScholaSlateNavy,
  heroHighlightColor: Color = ScholaTerracotta,
  secondaryMetricLabel: String = "DAILY ATTENDANCE RATE"
) {
  val shape = RoundedCornerShape(Radius.hero)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(ScholaSlateNavyDark, shape)
      .drawBehind {
        // 1. Charcoal-to-ink rich gradient base
        drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFF261D18),
              ScholaSlateNavy,
              InkBlack
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
          )
        )
        // 2. Ember warm radial under-glow
        val gw = size.width
        val glowCenter = Offset(gw * 0.85f, size.height * 0.20f)
        val glowRadius = gw * 0.9f
        drawCircle(
          brush = Brush.radialGradient(
            0f to ScholaTerracotta.copy(alpha = 0.12f),
            0.5f to ScholaTerracotta.copy(alpha = 0.03f),
            1f to Color.Transparent,
            center = glowCenter,
            radius = glowRadius
          ),
          radius = glowRadius,
          center = glowCenter
        )
        // 3. Top-edge terracotta crown highlight
        drawRoundRect(
          brush = Brush.horizontalGradient(
            colors = listOf(
              ScholaTerracotta,
              ScholaTerracottaLight.copy(alpha = 0.6f),
              Color.Transparent
            )
          ),
          topLeft = Offset(0f, 0f),
          size = Size(gw * 0.75f, 2.5f * density),
          cornerRadius = CornerRadius(1.5f * density, 1.5f * density)
        )
        // 4. Subtle telemetry dots in top right
        val dotRadius = 1.2f * density
        val dotSpacing = 10f * density
        val startX = gw - 50f * density
        val startY = 16f * density
        for (r in 0 until 3) {
          for (c in 0 until 3) {
            drawCircle(
              color = Color.White.copy(alpha = 0.05f),
              radius = dotRadius,
              center = Offset(startX + c * dotSpacing, startY + r * dotSpacing)
            )
          }
        }
      }
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          0f to Color.White.copy(alpha = 0.24f),
          0.5f to Color.White.copy(alpha = 0.08f),
          1f to Color.White.copy(alpha = 0.14f)
        ),
        shape = shape
      )
      .shadow(16.dp, shape, clip = false)
      .padding(Spacing.cardPaddingLarge)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top header: Cycle title + Category badge with live indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(ScholaTerracotta, CircleShape)
          )
          Spacer(modifier = Modifier.width(Spacing.s2))
          Text(
            text = cycleTitle.uppercase(Locale.getDefault()),
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(modifier = Modifier.width(Spacing.s2))
        Surface(
          shape = RoundedCornerShape(Radius.pill),
          color = badgeBgColor,
          border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = Spacing.s2, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF10B981), CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = categoryBadge.uppercase(Locale.getDefault()),
              color = badgeTextColor,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.6.sp
            )
          }
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
            color = Color.White.copy(alpha = 0.70f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Spacer(modifier = Modifier.height(3.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = primaryStatistic,
              color = Color.White,
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Bold
            )
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = Color(0xFF10B981).copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.35f))
            ) {
              Text(
                text = "ON TRACK",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        // Secondary metric capsule
        Surface(
          shape = RoundedCornerShape(Radius.md),
          color = Color.White.copy(alpha = 0.08f),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.12f))
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = secondaryMetricLabel.uppercase(Locale.getDefault()),
              color = Color.White.copy(alpha = 0.65f),
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = secondaryKeyMetric,
              color = ScholaTerracottaLight,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // Pill Action Button with Trailing Arrow (tactile gradient, styled arrow)
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = Color.Transparent,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .background(
            Brush.horizontalGradient(
              listOf(
                Color(0xFF1F1E24),
                ScholaSlateNavyDark
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.08f)
              )
            ),
            shape = RoundedCornerShape(Radius.pill)
          )
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
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.width(Spacing.s2))
          Box(
            modifier = Modifier
              .size(24.dp)
              .background(ScholaTerracotta.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = null,
              tint = ScholaTerracottaLight,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}

// ============================================================================
// 2. COMPACT STAT TILES (White surface, 20dp corners, hairline border)
// ============================================================================
@Composable
fun CompactStatTile(
  categoryLabel: String,
  statCounter: String,
  icon: ImageVector,
  iconTint: Color = ScholaSlateNavy,
  iconContainerColor: Color = ScholaOnyx,
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
      .glassEffect(cornerRadius = Radius.tile)
      .padding(Spacing.cardPadding)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top row: monochrome icon well + delta chip
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
                fontWeight = FontWeight.Medium,
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
// 3. BOTTOM NAVIGATION BAR (Ink black, solid, jump-up icon animation)
// ============================================================================

@Composable
fun ScholaFloatingDock(
  items: List<ScholaDockItem>,
  currentRoute: String?,
  onItemSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val barShape = RoundedCornerShape(
    topStart = Radius.md,
    topEnd = Radius.md,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
  )

  val slideY = remember { Animatable(90f) }
  LaunchedEffect(Unit) {
    slideY.animateTo(
      targetValue = 0f,
      animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )
  }

  val scope = rememberCoroutineScope()
  val selectedIndex = items.indexOfFirst { currentRoute == it.route }
    .coerceAtLeast(0)
  var pendingIndex by remember { mutableIntStateOf(selectedIndex) }
  var navJob by remember { mutableStateOf<Job?>(null) }
  val slideIndex = remember { Animatable(selectedIndex.toFloat()) }
  val iconPop = remember { Animatable(0f) }
  val clickTone = remember { DockClick() }
  val haptics = LocalHapticFeedback.current
  DisposableEffect(Unit) {
    onDispose { clickTone.release() }
  }

  LaunchedEffect(currentRoute) {
    pendingIndex = selectedIndex
    slideIndex.snapTo(selectedIndex.toFloat())
  }

  fun select(item: ScholaDockItem, index: Int) {
    if (index == pendingIndex) return
    navJob?.cancel()
    pendingIndex = index
    try {
      clickTone.play()
      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } catch (_: Exception) {
    }
    navJob = scope.launch {
      iconPop.snapTo(20f)
      launch {
        iconPop.animateTo(
          targetValue = 0f,
          animationSpec = spring(dampingRatio = 0.38f, stiffness = 700f)
        )
      }
      slideIndex.animateTo(
        targetValue = index.toFloat(),
        animationSpec = spring(
          dampingRatio = 0.46f,
          stiffness = 760f
        )
      )
      onItemSelected(item.route)
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(72.dp)
      .graphicsLayer { translationY = slideY.value.dp.toPx() }
      .testTag("bottom_nav_bar")
  ) {
    val itemWidth = maxWidth / items.size
    val circleX = (itemWidth * slideIndex.value) + (itemWidth - 50.dp) / 2

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .shadow(Elev.e3, barShape, clip = false)
        .background(InkBlack, barShape)
    )

    Box(
        modifier = Modifier
          .size(50.dp)
          .graphicsLayer {
            translationX = circleX.toPx()
            translationY = (-24).dp.toPx()
          }
      ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape)
          .background(ScholaLinen)
          .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
      )
      Box(
        modifier = Modifier
          .size(46.dp)
          .align(Alignment.Center)
          .clip(CircleShape)
          .background(InkBlack)
          .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        val ridingItem = items[pendingIndex]
        Icon(
          imageVector = ridingItem.selectedIcon,
          contentDescription = ridingItem.label,
          tint = ScholaTerracotta,
          modifier = Modifier
            .size(24.dp)
            .graphicsLayer { translationY = iconPop.value.dp.toPx() }
        )
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(horizontal = Spacing.s2),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      items.forEachIndexed { index, item ->
        val isSelected = index == pendingIndex

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { select(item, index) }
            .testTag("dock_tab_${item.route}"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.label,
              tint = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(24.dp)
            )
          }
          Text(
            text = item.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = if (isSelected) 10.sp else 9.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.offset(y = (-16).dp)
          )
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

private class DockClick {
  private val sampleRate = 44100
  private val track: AudioTrack

  init {
    val duration = 0.008f
    val total = (sampleRate * duration).toInt()
    val pcm = ShortArray(total)
    for (i in 0 until total) {
      val t = i.toFloat() / sampleRate
      val decay = exp(-t * 500f)
      val body = sin(2.0 * Math.PI.toFloat() * 1200.0f * t)
      pcm[i] = ((body * decay) * 0.20f * Short.MAX_VALUE).toInt().toShort()
    }
    track = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build()
      )
      .setBufferSizeInBytes(pcm.size * 2)
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()
    track.write(pcm, 0, pcm.size)
    track.reloadStaticData()
  }

  fun play() {
    try {
      track.stop()
      track.reloadStaticData()
      track.play()
    } catch (_: Exception) {
    }
  }

  fun release() {
    try {
      track.stop()
      track.release()
    } catch (_: Exception) {
    }
  }
}

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
  INLINE(72.dp, 6.dp),
  MEDIUM(64.dp, 6.dp),
  LARGE(88.dp, 8.dp),
  HERO(120.dp, 10.dp)
}

// Attendance ring with the person's profile picture inside it and the %
// as a small pendant badge hanging on the bottom arc of the ring.
@Composable
fun AttendanceBead(
  value: Float, // 0..100
  avatarLabel: String,
  modifier: Modifier = Modifier,
  avatarUrl: String = "",
  tint: Color = ScholaTerracotta
) {
  val size = VitalRingSize.INLINE.dpSize
  val strokeDp = VitalRingSize.INLINE.strokeDp
  val fraction = (value / 100f).coerceIn(0f, 1f)

  val ringProgress = remember { Animatable(0f) }
  val runningPct = remember(value) { Animatable(0f) }
  LaunchedEffect(value) {
    ringProgress.snapTo(0f)
    ringProgress.animateTo(fraction, animationSpec = tween(900, easing = FastOutSlowInEasing))
  }
  LaunchedEffect(value) {
    runningPct.snapTo(0f)
    runningPct.animateTo(value, animationSpec = tween(900, easing = FastOutSlowInEasing))
  }
  val badgeScale by animateFloatAsState(
    targetValue = if (runningPct.value > 0f) 1f else 0.6f,
    animationSpec = tween(350, easing = FastOutSlowInEasing),
    label = "beadBadgeScale"
  )

  Box(modifier = modifier.size(size)) {
    CircularProgressIndicator(
      progress = { ringProgress.value },
      modifier = Modifier.fillMaxSize(),
      color = tint,
      trackColor = Color.White.copy(alpha = 0.18f),
      strokeWidth = strokeDp
    )
    Avatar(
      name = avatarLabel,
      modifier = Modifier
        .align(Alignment.Center)
        .size(40.dp),
      background = Color.White,
      textColor = ScholaSlateNavyDark,
      imagePath = avatarUrl.ifBlank { null }
    )
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 3.dp)
        .clip(RoundedCornerShape(9.dp))
        .background(ScholaSlateNavyDark)
        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
        .padding(horizontal = 7.dp, vertical = 2.dp)
        .graphicsLayer { scaleX = badgeScale; scaleY = badgeScale }
    ) {
      Text(
        text = "${runningPct.value.roundToInt()}%",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
    }
  }
}

@Composable
fun VitalRing(
  value: Float, // 0..100
  label: String = "ATTENDANCE",
  size: VitalRingSize = VitalRingSize.MEDIUM,
  gradientStart: Color = ScholaTerracotta,
  gradientEnd: Color = ScholaTerracottaLight,
  trackColor: Color = ScholaOnyxBorder,
  textColor: Color = ScholaTextPrimary,
  labelAlpha: Float = 1f,
  animate: Boolean = true,
  modifier: Modifier = Modifier
) {
  val progress = remember { Animatable(0f) }
  LaunchedEffect(value, animate) {
    if (!animate) {
      progress.snapTo((value / 100f).coerceIn(0f, 1f))
      return@LaunchedEffect
    }
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
          color = textColor.copy(alpha = 0.7f * labelAlpha),
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
// 6a. ANIMATED ATTENDANCE HERO (shared by role dashboards)
//     Structured identity card: inline attendance ring + identity header +
//     a footer stat row, with a soft scale/fade entrance.
// ============================================================================
@Composable
fun AnimatedAttendanceHero(
  attendanceValue: Float,
  label: String,
  modifier: Modifier = Modifier,
  containerColor: Color = ScholaSlateNavyDark,
  avatarLabel: String = "",
  avatarUrl: String = "",
  introHasPlayed: Boolean = false,
  onIntroPassed: () -> Unit = {},
  nameContent: @Composable RowScope.() -> Unit,
  trailingContent: @Composable () -> Unit,
  statsContent: @Composable () -> Unit
) {
  val safeAttendance = attendanceValue.coerceIn(0f, 100f)

  var entered by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(60)
    entered = true
  }
  // Entrance is a fade ONLY. No scale or translation on the card: the fly
  // offset is computed in the card's local frame and applied inside it, so the
  // card's own layer must add zero positional transform to stay exact.
  val heroAlpha by animateFloatAsState(
    targetValue = if (entered) 1f else 0f,
    animationSpec = tween(440, easing = FastOutSlowInEasing),
    label = "heroAlpha"
  )

  // Choreography: the attendance ring first shows up LARGE dead-centre of the
  // whole card (with a small caption under it) while its ring fills and the %
  // counts up. Once the attendance has been shown, the ring glides back into
  // its slot next to the name and the rest of the card content reveals with it.
  //
// The travel and the scale are split across TWO layers. graphicsLayer applies
  // its own translation in space that its own scale has already shrunk —
  // coupling them made the ring land off-centre. Here the outer layer only
  // translates (unscaled => exact offset), the inner layer only scales about
  // its own centre, so the ring's centre never moves while it shrinks. We
  // capture both nodes exactly once, while untranslated.
  // The caller hoists `introHasPlayed`, so the entrance plays on every fresh
  // visit (cold start OR returning to this tab) but never replays while the
  // hero item is recycled by the lazy list.
// The fly offset is measured in ROOT coordinates via boundsInRoot, then
  // normalised by the scale ratio (local card size / root card size). On a
  // cold start that ratio is exactly 1.0 — identical pixels to the verified
  // first-visit centering. Under a nav transition the root measurement is
  // scaled/translated, but the RATIO cancels that scale, so the offset stays
  // exact whether the screen is entering fresh or mid-transition.
  var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var cardRect by remember { mutableStateOf<Rect?>(null) }
  var beadRect by remember { mutableStateOf<Rect?>(null) }
  val measured = cardCoords != null && cardRect != null && beadRect != null

  val haptics = LocalHapticFeedback.current
  // Bouncy spring (dampingRatio < 1) gives the settle a natural overshoot: the
  // ring glides to the slot, dips slightly past it, then eases into place.
  val fly = remember { Animatable(0f) }
  // Keyed ONLY on `measured` — reading `introHasPlayed` once at launch. If we
  // keyed on the flag too, flipping it onIntroPassed would cancel this
  // coroutine mid-flight and the intro would never play.
  LaunchedEffect(measured) {
    if (measured && !introHasPlayed) {
      onIntroPassed()
      delay(1150)
      fly.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.66f, stiffness = Spring.StiffnessMediumLow)
      )
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    } else if (measured) {
      fly.snapTo(1f)
    }
  }
  val beadAlpha by animateFloatAsState(
    targetValue = if (measured) 1f else 0f,
    animationSpec = tween(200, easing = FastOutSlowInEasing),
    label = "beadAlpha"
  )

  val eased = fly.value // spring output — overshoots past 1, then settles
  val reveal = eased.coerceIn(0f, 1f)
  val nameReveal = (eased / 0.75f).coerceIn(0f, 1f)
  val statsReveal = ((eased - 0.12f) / 0.88f).coerceIn(0f, 1f)
  // "Stage" alpha: strongest while the ring is held centred, dead by settle.
  val stageAlpha = (1f - reveal) * 0.5f
  val beadScale = 1.35f + (1f - 1.35f) * eased
  val contentOffsetY = (1f - reveal) * 10f

  val cc = cardCoords
  val cr = cardRect
  val br = beadRect
  // Convert a root-space delta into local px: divide by the transform factor
  // the root measurement is already subject to (cardRootW / cardLocalW).
  val flyOffsetX: Float
  val flyOffsetY: Float
  if (measured && cc != null && cr != null && br != null) {
    val xFactor = cc.size.width / cr.width
    val yFactor = cc.size.height / cr.height
    flyOffsetX = (cr.center.x - br.center.x) * xFactor * (1f - eased)
    flyOffsetY = (cr.center.y - br.center.y) * yFactor * (1f - eased)
  } else {
    flyOffsetX = 0f
    flyOffsetY = 0f
  }
  // Caption sits just under the centred ring, which is at the card's centre.
  val captionOffsetY = ((cc?.size?.width ?: 0) / 2f) + 30f

  // -------------------------------------------------------------------------
  // Signature "carbon & ember" surface — the hero never reads flat black:
  //   1. Warm charcoal vertical wash (ember-tinted crown down to ink)
  //   2. Ember glow blooming in the top-right corner
  //   3. A "stage" ring that exists only while the attendance ring is held
  //      centred — it dissolves as the ring settles into its slot
  //   4. Terracotta hairline along the top edge
  // -------------------------------------------------------------------------
  val cardShape = RoundedCornerShape(Radius.hero)
  Box(
    modifier = modifier
      .clip(cardShape)
      .background(containerColor, cardShape)
      .drawBehind {
        // 1. Base — one continuous dark family (slate easing into ink). No warm jump
        // mid-surface, so the card holds together like a single material and
        // the warmth below blends INTO it rather than sitting as a separate
        // box/patch.
        drawRect(
          brush = Brush.verticalGradient(
            0f to ScholaSlateNavy,
            0.7f to ScholaSlateNavyDark,
            1f to InkBlack
          )
        )
        val gw = size.width
        // 2. Ember under-wash — terracotta alpha rising from the bottom edge
        // and fading upward as a LINEAR overlay, so heat mixes into the whole
        // card with no visible glow boundary.
        drawRect(
          brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.55f to ScholaTerracotta.copy(alpha = 0.03f),
            0.85f to ScholaTerracotta.copy(alpha = 0.07f),
            1f to ScholaTerracotta.copy(alpha = 0.085f)
          )
        )
        // 3. Top glass frost — lifts the upper edge so the card sits above the
        // page instead of printing flat.
        drawRect(
          brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.05f),
            0.3f to Color.White.copy(alpha = 0.015f),
            1f to Color.Transparent
          ),
          size = Size(size.width, size.height * 0.5f)
        )
        // 4. Stage ring — a platform under the centred attendance ring; fades
        // with `stageAlpha` (1 while held centred -> 0 once settled).
        if (stageAlpha > 0.001f) {
          val stageCenter = Offset(size.width * 0.5f, size.height * 0.5f)
          drawCircle(
            color = ScholaTerracotta.copy(alpha = 0.16f * stageAlpha),
            radius = size.width * 0.20f,
            center = stageCenter,
            style = Stroke(width = 1.5f * density)
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.07f * stageAlpha),
            radius = size.width * 0.27f,
            center = stageCenter,
            style = Stroke(width = 1f * density)
          )
        }
        // 8. Terracotta hairline along the top edge — fades in as the ring
        // settles into its slot (reveal), so no stray line before the animation.
        val crown = 0.25f + 0.75f * reveal
        drawRoundRect(
          brush = Brush.horizontalGradient(
            colors = listOf(
              ScholaTerracotta.copy(alpha = 1f * crown),
              ScholaTerracotta.copy(alpha = 0.35f * crown),
              Color.Transparent
            )
          ),
          topLeft = Offset(0f, 0f),
          size = Size(gw, 2.5f * density),
          cornerRadius = CornerRadius(1.5f * density, 1.5f * density)
        )
      }
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          0f to Color.White.copy(alpha = 0.28f),
          0.4f to Color.White.copy(alpha = 0.10f),
          1f to Color.White.copy(alpha = 0.14f)
        ),
        shape = cardShape
      )
      .shadow(18.dp, cardShape, clip = false)
      // Captured outside the entrance transform so the captured bounds are the
      // card's true layout position and size.
      .onGloballyPositioned {
        cardCoords = it
        cardRect = it.boundsInRoot()
      }
      .graphicsLayer {
        alpha = heroAlpha
      }
      .padding(horizontal = Spacing.xl20, vertical = Spacing.xl20)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {

      // ---------- Header: bead + identity + trailing ----------
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Travel wrapper: carries ONLY the fly (translation + fade). The scale
          // lives on an inner layer, so the two never compound and the ring
          // lands dead-centre on the card. CRITICAL: the measurement sits
          // OUTSIDE the fly transform — measuring inside it used to capture the
          // *already-translated* bounds and permanently poisoned the centring.
          // The root-space delta is normalised by the card scale ratio above.
          Box(
            modifier = Modifier
              .onGloballyPositioned { beadRect = it.boundsInRoot() }
              .graphicsLayer {
                translationX = flyOffsetX
                translationY = flyOffsetY
                alpha = beadAlpha
              }
          ) {
            Box(
              modifier = Modifier
                .graphicsLayer {
                  scaleX = beadScale
                  scaleY = beadScale
                }
                .drawBehind {
                  // Soft ember halo — glows while the ring is held big, then
                  // settles down as the bead slides back into its slot.
                  val r = size.width * 0.62f
                  drawCircle(
                    brush = Brush.radialGradient(
                      colors = listOf(
                        ScholaTerracotta.copy(alpha = 0.26f),
                        ScholaTerracotta.copy(alpha = 0.05f),
                        Color.Transparent
                      ),
                      center = center,
                      radius = r
                    ),
                    radius = r,
                    center = center
                  )
                }
            ) {
              AttendanceBead(
                value = safeAttendance,
                avatarLabel = avatarLabel,
                avatarUrl = avatarUrl
              )
            }
          }
          Spacer(modifier = Modifier.width(Spacing.s3))

          Row(
            modifier = Modifier.graphicsLayer {
              alpha = nameReveal
              translationY = contentOffsetY
            },
            verticalAlignment = Alignment.CenterVertically
          ) {
            nameContent()
          }
        }

        Box(
          modifier = Modifier.graphicsLayer {
            alpha = nameReveal
            translationY = contentOffsetY
          }
        ) {
          trailingContent()
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // ---------- Section rail (designer divider) — appears with the stats ----------
      Row(
        modifier = Modifier.graphicsLayer {
          alpha = statsReveal
          translationY = contentOffsetY
        },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .width(38.dp)
            .height(2.5.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(
              Brush.horizontalGradient(
                listOf(ScholaTerracotta, ScholaTerracottaLight)
              )
            )
        )
        Spacer(modifier = Modifier.width(Spacing.s2))
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(
              Brush.horizontalGradient(
                listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))
              )
            )
        )
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // ---------- Stats (reveal trails the identity row by a beat) ----------
      Box(
        modifier = Modifier.graphicsLayer {
          alpha = statsReveal
          translationY = contentOffsetY
        }
      ) {
        statsContent()
      }
    }

      // Caption shown only while the ring is held centred on the card; it fades
      // out with a gentle upward drift as the ring settles back into its slot.
      if (measured) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
              alpha = 1f - reveal
              translationY = captionOffsetY + reveal * 16f
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.75f),
            maxLines = 1
          )
        }
      }
    }
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
  tint: Color = ScholaTerracottaLight,
  backgroundColor: Color = ScholaSlateNavyDark
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
  containerColor: Color = ScholaSlateNavyDark,
  contentColor: Color = Color.White,
  minHeight: Dp = 48.dp,
  leadingIcon: ImageVector? = null,
  fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
  fontWeight: FontWeight = FontWeight.Medium
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
      .clip(RoundedCornerShape(Radius.pill))
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
        Icon(leadingIcon, contentDescription = null, tint = if (enabled) contentColor else ScholaMuted, modifier = Modifier.size(18.dp))
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
  shape: RoundedCornerShape = RoundedCornerShape(Radius.xl),
  dark: Boolean = false,
  solidColor: Color? = null,
  sheen: Boolean = false,
  content: @Composable ColumnScope.() -> Unit
) {
  val sheenSweep = if (sheen && solidColor != null)
    rememberInfiniteTransition(label = "cardSheen").animateFloat(
      0f, 1f,
      infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
      label = "sheen"
    )
  else null

  val card = if (solidColor != null) {
    Modifier
      .background(color = solidColor, shape = shape)
      .border(0.8.dp, Color.White.copy(alpha = 0.14f), shape)
      .shadow(14.dp, shape, clip = false)
  } else {
    Modifier.glassEffect(cornerRadius = Radius.xl, dark = dark)
  }

  Box(
    modifier = modifier
      .then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
      .then(card)
      .then(
        if (sheenSweep != null) {
          Modifier
            .clip(shape)
            .drawWithContent {
              drawContent()
              val t = sheenSweep.value
              val bandWidth = size.width * 0.45f
              val fromX = (t - 1f) * (size.width + bandWidth)
              drawRect(
                brush = Brush.linearGradient(
                  colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.05f), Color.Transparent),
                  start = Offset(fromX, 0f),
                  end = Offset(fromX + bandWidth, size.height)
                ),
                size = size
              )
            }
        } else {
          Modifier
        }
      )
      .padding(Spacing.cardPaddingLarge)
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
  background: Color = ScholaSlateNavyDark,
  textColor: Color = Color.White,
  imagePath: String? = null
) {
  val resolver = LocalContext.current.contentResolver
  val photo by produceState<ImageBitmap?>(null, imagePath) {
    value = withContext(Dispatchers.IO) {
      val p = imagePath
      if (p.isNullOrBlank() || p.startsWith("http")) return@withContext null
      try {
        val decoded = if (p.startsWith("content://") || p.startsWith("file://")) {
          resolver.openInputStream(Uri.parse(p))?.use { BitmapFactory.decodeStream(it) }
        } else {
          BitmapFactory.decodeFile(p)
        }
        decoded?.asImageBitmap()
      } catch (e: Exception) {
        null
      }
    }
  }
  val initials = name.trim().split(" ").filter { it.isNotBlank() }
    .mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase(Locale.getDefault())
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(if (photo != null) Color.Transparent else background),
    contentAlignment = Alignment.Center
  ) {
    val bitmap = photo
    if (bitmap != null) {
      Image(
        bitmap = bitmap,
        contentDescription = "Profile picture",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    } else {
      Text(
        text = initials.ifBlank { "?" },
        color = textColor,
        fontSize = (size.value * 0.36f).sp,
        fontWeight = FontWeight.Bold
      )
    }
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