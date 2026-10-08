package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colours Material 3's ColorScheme doesn't have (success / warning / info),
 * with separate light and dark values so light mode keeps enough contrast.
 *
 * Usage inside a @Composable: MaterialTheme.extendedColors.success
 */
@Immutable
data class BrightExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color
)

val DarkExtendedColors = BrightExtendedColors(
    success = DarkPrimary,
    onSuccess = DarkOnPrimary,
    successContainer = DarkPrimaryContainer,
    warning = WarningAmber, // #FBBF24, matches dark tertiary
    onWarning = DarkOnTertiary,
    warningContainer = WarningAmber.copy(alpha = 0.15f),
    info = InfoSky,
    onInfo = ElegantDarkCanvas,
    infoContainer = InfoSky.copy(alpha = 0.15f)
)

val LightExtendedColors = BrightExtendedColors(
    success = LightPrimary, // calm green #15803D: readable on white
    onSuccess = Color.White,
    successContainer = LightPrimaryContainer,
    warning = WarningAmberDeep, // #B45309 amber-700, matches light tertiary
    onWarning = Color.White,
    warningContainer = Color(0x1AB45309),
    info = InfoSkyDark,
    onInfo = Color.White,
    infoContainer = Color(0x1A0284C7)
)

val LocalBrightExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Bright's extra semantic colours for the current theme. */
val MaterialTheme.extendedColors: BrightExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBrightExtendedColors.current
