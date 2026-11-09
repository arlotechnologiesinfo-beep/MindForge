package com.mindforge101.app.domain

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Category(val label: String) {
    MEMORY("Memory"),
    ATTENTION("Focus"),
    LOGIC("Logic"),
    MATH("Math"),
    LANGUAGE("Words"),
    SPATIAL("Spatial"),
    SPEED("Speed"),
    EXECUTIVE("Strategy"),
    PROBLEM("Problem Solving"),
    VISUAL("Visual"),
}

/** A single question/challenge produced by a [GameTemplate]. */
sealed interface Round {
    val prompt: String
    val timeLimitMs: Long
}

/** Pick one option. Optional [memorize] text is shown briefly and then hidden. */
data class ChoiceRound(
    override val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    override val timeLimitMs: Long,
    val stimulus: String? = null,
    val memorize: String? = null,
    val memorizeMs: Long = 0,
) : Round

/** Type a number after it was shown briefly. */
data class NumberRound(
    override val prompt: String,
    val answer: String,
    override val timeLimitMs: Long,
    val memorize: String,
    val memorizeMs: Long,
) : Round

/** Highlighted cells are shown for [showMs], then the player taps them from memory. */
data class GridRound(
    override val prompt: String,
    val size: Int,
    val targets: Set<Int>,
    val showMs: Long,
    override val timeLimitMs: Long,
) : Round

/** Pads flash in [sequence] order, then the player repeats the order. */
data class SequenceRound(
    override val prompt: String,
    val pads: Int,
    val sequence: List<Int>,
    val stepMs: Long,
    override val timeLimitMs: Long,
) : Round

/** Tap the one cell that differs. */
data class GlyphGridRound(
    override val prompt: String,
    val columns: Int,
    val glyphs: List<String>,
    val oddIndex: Int,
    override val timeLimitMs: Long,
) : Round

/** Numeric knobs derived from a 1..10 level. Every template reads what it needs. */
class DifficultyParams(val level: Int) {
    init {
        require(level in 1..10) { "level must be 1..10" }
    }

    private val t: Float get() = (level - 1) / 9f

    fun scale(easy: Int, hard: Int): Int = (easy + (hard - easy) * t).roundToInt()
    fun ms(easy: Long, hard: Long): Long = (easy + (hard - easy) * t).toLong()

    /** Number of answer options for choice questions (3..5). */
    val choices: Int get() = scale(3, 5)
}

fun interface GameTemplate {
    /** [index] is the round number within the session (0-based). */
    fun generate(rng: Random, p: DifficultyParams, index: Int): Round
}

data class GameDef(
    val id: String,
    val name: String,
    val category: Category,
    val blurb: String,
    val template: GameTemplate,
)

data class Performance(
    val accuracy: Float,
    val averageTimeMs: Long,
    val mistakes: Int,
    val streak: Int,
    /** Average time budget of the rounds, used to judge speed. */
    val averageLimitMs: Long = 10_000,
)

data class SessionResult(
    val gameId: String,
    val level: Int,
    val rounds: Int,
    val correct: Int,
    val accuracy: Float,
    val averageTimeMs: Long,
    val bestStreak: Int,
    val score: Int,
    val xp: Int,
    val perfect: Boolean,
    val performance: Performance,
)
