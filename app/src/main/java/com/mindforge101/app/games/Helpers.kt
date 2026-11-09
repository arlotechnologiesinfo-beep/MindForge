package com.mindforge101.app.games

import com.mindforge101.app.domain.ChoiceRound
import kotlin.random.Random

/** Builds a [ChoiceRound] from the correct answer and a pool of wrong candidates. */
internal fun choice(
    rng: Random,
    prompt: String,
    correct: String,
    wrong: Collection<String>,
    count: Int,
    timeMs: Long,
    stimulus: String? = null,
    memorize: String? = null,
    memorizeMs: Long = 0,
): ChoiceRound {
    val picked = wrong.filter { it != correct }.distinct().shuffled(rng).take(count - 1)
    val options = (picked + correct).shuffled(rng)
    return ChoiceRound(prompt, options, options.indexOf(correct), timeMs, stimulus, memorize, memorizeMs)
}

/** Builds a round whose options are exactly [options] (shuffled). */
internal fun choiceAll(
    rng: Random,
    prompt: String,
    correct: String,
    options: List<String>,
    timeMs: Long,
    stimulus: String? = null,
): ChoiceRound {
    val shuffled = options.distinct().shuffled(rng)
    return ChoiceRound(prompt, shuffled, shuffled.indexOf(correct), timeMs, stimulus)
}

/** Distinct numbers near [correct] (never equal to it). */
internal fun numWrong(rng: Random, correct: Int, spread: Int, want: Int = 8, min: Int = Int.MIN_VALUE): List<String> {
    val set = linkedSetOf<Int>()
    var tries = 0
    val s = spread.coerceAtLeast(2)
    while (set.size < want && tries < 200) {
        val d = rng.nextInt(1, s + 1) * if (rng.nextBoolean()) 1 else -1
        if (d != 0 && correct + d >= min) set += correct + d
        tries++
    }
    return set.map { it.toString() }
}

internal fun <T> Random.pick(list: List<T>): T = list[nextInt(list.size)]

/** Labels A, B, C ... used by position-style questions. */
internal fun label(i: Int): String = ('A' + i).toString()

internal val SHAPES = listOf("●", "■", "▲", "◆", "★", "✚")
