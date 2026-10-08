package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Bright palette: "Calm green".
 *
 * Every colour value in the app lives here. Screens read colours through
 * MaterialTheme.colorScheme / MaterialTheme.extendedColors, never these constants directly
 * (the constants exist for non-composable code such as Canvas drawing).
 *
 * Brand = green. Gold/amber is no longer a brand colour; it only means "warning"
 * (tertiary + extendedColors.warning).
 */

// ---------------------------------------------------------------------------
// LIGHT scheme tokens (default daylight look)
// ---------------------------------------------------------------------------
val LightPrimary = Color(0xFF15803D)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDCFCE7)
val LightOnPrimaryContainer = Color(0xFF14532D)

val LightSecondary = Color(0xFF3F6F52) // muted sage green for secondary accents
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE3EFE6)
val LightOnSecondaryContainer = Color(0xFF1E3A2A)

// Tertiary = warning amber (the only place gold/amber is used)
val LightTertiary = Color(0xFFB45309)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFEF3C7)
val LightOnTertiaryContainer = Color(0xFF78350F)

val LightError = Color(0xFFB91C1C)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF7F1D1D)

val LightBackground = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF111827)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF111827)
val LightSurfaceVariant = Color(0xFFF0F5F1)
val LightOnSurfaceVariant = Color(0xFF374151)
val LightOutline = Color(0xFF9CA3AF)
val LightOutlineVariant = Color(0xFFD1D9D3)
val LightInverseSurface = Color(0xFF1F2937)
val LightInverseOnSurface = Color(0xFFF9FAFB)
val LightInversePrimary = Color(0xFF4ADE80)
val LightSurfaceTint = LightPrimary
val LightScrim = Color(0xFF000000)

val LightSurfaceBright = Color(0xFFFFFFFF)
val LightSurfaceDim = Color(0xFFDCE4DE)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF7FAF8)
val LightSurfaceContainer = Color(0xFFF0F5F1)
val LightSurfaceContainerHigh = Color(0xFFE8EFEA)
val LightSurfaceContainerHighest = Color(0xFFE1E9E3)

// ---------------------------------------------------------------------------
// DARK scheme tokens (companion night look)
// ---------------------------------------------------------------------------
val DarkPrimary = Color(0xFF4ADE80)
val DarkOnPrimary = Color(0xFF052E16)
val DarkPrimaryContainer = Color(0xFF14532D)
val DarkOnPrimaryContainer = Color(0xFFDCFCE7)

val DarkSecondary = Color(0xFF9DC9AC)
val DarkOnSecondary = Color(0xFF0F2A1A)
val DarkSecondaryContainer = Color(0xFF24392C)
val DarkOnSecondaryContainer = Color(0xFFD4EBDC)

val DarkTertiary = Color(0xFFFBBF24)
val DarkOnTertiary = Color(0xFF451A03)
val DarkTertiaryContainer = Color(0xFF78350F)
val DarkOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkError = Color(0xFFF87171)
val DarkOnError = Color(0xFF450A0A)
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFEE2E2)

val DarkBackground = Color(0xFF0B1410)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkSurface = Color(0xFF111C16)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkSurfaceVariant = Color(0xFF1A2A21)
val DarkOnSurfaceVariant = Color(0xFFCBD5E1)
val DarkOutline = Color(0xFF3F5A4A)
val DarkOutlineVariant = Color(0xFF2A3D32)
val DarkInverseSurface = Color(0xFFE5EDE8)
val DarkInverseOnSurface = Color(0xFF111C16)
val DarkInversePrimary = Color(0xFF15803D)
val DarkSurfaceTint = DarkPrimary
val DarkScrim = Color(0xFF000000)

val DarkSurfaceBright = Color(0xFF2A3D32)
val DarkSurfaceDim = Color(0xFF0B1410)
val DarkSurfaceContainerLowest = Color(0xFF07100C)
val DarkSurfaceContainerLow = Color(0xFF142019)
val DarkSurfaceContainer = Color(0xFF17251D)
val DarkSurfaceContainerHigh = Color(0xFF1A2A21)
val DarkSurfaceContainerHighest = Color(0xFF22352A)

// ---------------------------------------------------------------------------
// Semantic accents for non-composable code (Canvas, models)
// ---------------------------------------------------------------------------
val SuccessEmerald = Color(0xFF10B981)
val WarningAmber = Color(0xFFFBBF24) // warning only, never brand
val WarningAmberDeep = Color(0xFFB45309)
val InfoSky = Color(0xFF38BDF8)
val InfoSkyDark = Color(0xFF0284C7)
val InfoBlue = Color(0xFF2563EB)
val InfoBlueBright = Color(0xFF3B82F6)
val HazardRedDeep = Color(0xFFDC2626)
val GreenDeep = Color(0xFF15803D)
val GreenSoft = Color(0xFF4ADE80)

// Always-dark "console" panels (gateway / telemetry screens are designed dark in both themes)
val ConsoleDarkBackground = Color(0xFF0B1410)
val ConsoleDarkSurface = Color(0xFF17251D)

// ---------------------------------------------------------------------------
// Legacy names kept so existing code (and open branches) still compile.
// They now point at the Calm green palette. "Gold" names no longer mean brand:
// they map to green, except the Amber ones, which map to warning amber.
// ---------------------------------------------------------------------------
val ElegantDarkCanvas = DarkBackground
val ElegantDarkBar = DarkSurface
val ElegantDarkCardStart = DarkSurfaceVariant
val ElegantDarkCardEnd = DarkSurfaceContainer
val ElegantDarkSurface = DarkSurface
val ElegantDarkSurfaceElevated = DarkSurfaceContainerHighest
val ElegantDarkSurfaceBright = DarkSurfaceBright
val ElegantDarkBorder = DarkOutlineVariant
val ElegantDarkBorderLight = DarkOutline

val ElegantGoldPrimary = DarkPrimary
val ElegantGoldDark = LightPrimary
val ElegantGoldContainer = DarkPrimaryContainer
val ElegantGoldLightPrimary = LightPrimary
val ElegantGoldLightContainer = LightPrimaryContainer

val ElegantGreenLive = Color(0xFF22C55E)
val ElegantGreenContainer = Color(0x1A22C55E)
val ElegantBluePhase = Color(0xFF60A5FA)
val ElegantBlueContainer = Color(0x1A60A5FA)
val ElegantRedHazard = Color(0xFFEF4444)
val ElegantRedContainer = Color(0x26EF4444)

val Slate100Text = DarkOnSurface
val Slate300Text = DarkOnSurfaceVariant
val Slate400Text = Color(0xFF94A3B8)
val Slate500Text = Color(0xFF64748B)
val Slate700Icon = Color(0xFF334155)
val Slate900Text = LightOnSurface
val Slate800Text = Color(0xFF1E293B)
val Slate600Text = LightOnSurfaceVariant

val ElegantLightCanvas = LightBackground
val ElegantLightBar = LightSurface
val ElegantLightCardStart = LightSurface
val ElegantLightCardEnd = LightSurfaceVariant
val ElegantLightSurface = LightSurface
val ElegantLightSurfaceElevated = LightSurfaceContainer
val ElegantLightBorder = LightOutlineVariant
val ElegantLightBorderLight = LightOutline

val GoldPrimary = DarkPrimary
val EmeraldAccent = ElegantGreenLive
val ElectricGreenLight = LightPrimary
val ElectricGreenDark = DarkPrimary
val ElectricGreenContainerLight = LightPrimaryContainer
val ElectricGreenContainerDark = DarkPrimaryContainer
val AmberElectricLight = LightTertiary
val AmberElectricDark = DarkTertiary
val AmberContainerLight = LightTertiaryContainer
val AmberContainerDark = DarkTertiaryContainer
val ElectricCyanLight = ElegantBluePhase
val ElectricCyanDark = ElegantBluePhase
val ElectricCyanContainerLight = ElegantBlueContainer
val ElectricCyanContainerDark = ElegantBlueContainer
val HazardRedLight = LightError
val HazardRedDark = DarkError
val HazardContainerLight = LightErrorContainer
val HazardContainerDark = DarkErrorContainer

val NavyDarkBackground = DarkBackground
val NavyDarkSurface = DarkSurface
val NavyDarkSurfaceElevated = DarkSurfaceContainerHighest
val NavyDarkBorder = DarkOutlineVariant
val LightSurfaceElevated = LightSurfaceContainer
val LightBorder = LightOutlineVariant

val DarkTextPrimary = DarkOnSurface
val DarkTextSecondary = DarkOnSurfaceVariant
val LightTextPrimary = LightOnSurface
val LightTextSecondary = LightOnSurfaceVariant

val DarkCharcoal = Color(0xFF1E293B)
val MutedSlateText = Color(0xFF94A3B8)
