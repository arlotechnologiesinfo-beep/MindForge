package com.mindforge101.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.data.SessionOutcome
import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import java.util.Locale

@Composable
fun ResultScreen(outcome: SessionOutcome, onAgain: () -> Unit, onDone: () -> Unit) {
    val r = outcome.result
    val reduce = LocalReduceMotion.current
    val before = AdaptiveDifficultyEngine.levelForPlay(outcome.previousLevel)
    val after = AdaptiveDifficultyEngine.levelForPlay(outcome.nextLevel)
    val levelLine = when {
        after > before -> "Next time: level $after, a step up"
        after < before -> "Next time: level $after, a step easier"
        else -> "Next time: level $after again"
    }
    val pop = remember { Animatable(if (reduce) 1f else 0.85f) }
    val fade = remember { Animatable(if (reduce) 1f else 0f) }
    LaunchedEffect(Unit) { if (!reduce) fade.animateTo(1f, tween(MfMotion.Long, easing = MfMotion.Easing)) }
    LaunchedEffect(Unit) { if (!reduce) pop.animateTo(1f, tween(MfMotion.Long, easing = MfMotion.Easing)) }

    Column(
        Modifier.fillMaxSize().background(MfColors.Background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            if (outcome.newBest) MfLottie(com.mindforge101.app.R.raw.burst, Modifier.size(150.dp), loop = false, stillProgress = 0.3f)
            Box(
                Modifier.size(84.dp).scale(pop.value).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(MfColors.PrimarySecondary, MfColors.Primary))),
                contentAlignment = Alignment.Center,
            ) { Text(if (outcome.newBest) "★" else "✓", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold) }
        }
        Text(outcome.gameName, fontSize = 16.sp, color = MfColors.TextSecondary, modifier = Modifier.alpha(fade.value))
        Text(
            "${r.score}", fontSize = 64.sp, lineHeight = 72.sp, fontWeight = FontWeight.Bold,
            color = MfColors.TextPrimary, modifier = Modifier.scale(pop.value),
        )
        Text(
            if (outcome.newBest) "★ New best! Previous ${outcome.previousBest}" else "Best ${outcome.previousBest}",
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            color = if (outcome.newBest) MfColors.Warning else MfColors.TextSecondary,
        )
        Spacer(Modifier.height(20.dp))
        Column(Modifier.alpha(fade.value), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Correct", "${r.correct} of ${r.rounds}", Modifier.weight(1f))
                StatCard("Accuracy", "${(r.accuracy * 100).toInt()}%", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Average time", String.format(Locale.US, "%.1f s", r.averageTimeMs / 1000f), Modifier.weight(1f))
                StatCard("Best streak", r.bestStreak.toString(), Modifier.weight(1f))
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MfColors.PrimaryLight).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("XP earned", Modifier.weight(1f), color = MfColors.TextSecondary)
                Text("+${r.xp}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MfColors.PrimaryDeep)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(levelLine, fontSize = 15.sp, color = MfColors.TextSecondary, textAlign = TextAlign.Center)
        if (outcome.unlocked.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MfColors.Surface)
                    .border(1.5.dp, MfColors.Warning, RoundedCornerShape(20.dp)).padding(16.dp),
            ) {
                Text("🏆 Achievement unlocked", fontWeight = FontWeight.SemiBold, color = MfColors.Warning)
                outcome.unlocked.forEach {
                    Spacer(Modifier.height(6.dp))
                    Text(it.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(it.description, fontSize = 14.sp, color = MfColors.TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .mfClickable(CircleShape, "Play again", onClick = onAgain)
                .shadow(8.dp, CircleShape, ambientColor = MfColors.Primary, spotColor = MfColors.Primary)
                .background(Brush.horizontalGradient(listOf(Color(0xFF3F7FEF), MfColors.PrimaryDeep)))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
        ) { Text("Play again", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .mfClickable(CircleShape, "Done", onClick = onDone)
                .background(MfColors.Surface)
                .border(1.5.dp, MfColors.Primary, CircleShape)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
        ) { Text("Done", color = MfColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(MfColors.Surface).padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MfColors.TextPrimary, textAlign = TextAlign.Center)
        Text(label, fontSize = 13.sp, color = MfColors.TextSecondary, textAlign = TextAlign.Center)
    }
}
