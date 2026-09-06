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
    outlineVariant = ElegantDarkBorderLight
)

// Elegant Light Color Scheme adhering directly to high contrast design guidelines
private val ElegantLightColorScheme = lightColorScheme(
    primary = ElegantGoldLightPrimary,
    onPrimary = Color.White,
    primaryContainer = ElegantGoldLightContainer,
    onPrimaryContainer = ElegantGoldLightPrimary,

    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0x1A16A34A),
    onSecondaryContainer = Color(0xFF15803D),

    tertiary = Color(0xFF2563EB),
    onTertiary = Color.White,
    tertiaryContainer = Color(0x1A2563EB),
    onTertiaryContainer = Color(0xFF1D4ED8),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0x1ADC2626),
    onErrorContainer = Color(0xFFB91C1C),

    background = ElegantLightCanvas,
    onBackground = Slate900Text,
    surface = ElegantLightSurface,
    onSurface = Slate900Text,
    surfaceVariant = ElegantLightCardEnd,
    onSurfaceVariant = Slate600Text,
    outline = ElegantLightBorder,
    outlineVariant = ElegantLightBorderLight
)

@Composable
fun BrightTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}

