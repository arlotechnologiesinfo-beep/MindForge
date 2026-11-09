package com.mindforge101.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mindforge101.app.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Short branded opening: logo fades in and settles from 0.9 to 1.0, then the whole overlay fades out
 * into Home. [onDone] is called once the fade-out finishes. Reduce motion uses a plain quick fade.
 */
@Composable
fun SplashOverlay(reduceMotion: Boolean, onDone: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(if (reduceMotion) 1f else 0.9f) }
    val overlay = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val fadeIn = async { alpha.animateTo(1f, tween(if (reduceMotion) 200 else 450, easing = MfMotion.Easing)) }
            val settle = async { if (!reduceMotion) scale.animateTo(1f, tween(600, easing = MfMotion.Easing)) }
            fadeIn.await(); settle.await()
        }
        delay(if (reduceMotion) 300 else 450)
        overlay.animateTo(0f, tween(MfMotion.Medium, easing = MfMotion.Easing))
        onDone()
    }

    Box(
        Modifier.fillMaxSize().alpha(overlay.value).background(Color(0xFF000A12)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.mf_logo),
            contentDescription = "MindForge",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().alpha(alpha.value).scale(scale.value),
        )
    }
}

/** App mark: soft blue rounded tile with a simple geometric "forged" hexagon. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(MfColors.PrimarySecondary, MfColors.Primary))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            fun hex(r: Float) = Path().apply {
                for (i in 0 until 6) {
                    val a = Math.toRadians((60.0 * i) - 90.0)
                    val x = c.x + r * Math.cos(a).toFloat()
                    val y = c.y + r * Math.sin(a).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(hex(size.minDimension * 0.30f), Color.White)
            drawPath(hex(size.minDimension * 0.15f), MfColors.Primary)
        }
    }
}
