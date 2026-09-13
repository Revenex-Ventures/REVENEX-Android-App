package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole

// ============================================================================
// ScholaOS Neo-Brutalist Design Tokens
// ============================================================================

// Spacing Scale
object Spacing {
  val xs4 = 4.dp
  val sm8 = 8.dp
  val md12 = 12.dp
  val lg16 = 16.dp
  val xl20 = 20.dp
  val xl24 = 24.dp
  val xxl32 = 32.dp
  val xxxl48 = 48.dp

  // Ergonomic semantic aliases
  val s1 = 4.dp
  val s2 = 8.dp
  val s3 = 12.dp
  val s4 = 16.dp
  val s5 = 20.dp
  val s6 = 24.dp
  val s8 = 32.dp

  val screenPadding = 16.dp
  val screenPaddingTablet = 24.dp
  val screenPaddingDesktop = 32.dp
  val cardPadding = 16.dp
  val cardPaddingLarge = 20.dp
}

// Corner Radii Scale
object Radius {
  val xs = 4.dp
  val sm = 8.dp
  val md = 12.dp
  val lg = 16.dp
  val xl = 20.dp
  val hero = 22.dp      // Exactly 22dp for Feature Hero Cards
  val tile = 20.dp      // Exactly 20dp for Compact Stat Tiles
  val input = 16.dp     // Outlined text fields 14-16dp
  val pill = 999.dp     // Pill badges & floating dock
}

// Subtle Neo-Brutalist Elevation (Zero drop-shadows, crisp 1px borders)
object Elev {
  val e0 = 0.dp
  val e1 = 1.dp
  val e2 = 2.dp
  val e3 = 4.dp
  val floatingDock = 10.dp // Floating rounded pill dock
}

// Micro Typography & Letter Spacing Tokens
object TypeTokens {
  val sizeHeroDisplay = 28.sp
  val sizeHeroStat = 28.sp
  val sizeTitle = 20.sp
  val sizeSubtitle = 16.sp
  val sizeBody = 14.sp
  val sizeLabel = 12.sp
  val sizeMicro = 10.sp

  // Tracked micro-labels (0.8sp tracking as specified)
  val trackingMicroLabel = 0.8.sp
  val trackingBadge = 0.6.sp
  val trackingHeroStat = (-0.5).sp

  val weightBold = androidx.compose.ui.text.font.FontWeight.Bold
  val weightSemiBold = androidx.compose.ui.text.font.FontWeight.SemiBold
  val weightMedium = androidx.compose.ui.text.font.FontWeight.Medium
  val weightNormal = androidx.compose.ui.text.font.FontWeight.Normal
}

// Animation & Motion Tokens
object Motion {
  const val fast = 150
  const val normal = 250
  const val slow = 400

  fun <T> springSmooth() = spring<T>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
  )

  fun <T> springBouncy() = spring<T>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
  )

  fun <T> tweenEase(durationMs: Int = normal) = tween<T>(
    durationMillis = durationMs,
    easing = FastOutSlowInEasing
  )
}

// Role Accent Configuration
object RoleAccent {
  val Admin = ScholaSlateNavy
  val Teacher = Color(0xFF6D28D9)
  val Student = ScholaTerracotta
  val Parent = Color(0xFF0D9488)

  fun of(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> Admin
    UserRole.TEACHER -> Teacher
    UserRole.STUDENT -> Student
    UserRole.PARENT -> Parent
    null -> Admin
  }

  fun softContainer(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> ScholaSlateContainer
    UserRole.TEACHER -> Color(0xFFEDE9FE)
    UserRole.STUDENT -> ScholaTerracottaContainer
    UserRole.PARENT -> Color(0xFFCCFBF1)
    null -> ScholaSlateContainer
  }
}