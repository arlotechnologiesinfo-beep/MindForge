package com.mindforge101.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavIcon { Home, Games, Progress, Profile }

data class NavTab(val route: String, val label: String, val icon: NavIcon)

/**
 * Floating, rounded bottom navigation. Selection animates a soft pill and a small icon scale;
 * with Reduce motion on, the colour change stays and the scale is dropped.
 */
@Composable
fun MfBottomBar(tabs: List<NavTab>, selectedRoute: String?, onSelect: (NavTab) -> Unit) {
    val reduce = LocalReduceMotion.current
    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MfShapes.Nav,
            color = MfColors.Surface,
            shadowElevation = MfElevation.Floating,
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabs.forEach { tab ->
                    val selected = tab.route == selectedRoute
                    val bg by animateColorAsState(
                        if (selected) MfColors.PrimaryLight else Color.Transparent,
                        tween(if (reduce) MfMotion.Short else MfMotion.Medium, easing = MfMotion.Easing), label = "navBg",
                    )
                    val tint by animateColorAsState(
                        if (selected) MfColors.Primary else MfColors.TextPrimary,
                        tween(MfMotion.Medium, easing = MfMotion.Easing), label = "navTint",
                    )
                    val scale by animateFloatAsState(
                        if (selected && !reduce) 1.1f else 1f,
                        tween(MfMotion.Medium, easing = MfMotion.Easing), label = "navScale",
                    )
                    Column(
                        Modifier.weight(1f).heightIn(min = MfSpacing.MinTouch + 8.dp)
                            .mfClickable(RoundedCornerShape(22.dp), onClickLabel = tab.label) { onSelect(tab) }
                            .background(bg)
                            .semantics {
                                role = Role.Tab
                                this.selected = selected
                                contentDescription = tab.label
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavGlyph(tab.icon, tint, Modifier.size(26.dp).scale(scale))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            tab.label, fontSize = 12.sp, color = tint, maxLines = 1,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

/** One consistent outline family: 24-unit grid, 2-unit round strokes. */
@Composable
private fun NavGlyph(icon: NavIcon, tint: Color, modifier: Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val stroke = Stroke(width = 2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        when (icon) {
            NavIcon.Home -> {
                val roof = Path().apply {
                    moveTo(3f * u, 11f * u); lineTo(12f * u, 3.5f * u); lineTo(21f * u, 11f * u)
                }
                drawPath(roof, tint, style = stroke)
                val body = Path().apply {
                    moveTo(5.5f * u, 9.5f * u); lineTo(5.5f * u, 19.5f * u); lineTo(18.5f * u, 19.5f * u); lineTo(18.5f * u, 9.5f * u)
                }
                drawPath(body, tint, style = stroke)
                drawLine(tint, p(10f, 19.5f), p(10f, 14f), 2f * u, StrokeCap.Round)
                drawLine(tint, p(14f, 19.5f), p(14f, 14f), 2f * u, StrokeCap.Round)
                drawLine(tint, p(10f, 14f), p(14f, 14f), 2f * u, StrokeCap.Round)
            }
            NavIcon.Games -> {
                drawRoundRect(tint, p(2.5f, 7f), Size(19f * u, 11f * u), CornerRadius(5.5f * u), style = stroke)
                drawLine(tint, p(7f, 10.5f), p(7f, 14.5f), 2f * u, StrokeCap.Round)
                drawLine(tint, p(5f, 12.5f), p(9f, 12.5f), 2f * u, StrokeCap.Round)
                drawCircle(tint, 1.1f * u, p(15.5f, 11.5f))
                drawCircle(tint, 1.1f * u, p(18f, 13.5f))
            }
            NavIcon.Progress -> {
                drawLine(tint, p(6f, 19f), p(6f, 12f), 3f * u, StrokeCap.Round)
                drawLine(tint, p(12f, 19f), p(12f, 5f), 3f * u, StrokeCap.Round)
                drawLine(tint, p(18f, 19f), p(18f, 9f), 3f * u, StrokeCap.Round)
            }
            NavIcon.Profile -> {
                drawCircle(tint, 4f * u, p(12f, 8f), style = stroke)
                val shoulders = Path().apply {
                    moveTo(4.5f * u, 20f * u)
                    cubicTo(4.5f * u, 15.5f * u, 8f * u, 14f * u, 12f * u, 14f * u)
                    cubicTo(16f * u, 14f * u, 19.5f * u, 15.5f * u, 19.5f * u, 20f * u)
                }
                drawPath(shoulders, tint, style = stroke)
            }
        }
    }
}
