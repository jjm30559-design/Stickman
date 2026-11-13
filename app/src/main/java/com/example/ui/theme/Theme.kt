package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StickManDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = DeepSpaceNavy,
    primaryContainer = Color(0xFF1E3264),
    onPrimaryContainer = TextPrimary,
    secondary = ElectricBlue,
    onSecondary = DeepSpaceNavy,
    secondaryContainer = Color(0xFF232C58),
    onSecondaryContainer = TextPrimary,
    tertiary = SoftPurple,
    onTertiary = DeepSpaceNavy,
    tertiaryContainer = Color(0xFF33265A),
    onTertiaryContainer = TextPrimary,
    background = DeepSpaceNavy,
    onBackground = TextPrimary,
    surface = DeepSpaceMid,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF1D254D),
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderSoft,
    outlineVariant = GlassBorderLight,
)

@Composable
fun StickManStudioTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = StickManDarkColorScheme,
        typography = StudioTypography,
        content = content,
    )
}
