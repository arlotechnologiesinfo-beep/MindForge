package com.mindforge101.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.data.ProfileEntity
import com.mindforge101.app.data.SessionEntity
import com.mindforge101.app.domain.Achievements
import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.DailyMix
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.Ranks
import com.mindforge101.app.games.Catalog
import java.time.LocalDate

internal val Blue = MfColors.Primary
internal val BlueDark = MfColors.PrimaryDeep
internal val BlueSoft = MfColors.PrimaryLight

internal class Tint(val bg: Color, val fg: Color, val glyph: String)

private fun glyphFor(c: Category) = when (c) {
    Category.MEMORY -> "▦"
    Category.ATTENTION -> "◎"
    Category.LOGIC -> "∞"
    Category.MATH -> "±"
    Category.LANGUAGE -> "Aa"
    Category.SPATIAL -> "⬡"
    Category.SPEED -> "⚡"
    Category.EXECUTIVE -> "⇄"
    Category.PROBLEM -> "?"
    Category.VISUAL -> "◈"
}

/** Category colors come from the design system: soft tint background, darker accent for the symbol. */
internal fun tintFor(c: Category): Tint {
    val accent = MfColors.accent(c)
    return Tint(lerp(accent, Color.White, 0.78f), lerp(accent, Color(0xFF14233D), 0.38f), glyphFor(c))
}

@Composable
fun HomeScreen(
    profile: ProfileEntity?,
    sessions: List<SessionEntity>,
    unlockedCount: Int,
    onPlay: (String) -> Unit,
) {
    val today = LocalDate.now().toEpochDay()
    var mixOffset by rememberSaveable { mutableIntStateOf(0) }
    val mix = remember(today, mixOffset) { DailyMix.forDay(today + mixOffset * 7919L, Catalog.games) }
    val doneToday = sessions.filter { it.day == today }.map { it.gameId }.toSet()
    val nextUp = mix.firstOrNull { it.id !in doneToday } ?: mix.first()
    val scores = categoryScores(sessions)
    val weakest = scores.minByOrNull { it.value }?.key ?: Category.ATTENTION
    val pick = remember(today, weakest) {
        Catalog.games.filter { it.category == weakest }.random(kotlin.random.Random(today))
    }
    val xp = profile?.totalXp ?: 0

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(profile?.name ?: "Player")
        RankCard(xp, profile?.streakDays ?: 0)
        StartButton(nextUp, allDone = mix.all { it.id in doneToday }) { onPlay(nextUp.id) }
        MixCard(mix, doneToday, onPlay, onNewMix = { mixOffset++ })
        FocusCard(weakest, pick, hasData = scores.isNotEmpty(), onPlay = { onPlay(pick.id) })
        StatsRow(Catalog.games.size, Category.entries.size, unlockedCount, Achievements.all.size)
    }
}

@Composable
private fun Header(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("MindForge 101", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Blue)
            Spacer(Modifier.height(8.dp))
            Text(
                "Hello, $name", fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Ready to train your brain today?",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(84.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF8DB8FF), Color(0xFFDDE9FD)))),
            contentAlignment = Alignment.Center,
        ) { Text("🧠", fontSize = 42.sp) }
    }
}

@Composable
internal fun CardSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Blue.copy(alpha = 0.15f), spotColor = Blue.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        content = content,
    )
}

@Composable
internal fun RankCard(xp: Int, streak: Int) {
    val rank = Ranks.forXp(xp)
    val next = Ranks.ladder.getOrNull(Ranks.ladder.indexOf(rank) + 1)
    CardSurface {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF4C8DF6), BlueDark))),
                    contentAlignment = Alignment.Center,
                ) { Text("♛", fontSize = 28.sp, color = Color.White) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1.3f)) {
                    Text(rank.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("Your Current Rank", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("★", fontSize = 20.sp, color = Color(0xFFF5A524))
                    Text("%,d XP".format(xp), fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 20.sp)
                    Text(if (streak == 1) "1 Day" else "$streak Days", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    Text("Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            val p = Ranks.progress(xp).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth(p).height(10.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Color(0xFF4C8DF6), Blue))))
            }
            Spacer(Modifier.height(8.dp))
            Row {
                if (next != null) {
                    Text("Next Rank: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(next.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Blue)
                    Spacer(Modifier.weight(1f))
                    Text("%,d XP needed".format(next.minXp - xp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Top rank reached", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Blue)
                }
            }
        }
    }
}

@Composable
private fun StartButton(next: GameDef, allDone: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp)
            .mfClickable(CircleShape, "Start today's brain mix", onClick = onClick)
            .shadow(8.dp, CircleShape, ambientColor = Blue, spotColor = Blue)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF3F7FEF), BlueDark)))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (allDone) "Mix complete. Play again" else "Start Today's Brain Mix",
            color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun MixCard(mix: List<GameDef>, done: Set<String>, onPlay: (String) -> Unit, onNewMix: () -> Unit) {
    CardSurface {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(BlueSoft),
                    contentAlignment = Alignment.Center,
                ) { Text("◎", fontSize = 22.sp, color = Blue) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Today's Brain Mix", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("A quick 5-game workout", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    Modifier.clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        .clickable(onClickLabel = "Shuffle the mix", onClick = onNewMix)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Mix", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mix.forEach { game ->
                    GameTile(game, game.id in done, Modifier.weight(1f)) { onPlay(game.id) }
                }
            }
        }
    }
}

@Composable
private fun GameTile(game: GameDef, done: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val d = androidx.compose.ui.platform.LocalDensity.current
    val t = tintFor(game.category)
    Column(
        modifier.mfClickable(RoundedCornerShape(18.dp), onClick = onClick).background(t.bg)
            .semantics { contentDescription = "${game.name}, ${game.category.label}${if (done) ", done" else ""}" }
            .padding(vertical = 12.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopEnd) { GameIcon(game, 44.dp); if (done) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.clip(CircleShape).background(MfColors.Success).padding(horizontal = 4.dp)) }
        Spacer(Modifier.height(6.dp))
        Text(
            game.name, fontSize = with(d) { 12.dp.toSp() }, lineHeight = with(d) { 15.dp.toSp() }, textAlign = TextAlign.Center, color = t.fg,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FocusCard(category: Category, pick: GameDef, hasData: Boolean, onPlay: () -> Unit) {
    CardSurface {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(BlueSoft),
                contentAlignment = Alignment.Center,
            ) { Text("▂▅▇", fontSize = 16.sp, color = Blue, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Improve Your ${category.label}", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (hasData) "Your recent ${category.label} score is lower. Try ${pick.name} to get better!"
                    else "A good place to start. Try ${pick.name}!",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                Modifier.clip(CircleShape).background(BlueSoft).clickable(onClickLabel = "Play ${pick.name}", onClick = onPlay)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                Text("Play", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StatsRow(games: Int, categories: Int, unlocked: Int, totalAchievements: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatChip("🎮", "$games", "Games", Modifier.weight(0.9f))
        StatChip("◫", "$categories", "Categories", Modifier.weight(1.1f))
        StatChip("🏆", "$unlocked/$totalAchievements", "Achievements", Modifier.weight(1.45f))
    }
}

@Composable
private fun StatChip(icon: String, value: String, label: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
            }
        }
    }
}
