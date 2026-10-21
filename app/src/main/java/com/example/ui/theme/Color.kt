package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Revenex palette. Legacy token names retain source compatibility; new UI resolves
// semantic Material roles and SemanticColors for coordinated light/dark appearances.
// Warm canvas, ink typography, teal actions, mint/lavender/peach module groupings.

// 1. Foundation (neutral, monochrome)
val ScholaLinen = Color(0xFFF7F8F4)
val ScholaSurface = Color(0xFFFFFFFF)
val ScholaSurfaceWarm = Color(0xFFEFF3EF)
val ScholaBorder = Color(0xFFDCE5DF)
val ScholaBorderFocused = Color(0xFF006B61)
val ScholaMuted = Color(0xFF526563)
val ScholaTextPrimary = Color(0xFF172B2B)
val ScholaTextSecondary = Color(0xFF526563)

// 2. Teal brand accent
val ScholaTerracotta = Color(0xFF006B61)
val ScholaTerracottaLight = Color(0xFF75D7C5)
val ScholaTerracottaDark = Color(0xFF005047)
val ScholaTerracottaContainer = Color(0xFFD5F2E9)
val ScholaOnTerracottaContainer = Color(0xFF005047)

// Structural (black family)
val InkBlack = Color(0xFF102321)
val ScholaSlateNavy = Color(0xFF18312E)
val ScholaSlateNavyDark = InkBlack
val ScholaSlateContainer = Color(0xFFE9EFEB)
val GlassBgTransparent = Color(0x00000000)

// 3. Hero surfaces
val ScholaOnyx = Color(0xFFEEF5F0)
val ScholaOnyxSurface = Color(0xFFFFFFFF)
val ScholaOnyxBorder = Color(0xFFDCE5DF)
val ScholaOnyxText = Color(0xFF172B2B)
val ScholaOnyxMuted = Color(0xFF526563)

// 4. Gold (merit / crest — muted amber, sits inside orange family)
val ScholaGold = Color(0xFFB45309)
val ScholaGoldLight = Color(0xFFD97706)
val ScholaGoldContainer = Color(0xFFFFFBEB)
val ScholaGoldText = Color(0xFF92400E)

// 5. Role family (monochrome grays — no competing accent)
val PastelSage = Color(0xFF757575)
val PastelSageContainer = Color(0xFFDDF1E5)
val PastelSageText = Color(0xFF285743)

val PastelPowderBlue = Color(0xFF757575)
val PastelPowderContainer = Color(0xFFE9EFEB)
val PastelPowderText = Color(0xFF3D3D3D)

val PastelLavender = Color(0xFF757575)
val PastelLavenderContainer = Color(0xFFEDE8F7)
val PastelLavenderText = Color(0xFF514365)

val PastelPeach = Color(0xFF757575)
val PastelPeachContainer = Color(0xFFFBE7DB)
val PastelPeachText = Color(0xFF724833)

// 6. Status (functional, muted pastels)
val StatusSuccessBg = Color(0xFFECFDF5)
val StatusSuccessText = Color(0xFF047857)
val StatusWarningBg = Color(0xFFFFFBEB)
val StatusWarningText = Color(0xFFB45309)
val StatusDangerBg = Color(0xFFFEF2F2)
val StatusDangerText = Color(0xFFB91C1C)
val StatusNeutralBg = Color(0xFFF3F4F6)
val StatusNeutralText = Color(0xFF4B5563)
val StatusInfoBg = Color(0xFFD5F2E9)
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
val RevenexCyan = Color(0xFF006B61)
val RevenexCyanContainer = Color(0xFFD5F2E9)
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
val DarkSurfaceVariant = Color(0xFF213B37)
val DarkBorder = Color(0xFF405A55)
val DarkTextPrimary = Color(0xFFF7F8F4)
val DarkTextSecondary = Color(0xFFC1D3CD)
val DarkTextMuted = Color(0xFFA5BCB4)

val VitalRingSky = Color(0xFF75D7C5)
val VitalRingCyan = Color(0xFF006B61)
val VitalRingTeal = Color(0xFFE84800)
val VitalRingBlue = Color(0xFF005047)
val VitalRingPurple = Color(0xFFB02E00)