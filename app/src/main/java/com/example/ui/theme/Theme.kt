package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun BrightTheme(
    darkTheme: Boolean = true, // Default to Elegant Dark as instructed
    dynamicColor: Boolean = false, // Preserve Elegant Dark palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}
