package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Elegant Dark Palette (from Design HTML specification)
val ElegantDarkCanvas = Color(0xFF0A0C10) // Deep noir background
val ElegantDarkBar = Color(0xFF11141B) // App header & navigation bar background
val ElegantDarkCardStart = Color(0xFF1C1F26) // Card gradient start
val ElegantDarkCardEnd = Color(0xFF14171E) // Card gradient end
val ElegantDarkSurface = Color(0xFF161920) // Secondary card & container surface
val ElegantDarkSurfaceElevated = Color(0xFF1F242F) // Elevated dialog & modal surface
val ElegantDarkBorder = Color(0xFF1E2430) // Subtle slate-800 border
val ElegantDarkBorderLight = Color(0xFF2E384A) // Slate-700 border for interactive components

// Accent Colors
val ElegantGoldPrimary = Color(0xFFFACC15) // Signature Bright gold #FACC15
val ElegantGoldDark = Color(0xFFEAB308) // Darker gold shade for pressed state
val ElegantGoldContainer = Color(0x2BFACC15) // Gold 17% alpha for badges and highlights

val ElegantGreenLive = Color(0xFF22C55E) // Live feed emerald pulse & verified meters
val ElegantGreenContainer = Color(0x1A22C55E) // Green 10% alpha

val ElegantBluePhase = Color(0xFF60A5FA) // Phase voltage & telemetry cyan/blue
val ElegantBlueContainer = Color(0x1A60A5FA) // Blue 10% alpha

val ElegantRedHazard = Color(0xFFEF4444) // Life safety emergency hazard
val ElegantRedContainer = Color(0x26EF4444) // Hazard 15% container

// Typography / Slates
val Slate100Text = Color(0xFFF1F5F9) // Primary text
val Slate300Text = Color(0xFFCBD5E1) // Secondary text
val Slate400Text = Color(0xFF94A3B8) // Muted labels & subtitles
val Slate500Text = Color(0xFF64748B) // Metadata & timestamps
val Slate700Icon = Color(0xFF334155) // Inactive state

// Elegant Light Palette (High-contrast, crisp styling)
val ElegantLightCanvas = Color(0xFFF8FAFC) // Crisp daylight canvas #F8FAFC
val ElegantLightBar = Color(0xFFFFFFFF) // Clean header & nav bar background
val ElegantLightCardStart = Color(0xFFFFFFFF) // Pure white card surface
val ElegantLightCardEnd = Color(0xFFF1F5F9) // Slate-100 card subtle gradient
val ElegantLightSurface = Color(0xFFFFFFFF) // Surface container
val ElegantLightSurfaceElevated = Color(0xFFF1F5F9) // Elevated modal
val ElegantLightBorder = Color(0xFFE2E8F0) // Subtle border
val ElegantLightBorderLight = Color(0xFFCBD5E1) // Interactive border

val Slate900Text = Color(0xFF0F172A) // Dark slate primary text in light mode
val Slate800Text = Color(0xFF1E293B) // Dark slate titles
val Slate600Text = Color(0xFF475569) // Secondary text in light mode
val ElegantGoldLightPrimary = Color(0xFFD97706) // Rich amber-gold for high-contrast light mode
val ElegantGoldLightContainer = Color(0x1AD97706)


// Legacy aliases mapped to Elegant Dark for theme compatibility
val GoldPrimary = ElegantGoldPrimary
val EmeraldAccent = ElegantGreenLive

val ElectricGreenLight = ElegantGoldPrimary
val ElectricGreenDark = ElegantGoldPrimary
val ElectricGreenContainerLight = ElegantGoldContainer
val ElectricGreenContainerDark = ElegantGoldContainer

val AmberElectricLight = ElegantGoldPrimary
val AmberElectricDark = ElegantGoldPrimary
val AmberContainerLight = ElegantGoldContainer
val AmberContainerDark = ElegantGoldContainer

val ElectricCyanLight = ElegantBluePhase
val ElectricCyanDark = ElegantBluePhase
val ElectricCyanContainerLight = ElegantBlueContainer
val ElectricCyanContainerDark = ElegantBlueContainer

val HazardRedLight = ElegantRedHazard
val HazardRedDark = ElegantRedHazard
val HazardContainerLight = ElegantRedContainer
val HazardContainerDark = ElegantRedContainer

val NavyDarkBackground = ElegantDarkCanvas
val NavyDarkSurface = ElegantDarkSurface
val NavyDarkSurfaceElevated = ElegantDarkSurfaceElevated
val NavyDarkBorder = ElegantDarkBorder

val LightBackground = ElegantLightCanvas
val LightSurface = ElegantLightSurface
val LightSurfaceElevated = ElegantLightSurfaceElevated
val LightBorder = ElegantLightBorder

val DarkTextPrimary = Slate100Text
val DarkTextSecondary = Slate400Text
val LightTextPrimary = Slate900Text
val LightTextSecondary = Slate600Text

val DarkCharcoal = Color(0xFF1E293B)
val MutedSlateText = Color(0xFF94A3B8)

// Named semantic accents (use MaterialTheme.colorScheme / MaterialTheme.extendedColors inside
// composables; these constants are for non-composable code such as Canvas drawing and models).
val SuccessEmerald = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val InfoSky = Color(0xFF38BDF8)
val InfoSkyDark = Color(0xFF0284C7)
val InfoBlue = Color(0xFF2563EB)
val InfoBlueBright = Color(0xFF3B82F6)
val HazardRedDeep = Color(0xFFDC2626)
val GreenDeep = Color(0xFF16A34A)
val GreenSoft = Color(0xFF4ADE80)

// Always-dark "console" panels (gateway / telemetry screens are designed dark in both themes)
val ConsoleDarkBackground = Color(0xFF0F172A)
val ConsoleDarkSurface = Color(0xFF1E293B)

// Light scheme tokens (single place to swap the light palette)
val LightPrimary = Color(0xFFA16207)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFEF3C7)
val LightOnPrimaryContainer = Color(0xFF713F12)
val LightSecondary = Color(0xFF15803D)
val LightSecondaryContainer = Color(0xFFDCFCE7)
val LightOnSecondaryContainer = Color(0xFF14532D)
val LightTertiary = Color(0xFF1D4ED8)
val LightTertiaryContainer = Color(0xFFDBEAFE)
val LightOnTertiaryContainer = Color(0xFF1E3A8A)
val LightError = Color(0xFFB91C1C)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF7F1D1D)
val LightBackground = Color(0xFFFAFAF7)
val LightOnBackground = Color(0xFF111827)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF111827)
val LightSurfaceVariant = Color(0xFFF1F2F4)
val LightOnSurfaceVariant = Color(0xFF374151)
val LightOutline = Color(0xFF9CA3AF)
val LightOutlineVariant = Color(0xFFD1D5DB)
val LightInverseSurface = Color(0xFF1F2937)
val LightInverseOnSurface = Color(0xFFF9FAFB)
val LightSurfaceTint = Color(0xFFA16207)
val LightSurfaceBright = Color(0xFFFFFFFF)
val LightSurfaceDim = Color(0xFFE5E7EB)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF9FAFB)
val LightSurfaceContainer = Color(0xFFF3F4F6)
val LightSurfaceContainerHigh = Color(0xFFEDEEF1)
val LightSurfaceContainerHighest = Color(0xFFE5E7EB)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightOnError = Color(0xFFFFFFFF)
val ElegantDarkSurfaceBright = Color(0xFF2A2F3A) // Dark scheme surfaceBright
