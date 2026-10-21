package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color

private val ScholaLightColorScheme = lightColorScheme(
  primary = ScholaTerracotta,
  onPrimary = Color.White,
  primaryContainer = ScholaTerracottaContainer,
  onPrimaryContainer = ScholaOnTerracottaContainer,
  secondary = Color(0xFF42665C),
  onSecondary = Color.White,
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
  outline = Color(0xFF70867E),
  outlineVariant = ScholaBorder,
  surfaceTint = ScholaTerracotta,
  surfaceDim = Color(0xFFE1E7E1),
  surfaceBright = Color.White,
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFF4F7F1),
  surfaceContainer = Color(0xFFEEF3EC),
  surfaceContainerHigh = Color(0xFFE8EEE6),
  surfaceContainerHighest = Color(0xFFE2E9E1),
  inverseSurface = Color(0xFF203B35),
  inverseOnSurface = Color(0xFFF5F9F3),
  inversePrimary = ScholaTerracottaLight,
  error = StatusDangerText,
  onError = Color.White,
  errorContainer = StatusDangerBg,
  onErrorContainer = StatusDangerText
)

private val ScholaDarkColorScheme = darkColorScheme(
  primary = ScholaTerracottaLight,
  onPrimary = ScholaSlateNavyDark,
  primaryContainer = Color(0xFF174F46),
  onPrimaryContainer = ScholaTerracottaLight,
  secondary = ScholaTerracottaLight,
  onSecondary = ScholaSlateNavyDark,
  secondaryContainer = Color(0xFF28443D),
  onSecondaryContainer = DarkTextPrimary,
  tertiary = Color(0xFFF1BD9F),
  onTertiary = Color(0xFF412A1D),
  tertiaryContainer = Color(0xFF533C2D),
  onTertiaryContainer = ScholaGoldLight,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorder,
  surfaceTint = ScholaTerracottaLight,
  surfaceDim = DarkBackground,
  surfaceBright = Color(0xFF2C4841),
  surfaceContainerLowest = Color(0xFF0B1C19),
  surfaceContainerLow = Color(0xFF142A25),
  surfaceContainer = DarkSurface,
  surfaceContainerHigh = Color(0xFF213C35),
  surfaceContainerHighest = Color(0xFF2B463F),
  inverseSurface = Color(0xFFDCE9E0),
  inverseOnSurface = Color(0xFF172B2B),
  inversePrimary = ScholaTerracotta,
  error = Color(0xFFE09078),
  onError = ScholaSlateNavyDark,
  errorContainer = Color(0xFF4A2B24),
  onErrorContainer = Color(0xFFF0C0AE)
)

@Composable
fun ScholaTheme(
  darkTheme: Boolean? = null,
  content: @Composable () -> Unit
) {
  val preference = LocalAppearance.current ?: rememberAppearancePreference()
  val dark = darkTheme ?: preference.selection.usesDark(isSystemInDarkTheme())
  val colorScheme = if (dark) ScholaDarkColorScheme else ScholaLightColorScheme
  val view = LocalView.current
  SideEffect {
    if (android.os.Build.VERSION.SDK_INT >= 29) view.isForceDarkAllowed = false
    val activity = view.context.findActivity()
    if (activity != null) {
      WindowCompat.getInsetsController(activity.window, view).apply {
        isAppearanceLightStatusBars = !dark
        isAppearanceLightNavigationBars = !dark
      }
    }
  }
  CompositionLocalProvider(LocalAppearance provides preference) {

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = androidx.compose.material3.Shapes(
      extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs),
      small = androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm),
      medium = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md),
      large = androidx.compose.foundation.shape.RoundedCornerShape(Radius.lg),
      extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(Radius.hero)
    ),
    content = content
  )
  }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> if (baseContext !== this) baseContext.findActivity() else null
  else -> null
}

// Backward-compatible alias
@Composable
fun RevenexTheme(
  darkTheme: Boolean? = null,
  content: @Composable () -> Unit
) {
  ScholaTheme(darkTheme = darkTheme, content = content)
}
