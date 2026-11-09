package com.mindforge101.app.games

import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GameTemplate
import com.mindforge101.app.domain.Round
import kotlin.random.Random

// ---------------------------------------------------------------- Problem solving

class SymbolAlgebra : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val hi = p.scale(8, 20)
        val y = rng.nextInt(1, hi)
        val x = y + rng.nextInt(1, hi)
        val time = p.ms(24_000, 16_000)
        return if (p.level >= 6 && rng.nextBoolean()) {
            val text = "▲ + ▲ = ${x + x}\n▲ + ● = ${x + y}\n● = ?"
            choice(rng, "Work out the value", y.toString(), numWrong(rng, y, 5, min = 0), p.choices, time, stimulus = text)
        } else {
            val text = "▲ + ● = ${x + y}\n▲ − ● = ${x - y}\n▲ = ?"
            choice(rng, "Work out the value", x.toString(), numWrong(rng, x, 5, min = 0), p.choices, time, stimulus = text)
        }
    }
}

class BalanceScale : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val a = rng.nextInt(2, 5)
        val b = rng.nextInt(2, p.scale(3, 6) + 1)
        val lines = mutableListOf("■ weighs the same as $a ●", "▲ weighs the same as $b ■")
        val answer: Int
        val ask: String
        if (p.level >= 5) {
            val c = rng.nextInt(1, 6)
            lines += "◆ weighs the same as ▲ + $c ●"
            answer = a * b + c
            ask = "How many ● balance one ◆?"
        } else {
            answer = a * b
            ask = "How many ● balance one ▲?"
        }
        return choice(rng, ask, answer.toString(), numWrong(rng, answer, 5, min = 1), p.choices, p.ms(24_000, 16_000), stimulus = lines.joinToString("\n"))
    }
}

class AgePuzzle : GameTemplate {
    private val names = listOf("Mira", "Tom", "Lena", "Omar", "Priya", "Sam")
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val (older, younger) = names.shuffled(rng).take(2)
        val age = rng.nextInt(5, p.scale(15, 45))
        val gap = rng.nextInt(1, p.scale(6, 15))
        val text = "$older is $gap years older than $younger.\nTogether they are ${age + age + gap}."
        return choice(rng, "How old is $younger?", age.toString(), numWrong(rng, age, 4, min = 1), p.choices, p.ms(26_000, 18_000), stimulus = text)
    }
}

class ChangeMaker : GameTemplate {
    private val sets = listOf(listOf(1, 5, 10), listOf(1, 2, 5, 10), listOf(1, 4, 9), listOf(1, 5, 12))

    private fun fewestCoins(amount: Int, coins: List<Int>): Int {
        val best = IntArray(amount + 1) { Int.MAX_VALUE / 2 }
        best[0] = 0
        for (v in 1..amount) for (c in coins) if (c <= v) best[v] = minOf(best[v], best[v - c] + 1)
        return best[amount]
    }

    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val coins = sets[if (p.level < 5) rng.nextInt(2) else rng.nextInt(sets.size)]
        val amount = rng.nextInt(p.scale(6, 20), p.scale(20, 60))
        val answer = fewestCoins(amount, coins)
        return choice(
            rng, "What is the fewest coins needed?", answer.toString(), numWrong(rng, answer, 3, min = 1), p.choices,
            p.ms(24_000, 16_000), stimulus = "Make $amount\nCoins: ${coins.joinToString(", ")}",
        )
    }
}

class PatternBreaker : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val cycle = SHAPES.shuffled(rng).take(p.scale(2, 4))
        val n = p.scale(6, 12)
        val seq = MutableList(n) { cycle[it % cycle.size] }
        val at = rng.nextInt(cycle.size, n)
        seq[at] = rng.pick(cycle.filter { it != seq[at] })
        val text = seq.mapIndexed { i, s -> "${label(i)} $s" }.chunked(4).joinToString("\n") { it.joinToString("   ") }
        return choice(rng, "Which position breaks the pattern?", label(at), List(n) { label(it) }, p.choices, p.ms(18_000, 11_000), stimulus = text)
    }
}

// ---------------------------------------------------------------- Visual

class SymmetryCheck : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val rows = p.scale(2, 5)
        val half = p.scale(2, 4)
        val glyphs = if (p.level < 5) listOf("■", "□") else listOf("■", "□", "▲", "●")
        val grid = List(rows) {
            val left = List(half) { rng.pick(glyphs) }
            (left + left.reversed()).toMutableList()
        }
        val symmetric = rng.nextBoolean()
        if (!symmetric) {
            val row = grid[rng.nextInt(rows)]
            val col = rng.nextInt(half, half * 2)
            row[col] = rng.pick(glyphs.filter { it != row[col] })
        }
        val text = grid.joinToString("\n") { it.joinToString(" ") }
        return choiceAll(rng, "Is it the same on both sides?", if (symmetric) "Yes" else "No", listOf("Yes", "No"), p.ms(10_000, 5_000), stimulus = text)
    }
}

/** A small Latin square: each shape appears once in every row and column. */
class FigureCompletion : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = if (p.level < 5) 3 else 4
        val glyphs = SHAPES.shuffled(rng).take(n)
        val rows = (0 until n).shuffled(rng)
        val cols = (0 until n).shuffled(rng)
        val grid = rows.map { r -> cols.map { c -> glyphs[(r + c) % n] } }
        val hideRow = rng.nextInt(n)
        val hideCol = rng.nextInt(n)
        val answer = grid[hideRow][hideCol]
        val text = grid.mapIndexed { r, row ->
            row.mapIndexed { c, g -> if (r == hideRow && c == hideCol) "?" else g }.joinToString(" ")
        }.joinToString("\n")
        return choiceAll(rng, "Each shape appears once per row and column. What fits?", answer, glyphs, p.ms(18_000, 11_000), stimulus = text)
    }
}

class GrowthPattern : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val glyph = rng.pick(SHAPES)
        val start = rng.nextInt(1, 4)
        val step = rng.nextInt(1, p.scale(2, 3) + 1)
        val terms = List(3) { start + it * step }
        val answer = start + 3 * step
        val text = terms.joinToString("\n") { n -> List(n) { glyph }.joinToString(" ") }
        return choice(rng, "How many in the next row?", answer.toString(), numWrong(rng, answer, 3, min = 1), p.choices, p.ms(12_000, 7_000), stimulus = text)
    }
}

class SpotDifference : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.scale(4, 12)
        val top = List(n) { rng.pick(SHAPES) }
        val at = rng.nextInt(n)
        val changed = rng.pick(SHAPES.filter { it != top[at] })
        val text = top.mapIndexed { i, g -> "${label(i)} $g${if (i == at) changed else g}" }
            .chunked(3).joinToString("\n") { it.joinToString("   ") }
        return choice(rng, "Which pair does not match?", label(at), List(n) { label(it) }, p.choices, p.ms(14_000, 8_000), stimulus = text)
    }
}

class MatchingPair : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.scale(4, 8)
        val len = p.scale(2, 4)
        val pool = SHAPES.take(4)
        val distinct = linkedSetOf<String>()
        while (distinct.size < n - 1) distinct += List(len) { rng.pick(pool) }.joinToString("")
        val items = (distinct.toList() + distinct.first()).shuffled(rng)
        val first = items.indexOfFirst { items.count { other -> other == it } == 2 }
        val second = items.indexOfLast { it == items[first] }
        val answer = "${label(first)} & ${label(second)}"
        val wrong = (0 until n).flatMap { a -> (a + 1 until n).map { b -> "${label(a)} & ${label(b)}" } }
        val text = items.mapIndexed { i, s -> "${label(i)} $s" }.chunked(2).joinToString("\n") { it.joinToString("    ") }
        return choice(rng, "Which two are identical?", answer, wrong, p.choices, p.ms(16_000, 10_000), stimulus = text)
    }
}
