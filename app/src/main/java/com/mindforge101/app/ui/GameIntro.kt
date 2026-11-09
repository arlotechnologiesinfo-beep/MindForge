package com.mindforge101.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.ROUNDS_PER_SESSION

/** Shared intro template, personalised per game by icon, category colour and instructions. */
@Composable
fun GameIntro(game: GameDef, level: Int, bestScore: Int?, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val accent = MfColors.accent(game.category)
    val badgeFg = lerp(accent, Color(0xFF14233D), 0.45f)
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            MfLottie(com.mindforge101.app.R.raw.ring_pulse, Modifier.size(160.dp))
            GameIcon(game, 112.dp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            game.category.label,
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = badgeFg,
            modifier = Modifier.clip50().background(lerp(accent, Color.White, 0.8f)).padding(horizontal = 14.dp, vertical = 6.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            game.name, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            GameInstructions.forGame(game),
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoChip("Level", "$level", Modifier.weight(1f))
            InfoChip("Best score", bestScore?.toString() ?: "–", Modifier.weight(1f))
            InfoChip("Rounds", "$ROUNDS_PER_SESSION", Modifier.weight(1f))
        }
        Spacer(Modifier.height(28.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp)
                .mfClickable(CircleShape, "Start ${game.name}", onClick = onStart)
                .shadow(8.dp, CircleShape, ambientColor = Blue, spotColor = Blue)
                .background(Brush.horizontalGradient(listOf(Color(0xFF3F7FEF), BlueDark)))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) { Text("Start Game", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(16.dp))
    }
}

private fun Modifier.clip50() = this.then(Modifier.clip(CircleShape))

@Composable
private fun InfoChip(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(MfColors.Surface).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
