package com.mindforge101.app.games

import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GameTemplate
import com.mindforge101.app.domain.GlyphGridRound
import com.mindforge101.app.domain.GridRound
import com.mindforge101.app.domain.NumberRound
import com.mindforge101.app.domain.Round
import com.mindforge101.app.domain.SequenceRound
import kotlin.random.Random

// ---------------------------------------------------------------- Memory

class GridMemory(private val baseSize: Int) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val size = minOf(6, baseSize + (p.level - 1) / 3)
        val targetCount = minOf(p.scale(3, 9), size * size / 2)
        val targets = (0 until size * size).shuffled(rng).take(targetCount).toSet()
        return GridRound(
            prompt = "Remember the marked squares",
            size = size,
            targets = targets,
            showMs = p.ms(2600, 1500),
            timeLimitMs = p.ms(20_000, 14_000),
        )
    }
}

class SequenceMemory(private val pads: Int) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val length = p.scale(3, 9)
        return SequenceRound(
            prompt = "Repeat the sequence",
            pads = pads,
            sequence = List(length) { rng.nextInt(pads) },
            stepMs = p.ms(800, 450),
            timeLimitMs = length * 2_500L + 6_000,
        )
    }
}

class NumberMemory : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val digits = p.scale(3, 9)
        val text = buildString {
            append(rng.nextInt(1, 10))
            repeat(digits - 1) { append(rng.nextInt(10)) }
        }
        return NumberRound(
            prompt = "Type the number you saw",
            answer = text,
            timeLimitMs = 20_000,
            memorize = text,
            memorizeMs = digits * p.ms(750, 500),
        )
    }
}

/** Show a few items, hide them, then ask which one was in the list. */
class RecallChoice(private val pool: List<String>, private val separator: String = "   ") : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.scale(3, 7)
        val shown = pool.shuffled(rng).take(n)
        val correct = shown.first()
        val others = pool.filter { it !in shown }
        return choice(
            rng, "Which one was in the list?", correct, others, p.choices, 12_000,
            memorize = shown.shuffled(rng).joinToString(separator),
            memorizeMs = n * p.ms(1000, 650),
        )
    }
}

// ---------------------------------------------------------------- Attention

/** [pairs] are (normal, odd) glyphs ordered easy -> hard (more similar). */
class OddGlyph(private val pairs: List<Pair<String, String>>) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val pairIdx = ((p.level - 1) * pairs.size / 10).coerceIn(0, pairs.lastIndex)
        val (base, odd) = pairs[pairIdx]
        val cols = p.scale(3, 6)
        val cells = cols * cols
        val oddAt = rng.nextInt(cells)
        val swap = rng.nextBoolean() && pairIdx > 0
        val glyphs = List(cells) { i -> if (i == oddAt) (if (swap) base else odd) else (if (swap) odd else base) }
        return GlyphGridRound(
            prompt = "Tap the one that is different",
            columns = cols,
            glyphs = glyphs,
            oddIndex = oddAt,
            timeLimitMs = p.ms(12_000, 7_000),
        )
    }
}

class CountTargets(private val target: String, private val fillers: List<String>) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val total = p.scale(10, 30)
        val count = rng.nextInt(2, minOf(p.scale(5, 10), total / 2) + 1)
        val cells = (List(count) { target } + List(total - count) { rng.pick(fillers) }).shuffled(rng)
        val rows = cells.chunked(8).joinToString("\n") { it.joinToString(" ") }
        return choice(
            rng, "How many $target are there?", count.toString(),
            numWrong(rng, count, 3, min = 1), p.choices, p.ms(14_000, 9_000), stimulus = rows,
        )
    }
}

class CountLetters : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val letters = listOf("E", "F", "B", "P", "R", "H")
        val target = "E"
        val total = p.scale(10, 28)
        val count = rng.nextInt(2, minOf(p.scale(5, 9), total / 2) + 1)
        val fillers = letters.filter { it != target }
        val cells = (List(count) { target } + List(total - count) { rng.pick(fillers) }).shuffled(rng)
        val rows = cells.chunked(8).joinToString("\n") { it.joinToString(" ") }
        return choice(
            rng, "How many letter E?", count.toString(),
            numWrong(rng, count, 3, min = 1), p.choices, p.ms(14_000, 9_000), stimulus = rows,
        )
    }
}
