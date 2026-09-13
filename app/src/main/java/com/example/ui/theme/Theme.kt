package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ScholaLightColorScheme = lightColorScheme(
  primary = ScholaTerracotta,
  onPrimary = Color.White,
  primaryContainer = ScholaTerracottaContainer,
  onPrimaryContainer = ScholaOnTerracottaContainer,
  secondary = ScholaSlateNavy,
  onSecondary = Color.White,
  secondaryContainer = ScholaSlateContainer,
  onSecondaryContainer = ScholaSlateNavyDark,
  tertiary = ScholaGold,
  onTertiary = Color.Black,
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
  onPrimary = Color.White,
  primaryContainer = ScholaOnyxSurface,
  onPrimaryContainer = ScholaTerracottaContainer,
  secondary = ScholaGold,
  onSecondary = Color.Black,
  secondaryContainer = ScholaOnyxBorder,
  onSecondaryContainer = ScholaGoldLight,
  tertiary = ScholaSlateNavy,
  onTertiary = Color.White,
  tertiaryContainer = ScholaOnyx,
  onTertiaryContainer = Color.White,
  background = ScholaOnyx,
  onBackground = ScholaOnyxText,
  surface = ScholaOnyxSurface,
  onSurface = ScholaOnyxText,
  surfaceVariant = Color(0xFF262320),
  onSurfaceVariant = ScholaOnyxMuted,
  outline = ScholaOnyxBorder,
  outlineVariant = ScholaOnyxBorder,
  error = StatusDangerText,
  onError = Color.White,
  errorContainer = Color(0xFF450A0A),
  onErrorContainer = StatusDangerBg
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
