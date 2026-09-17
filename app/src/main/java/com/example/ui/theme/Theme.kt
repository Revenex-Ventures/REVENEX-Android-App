package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ScholaLightColorScheme = lightColorScheme(
  primary = ScholaTerracotta,
  onPrimary = ScholaSlateNavyDark,
  primaryContainer = ScholaTerracottaContainer,
  onPrimaryContainer = ScholaOnTerracottaContainer,
  secondary = ScholaTerracottaLight,
  onSecondary = ScholaSlateNavyDark,
  secondaryContainer = ScholaSlateContainer,
  onSecondaryContainer = ScholaTextPrimary,
  tertiary = Color(0xFF78350F),
  onTertiary = Color.White,
  tertiaryContainer = ScholaGoldContainer,
  onTertiaryContainer = ScholaGoldText,
  background = ScholaLinen,
  onBackground = ScholaTextPrimary,
  surface = ScholaSurface,
  onSurface = ScholaTextPrimary,
  surfaceVariant = ScholaSurfaceWarm,
  onSurfaceVariant = ScholaTextSecondary,
  outline = ScholaBorder,
  outlineVariant = ScholaBorder,
  error = StatusDangerText,
  onError = Color.White,
  errorContainer = StatusDangerBg,
  onErrorContainer = StatusDangerText
)

private val ScholaDarkColorScheme = darkColorScheme(
  primary = ScholaTerracottaLight,
  onPrimary = ScholaSlateNavyDark,
  primaryContainer = Color(0xFF2E3A12),
  onPrimaryContainer = ScholaTerracottaLight,
  secondary = ScholaTerracottaLight,
  onSecondary = ScholaSlateNavyDark,
  secondaryContainer = Color(0xFF34343B),
  onSecondaryContainer = DarkTextPrimary,
  tertiary = Color(0xFF78350F),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF3A3423),
  onTertiaryContainer = ScholaGoldLight,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorder,
  error = Color(0xFFE09078),
  onError = ScholaSlateNavyDark,
  errorContainer = Color(0xFF4A2B24),
  onErrorContainer = Color(0xFFF0C0AE)
)

@Composable
fun ScholaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) ScholaDarkColorScheme else ScholaLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

// Backward-compatible alias
@Composable
fun RevenexTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  ScholaTheme(darkTheme = darkTheme, content = content)
}