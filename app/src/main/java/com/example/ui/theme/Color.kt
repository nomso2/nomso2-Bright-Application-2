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

val LightBackground = ElegantDarkCanvas
val LightSurface = ElegantDarkSurface
val LightSurfaceElevated = ElegantDarkSurfaceElevated
val LightBorder = ElegantDarkBorder

val DarkTextPrimary = Slate100Text
val DarkTextSecondary = Slate400Text
val LightTextPrimary = Slate100Text
val LightTextSecondary = Slate400Text
