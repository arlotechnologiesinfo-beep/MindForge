package com.mindforge101.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindforge101.app.data.AchievementEntity
import com.mindforge101.app.data.GameProgressEntity
import com.mindforge101.app.data.ProfileEntity
import com.mindforge101.app.data.SessionEntity
import com.mindforge101.app.data.SessionOutcome
import com.mindforge101.app.data.Settings
import com.mindforge101.app.domain.Achievements
import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.DailyMix
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.Ranks
import com.mindforge101.app.games.Catalog
import java.time.LocalDate
import java.util.Locale

private val ScreenPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun ScoreBar(label: String, score: Int?) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(score?.let { "$it%" } ?: "Not played", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { (score ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RankPanel(xp: Int, streak: Int) {
    val rank = Ranks.forXp(xp)
    val next = Ranks.ladder.getOrNull(Ranks.ladder.indexOf(rank) + 1)
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(rank.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("$xp XP", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                if (streak == 1) "1 day streak" else "$streak day streak",
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(progress = { Ranks.progress(xp) }, modifier = Modifier.fillMaxWidth())
        if (next != null) {
            Spacer(Modifier.height(4.dp))
            Text("${next.minXp - xp} XP to ${next.name}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun HomeScreen(
    profile: ProfileEntity?,
    sessions: List<SessionEntity>,
    onPlay: (String) -> Unit,
) {
    val today = LocalDate.now().toEpochDay()
    val mix = remember(today) { DailyMix.forDay(today, Catalog.games) }
    val doneToday = sessions.filter { it.day == today }.map { it.gameId }.toSet()
    val nextUp = mix.firstOrNull { it.id !in doneToday }
    val scores = categoryScores(sessions)
    val weakest = scores.minByOrNull { it.value }?.key

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding) {
        item {
            Text("Hello, ${profile?.name ?: "Player"}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            RankPanel(profile?.totalXp ?: 0, profile?.streakDays ?: 0)
            SectionTitle("Today's Brain Mix")
        }
        items(mix, key = { it.id }) { game ->
            val done = game.id in doneToday
            Row(
                Modifier.fillMaxWidth().clickable { onPlay(game.id) }.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(game.name, style = MaterialTheme.typography.bodyLarge)
                    Text(game.category.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (done) "✓ Done" else "Play", color = if (done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
        }
        item {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { (nextUp ?: mix.first()).let { onPlay(it.id) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(if (nextUp != null) "Start: ${nextUp.name}" else "Mix complete. Play again") }
            if (weakest != null) {
                SectionTitle("Suggested focus")
                val pick = remember(today, weakest) { Catalog.games.filter { it.category == weakest }.random(kotlin.random.Random(today)) }
                Panel {
                    Text("${weakest.label} is your lowest recent area.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onPlay(pick.id) }) { Text("Try ${pick.name}") }
                }
            }
        }
    }
}

@Composable
fun GamesScreen(progress: Map<String, GameProgressEntity>, onPlay: (String) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<Category?>(null) }
    val games = Catalog.games.filter { selected == null || it.category == selected }
    Column(Modifier.fillMaxSize()) {
        Text(
            "${Catalog.games.size} games", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = selected == null, onClick = { selected = null }, label = { Text("All") }) }
            items(Category.entries) { c ->
                FilterChip(selected = selected == c, onClick = { selected = c }, label = { Text(c.label) })
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = ScreenPadding) {
            items(games, key = { it.id }) { game -> GameRow(game, progress[game.id], onPlay) }
        }
    }
}

@Composable
private fun GameRow(game: GameDef, progress: GameProgressEntity?, onPlay: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onPlay(game.id) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(game.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(game.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("Level ${AdaptiveDifficultyEngine.levelForPlay(progress?.level ?: 1f)}", style = MaterialTheme.typography.labelLarge)
            if (progress != null && progress.bestScore > 0) {
                Text("Best ${progress.bestScore}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun ProgressScreen(profile: ProfileEntity?, sessions: List<SessionEntity>, unlocked: List<AchievementEntity>) {
    val scores = categoryScores(sessions)
    val unlockedIds = unlocked.map { it.id }.toSet()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding) {
        item {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            RankPanel(profile?.totalXp ?: 0, profile?.streakDays ?: 0)
            SectionTitle("Game performance")
            Panel { Category.entries.forEach { ScoreBar(it.label, scores[it]) } }
            Spacer(Modifier.height(8.dp))
            Text(
                "Recent accuracy in these games. This is not a medical or IQ measure.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionTitle("Achievements (${unlockedIds.size} of ${Achievements.all.size})")
        }
        items(Achievements.all, key = { it.id }) { a ->
            val got = a.id in unlockedIds
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(a.title, fontWeight = FontWeight.Medium, color = if (got) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(a.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (got) "✓ Earned" else "Locked", style = MaterialTheme.typography.labelLarge, color = if (got) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun ProfileScreen(
    profiles: List<ProfileEntity>,
    active: ProfileEntity?,
    settings: Settings,
    vm: AppViewModel,
) {
    var nameDialog by remember { mutableStateOf<ProfileEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ProfileEntity?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ScreenPadding)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        SectionTitle("Players on this device")
        Panel {
            profiles.forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = p.id == active?.id, onClick = { vm.switchProfile(p.id) })
                    Text(p.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { nameDialog = p }) { Text("Rename") }
                    if (profiles.size > 1) TextButton(onClick = { deleting = p }) { Text("Delete") }
                }
            }
            TextButton(onClick = { adding = true }) { Text("Add player") }
        }
        SectionTitle("Settings")
        Panel {
            SettingRow("Large text", settings.largeText, vm::setLargeText)
            SettingRow("Reduce motion", settings.reduceMotion, vm::setReduceMotion)
            SettingRow("Vibrate on wrong answers", settings.haptics, vm::setHaptics)
        }
        SectionTitle("Privacy")
        Panel {
            Text(
                "MindForge 101 works fully offline. It has no accounts, no ads and no internet access. " +
                    "Your progress stays on this device and is removed when you uninstall the app.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "These games are for practice and fun. They do not diagnose, treat or prevent any condition.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (adding) {
        NameDialog("Add player", "", onDismiss = { adding = false }) {
            vm.addProfile(it)
            adding = false
        }
    }
    nameDialog?.let { p ->
        NameDialog("Rename player", p.name, onDismiss = { nameDialog = null }) {
            vm.renameProfile(p.id, it)
            nameDialog = null
        }
    }
    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete ${p.name}?") },
            text = { Text("This removes their progress, scores and achievements from this device. It can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProfile(p.id)
                    deleting = null
                }) { Text("Delete player") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep player") } },
        )
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it.take(24) }, label = { Text("Name") }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun ResultScreen(outcome: SessionOutcome, onAgain: () -> Unit, onDone: () -> Unit) {
    val r = outcome.result
    val before = AdaptiveDifficultyEngine.levelForPlay(outcome.previousLevel)
    val after = AdaptiveDifficultyEngine.levelForPlay(outcome.nextLevel)
    val levelLine = when {
        after > before -> "Next time: level $after, a step up"
        after < before -> "Next time: level $after, a step easier"
        else -> "Next time: level $after again"
    }
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(outcome.gameName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${r.score}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.SemiBold)
        Text(
            if (outcome.newBest) "New best! Previous ${outcome.previousBest}" else "Best ${outcome.previousBest}",
            color = if (outcome.newBest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Panel {
            StatRow("Correct", "${r.correct} of ${r.rounds}")
            StatRow("Accuracy", "${(r.accuracy * 100).toInt()}%")
            StatRow("Average time", String.format(Locale.US, "%.1f s", r.averageTimeMs / 1000f))
            StatRow("Best streak", r.bestStreak.toString())
            StatRow("XP earned", "+${r.xp}")
        }
        Spacer(Modifier.height(12.dp))
        Text(levelLine, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (outcome.unlocked.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Panel {
                Text("Achievement unlocked", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                outcome.unlocked.forEach {
                    Spacer(Modifier.height(4.dp))
                    Text(it.title, fontWeight = FontWeight.SemiBold)
                    Text(it.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Play again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Done") }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}
