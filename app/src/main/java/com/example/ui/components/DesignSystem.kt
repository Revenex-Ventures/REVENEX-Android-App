package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Radius
import com.example.ui.theme.RoleAccent
import com.example.ui.theme.Spacing
import java.util.Locale

// Press-scale + haptics button. Press animates scale to 0.97 with a spring, with a light haptic tick.
@Composable
fun AppButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = Color.White,
  minHeight: Dp = 52.dp,
  leadingIcon: ImageVector? = null,
  fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
  fontWeight: FontWeight = FontWeight.SemiBold
) {
  val haptics = LocalHapticFeedback.current
  var pressed by remember { mutableStateOf(false) }

  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.97f else 1f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
    label = "btnScale"
  )
  val targetColor by animateColorAsState(
    targetValue = if (enabled) containerColor else MaterialTheme.colorScheme.surfaceVariant,
    label = "btnColor"
  )
  val targetContent by animateColorAsState(
    targetValue = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "btnContent"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(Radius.lg))
      .background(targetColor)
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
        .padding(horizontal = Spacing.xl24),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (leadingIcon != null) {
        Icon(leadingIcon, contentDescription = null, tint = targetContent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(Spacing.sm8))
      }
      Text(
        text = text,
        color = targetContent,
        fontSize = fontSize,
        fontWeight = fontWeight
      )
    }
  }
}

// Standard elevated card
@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  elevation: Dp = 1.dp,
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
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape)
  ) {
    Column(modifier = Modifier, content = content)
  }
}

// Floating-label input
@Composable
fun AppInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "",
  placeholder: String = "",
  leadingIcon: ImageVector? = null,
  keyboardType: KeyboardType = KeyboardType.Text,
  singleLine: Boolean = true,
  supportingText: String = ""
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    label = if (label.isNotEmpty()) { { Text(label) } } else null,
    placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
    leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
    singleLine = singleLine,
    supportingText = if (supportingText.isNotEmpty()) { { Text(supportingText) } } else null,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = RoundedCornerShape(Radius.lg),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = MaterialTheme.colorScheme.primary,
      unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
  )
}

// Pill badge
@Composable
fun Badge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.primary,
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(Radius.pill))
      .background(color.copy(alpha = 0.14f))
      .padding(horizontal = Spacing.sm8, vertical = 3.dp)
  ) {
    Text(
      text = text.uppercase(Locale.getDefault()),
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.3.sp
    )
  }
}

// Initials avatar
@Composable
fun Avatar(
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  background: Color = RoleAccent.Admin,
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
      fontSize = (size.value * 0.34f).sp,
      fontWeight = FontWeight.Bold
    )
  }
}

// Shimmer skeleton block
@Composable
fun SkeletonShimmer(
  modifier: Modifier = Modifier,
  baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
  cornerRadius: Dp = 12.dp
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

// Animated empty state (pulsing icon emblem + friendly copy + optional retry)
@Composable
fun AnimatedEmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  retryText: String? = null,
  onRetry: (() -> Unit)? = null,
  accent: Color = RoleAccent.Admin
) {
  val transition = rememberInfiniteTransition(label = "empty")
  val pulse by transition.animateFloat(
    initialValue = 0.82f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "emptyPulse"
  )
  val anchorAlpha by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "emptyAlpha"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(Spacing.xxl32),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(88.dp)
        .scale(pulse)
        .alpha(anchorAlpha)
        .clip(CircleShape)
        .background(accent.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(40.dp))
    }
    Spacer(modifier = Modifier.height(Spacing.lg16))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(Spacing.sm8))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    if (retryText != null && onRetry != null) {
      Spacer(modifier = Modifier.height(Spacing.xl24))
      TextButton(
        onClick = onRetry,
        contentPadding = PaddingValues(horizontal = Spacing.xl24, vertical = 10.dp)
      ) {
        Text(retryText, fontWeight = FontWeight.Bold)
      }
    }
  }
}

// Animated count-up number, with a live progress ring as it animates
@Composable
fun CountUpNumber(
  value: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.titleLarge,
  color: Color = MaterialTheme.colorScheme.onSurface,
  withProgressRing: Boolean = false,
  ringColor: Color = MaterialTheme.colorScheme.primary,
  min: Float = 0f,
  max: Float = 100f
) {
  // Try to parse a numeric prefix (e.g. "96.4", "1240", "₹85k" -> 85)
  val parsed = remember(value) {
    value.replace(",", "").toDoubleOrNull()?.toFloat()
      ?: Regex("""\d+(\.\d+)?""").find(value)?.value?.toFloatOrNull()
  }
  var display by remember { mutableStateOf("") }
  val progress = remember { Animatable(0f) }

  LaunchedEffect(value) {
    if (parsed != null) {
      progress.snapTo(0f)
      progress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
      val finalNum = parsed
      display = if (finalNum % 1.0f == 0f) {
        finalNum.toInt().toString()
      } else {
        String.format(Locale.US, "%.1f", finalNum)
      }
    } else {
      display = value
    }
  }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (withProgressRing && parsed != null) {
      CircularProgressIndicator(
        progress = { progress.value },
        modifier = Modifier.size(64.dp),
        color = ringColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        strokeWidth = 6.dp
      )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = display.ifBlank {
          parsed?.let { if (it % 1.0f == 0f) it.toInt().toString() else String.format(Locale.US, "%.1f", it) } ?: value
        },
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
      )
      if (withProgressRing) {
        Text(
          text = "ATTENDANCE",
          color = color.copy(alpha = 0.7f),
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.4.sp
        )
      }
    }
  }
}

// Blurred gradient greeting header with the role accent
@Composable
fun GreetingHeader(
  name: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  role: com.example.data.model.UserRole? = null,
  roleLabel: String? = null,
  accent: Color? = null
) {
  val accentColor = accent ?: RoleAccent.of(role)
  val heroBackground = Color(0xFF0F172A)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(Radius.xl))
      .background(heroBackground)
  ) {
    // Soft radial glows (blur) behind content
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .size(140.dp)
        .offset(x = 30.dp, y = (-40).dp)
        .blur(40.dp)
        .background(accentColor.copy(alpha = 0.35f), CircleShape)
    )
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .size(120.dp)
        .offset(x = (-30).dp, y = 34.dp)
        .blur(36.dp)
        .background(RoleAccent.Admin.copy(alpha = 0.25f), CircleShape)
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(all = Spacing.xl24)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "WELCOME BACK",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Hi, $name",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
          )
        }
        if (roleLabel != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(Radius.pill))
              .background(accentColor)
              .padding(horizontal = Spacing.md12, vertical = 6.dp)
          ) {
            Text(
              text = roleLabel,
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.4.sp
            )
          }
        }
      }
    }
  }
}