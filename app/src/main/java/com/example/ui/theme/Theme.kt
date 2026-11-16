package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianPrimary,
    onPrimary = ObsidianOnPrimary,
    primaryContainer = ObsidianPrimaryContainer,
    onPrimaryContainer = ObsidianOnPrimaryContainer,
    secondary = ObsidianSecondary,
    onSecondary = ObsidianOnSecondary,
    secondaryContainer = ObsidianSecondaryContainer,
    onSecondaryContainer = ObsidianOnSecondaryContainer,
    tertiary = ObsidianTertiary,
    onTertiary = ObsidianOnTertiary,
    tertiaryContainer = ObsidianTertiaryContainer,
    onTertiaryContainer = ObsidianOnTertiaryContainer,
    background = ObsidianBackground,
    onBackground = ObsidianOnSurface,
    surface = ObsidianSurface,
    onSurface = ObsidianOnSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianOnSurfaceVariant,
    surfaceContainerLowest = ObsidianSurfaceContainerLowest,
    surfaceContainerLow = ObsidianSurfaceContainerLow,
    surfaceContainer = ObsidianSurfaceContainer,
    surfaceContainerHigh = ObsidianSurfaceContainerHigh,
    surfaceContainerHighest = ObsidianSurfaceContainerHighest,
    outline = ObsidianOutline,
    outlineVariant = ObsidianOutlineVariant,
    error = ObsidianError,
    onError = ObsidianOnError,
    errorContainer = ObsidianErrorContainer,
    onErrorContainer = ObsidianOnErrorContainer
)

val ObsidianShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun ObsidianAvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianDarkColorScheme,
        typography = ObsidianTypography,
        shapes = ObsidianShapes,
        content = content
    )
}
