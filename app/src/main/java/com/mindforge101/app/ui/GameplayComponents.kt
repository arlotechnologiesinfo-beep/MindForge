package com.mindforge101.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Category accent of the game being played, so cells and pads match the game's icon colour. */
val LocalAccent = compositionLocalOf { MfColors.Primary }

enum class OptionState { Idle, Correct, Wrong, Dim }

/** Thin rounded bar used for round progress and the countdown. */
@Composable
fun SoftBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp, animate: Boolean = true) {
    val reduce = LocalReduceMotion.current
    val f by animateFloatAsState(
        fraction.coerceIn(0f, 1f),
        tween(if (animate && !reduce) MfMotion.Long else 0, easing = MfMotion.Easing), label = "bar",
    )
    Box(modifier.height(height).clip(CircleShape).background(MfColors.PrimaryLight)) {
        Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(CircleShape).background(color))
    }
}

/** Multiple-choice answer: soft green/red feedback with a mark, never colour alone. */
@Composable
fun AnswerOption(text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val reduce = LocalReduceMotion.current
    val spec = tween<Color>(if (reduce) MfMotion.Short else MfMotion.Medium, easing = MfMotion.Easing)
    val bg by animateColorAsState(
        when (state) { OptionState.Correct -> MfColors.SuccessSoft; OptionState.Wrong -> MfColors.DangerSoft; else -> MfColors.Surface },
        spec, label = "optBg",
    )
    val border by animateColorAsState(
        when (state) { OptionState.Correct -> MfColors.Success; OptionState.Wrong -> MfColors.Danger; else -> MfColors.TextMuted.copy(alpha = 0.35f) },
        spec, label = "optBorder",
    )
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).heightIn(min = 60.dp)
            .alpha(if (state == OptionState.Dim) 0.55f else 1f)
            .mfClickable(shape, enabled = enabled, onClick = onClick)
            .background(bg)
            .border(1.5.dp, border, shape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        when (state) {
            OptionState.Correct -> Text("✓", color = MfColors.Success, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            OptionState.Wrong -> Text("✗", color = MfColors.Danger, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            else -> {}
        }
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** Keypad key for number entry. */
@Composable
fun KeyButton(label: String, enabled: Boolean, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.heightIn(min = 60.dp)
            .mfClickable(shape, enabled = enabled, onClick = onClick)
            .background(if (primary) MfColors.Primary else MfColors.Surface)
            .border(1.dp, if (primary) MfColors.Primary else MfColors.TextMuted.copy(alpha = 0.3f), shape)
            .semantics { contentDescription = when (label) { "⌫" -> "Delete"; "OK" -> "Submit"; else -> label } },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = if (primary) Color.White else MaterialTheme.colorScheme.onBackground)
    }
}

/** Square play cell used by grids, pads and odd-one-out boards. Colour changes animate. */
@Composable
fun PlayCell(
    text: String,
    background: Color,
    description: String,
    modifier: Modifier,
    fontSize: Int = 24,
    borderColor: Color = MfColors.TextMuted.copy(alpha = 0.35f),
    onClick: (() -> Unit)? = null,
) {
    val reduce = LocalReduceMotion.current
    val bg by animateColorAsState(background, tween(if (reduce) MfMotion.Short else MfMotion.Medium, easing = MfMotion.Easing), label = "cellBg")
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.aspectRatio(1f)
            .then(if (onClick != null) Modifier.mfClickable(shape, onClick = onClick) else Modifier.clip(shape))
            .background(bg)
            .border(1.5.dp, borderColor, shape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** Soft pill that confirms the last answer. Fades (and gently scales unless Reduce motion is on). */
@Composable
fun FeedbackPill(correct: Boolean?) {
    val reduce = LocalReduceMotion.current
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = correct != null,
            enter = fadeIn(tween(MfMotion.Short)) + if (reduce) androidx.compose.animation.EnterTransition.None else scaleIn(tween(MfMotion.Medium), initialScale = 0.9f),
            exit = fadeOut(tween(MfMotion.Short)),
        ) {
            val ok = correct == true
            Row(
                Modifier.clip(CircleShape).background(if (ok) MfColors.SuccessSoft else MfColors.DangerSoft)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (ok) "✓" else "✗", color = if (ok) MfColors.Success else MfColors.Danger, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(Modifier.width(8.dp))
                Text(
                    if (ok) "Correct" else "Not quite",
                    color = if (ok) MfColors.Success else MfColors.Danger, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                )
            }
        }
    }
}

/** White rounded surface that holds the question stimulus. */
@Composable
fun QuestionSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MfColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

fun accentSoft(accent: Color): Color = lerp(accent, Color.White, 0.72f)
fun accentDeep(accent: Color): Color = lerp(accent, Color(0xFF14233D), 0.4f)
