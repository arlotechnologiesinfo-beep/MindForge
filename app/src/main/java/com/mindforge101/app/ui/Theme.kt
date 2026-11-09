package com.mindforge101.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** The app uses one calm light theme everywhere (see [MfColors]). */
private val Light = lightColorScheme(
    primary = MfColors.Primary,
    onPrimary = Color.White,
    primaryContainer = MfColors.PrimaryLight,
    onPrimaryContainer = MfColors.PrimaryDeep,
    secondary = MfColors.Success,
    onSecondary = Color.White,
    secondaryContainer = MfColors.SuccessSoft,
    onSecondaryContainer = Color(0xFF14533A),
    tertiary = Color(0xFFC2610F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFDEBD3),
    onTertiaryContainer = Color(0xFF4A2402),
    error = MfColors.Danger,
    errorContainer = MfColors.DangerSoft,
    onErrorContainer = Color(0xFF5F1B19),
    background = MfColors.Background,
    onBackground = MfColors.TextPrimary,
    surface = MfColors.Background,
    onSurface = MfColors.TextPrimary,
    surfaceVariant = Color(0xFFE3EAF5),
    onSurfaceVariant = MfColors.TextSecondary,
    surfaceContainer = MfColors.Surface,
    surfaceContainerHigh = MfColors.SurfaceSecondary,
    outline = MfColors.TextMuted,
)

@Composable
fun MindForgeTheme(largeText: Boolean, reduceMotion: Boolean = false, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scaled = if (largeText) Density(density.density, density.fontScale * 1.25f) else density
    MaterialTheme(colorScheme = Light) {
        CompositionLocalProvider(
            LocalDensity provides scaled,
            LocalReduceMotion provides reduceMotion,
            content = content,
        )
    }
}
