package com.example.ui.components

import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import kotlin.math.exp
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
  val glassShape = RoundedCornerShape(Radius.hero)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .glassEffect(cornerRadius = Radius.hero, dark = true)
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
          color = Color.White.copy(alpha = 0.70f),
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
            color = Color.White.copy(alpha = 0.70f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = primaryStatistic,
            color = Color.White,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = secondaryMetricLabel.uppercase(Locale.getDefault()),
            color = Color.White.copy(alpha = 0.70f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = secondaryKeyMetric,
            color = ScholaTerracottaLight,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(Spacing.s4))

      // Pill Action Button with Trailing Arrow (charcoal, lime arrow)
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = ScholaSlateNavyDark,
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
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.width(Spacing.s2))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = ScholaTerracottaLight,
            modifier = Modifier.size(16.dp)
          )
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
val InkBlack = Color(0xFF0D0D0F)

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

  Box(modifier = modifier.size(size)) {
    CircularProgressIndicator(
      progress = { fraction },
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
    ) {
      Text(
        text = "${value.toInt()}%",
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
  val heroScale by animateFloatAsState(
    targetValue = if (entered) 1f else 0.94f,
    animationSpec = tween(440, easing = FastOutSlowInEasing),
    label = "heroScale"
  )
  val heroAlpha by animateFloatAsState(
    targetValue = if (entered) 1f else 0f,
    animationSpec = tween(440, easing = FastOutSlowInEasing),
    label = "heroAlpha"
  )

  AppCard(
    modifier = modifier.graphicsLayer {
      scaleX = heroScale
      scaleY = heroScale
      alpha = heroAlpha
    },
    containerColor = containerColor,
    elevation = Elev.e2,
    dark = true,
    solidColor = containerColor,
    sheen = true
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AttendanceBead(
          value = safeAttendance,
          avatarLabel = avatarLabel,
          avatarUrl = avatarUrl
        )
        Spacer(modifier = Modifier.width(Spacing.s3))

        nameContent()
      }

      trailingContent()
    }

    Spacer(modifier = Modifier.height(Spacing.s5))

    statsContent()
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