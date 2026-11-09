package com.mindforge101.app.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

const val MAX_LEVEL = 10
const val ROUNDS_PER_SESSION = 8

object AdaptiveDifficultyEngine {
    /** Returns the next (fractional) level, clamped to 1..10. */
    fun nextLevel(current: Float, perf: Performance): Float {
        val limit = max(1L, perf.averageLimitMs)
        val speed = (1f - perf.averageTimeMs.toFloat() / limit).coerceIn(0f, 1f)
        val delta = ((perf.accuracy - 0.7f) * 3f + (speed - 0.4f) * 0.8f).coerceIn(-1.5f, 1.5f)
        return (current + delta).coerceIn(1f, MAX_LEVEL.toFloat())
    }

    fun levelForPlay(fractional: Float): Int = Math.round(fractional).coerceIn(1, MAX_LEVEL)
}

object Scoring {
    private const val BASE = 100
    private const val MAX_SPEED_BONUS = 50

    /** Upper bound of the score a legit session could reach (used for tamper checks). */
    fun maxScore(rounds: Int, level: Int): Int = rounds * (BASE + MAX_SPEED_BONUS + 20 * rounds) + level * 100

    fun roundScore(correct: Boolean, timeMs: Long, limitMs: Long, streak: Int): Int {
        if (!correct) return 0
        val speed = (1f - timeMs.toFloat() / max(1L, limitMs)).coerceIn(0f, 1f)
        return BASE + (speed * MAX_SPEED_BONUS).toInt() + min(streak, 10) * 5
    }

    fun xp(accuracy: Float, speed: Float, bestStreak: Int, level: Int): Int =
        10 + (accuracy * 40).toInt() + (speed * 20).toInt() + bestStreak * 2 + level * 5
}

class Rank(val name: String, val minXp: Int)

object Ranks {
    val ladder = listOf(
        Rank("Beginner", 0),
        Rank("Thinker", 300),
        Rank("Solver", 900),
        Rank("Strategist", 2000),
        Rank("Master", 4000),
        Rank("Genius", 7500),
        Rank("Mind Forge", 12000),
    )

    fun forXp(xp: Int): Rank = ladder.last { xp >= it.minXp }

    /** Fraction 0..1 toward the next rank (1 for the top rank). */
    fun progress(xp: Int): Float {
        val i = ladder.indexOf(forXp(xp))
        if (i == ladder.lastIndex) return 1f
        val lo = ladder[i].minXp
        val hi = ladder[i + 1].minXp
        return (xp - lo).toFloat() / (hi - lo)
    }
}

/** Runs one play-through of a game at a fixed level. Pure Kotlin so it can be unit-tested. */
class GameSession(
    val game: GameDef,
    val level: Int,
    val seed: Long,
    val totalRounds: Int = ROUNDS_PER_SESSION,
) {
    private val rng = Random(seed)
    private val params = DifficultyParams(level)
    private val times = mutableListOf<Long>()
    private val limits = mutableListOf<Long>()
    private var correctCount = 0
    private var streak = 0
    private var bestStreak = 0
    private var score = 0

    var index = 0
        private set
    val finished: Boolean get() = index >= totalRounds

    fun nextRound(): Round = game.template.generate(rng, params, index)

    fun submit(round: Round, correct: Boolean, timeMs: Long) {
        check(!finished) { "session already finished" }
        times += timeMs
        limits += round.timeLimitMs
        if (correct) {
            correctCount++
            streak++
            bestStreak = max(bestStreak, streak)
        } else {
            streak = 0
        }
        score += Scoring.roundScore(correct, timeMs, round.timeLimitMs, streak)
        index++
    }

    fun result(): SessionResult {
        val n = max(1, times.size)
        val accuracy = correctCount.toFloat() / n
        val avgTime = times.sum() / n
        val avgLimit = limits.sum() / n
        val speed = (1f - avgTime.toFloat() / max(1L, avgLimit)).coerceIn(0f, 1f)
        val perf = Performance(accuracy, avgTime, times.size - correctCount, bestStreak, avgLimit)
        return SessionResult(
            gameId = game.id,
            level = level,
            rounds = times.size,
            correct = correctCount,
            accuracy = accuracy,
            averageTimeMs = avgTime,
            bestStreak = bestStreak,
            score = score,
            xp = Scoring.xp(accuracy, speed, bestStreak, level),
            perfect = correctCount == times.size && times.isNotEmpty(),
            performance = perf,
        )
    }
}

/** Today's mix: five games from five different categories, stable for a given day. */
object DailyMix {
    fun forDay(epochDay: Long, catalog: List<GameDef>, size: Int = 5): List<GameDef> {
        val rng = Random(epochDay * 7919L + 13)
        val byCat = catalog.groupBy { it.category }
        val cats = byCat.keys.toList().shuffled(rng).take(size)
        return cats.map { byCat.getValue(it).random(rng) }
    }
}
