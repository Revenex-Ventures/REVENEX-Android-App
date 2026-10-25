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

val LocalTeacherPresentation = staticCompositionLocalOf { false }

private val ScholaLightColorScheme = lightColorScheme(
  primary = ScholaTerracotta,
  onPrimary = Color.White,
  primaryContainer = ScholaTerracottaContainer,
  onPrimaryContainer = ScholaOnTerracottaContainer,
  secondary = Color(0xFF5B6650),
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
  outline = Color(0xFF85786D),
  outlineVariant = ScholaBorder,
  surfaceTint = ScholaTerracotta,
  surfaceDim = Color(0xFFE9E0D5),
  surfaceBright = Color.White,
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFFFFAF4),
  surfaceContainer = Color(0xFFF8F1E7),
  surfaceContainerHigh = Color(0xFFF1E8DC),
  surfaceContainerHighest = Color(0xFFEAE0D2),
  inverseSurface = Color(0xFF332A23),
  inverseOnSurface = Color(0xFFFFF8F0),
  inversePrimary = ScholaTerracottaLight,
  error = StatusDangerText,
  onError = Color.White,
  errorContainer = StatusDangerBg,
  onErrorContainer = StatusDangerText
)

private val ScholaDarkColorScheme = darkColorScheme(
  primary = ScholaTerracottaLight,
  onPrimary = ScholaSlateNavyDark,
  primaryContainer = Color(0xFF603311),
  onPrimaryContainer = ScholaTerracottaLight,
  secondary = ScholaTerracottaLight,
  onSecondary = ScholaSlateNavyDark,
  secondaryContainer = Color(0xFF3B3530),
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
  surfaceBright = Color(0xFF46392F),
  surfaceContainerLowest = Color(0xFF191510),
  surfaceContainerLow = Color(0xFF252019),
  surfaceContainer = DarkSurface,
  surfaceContainerHigh = Color(0xFF393027),
  surfaceContainerHighest = Color(0xFF45392E),
  inverseSurface = Color(0xFFF0E3D3),
  inverseOnSurface = Color(0xFF292621),
  inversePrimary = ScholaTerracotta,
  error = Color(0xFFE09078),
  onError = ScholaSlateNavyDark,
  errorContainer = Color(0xFF4A2B24),
  onErrorContainer = Color(0xFFF0C0AE)
)

@Composable
fun ScholaTheme(
  darkTheme: Boolean? = null,
  principal: Boolean = false,
  teacher: Boolean = false,
  content: @Composable () -> Unit
) {
  val preference = LocalAppearance.current ?: rememberAppearancePreference()
  val dark = darkTheme ?: preference.selection.usesDark(isSystemInDarkTheme())
  val colorScheme = (if (dark) ScholaDarkColorScheme else ScholaLightColorScheme).let { original ->
    val base=if(!teacher)original else if(dark)original.copy(background=Color(0xFF151B24),surface=Color(0xFF1D2530),onSurface=Color(0xFFEAF0F8),onBackground=Color(0xFFEAF0F8),onSurfaceVariant=Color(0xFFB5C0D0),surfaceVariant=Color(0xFF283241),outlineVariant=Color(0xFF364253),primary=Color(0xFFFFB581),onPrimary=Color(0xFF3E1900),primaryContainer=Color(0xFF53301F),onPrimaryContainer=Color(0xFFFFDCC5),surfaceTint=Color.Transparent) else original.copy(background=TeacherColors.canvas,surface=TeacherColors.surface,onBackground=TeacherColors.ink,onSurface=TeacherColors.ink,onSurfaceVariant=TeacherColors.muted,primary=TeacherColors.orange,onPrimary=Color(0xFF241000),primaryContainer=Color(0xFFFFF0E5),onPrimaryContainer=TeacherColors.pressed,outlineVariant=TeacherColors.border,outline=TeacherColors.muted,surfaceVariant=TeacherColors.canvas,surfaceTint=Color.Transparent,error=Color(0xFFB42318))
    if(principal)base.copy(background=if(dark)base.background else PrincipalColors.canvasLight,onSurface=if(dark)base.onSurface else PrincipalColors.inkLight,onBackground=if(dark)base.onBackground else PrincipalColors.inkLight,onSurfaceVariant=if(dark)base.onSurfaceVariant else PrincipalColors.mutedLight,primary=if(dark)ScholaTerracottaLight else PrincipalColors.actionLight, surfaceTint=if(dark)ScholaTerracottaLight else PrincipalColors.actionLight) else base
  }
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
  CompositionLocalProvider(LocalAppearance provides preference, LocalTeacherPresentation provides teacher,
    androidx.compose.material3.LocalContentColor provides colorScheme.onBackground) {

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
  principal: Boolean = false,
  content: @Composable () -> Unit
) {
  ScholaTheme(darkTheme = darkTheme, principal = principal, content = content)
}
