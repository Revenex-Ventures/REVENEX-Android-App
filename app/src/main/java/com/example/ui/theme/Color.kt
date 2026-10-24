package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Revenex palette. Legacy token names retain source compatibility; new UI resolves
// semantic Material roles and SemanticColors for coordinated light/dark appearances.
// Warm canvas, ink typography, orange actions, sage/lavender/blue/butter module groupings.

// 1. Foundation (neutral, monochrome)
val ScholaLinen = Color(0xFFF8F7F3)
val RevenexBrandOrange = Color(0xFFF56600)
val ScholaSurface = Color(0xFFFFFFFF)
val ScholaSurfaceWarm = Color(0xFFF7F6F3)
val ScholaBorder = Color(0xFFE8E7E3)
val ScholaBorderFocused = Color(0xFFAD3F00)
val ScholaMuted = Color(0xFF665D55)
val ScholaTextPrimary = Color(0xFF20242B)
val ScholaTextSecondary = Color(0xFF665D55)

// 2. Burnt orange actions (white text meets contrast requirements)
val ScholaTerracotta = Color(0xFFC74400)
val ScholaTerracottaLight = Color(0xFFFFB681)
val ScholaTerracottaDark = Color(0xFF7D2C00)
val ScholaTerracottaContainer = Color(0xFFFFE2CB)
val ScholaOnTerracottaContainer = Color(0xFF7D2C00)

// Structural (black family)
val InkBlack = Color(0xFF211D19)
val ScholaSlateNavy = Color(0xFF2B2520)
val ScholaSlateNavyDark = InkBlack
val ScholaSlateContainer = Color(0xFFE8EDF8)
val GlassBgTransparent = Color(0x00000000)

// 3. Hero surfaces
val ScholaOnyx = Color(0xFFF3EEDC)
val ScholaOnyxSurface = Color(0xFFFFFFFF)
val ScholaOnyxBorder = Color(0xFFE7DED3)
val ScholaOnyxText = Color(0xFF292621)
val ScholaOnyxMuted = Color(0xFF665D55)

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
val PastelPowderContainer = Color(0xFFE8EDF8)
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
val StatusInfoBg = Color(0xFFFFE2CB)
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
val RevenexCyan = Color(0xFFAD3F00)
val RevenexCyanContainer = Color(0xFFFFE2CB)
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

val DarkBackground = Color(0xFF1D2228)
val DarkSurface = Color(0xFF252B33)
val DarkSurfaceVariant = Color(0xFF303741)
val DarkBorder = Color(0xFF626D79)
val DarkTextPrimary = Color(0xFFF8F7F3)
val DarkTextSecondary = Color(0xFFD3D9E0)
val DarkTextMuted = Color(0xFFBBC4CE)

val VitalRingSky = Color(0xFFFFB681)
val VitalRingCyan = Color(0xFFAD3F00)
val VitalRingTeal = Color(0xFFE84800)
val VitalRingBlue = Color(0xFF7D2C00)
val VitalRingPurple = Color(0xFFB02E00)
