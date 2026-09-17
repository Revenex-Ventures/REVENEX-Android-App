package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// SCHOLAOS — Black & Orange (clean minimal light)
// ⬜ Canvas      #FFFFFF — white
// ⬛ Text        #111111 — near-black charcoal
// 🩶 Muted       #9C9C9C — secondary / inactive grays
// 🟠 Accent      #FF5A1F — vivid orange (single accent)
// ⸻ Monochrome black/white base, one bold orange accent.
// ============================================================================

// 1. Foundation (neutral, monochrome)
val ScholaLinen = Color(0xFFFAFAFA)
val ScholaSurface = Color(0xFFFFFFFF)
val ScholaSurfaceWarm = Color(0xFFF5F5F5)
val ScholaBorder = Color(0xFFE7E7E7)
val ScholaBorderFocused = Color(0xFFFF5A1F)
val ScholaMuted = Color(0xFF6E6E6E)
val ScholaTextPrimary = Color(0xFF111111)
val ScholaTextSecondary = Color(0xFF6E6E6E)

// 2. Brand accent (vivid orange)
val ScholaTerracotta = Color(0xFFFF5A1F)
val ScholaTerracottaLight = Color(0xFFFF8A5C)
val ScholaTerracottaDark = Color(0xFFD93F00)
val ScholaTerracottaContainer = Color(0xFFFFF1E8)
val ScholaOnTerracottaContainer = Color(0xFFA32C00)

// Structural (black family)
val InkBlack = Color(0xFF0C0C0E)
val ScholaSlateNavy = Color(0xFF1A1A1A)
val ScholaSlateNavyDark = InkBlack
val ScholaSlateContainer = Color(0xFFF1F1F1)
val GlassBgTransparent = Color(0x00000000)

// 3. Hero surfaces
val ScholaOnyx = Color(0xFFF7F7F7)
val ScholaOnyxSurface = Color(0xFFFFFFFF)
val ScholaOnyxBorder = Color(0xFFE7E7E7)
val ScholaOnyxText = Color(0xFF111111)
val ScholaOnyxMuted = Color(0xFF6E6E6E)

// 4. Gold (merit / crest — muted amber, sits inside orange family)
val ScholaGold = Color(0xFFB45309)
val ScholaGoldLight = Color(0xFFD97706)
val ScholaGoldContainer = Color(0xFFFFFBEB)
val ScholaGoldText = Color(0xFF92400E)

// 5. Role family (monochrome grays — no competing accent)
val PastelSage = Color(0xFF757575)
val PastelSageContainer = Color(0xFFF1F1F1)
val PastelSageText = Color(0xFF3D3D3D)

val PastelPowderBlue = Color(0xFF757575)
val PastelPowderContainer = Color(0xFFF1F1F1)
val PastelPowderText = Color(0xFF3D3D3D)

val PastelLavender = Color(0xFF757575)
val PastelLavenderContainer = Color(0xFFF1F1F1)
val PastelLavenderText = Color(0xFF3D3D3D)

val PastelPeach = Color(0xFF757575)
val PastelPeachContainer = Color(0xFFF1F1F1)
val PastelPeachText = Color(0xFF3D3D3D)

// 6. Status (functional, muted pastels)
val StatusSuccessBg = Color(0xFFECFDF5)
val StatusSuccessText = Color(0xFF047857)
val StatusWarningBg = Color(0xFFFFFBEB)
val StatusWarningText = Color(0xFFB45309)
val StatusDangerBg = Color(0xFFFEF2F2)
val StatusDangerText = Color(0xFFB91C1C)
val StatusNeutralBg = Color(0xFFF3F4F6)
val StatusNeutralText = Color(0xFF4B5563)
val StatusInfoBg = Color(0xFFFFF1E8)
val StatusInfoText = Color(0xFFC2410C)

// Legacy aliases
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
val RevenexCyan = Color(0xFFFF5A1F)
val RevenexCyanContainer = Color(0xFFFFF1E8)
val RevenexInk = ScholaSlateNavyDark
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
val StatusInfo = StatusInfoText
val StatusInfoContainer = StatusInfoBg

val LightBackground = ScholaLinen
val LightSurface = ScholaSurface
val LightSurfaceVariant = ScholaSurfaceWarm
val LightBorder = ScholaBorder
val LightTextPrimary = ScholaTextPrimary
val LightTextSecondary = ScholaTextSecondary
val LightTextMuted = ScholaMuted

val DarkBackground = InkBlack
val DarkSurface = ScholaSlateNavy
val DarkSurfaceVariant = Color(0xFF1C1C1F)
val DarkBorder = Color(0xFF2A2A2E)
val DarkTextPrimary = Color(0xFFFAFAFA)
val DarkTextSecondary = Color(0xFFCCCCCC)
val DarkTextMuted = Color(0xFF9C9C9C)

val VitalRingSky = Color(0xFFFF8A5C)
val VitalRingCyan = Color(0xFFFF5A1F)
val VitalRingTeal = Color(0xFFE84800)
val VitalRingBlue = Color(0xFFD93F00)
val VitalRingPurple = Color(0xFFB02E00)