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
import androidx.compose.ui.composed
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
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.ui.platform.LocalDensity
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
  cornerRadius: Dp = Radius.lg,
  borderAlpha: Float = 0.45f,
  dark: Boolean = false
): Modifier = this.composed {
  this.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(cornerRadius))
    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(cornerRadius))
}

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
        .background(MaterialTheme.colorScheme.background)
    )
    content()
  }
}

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
  badgeBgColor: Color = MaterialTheme.colorScheme.secondaryContainer,
  badgeTextColor: Color = ScholaSlateNavy,
  heroHighlightColor: Color = MaterialTheme.colorScheme.primary,
  secondaryMetricLabel: String = "DAILY ATTENDANCE RATE"
) {
  Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.hero),
    color = MaterialTheme.colorScheme.secondaryContainer) {
    Column(Modifier.padding(Spacing.lg16), verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
      Text(cycleTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
      Text(categoryBadge, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
        Column(Modifier.weight(1f)) {
          Text(primaryStatistic, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
          Text(statisticLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Column(Modifier.weight(1f)) {
          Text(secondaryKeyMetric, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Text(secondaryMetricLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
      }
      TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
        Text(actionButtonText); Spacer(Modifier.width(Spacing.sm8)); Icon(Icons.Default.ArrowForward, null)
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
  iconContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  deltaText: String? = null,
  isPositiveDelta: Boolean = true,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Surface(modifier = modifier.then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier),
    shape = RoundedCornerShape(Radius.lg), color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(Modifier.padding(Spacing.md12), verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        Surface(color = iconContainerColor, shape = RoundedCornerShape(Radius.sm)) {
          Icon(icon, contentDescription = null, tint = if (iconContainerColor.luminance() < 0.3f && iconTint.luminance() < 0.12f) MaterialTheme.colorScheme.onSurface else iconTint,
            modifier = Modifier.padding(Spacing.sm8).size(20.dp))
        }
        Text(statCounter, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f))
      }
      Text(categoryLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      deltaText?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
  }
}

// ============================================================================
// 3. COMPACT BOTTOM NAVIGATION
// ============================================================================

@Composable
fun ScholaFloatingDock(items: List<ScholaDockItem>,currentRoute: String?,onItemSelected:(String)->Unit,modifier:Modifier=Modifier) {
  val largeText=LocalDensity.current.fontScale>1.3f
  fun destinationLabel(label: String): String = when (label) {
    "Attendance" -> "Roll call"
    "Admissions" -> "Students"
    "Homework" -> "Tasks"
    "Resources" -> "Notes"
    "Timetable" -> "Schedule"
    else -> label
  }
  Surface(modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.lg),color=MaterialTheme.colorScheme.surface,
    border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),shadowElevation=Elev.e1) {
    Column {
      if(largeText) Text(destinationLabel(items.firstOrNull {it.route==currentRoute}?.label.orEmpty()),Modifier.padding(horizontal=Spacing.md12,vertical=Spacing.xs4),style=MaterialTheme.typography.labelLarge)
      Row(Modifier.fillMaxWidth()) {
        items.forEach {item->
          val selected=currentRoute==item.route
          val ink=if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          Column(Modifier.weight(1f).clickable(onClickLabel=item.label){onItemSelected(item.route)}.heightIn(min=64.dp).padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4,Alignment.CenterVertically)) {
            Icon(if(selected)item.selectedIcon else item.unselectedIcon,item.label,Modifier.size(24.dp),tint=ink)
            if(!largeText) Text(destinationLabel(item.label),style=MaterialTheme.typography.labelSmall,color=ink,maxLines=1,overflow=TextOverflow.Ellipsis)
          }
        }
      }
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
      { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    } else null,
    leadingIcon = leadingIcon?.let {
      { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
    },
    trailingIcon = {
      if (value.isNotEmpty() && onClear != null) {
        IconButton(onClick = onClear) {
          Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
      }
    },
    singleLine = singleLine,
    supportingText = if (supportingText.isNotEmpty()) { { Text(supportingText) } } else null,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = shape,
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surface,
      unfocusedContainerColor = MaterialTheme.colorScheme.surface,
      focusedBorderColor = MaterialTheme.colorScheme.primary,
      unfocusedBorderColor = MaterialTheme.colorScheme.outline,
      focusedLabelColor = MaterialTheme.colorScheme.primary,
      unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
      focusedTextColor = MaterialTheme.colorScheme.onSurface,
      unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
      SemanticColors.successContainer to SemanticColors.onSuccess
    "LATE", "PARTIAL", "PARTIALLY PAID", "PENDING", "UNDER MAINTENANCE", "WARNING" ->
      SemanticColors.warningContainer to SemanticColors.onWarning
    "ABSENT", "OVERDUE", "REJECTED", "DANGER", "ERROR", "NEEDS REPLACEMENT" ->
      SemanticColors.dangerContainer to SemanticColors.onDanger
    "EXCUSED", "SCHEDULED", "ON LEAVE", "NEUTRAL", "CIRCULAR", "UPCOMING", "INFO" ->
      MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
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
  tint: Color = MaterialTheme.colorScheme.primary
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
  gradientStart: Color = MaterialTheme.colorScheme.primary,
  gradientEnd: Color = ScholaTerracottaLight,
  trackColor: Color = ScholaOnyxBorder,
  textColor: Color = MaterialTheme.colorScheme.onSurface,
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
  color: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(progress) {
    animProgress.snapTo(progress.coerceIn(0f, 1f))
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
  LaunchedEffect(Unit) { if (!introHasPlayed) onIntroPassed() }
  Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.lg),
    color = MaterialTheme.colorScheme.surfaceVariant) {
    Column(Modifier.padding(Spacing.lg16), verticalArrangement = Arrangement.spacedBy(Spacing.md12)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(name = avatarLabel, imagePath = avatarUrl.ifBlank { null }, modifier = Modifier.size(48.dp),
          background = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(Modifier.width(Spacing.md12))
        Row(Modifier.weight(1f), content = nameContent)
      }
      Text("$label · ${attendanceValue.coerceIn(0f, 100f).roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
      LinearProgressIndicator(progress = { (attendanceValue / 100f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
      trailingContent()
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      statsContent()
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
  color: Color = MaterialTheme.colorScheme.onSurface
) {
  Text(value, modifier = modifier, style = style, color = color, fontWeight = FontWeight.Bold)
}

@Composable
fun CountUpCurrency(
  amountPaise: Long,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.titleLarge,
  color: Color = MaterialTheme.colorScheme.onSurface,
  currencySymbol: String = "₹"
) {
  Text("$currencySymbol${String.format(Locale.US, "%,.0f", amountPaise / 100.0)}", modifier = modifier,
    style = style, color = color, fontWeight = FontWeight.Bold)
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
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = MaterialTheme.colorScheme.onPrimary,
  minHeight: Dp = 48.dp,
  leadingIcon: ImageVector? = null,
  fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
  fontWeight: FontWeight = FontWeight.Medium
) {
  Button(onClick = onClick, enabled = enabled,
    modifier = modifier.heightIn(min = minHeight), shape = RoundedCornerShape(Radius.md),
    colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)) {
    leadingIcon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm8)) }
    Text(text, fontSize = fontSize, fontWeight = fontWeight)
  }
}

@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  elevation: Dp = Elev.e0,
  shape: RoundedCornerShape = RoundedCornerShape(Radius.xl),
  dark: Boolean = false,
  solidColor: Color? = null,
  sheen: Boolean = false,
  content: @Composable ColumnScope.() -> Unit
) {
  val background = solidColor ?: containerColor
  Surface(modifier = modifier.then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier),
    shape = shape, color = background,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = Elev.e0, shadowElevation = Elev.e0) {
    Column(Modifier.fillMaxWidth().padding(Spacing.cardPadding), content = content)
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
          Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(Spacing.s2))
        }
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
  baseColor: Color = MaterialTheme.colorScheme.outlineVariant,
  highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  cornerRadius: Dp = Radius.md
) {
  if (rememberReducedMotion()) {
    Box(modifier.clip(RoundedCornerShape(cornerRadius)).background(baseColor))
    return
  }
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
  // Content is immediately available; standard navigation/press feedback supplies motion.
  content()
}

@Composable
fun AnimatedEmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  retryText: String? = null,
  onRetry: (() -> Unit)? = null,
  accent: Color = MaterialTheme.colorScheme.primary
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
      color = MaterialTheme.colorScheme.onSurfaceVariant,
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
  color: Color = MaterialTheme.colorScheme.primary
) {
  ScholaPillBadge(status = text, modifier = modifier)
}

@Composable
fun rememberReducedMotion(): Boolean {
  val context = LocalContext.current
  return android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}
