package com.mindforge101.app.games

import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GameTemplate
import com.mindforge101.app.domain.Round
import kotlin.random.Random

// ---------------------------------------------------------------- Logic

enum class SeriesKind { ARITH, GEO, ALT, SQUARES, TRIANGULAR, FIB }

class NumberSeries(private val kinds: List<SeriesKind>) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val kind = rng.pick(kinds)
        val terms: List<Int> = when (kind) {
            SeriesKind.ARITH -> {
                val start = rng.nextInt(1, p.scale(10, 40))
                val step = rng.nextInt(2, p.scale(5, 15))
                List(6) { start + it * step }
            }
            SeriesKind.GEO -> {
                val start = rng.nextInt(1, 5)
                val ratio = rng.nextInt(2, if (p.level > 6) 4 else 3)
                List(5) { i -> var v = start; repeat(i) { v *= ratio }; v }
            }
            SeriesKind.ALT -> {
                val a = rng.nextInt(1, 20); val sa = rng.nextInt(2, 6)
                val b = rng.nextInt(1, 20); val sb = rng.nextInt(2, 8)
                List(8) { i -> if (i % 2 == 0) a + (i / 2) * sa else b + (i / 2) * sb }
            }
            SeriesKind.SQUARES -> {
                val s = rng.nextInt(1, 6)
                List(5) { (s + it) * (s + it) }
            }
            SeriesKind.TRIANGULAR -> {
                val s = rng.nextInt(1, 5)
                List(6) { val n = s + it; n * (n + 1) / 2 }
            }
            SeriesKind.FIB -> {
                val a = rng.nextInt(1, 5); val b = rng.nextInt(1, 6)
                val l = mutableListOf(a, b)
                while (l.size < 7) l += l[l.size - 1] + l[l.size - 2]
                l
            }
        }
        val shownCount = terms.size - 1
        val answer = terms.last()
        val shown = terms.take(shownCount).joinToString(", ") + ", ?"
        return choice(
            rng, "What number comes next?", answer.toString(),
            numWrong(rng, answer, maxOf(4, answer / 6)), p.choices, p.ms(18_000, 12_000), stimulus = shown,
        )
    }
}

class LetterSeries : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val step = rng.nextInt(1, p.scale(2, 4) + 1)
        val len = 5
        val start = rng.nextInt(0, 26 - len * step)
        val letters = List(len) { ('A' + start + it * step).toString() }
        val answer = letters.last()
        val wrong = ('A'..'Z').map { it.toString() }.filter { it != answer }
        val near = listOf(-2, -1, 1, 2).map { d -> ('A' + (answer[0] - 'A' + d).coerceIn(0, 25)).toString() }
        return choice(
            rng, "Which letter comes next?", answer, near + wrong, p.choices, p.ms(16_000, 10_000),
            stimulus = letters.dropLast(1).joinToString(" ") + " ?",
        )
    }
}

class PatternNext : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val cycleLen = p.scale(2, 4)
        val cycle = SHAPES.shuffled(rng).take(cycleLen)
        val shownLen = cycleLen * 2 + rng.nextInt(1, cycleLen)
        val seq = List(shownLen + 1) { cycle[it % cycleLen] }
        val answer = seq.last()
        return choice(
            rng, "Which shape comes next?", answer, SHAPES, p.choices, p.ms(14_000, 9_000),
            stimulus = seq.dropLast(1).joinToString(" ") + " ?",
        )
    }
}

class OddNumberOut : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val k = rng.nextInt(2, p.scale(5, 9))
        val n = if (p.level < 5) 4 else 5
        val multiples = (1..30).filter { it % k == 0 }.shuffled(rng).take(n - 1)
        var odd = rng.nextInt(2, 60)
        while (odd % k == 0) odd++
        val all = (multiples + odd).map { it.toString() }
        return choiceAll(rng, "Which number does not belong?", odd.toString(), all, p.ms(14_000, 9_000))
    }
}

class LogicChain : GameTemplate {
    private val names = listOf("Asha", "Ben", "Chen", "Dia", "Eli", "Faye", "Gus", "Hana")
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.scale(3, 5)
        val order = names.shuffled(rng).take(n) // tallest first
        val trait = rng.pick(listOf("taller" to ("tallest" to "shortest"), "older" to ("oldest" to "youngest"), "faster" to ("fastest" to "slowest")))
        val statements = (0 until n - 1).map { "${order[it]} is ${trait.first} than ${order[it + 1]}." }.shuffled(rng)
        val askTop = rng.nextBoolean()
        val answer = if (askTop) order.first() else order.last()
        val q = "Who is the ${if (askTop) trait.second.first else trait.second.second}?"
        return choiceAll(rng, q, answer, order, p.ms(20_000, 14_000), stimulus = statements.joinToString("\n"))
    }
}

// ---------------------------------------------------------------- Math

class Arithmetic(private val ops: String, private val speedFactor: Float = 1f) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val op = ops[rng.nextInt(ops.length)]
        val text: String
        val answer: Int
        when (op) {
            '+' -> {
                val hi = p.scale(9, 120)
                val a = rng.nextInt(2, hi); val b = rng.nextInt(2, hi)
                if (p.level >= 7) {
                    val c = rng.nextInt(2, hi / 2 + 3)
                    text = "$a + $b + $c"; answer = a + b + c
                } else { text = "$a + $b"; answer = a + b }
            }
            '-' -> {
                val hi = p.scale(12, 150)
                val a = rng.nextInt(5, hi); val b = rng.nextInt(2, a)
                text = "$a − $b"; answer = a - b
            }
            else -> {
                val a = rng.nextInt(2, p.scale(6, 13)); val b = rng.nextInt(2, p.scale(6, 16))
                text = "$a × $b"; answer = a * b
            }
        }
        val limit = (p.ms(10_000, 6_000) * speedFactor).toLong()
        return choice(rng, "Solve it", answer.toString(), numWrong(rng, answer, maxOf(3, minOf(10, answer / 5 + 2))), p.choices, limit, stimulus = "$text = ?")
    }
}

class MissingOperand : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val hi = p.scale(10, 60)
        val op = rng.pick(if (p.level < 4) listOf('+', '-') else listOf('+', '-', '×'))
        val a: Int; val b: Int; val c: Int
        when (op) {
            '+' -> { a = rng.nextInt(2, hi); b = rng.nextInt(2, hi); c = a + b }
            '-' -> { a = rng.nextInt(5, hi + 5); b = rng.nextInt(2, a); c = a - b }
            else -> { a = rng.nextInt(2, 13); b = rng.nextInt(2, 13); c = a * b }
        }
        val hideFirst = rng.nextBoolean()
        val shown = if (hideFirst) "? $op $b = $c" else "$a $op ? = $c"
        val answer = if (hideFirst) a else b
        return choice(rng, "Find the missing number", answer.toString(), numWrong(rng, answer, 6, min = 0), p.choices, p.ms(14_000, 9_000), stimulus = shown)
    }
}

class MissingOperator : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val ops = listOf("+", "−", "×", "÷")
        fun apply(op: String, a: Int, b: Int): Int? = when (op) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b
            else -> if (b != 0 && a % b == 0) a / b else null
        }
        while (true) {
            val hi = p.scale(10, 30)
            val a = rng.nextInt(2, hi); val b = rng.nextInt(2, 13)
            val op = rng.pick(ops)
            val c = apply(op, a, b) ?: continue
            val valid = ops.filter { apply(it, a, b) == c }
            if (valid.size != 1) continue
            return choiceAll(rng, "Which sign fits?", op, ops, p.ms(12_000, 7_000), stimulus = "$a ? $b = $c")
        }
        @Suppress("UNREACHABLE_CODE")
        error("unreachable")
    }
}

class EstimateIt : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val a = rng.nextInt(p.scale(12, 40), p.scale(40, 99))
        val b = rng.nextInt(p.scale(12, 30), p.scale(30, 99))
        val product = a * b
        val unit = if (product >= 1000) 100 else 10
        val estimate = ((product + unit / 2) / unit) * unit
        val wrong = listOf(-3, -2, -1, 1, 2, 3).map { estimate + it * unit }
        return choice(
            rng, "Which is the closest estimate?", estimate.toString(), wrong.map { it.toString() },
            p.choices, p.ms(18_000, 12_000), stimulus = "$a × $b ≈ ?",
        )
    }
}

class CompareFractions : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val maxDen = p.scale(5, 14)
        while (true) {
            val d1 = rng.nextInt(2, maxDen + 1); val d2 = rng.nextInt(2, maxDen + 1)
            val n1 = rng.nextInt(1, d1); val n2 = rng.nextInt(1, d2)
            if (n1 * d2 == n2 * d1) continue
            val big = if (n1 * d2 > n2 * d1) "$n1/$d1" else "$n2/$d2"
            val small = if (big == "$n1/$d1") "$n2/$d2" else "$n1/$d1"
            if (big == small) continue
            return choiceAll(rng, "Which fraction is larger?", big, listOf(big, small), p.ms(14_000, 9_000))
        }
        @Suppress("UNREACHABLE_CODE")
        error("unreachable")
    }
}

class PrimeHunt : GameTemplate {
    private fun isPrime(n: Int): Boolean = n >= 2 && (2..Math.sqrt(n.toDouble()).toInt()).none { n % it == 0 }
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val hi = p.scale(30, 200)
        val primes = (2..hi).filter { isPrime(it) }
        val composites = (4..hi).filter { !isPrime(it) && (p.level < 5 || it % 2 == 1) }
        val prime = rng.pick(primes)
        return choice(rng, "Which number is prime?", prime.toString(), composites.shuffled(rng).take(6).map { it.toString() }, p.choices, p.ms(16_000, 10_000))
    }
}
