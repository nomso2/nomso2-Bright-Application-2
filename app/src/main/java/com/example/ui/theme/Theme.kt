package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { true }

// Elegant Dark Color Scheme adhering directly to the design HTML specifications
private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantGoldPrimary,
    onPrimary = Color(0xFF0A0C10),
    primaryContainer = ElegantGoldContainer,
    onPrimaryContainer = ElegantGoldPrimary,

    secondary = ElegantGreenLive,
    onSecondary = Color(0xFF0A0C10),
    secondaryContainer = ElegantGreenContainer,
    onSecondaryContainer = ElegantGreenLive,

    tertiary = ElegantBluePhase,
    onTertiary = Color(0xFF0A0C10),
    tertiaryContainer = ElegantBlueContainer,
    onTertiaryContainer = ElegantBluePhase,

    error = ElegantRedHazard,
    onError = Color.White,
    errorContainer = ElegantRedContainer,
    onErrorContainer = ElegantRedHazard,

    background = ElegantDarkCanvas,
    onBackground = Slate100Text,
    surface = ElegantDarkSurface,
    onSurface = Slate100Text,
    surfaceVariant = ElegantDarkCardStart,
    onSurfaceVariant = Slate400Text,
    outline = ElegantDarkBorder,
    outlineVariant = ElegantDarkBorderLight,
    surfaceTint = ElegantGoldPrimary,

    surfaceBright = ElegantDarkSurfaceBright,
    surfaceDim = ElegantDarkCanvas,
    surfaceContainerLowest = ElegantDarkCanvas,
    surfaceContainerLow = ElegantDarkBar,
    surfaceContainer = ElegantDarkSurface,
    surfaceContainerHigh = ElegantDarkCardStart,
    surfaceContainerHighest = ElegantDarkSurfaceElevated
)

// Light scheme: calm, high-contrast daylight colours. Gold text uses the deeper amber
// primary (#A16207) so it stays readable on white.
private val ElegantLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,

    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = ElegantGoldPrimary,
    surfaceTint = LightSurfaceTint,

    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)

@Composable
fun BrightTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalBrightExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}

