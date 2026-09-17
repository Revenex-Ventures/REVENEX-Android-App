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
// ScholaOS Charcoal Command Center — Design Tokens
// 8px spacing rhythm · premium card radii · pill controls · light shadows
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

// Corner Radii Scale (Premium — large rounded modules, pill controls)
object Radius {
  val xs = 6.dp
  val sm = 10.dp
  val md = 12.dp
  val lg = 14.dp
  val xl = 18.dp
  val hero = 24.dp      // Feature Hero Cards
  val tile = 20.dp      // Compact Stat Tiles
  val input = 14.dp     // Outlined text fields
  val pill = 999.dp     // Pill badges / buttons
}

// Subtle Elevation (Restrained — hairline borders do the definition)
object Elev {
  val e0 = 0.dp
  val e1 = 1.dp
  val e2 = 2.dp
  val e3 = 3.dp
  val floatingDock = 8.dp // Reserved (bottom bar shadow)
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

// Role Accent Configuration (soft academic pastel family)
object RoleAccent {
  val Admin = ScholaSlateNavy
  val Teacher = PastelLavenderText
  val Student = PastelSageText
  val Parent = PastelPowderText

  fun of(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> Admin
    UserRole.TEACHER -> Teacher
    UserRole.STUDENT -> Student
    UserRole.PARENT -> Parent
    null -> Admin
  }

  fun softContainer(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> ScholaSlateContainer
    UserRole.TEACHER -> PastelLavenderContainer
    UserRole.STUDENT -> PastelSageContainer
    UserRole.PARENT -> PastelPowderContainer
    null -> ScholaSlateContainer
  }
}