package com.mindforge101.app.games

import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GameTemplate
import com.mindforge101.app.domain.Round
import kotlin.random.Random

/** Two different numbers; the gap narrows as the level rises. */
private fun closePair(rng: Random, p: DifficultyParams): Pair<Int, Int> {
    val hi = p.scale(20, 999)
    val a = rng.nextInt(1, hi)
    val gap = rng.nextInt(1, maxOf(2, hi / p.scale(3, 40)) + 1)
    val b = if (a > gap && rng.nextBoolean()) a - gap else a + gap
    return a to b
}

// ---------------------------------------------------------------- Speed

class QuickCompare : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val (a, b) = closePair(rng, p)
        return choiceAll(rng, "Tap the larger number", maxOf(a, b).toString(), listOf(a.toString(), b.toString()), p.ms(4_000, 1_800))
    }
}

class EvenOdd : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = rng.nextInt(2, p.scale(30, 999))
        val time = p.ms(4_000, 1_800)
        return if (p.level < 4) {
            choiceAll(rng, "Even or odd?", if (n % 2 == 0) "Even" else "Odd", listOf("Even", "Odd"), time, stimulus = n.toString())
        } else {
            choiceAll(rng, "Divisible by 3?", if (n % 3 == 0) "Yes" else "No", listOf("Yes", "No"), time, stimulus = n.toString())
        }
    }
}

/** The word and the arrow can disagree. With [opposite] the player answers the reverse direction. */
class ArrowStroop(private val opposite: Boolean) : GameTemplate {
    private val names = listOf("Up", "Right", "Down", "Left")
    private val arrows = listOf("↑", "→", "↓", "←")
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val arrow = rng.nextInt(4)
        val word = if (rng.nextInt(10) < 3) arrow else (arrow + rng.nextInt(1, 4)) % 4
        val answer = if (opposite) (arrow + 2) % 4 else arrow
        val prompt = if (opposite) "Tap the OPPOSITE of the arrow" else "Which way does the ARROW point?"
        return choiceAll(
            rng, prompt, names[answer], names, p.ms(5_000, 2_200),
            stimulus = "${names[word].uppercase()}\n${arrows[arrow]}",
        )
    }
}

class GoNoGo : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val go = rng.nextInt(10) < 7
        val flipped = p.level >= 5 && index % 3 == 2
        val (goShape, stopShape) = if (flipped) "■" to "●" else "●" to "■"
        return choiceAll(
            rng, "$goShape means GO, $stopShape means STOP", if (go) "GO" else "STOP", listOf("GO", "STOP"),
            p.ms(3_000, 1_200), stimulus = if (go) goShape else stopShape,
        )
    }
}

class RapidCount : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = rng.nextInt(1, p.scale(5, 12) + 1)
        val rows = List(n) { "●" }.chunked(4).joinToString("\n") { it.joinToString(" ") }
        return choice(rng, "How many dots?", n.toString(), numWrong(rng, n, 3, min = 1), p.choices, p.ms(4_000, 2_000), stimulus = rows)
    }
}

// ---------------------------------------------------------------- Executive

class RuleShift : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val (a, b) = closePair(rng, p)
        val larger = if (p.level < 5) index % 2 == 0 else rng.nextBoolean()
        val answer = if (larger) maxOf(a, b) else minOf(a, b)
        val rule = if (larger) "LARGER" else "SMALLER"
        return choiceAll(rng, "Rule: tap the $rule number", answer.toString(), listOf(a.toString(), b.toString()), p.ms(5_000, 2_200))
    }
}

class TaskSwitcher : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        var n = rng.nextInt(1, 100)
        if (n == 50) n = 51
        val parity = if (p.level < 5) index % 2 == 0 else rng.nextBoolean()
        val prompt = if (parity) "Rule: is it EVEN?" else "Rule: is it MORE than 50?"
        val yes = if (parity) n % 2 == 0 else n > 50
        return choiceAll(rng, prompt, if (yes) "Yes" else "No", listOf("Yes", "No"), p.ms(5_500, 2_500), stimulus = n.toString())
    }
}

class RuleKeeper : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val k = rng.nextInt(2, p.scale(5, 10))
        val correct = k * rng.nextInt(2, 13)
        val wrong = (2..(k * 13)).filter { it % k != 0 }.map { it.toString() }
        return choice(rng, "Tap the number divisible by $k", correct.toString(), wrong, p.choices + 1, p.ms(10_000, 6_000))
    }
}

class DeadlineDash : GameTemplate {
    private val tasks = listOf("Report", "Laundry", "Email", "Groceries", "Homework", "Call bank", "Dishes", "Gym", "Dentist", "Bills")
    private fun clock(h: Int): String = when {
        h == 12 -> "12 PM"
        h > 12 -> "${h - 12} PM"
        else -> "$h AM"
    }

    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.choices + (if (p.level >= 7) 1 else 0)
        val names = tasks.shuffled(rng).take(n)
        val hours = (8..20).shuffled(rng).take(n)
        val plan = names.zip(hours)
        val askLast = p.level >= 5 && rng.nextBoolean()
        val answer = (if (askLast) plan.maxBy { it.second } else plan.minBy { it.second }).first
        val q = if (askLast) "Which task can wait the longest?" else "Which task is due first?"
        val text = plan.joinToString("\n") { "${it.first}: ${clock(it.second)}" }
        return choiceAll(rng, q, answer, names, p.ms(16_000, 9_000), stimulus = text)
    }
}
