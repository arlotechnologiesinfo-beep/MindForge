package com.mindforge101.app.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

/** The round on screen, tied to its key so a fading-out round can never answer for the next one. */
private data class RoundState(val key: Int, val round: Round)

@Composable
fun PlayScreen(
    game: GameDef,
    level: Int,
    reduceMotion: Boolean,
    haptics: Boolean,
    onFinished: (SessionResult) -> Unit,
    onExit: () -> Unit,
    bestScore: Int? = null,
) {
    var started by remember { mutableStateOf(false) }
    BackHandler(onBack = onExit)

    CompositionLocalProvider(LocalAccent provides MfColors.accent(game.category)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
        ) {
            PlayHeader(game.name, level, onExit)
            if (started) {
                SessionRunner(game, level, reduceMotion, haptics, onFinished)
            } else {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                    GameIntro(game, level, bestScore, onStart = { started = true })
                }
            }
        }
    }
}

@Composable
private fun PlayHeader(name: String, level: Int, onExit: () -> Unit) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(MfColors.Surface)
                .mfClickable(CircleShape, "Leave game", onClick = onExit)
                .semantics { contentDescription = "Leave game" },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        Spacer(Modifier.width(12.dp))
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
        Text(
            "Level $level", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MfColors.Primary,
            modifier = Modifier.clip(CircleShape).background(MfColors.PrimaryLight).padding(horizontal = 14.dp, vertical = 6.dp),
        )
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

    val accent = LocalAccent.current
    val shownRound = minOf(roundKey + 1, session.totalRounds)

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Round $shownRound of ${session.totalRounds}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = "Round $shownRound of ${session.totalRounds}" },
            )
        }
        Spacer(Modifier.height(8.dp))
        SoftBar(session.index.toFloat() / session.totalRounds, accent, Modifier.fillMaxWidth(), height = 6.dp)
        Spacer(Modifier.height(4.dp))
        FeedbackPill(feedback)

        AnimatedContent(
            targetState = RoundState(roundKey, round),
            transitionSpec = {
                fadeIn(tween(if (reduceMotion) MfMotion.Short else MfMotion.Medium, delayMillis = 60)) togetherWith
                    fadeOut(tween(MfMotion.Short))
            },
            label = "round",
        ) { state ->
            // A round that is fading out is frozen, and its answers are ignored.
            val live = state.key == roundKey
            val paused = feedback != null || !live
            val onResult: (Boolean, Long) -> Unit = { correct, ms ->
                if (live && feedback == null) {
                    session.submit(state.round, correct, ms)
                    feedback = correct
                    if (haptics && !correct) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            Column(Modifier.fillMaxSize()) {
                when (val r = state.round) {
                    is ChoiceRound -> ChoiceView(r, paused, reduceMotion, onResult)
                    is NumberRound -> NumberView(r, paused, reduceMotion, onResult)
                    is GridRound -> GridView(r, paused, reduceMotion, onResult)
                    is SequenceRound -> SequenceView(r, paused, reduceMotion, onResult)
                    is GlyphGridRound -> GlyphGridView(r, paused, reduceMotion, onResult)
                }
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
            "$seconds s", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
        )
    } else {
        SoftBar(
            remaining, if (remaining < 0.25f) MfColors.Danger else LocalAccent.current,
            Modifier.fillMaxWidth().semantics { contentDescription = "Time left" }, height = 6.dp, animate = false,
        )
    }
}

@Composable
private fun Prompt(text: String) {
    Text(
        text, fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
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
        longest <= 16 && lines.size <= 5 -> 30.sp
        else -> 22.sp
    }
    QuestionSurface(Modifier.padding(vertical = 8.dp)) {
        Text(
            text, fontSize = size, lineHeight = size * 1.3f, textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
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
                val state = when {
                    paused && isCorrect -> OptionState.Correct
                    paused && i == picked -> OptionState.Wrong
                    paused -> OptionState.Dim
                    else -> OptionState.Idle
                }
                AnswerOption(option, state, enabled = !paused) {
                    picked = i
                    submit(isCorrect)
                }
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
        QuestionSurface(Modifier.padding(vertical = 4.dp)) {
            Text(
                entry.ifEmpty { "–" }, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                color = if (entry.isEmpty()) MfColors.TextMuted else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            )
        }
        if (paused) {
            Text(
                "Answer: ${r.answer}", textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                color = MfColors.Success, modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    KeyButton(k, enabled = !paused, primary = k == "OK", modifier = Modifier.weight(1f)) {
                        when (k) {
                            "⌫" -> entry = entry.dropLast(1)
                            "OK" -> if (entry.isNotEmpty()) submit(entry == r.answer)
                            else -> if (entry.length < r.answer.length) entry += k
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridView(r: GridRound, paused: Boolean, reduceMotion: Boolean, onResult: (Boolean, Long) -> Unit) {
    val found = remember { mutableStateOf(setOf<Int>()) }
    var wrong by remember { mutableIntStateOf(-1) }
    val accent = LocalAccent.current

    @Composable
    fun Board(revealTargets: Boolean, onTap: ((Int) -> Unit)?) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until r.size) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until r.size) {
                        val i = row * r.size + col
                        val marked = i in found.value || (revealTargets && i in r.targets)
                        PlayCell(
                            text = if (i == wrong) "✗" else if (marked) "●" else "",
                            background = when {
                                i == wrong -> MfColors.DangerSoft
                                marked -> accentSoft(accent)
                                else -> MfColors.Surface
                            },
                            description = "Row ${row + 1}, column ${col + 1}" + if (marked) ", marked" else "",
                            modifier = Modifier.weight(1f),
                            borderColor = when {
                                i == wrong -> MfColors.Danger
                                marked -> accent
                                else -> MfColors.TextMuted.copy(alpha = 0.35f)
                            },
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
    val accent = LocalAccent.current

    @Composable
    fun Pads(onTap: ((Int) -> Unit)?) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (0 until r.pads).chunked(columns).forEach { rowPads ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowPads.forEach { pad ->
                        PlayCell(
                            text = SHAPES[pad],
                            background = if (pad == lit) accent else MfColors.Surface,
                            description = "Pad ${pad + 1}",
                            modifier = Modifier.weight(1f),
                            fontSize = if (pad == lit) 44 else 30,
                            borderColor = if (pad == lit) accentDeep(accent) else MfColors.TextMuted.copy(alpha = 0.35f),
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
    var picked by remember { mutableIntStateOf(-1) }
    Phased(r.timeLimitMs, 0, paused, reduceMotion, onResult, show = {}) { submit ->
        Prompt(r.prompt)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            r.glyphs.chunked(r.columns).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEachIndexed { colIndex, glyph ->
                        val i = rowIndex * r.columns + colIndex
                        val reveal = paused && i == r.oddIndex
                        val missed = paused && i == picked && i != r.oddIndex
                        PlayCell(
                            text = glyph,
                            background = if (reveal) MfColors.SuccessSoft else if (missed) MfColors.DangerSoft else MfColors.Surface,
                            description = "$glyph, row ${rowIndex + 1}, column ${colIndex + 1}",
                            modifier = Modifier.weight(1f),
                            borderColor = if (reveal) MfColors.Success else if (missed) MfColors.Danger else MfColors.TextMuted.copy(alpha = 0.35f),
                            onClick = { if (!paused) { picked = i; submit(i == r.oddIndex) } },
                        )
                    }
                }
            }
        }
    }
}
