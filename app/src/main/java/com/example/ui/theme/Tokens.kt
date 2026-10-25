package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
  val cardPaddingLarge = 16.dp
}

// Corner Radii Scale (Premium — large rounded modules, pill controls)
object Radius {
  val xs = 6.dp
  val sm = 10.dp
  val md = 12.dp
  val lg = 14.dp
  val xl = 18.dp
  val hero = 20.dp      // Feature Hero Cards
  val tile = 18.dp      // Compact Stat Tiles
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
  const val normal = 180
  const val slow = 240

  fun <T> springSmooth() = spring<T>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
  )

  fun <T> springBouncy() = spring<T>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
  )

  fun <T> tweenEase(durationMs: Int = normal) = tween<T>(
    durationMillis = durationMs,
    easing = FastOutSlowInEasing
  )
}

/** Company-card artwork geometry and focused account-entry layout. */
object EntryTokens {
  val launchBackground = Color(0xFF090C12)
  val brandWhite = Color(0xFFF2F8FC)
  val fieldLight = Color(0xFFECEBE6)
  val markWidth = 120.dp
  val markTravel = 25.dp
  val wordmarkOffset = 52.dp
  val wordmarkMaxWidth = 360.dp
  val brandStageHeight = 192.dp
  val compactMark = 20.dp
  val compactName = 88.dp
  val formWidth = 480.dp
  val tabletBreakpoint = 720.dp
  val inputHeight = 56.dp
  const val photoFraction = .42f
  const val wordmarkFraction = .86f
  const val brandDuration = 1400
  const val contentFade = 140
}

// Role Accent Configuration (soft academic pastel family)
object RoleAccent {
  // Role identity wells remain dark in both palettes.
  val onAccent = Color.White
  val Admin = ScholaSlateNavy
  val Teacher = PastelLavenderText
  val Student = PastelSageText
  val Parent = PastelPowderText

  fun of(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> Admin
    UserRole.ADMIN -> Admin
    UserRole.TEACHER -> Teacher
    UserRole.STUDENT -> Student
    UserRole.PARENT -> Parent
    null -> Admin
  }

  fun softContainer(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> ScholaSlateContainer
    UserRole.ADMIN -> ScholaSlateContainer
    UserRole.TEACHER -> PastelLavenderContainer
    UserRole.STUDENT -> PastelSageContainer
    UserRole.PARENT -> PastelPowderContainer
    null -> ScholaSlateContainer
  }
}

/** Approved-board proportions; large text can grow beyond these minima. */
object Presentation {
  val sectionHeading = 32.dp
  val rowHeight = 56.dp
  val chartHeight = 112.dp
  val timelineTimeWidth = 64.dp
  val timelineStem = 28.dp
}

/** Geometry and functional colors taken from the approved Principal panels. */
object PrincipalLayout {
  val compactModuleHeight = 88.dp
  val contextWidth = 160.dp
  val reportColumn = 144.dp
  val summaryHeight = 80.dp
  val historyHeight = 320.dp
  val attentionRowHeight = 52.dp
  val wordmarkWidth = 108.dp
  val metricHeight = 108.dp
  val moduleWell = 64.dp
  val moduleIcon = 30.dp
  val listAvatar = 40.dp
  val rowHeight = 72.dp
  val titleSize = 24.sp
  val sectionSize = 20.sp
  val metricSize = 26.sp
}
object PrincipalColors {
  val canvasLight = Color(0xFFFCFCFB)
  val inkLight = Color(0xFF111827)
  val mutedLight = Color(0xFF515665)
  val actionLight = Color(0xFFD44200)
  private val dark: Boolean @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < .5f
  val blue: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.blueContainer else Color(0xFFEDF6FF)
  val blueInk: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.onBlue else Color(0xFF1460D9)
  val mint: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.successContainer else Color(0xFFECFAF2)
  val mintInk: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.onSuccess else Color(0xFF087741)
  val peach: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.peachContainer else Color(0xFFFFF4E7)
  val peachInk: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.onPeach else Color(0xFFD54500)
  val lavender: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.lavenderContainer else Color(0xFFF2EDFF)
  val lavenderInk: Color @androidx.compose.runtime.Composable get() = if(dark) SemanticColors.onLavender else Color(0xFF4816AA)
}
