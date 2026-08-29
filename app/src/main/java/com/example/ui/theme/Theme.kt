package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = RevenexPrimaryLight,
  onPrimary = Color.White,
  primaryContainer = RevenexNavy,
  onPrimaryContainer = RevenexPrimaryContainer,
  secondary = RevenexGoldLight,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF451A03),
  onSecondaryContainer = RevenexGoldContainer,
  tertiary = RevenexCyan,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF082F49),
  onTertiaryContainer = RevenexCyanContainer,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  error = StatusError,
  onError = Color.White,
  errorContainer = Color(0xFF450A0A),
  onErrorContainer = StatusErrorContainer
)

private val LightColorScheme = lightColorScheme(
  primary = RevenexBlue,
  onPrimary = Color.White,
  primaryContainer = RevenexPrimaryContainer,
  onPrimaryContainer = RevenexOnPrimaryContainer,
  secondary = RevenexGold,
  onSecondary = Color.White,
  secondaryContainer = RevenexGoldContainer,
  onSecondaryContainer = Color(0xFF78350F),
  tertiary = RevenexCyan,
  onTertiary = Color.White,
  tertiaryContainer = RevenexCyanContainer,
  onTertiaryContainer = Color(0xFF0369A1),
  background = LightBackground,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  error = StatusError,
  onError = Color.White,
  errorContainer = StatusErrorContainer,
  onErrorContainer = StatusErrorText
)

@Composable
fun RevenexTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
