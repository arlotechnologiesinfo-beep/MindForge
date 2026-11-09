package com.mindforge101.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mindforge101.app.domain.Category

/** Central design tokens. Presentation only: no business logic lives here. */
object MfColors {
    val Background = Color(0xFFF6F8FC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSecondary = Color(0xFFEEF4FC)

    val Primary = Color(0xFF3985E8)
    val PrimaryDeep = Color(0xFF2A6DD0)
    val PrimaryLight = Color(0xFFE4F0FF)
    val PrimarySecondary = Color(0xFF76ADF4)

    val TextPrimary = Color(0xFF14233D)
    val TextSecondary = Color(0xFF60718D)
    val TextMuted = Color(0xFF94A3B8)

    val Success = Color(0xFF2F9E6E)
    val SuccessSoft = Color(0xFFE3F5EC)
    val Danger = Color(0xFFD9534F)
    val DangerSoft = Color(0xFFFCE9E8)
    val Warning = Color(0xFFE59A1F)

    fun accent(c: Category): Color = when (c) {
        Category.MEMORY -> Color(0xFFA58AEF)
        Category.ATTENTION -> Color(0xFF69CDBB)
        Category.LOGIC -> Color(0xFF6C9FEA)
        Category.MATH -> Color(0xFFF3B16F)
        Category.LANGUAGE -> Color(0xFFD99BC6)
        Category.SPATIAL -> Color(0xFF76CDB9)
        Category.SPEED -> Color(0xFF64BCEB)
        Category.EXECUTIVE -> Color(0xFFA998E9)
        Category.PROBLEM -> Color(0xFFE8BD72)
        Category.VISUAL -> Color(0xFFA78BEA)
    }
}

object MfSpacing {
    val ScreenH: Dp = 20.dp
    val ScreenV: Dp = 16.dp
    val Section: Dp = 16.dp
    val Card: Dp = 16.dp
    val MinTouch: Dp = 48.dp
}

object MfShapes {
    val Card = RoundedCornerShape(24.dp)
    val Tile = RoundedCornerShape(18.dp)
    val Chip = RoundedCornerShape(50)
    val Nav = RoundedCornerShape(28.dp)
}

object MfElevation {
    val Card: Dp = 4.dp
    val Floating: Dp = 10.dp
}

/** One shared motion vocabulary so no screen invents its own timings. */
object MfMotion {
    const val Short = 150
    const val Medium = 250
    const val Long = 400
    val Easing = FastOutSlowInEasing
}

/** True when the player turned on "Reduce motion". Provided by [MindForgeTheme]. */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * Standard pressable surface: ripple, clipped to [shape], gently scales to 0.98 while pressed
 * (skipped when Reduce motion is on).
 */
fun Modifier.mfClickable(
    shape: Shape = RoundedCornerShape(0.dp),
    onClickLabel: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val reduce = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduce) 0.98f else 1f,
        animationSpec = tween(MfMotion.Short, easing = MfMotion.Easing),
        label = "press",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .clickable(
            interactionSource = source,
            indication = ripple(),
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
}
