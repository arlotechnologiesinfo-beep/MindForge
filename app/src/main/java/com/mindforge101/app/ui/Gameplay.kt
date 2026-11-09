package com.mindforge101.app.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.domain.ChoiceRound
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.GameSession
import com.mindforge101.app.domain.GlyphGridRound
import com.mindforge101.app.domain.GridRound
import com.mindforge101.app.domain.NumberRound
import com.mindforge101.app.domain.Round
import com.mindforge101.app.domain.SequenceRound
import com.mindforge101.app.domain.SessionResult
import com.mindforge101.app.games.SHAPES
import kotlinx.coroutines.delay
import kotlin.math.ceil

private const val FEEDBACK_MS = 700L

@Composable
fun PlayScreen(
    game: GameDef,
    level: Int,
    reduceMotion: Boolean,
    haptics: Boolean,
    onFinished: (SessionResult) -> Unit,
    onExit: () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    BackHandler(onBack = onExit)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave game")
            }
            Text(game.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Level $level", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        if (started) {
            SessionRunner(game, level, reduceMotion, haptics, onFinished)
        } else {
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(game.category.label.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                Text(game.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(game.blurb, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Start") }
            }
        }
    }
}

@Composable
private fun SessionRunner(
    game: GameDef,
    level: Int,
    reduceMotion: Boolean,
    haptics: Boolean,
    onFinished: (SessionResult) -> Unit,
) {
    val session = remember { GameSession(game, level, System.nanoTime()) }
    var round by remember { mutableStateOf<Round>(session.nextRound()) }
    var roundKey by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<Boolean?>(null) }
    val haptic = LocalHapticFeedback.current
    val finished by rememberUpdatedState(onFinished)

    LaunchedEffect(roundKey, feedback) {
        if (feedback == null) return@LaunchedEffect
        delay(FEEDBACK_MS)
        if (session.finished) {
            finished(session.result())
        } else {
            round = session.nextRound()
            feedback = null
            roundKey++
        }
    }

    val onResult: (Boolean, Long) -> Unit = { correct, ms ->
        if (feedback == null) {
            session.submit(round, correct, ms)
            feedback = correct
            if (haptics && !correct) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { session.index.toFloat() / session.totalRounds },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Round ${session.index + 1} of ${session.totalRounds}" },
        )
        Spacer(Modifier.height(12.dp))
        Box(Modifier.height(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (feedback) {
                true -> Text("✓ Correct", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                false -> Text("✗ Not quite", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                null -> Unit
            }
        }
        key(roundKey) {
            when (val r = round) {
                is ChoiceRound -> ChoiceView(r, feedback != null, reduceMotion, onResult)
                is NumberRound -> NumberView(r, feedback != null, reduceMotion, onResult)
                is GridRound -> GridView(r, feedback != null, reduceMotion, onResult)
                is SequenceRound -> SequenceView(r, feedback != null, reduceMotion, onResult)
                is GlyphGridRound -> GlyphGridView(r, feedback != null, reduceMotion, onResult)
            }
        }
    }
}

/** Runs the optional "look" stage, then the timed "answer" stage. */
@Composable
private fun Phased(
    limitMs: Long,
    showMs: Long,
    paused: Boolean,
    reduceMotion: Boolean,
    onResult: (Boolean, Long) -> Unit,
    show: @Composable () -> Unit,
    answer: @Composable (submit: (Boolean) -> Unit) -> Unit,
) {
    var answering by remember { mutableStateOf(showMs <= 0) }
    LaunchedEffect(Unit) {
        if (!answering) {
            delay(showMs)
            answering = true
        }
    }
    if (!answering) {
        show()
        return
    }
    val startedAt = remember { SystemClock.elapsedRealtime() }
    val submit: (Boolean) -> Unit = { onResult(it, SystemClock.elapsedRealtime() - startedAt) }
    Countdown(limitMs, paused, reduceMotion) { submit(false) }
    Spacer(Modifier.height(16.dp))
    answer(submit)
}

@Composable
private fun Countdown(limitMs: Long, paused: Boolean, reduceMotion: Boolean, onExpired: () -> Unit) {
    var remaining by remember { mutableFloatStateOf(1f) }
    val expired by rememberUpdatedState(onExpired)
    LaunchedEffect(paused) {
        if (paused) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            remaining = (1f - (now - start).toFloat() / limitMs).coerceAtLeast(0f)
            if (remaining <= 0f) {
                expired()
                break
            }
        }
    }
    if (reduceMotion) {
        val seconds = ceil(remaining * limitMs / 1000f).toInt()
        Text(
            "$seconds s", style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
        )
    } else {
        LinearProgressIndicator(
            progress = { remaining },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).semantics { contentDescription = "Time left" },
            color = if (remaining < 0.25f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun Prompt(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}

@Composable
private fun Stimulus(text: String) {
    val lines = text.lines()
    val longest = lines.maxOf { it.length }
    val size = when {
        lines.size == 1 && longest <= 3 -> 72.sp
        lines.size == 1 && longest <= 9 -> 44.sp
        longest <= 16 && lines.size <= 5 -> 28.sp
        else -> 20.sp
    }
    Text(
        text, fontSize = size, lineHeight = size * 1.35f, textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    )
}

@Composable
private fun ChoiceView(r: ChoiceRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    var picked by remember { mutableIntStateOf(-1) }
    Phased(
        r.timeLimitMs, if (r.memorize != null) r.memorizeMs else 0, paused, reduceMotion, onResult,
        show = {
            Prompt("Memorize")
            Stimulus(r.memorize.orEmpty())
        },
    ) { submit ->
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Prompt(r.prompt)
            r.stimulus?.let { Stimulus(it) }
            Spacer(Modifier.height(8.dp))
            r.options.forEachIndexed { i, option ->
                val isCorrect = i == r.correctIndex
                val mark = when {
                    paused && isCorrect -> "✓ "
                    paused && i == picked -> "✗ "
                    else -> ""
                }
                OutlinedButton(
                    onClick = {
                        if (!paused) {
                            picked = i
                            submit(isCorrect)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 4.dp),
                ) { Text(mark + option, fontSize = 18.sp, textAlign = TextAlign.Center) }
            }
        }
    }
}

@Composable
private fun NumberView(r: NumberRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    var entry by remember { mutableStateOf("") }
    Phased(
        r.timeLimitMs, r.memorizeMs, paused, reduceMotion, onResult,
        show = {
            Prompt("Memorize")
            Stimulus(r.memorize)
        },
    ) { submit ->
        Prompt(r.prompt)
        Text(
            entry.ifEmpty { " " }, fontSize = 36.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        if (paused) {
            Text("Answer: ${r.answer}", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    OutlinedButton(
                        onClick = {
                            if (paused) return@OutlinedButton
                            when (k) {
                                "⌫" -> entry = entry.dropLast(1)
                                "OK" -> if (entry.isNotEmpty()) submit(entry == r.answer)
                                else -> if (entry.length < r.answer.length) entry += k
                            }
                        },
                        modifier = Modifier.weight(1f).height(60.dp).padding(vertical = 4.dp),
                    ) { Text(k, fontSize = 20.sp) }
                }
            }
        }
    }
}

@Composable
private fun Cell(
    text: String,
    background: androidx.compose.ui.graphics.Color,
    description: String,
    modifier: Modifier,
    fontSize: Int = 22,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = fontSize.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun GridView(r: GridRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    val found = remember { mutableStateOf(setOf<Int>()) }
    var wrong by remember { mutableIntStateOf(-1) }

    @Composable
    fun Board(revealTargets: Boolean, onTap: ((Int) -> Unit)?) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until r.size) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0 until r.size) {
                        val i = row * r.size + col
                        val marked = i in found.value || (revealTargets && i in r.targets)
                        val bg = when {
                            i == wrong -> MaterialTheme.colorScheme.errorContainer
                            marked -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                        Cell(
                            text = if (i == wrong) "✗" else if (marked) "●" else "",
                            background = bg,
                            description = "Row ${row + 1}, column ${col + 1}" + if (marked) ", marked" else "",
                            modifier = Modifier.weight(1f),
                            onClick = onTap?.let { tap -> { tap(i) } },
                        )
                    }
                }
            }
        }
    }

    Phased(
        r.timeLimitMs, r.showMs, paused, reduceMotion, onResult,
        show = {
            Prompt(r.prompt)
            Board(revealTargets = true, onTap = null)
        },
    ) { submit ->
        Prompt("Tap the ${r.targets.size} squares")
        Board(revealTargets = paused) { i ->
            if (!paused && i !in found.value) {
                if (i in r.targets) {
                    found.value = found.value + i
                    if (found.value.size == r.targets.size) submit(true)
                } else {
                    wrong = i
                    submit(false)
                }
            }
        }
    }
}

@Composable
private fun SequenceView(r: SequenceRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    var lit by remember { mutableIntStateOf(-1) }
    var entered by remember { mutableIntStateOf(0) }
    val columns = if (r.pads <= 4) 2 else 3

    @Composable
    fun Pads(onTap: ((Int) -> Unit)?) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (0 until r.pads).chunked(columns).forEach { rowPads ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowPads.forEach { pad ->
                        Cell(
                            text = SHAPES[pad],
                            background = if (pad == lit) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            description = "Pad ${pad + 1}",
                            modifier = Modifier.weight(1f),
                            fontSize = if (pad == lit) 48 else 30,
                            onClick = onTap?.let { tap -> { tap(pad) } },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(400)
        for (pad in r.sequence) {
            lit = pad
            delay(r.stepMs * 7 / 10)
            lit = -1
            delay(r.stepMs * 3 / 10)
        }
    }

    Phased(
        r.timeLimitMs, 400 + r.sequence.size * r.stepMs + 150, paused, reduceMotion, onResult,
        show = {
            Prompt("Watch")
            Pads(onTap = null)
        },
    ) { submit ->
        Prompt("${r.prompt} (${entered} of ${r.sequence.size})")
        Pads { pad ->
            if (!paused) {
                if (pad == r.sequence[entered]) {
                    entered++
                    if (entered == r.sequence.size) submit(true)
                } else {
                    submit(false)
                }
            }
        }
    }
}

@Composable
private fun GlyphGridView(r: GlyphGridRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    Phased(r.timeLimitMs, 0, paused, reduceMotion, onResult, show = {}) { submit ->
        Prompt(r.prompt)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            r.glyphs.chunked(r.columns).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEachIndexed { colIndex, glyph ->
                        val i = rowIndex * r.columns + colIndex
                        Cell(
                            text = glyph,
                            background = if (paused && i == r.oddIndex) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            description = "$glyph, row ${rowIndex + 1}, column ${colIndex + 1}",
                            modifier = Modifier.weight(1f),
                            onClick = { if (!paused) submit(i == r.oddIndex) },
                        )
                    }
                }
            }
        }
    }
}
