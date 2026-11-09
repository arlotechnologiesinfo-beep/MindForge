package com.mindforge101.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val Light = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E0FF),
    onPrimaryContainer = Color(0xFF1E1A6B),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF1EA),
    onSecondaryContainer = Color(0xFF06332F),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFDE9C8),
    onTertiaryContainer = Color(0xFF4A2402),
    error = Color(0xFFB91C1C),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF5F0F0F),
    background = Color(0xFFF7F6FB),
    onBackground = Color(0xFF17161F),
    surface = Color(0xFFF7F6FB),
    onSurface = Color(0xFF17161F),
    surfaceVariant = Color(0xFFE9E7F2),
    onSurfaceVariant = Color(0xFF4A4858),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFEFEDF7),
    outline = Color(0xFF8A8799),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB9B5FF),
    onPrimary = Color(0xFF211C78),
    primaryContainer = Color(0xFF332E96),
    onPrimaryContainer = Color(0xFFE2E0FF),
    secondary = Color(0xFF7FDACB),
    onSecondary = Color(0xFF06332F),
    secondaryContainer = Color(0xFF0F4F49),
    onSecondaryContainer = Color(0xFFCCF1EA),
    tertiary = Color(0xFFF6C177),
    onTertiary = Color(0xFF4A2402),
    tertiaryContainer = Color(0xFF6B3A08),
    onTertiaryContainer = Color(0xFFFDE9C8),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF121118),
    onBackground = Color(0xFFE8E6F0),
    surface = Color(0xFF121118),
    onSurface = Color(0xFFE8E6F0),
    surfaceVariant = Color(0xFF2C2A38),
    onSurfaceVariant = Color(0xFFC6C3D3),
    surfaceContainer = Color(0xFF1C1B25),
    surfaceContainerHigh = Color(0xFF26242F),
    outline = Color(0xFF8F8CA0),
)

@Composable
fun MindForgeTheme(largeText: Boolean, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scaled = if (largeText) Density(density.density, density.fontScale * 1.25f) else density
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light) {
        CompositionLocalProvider(LocalDensity provides scaled, content = content)
    }
}
