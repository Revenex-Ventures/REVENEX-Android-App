package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// ScholaOS Neo-Brutalist Visual Theme Palette
// ============================================================================

// 1. Background Canvas & Surface Borders
val ScholaLinen = Color(0xFFF7F4EE)            // Warm linen canvas background
val ScholaSurface = Color(0xFFFFFFFF)          // Crisp white primary card surface
val ScholaSurfaceWarm = Color(0xFFFDFCF9)      // Soft off-white secondary surface
val ScholaBorder = Color(0xFFE5E0D8)           // Subtle neo-brutalist 1px border
val ScholaBorderFocused = Color(0xFFC2410C)    // Border focused highlight
val ScholaMuted = Color(0xFF78716C)            // Stone muted text
val ScholaTextPrimary = Color(0xFF1C1917)      // Deep charcoal/stone text primary
val ScholaTextSecondary = Color(0xFF44403C)    // Stone text secondary

// 2. Brand & Interactive Accents
val ScholaTerracotta = Color(0xFFC2410C)       // Deep burnt terracotta brand primary
val ScholaTerracottaLight = Color(0xFFEA580C)  // Vibrant burnt terracotta highlight
val ScholaTerracottaDark = Color(0xFF9A3412)   // Deep burnt terracotta shade
val ScholaTerracottaContainer = Color(0xFFFFEDD5) // Soft peach/terracotta container
val ScholaOnTerracottaContainer = Color(0xFF7C2D12)

val ScholaSlateNavy = Color(0xFF1E293B)        // Slate navy interactive accent
val ScholaSlateNavyDark = Color(0xFF0F172A)    // Dark slate navy
val ScholaSlateContainer = Color(0xFFE2E8F0)   // Soft slate container

// 3. High-Contrast Hero Surfaces (Dark Onyx & Amber-Gold)
val ScholaOnyx = Color(0xFF161412)             // Deep onyx hero surface
val ScholaOnyxSurface = Color(0xFF1E1B18)      // Secondary dark onyx surface
val ScholaOnyxBorder = Color(0xFF2A2724)       // 1px dark onyx border
val ScholaOnyxText = Color(0xFFF5F5F4)         // Crisp white onyx typography
val ScholaOnyxMuted = Color(0xFFA8A29E)        // Muted white/stone on onyx

val ScholaGold = Color(0xFFF59E0B)             // Amber gold hero metric highlight
val ScholaGoldLight = Color(0xFFFBBF24)        // Bright amber gold
val ScholaGoldContainer = Color(0xFFFEF3C7)    // Soft amber gold pill
val ScholaGoldText = Color(0xFF92400E)

// 4. Status Pill System (High-Contrast Background & Text Pairs)
// Success / Paid / Present / Approved
val StatusSuccessBg = Color(0xFFDCFCE7)        // Mint green pill background
val StatusSuccessText = Color(0xFF15803D)      // Mint green text

// Warning / Partial / Late / Pending
val StatusWarningBg = Color(0xFFFEF3C7)        // Warm amber pill background
val StatusWarningText = Color(0xFFB45309)      // Warm amber text

// Danger / Overdue / Absent / Rejected
val StatusDangerBg = Color(0xFFFFE4E6)         // Rose red pill background
val StatusDangerText = Color(0xFFBE123C)       // Rose red text

// Neutral / Scheduled / Excused / Circular
val StatusNeutralBg = Color(0xFFEEF2FF)        // Soft indigo pill background
val StatusNeutralText = Color(0xFF4338CA)      // Soft indigo text

// Legacy alias compatibility
val RevenexNavyDark = ScholaSlateNavyDark
val RevenexNavy = ScholaSlateNavy
val RevenexBlue = ScholaSlateNavy
val RevenexPrimary = ScholaTerracotta
val RevenexPrimaryLight = ScholaTerracottaLight
val RevenexPrimaryContainer = ScholaTerracottaContainer
val RevenexOnPrimaryContainer = ScholaOnTerracottaContainer
val RevenexGold = ScholaGold
val RevenexGoldLight = ScholaGoldLight
val RevenexGoldContainer = ScholaGoldContainer
val RevenexCyan = Color(0xFF0284C7)
val RevenexCyanContainer = Color(0xFFE0F2FE)
val RevenexInk = ScholaOnyx
val RevenexMeritGold = ScholaGold
val RevenexMeritGoldContainer = ScholaGoldContainer
val RevenexOnMeritGoldContainer = ScholaGoldText

val StatusSuccess = StatusSuccessText
val StatusSuccessContainer = StatusSuccessBg
val StatusWarning = StatusWarningText
val StatusWarningContainer = StatusWarningBg
val StatusError = StatusDangerText
val StatusErrorContainer = StatusDangerBg
val StatusErrorText = StatusDangerText
val StatusInfo = StatusNeutralText
val StatusInfoContainer = StatusNeutralBg
val StatusInfoText = StatusNeutralText

val LightBackground = ScholaLinen
val LightSurface = ScholaSurface
val LightSurfaceVariant = ScholaSurfaceWarm
val LightBorder = ScholaBorder
val LightTextPrimary = ScholaTextPrimary
val LightTextSecondary = ScholaTextSecondary
val LightTextMuted = ScholaMuted

val DarkBackground = ScholaOnyx
val DarkSurface = ScholaOnyxSurface
val DarkSurfaceVariant = Color(0xFF262320)
val DarkBorder = ScholaOnyxBorder
val DarkTextPrimary = ScholaOnyxText
val DarkTextSecondary = ScholaOnyxMuted
val DarkTextMuted = Color(0xFF78716C)

val VitalRingSky = Color(0xFF38BDF8)
val VitalRingCyan = Color(0xFF00E5FF)
val VitalRingTeal = Color(0xFF2DD4BF)
val VitalRingBlue = Color(0xFF60A5FA)
val VitalRingPurple = Color(0xFFC084FC)
