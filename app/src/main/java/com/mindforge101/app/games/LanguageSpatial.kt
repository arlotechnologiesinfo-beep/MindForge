package com.mindforge101.app.games

import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GameTemplate
import com.mindforge101.app.domain.Round
import kotlin.random.Random

// ---------------------------------------------------------------- Language data

internal object Words {
    val byLength: Map<Int, List<String>> = mapOf(
        4 to listOf("tree", "bird", "moon", "star", "rain", "fish", "door", "lamp", "ship", "gold", "wind", "sand", "cake", "road", "bell", "farm"),
        5 to listOf("apple", "river", "cloud", "bread", "light", "house", "plant", "stone", "chair", "music", "ocean", "tiger", "water", "smile", "green"),
        6 to listOf("planet", "garden", "bridge", "window", "silver", "rocket", "forest", "castle", "pencil", "button", "orange", "travel"),
        7 to listOf("journey", "picture", "balance", "curious", "kitchen", "justice", "mystery", "network", "theater"),
        8 to listOf("elephant", "mountain", "notebook", "sunshine", "computer", "triangle", "language", "thinking"),
    )
    val all: List<String> = byLength.values.flatten()

    val spelling = listOf(
        "necessary", "separate", "definitely", "receive", "believe", "calendar", "embarrass", "environment",
        "occurred", "tomorrow", "restaurant", "government", "library", "beginning", "different", "interest",
        "lightning", "parallel", "address", "achieve", "answer", "bicycle", "business", "comfortable",
    )

    val synonyms = listOf(
        "happy" to "joyful", "big" to "large", "fast" to "quick", "smart" to "clever", "begin" to "start",
        "end" to "finish", "small" to "tiny", "angry" to "furious", "calm" to "peaceful", "brave" to "fearless",
        "rich" to "wealthy", "tired" to "weary", "strange" to "odd", "silent" to "quiet", "shout" to "yell",
        "hard" to "difficult", "buy" to "purchase", "help" to "assist", "choose" to "select", "error" to "mistake",
        "huge" to "enormous", "trip" to "journey", "scared" to "afraid", "look" to "gaze", "keen" to "eager",
    )

    val antonyms = listOf(
        "hot" to "cold", "up" to "down", "light" to "dark", "fast" to "slow", "happy" to "sad", "open" to "closed",
        "full" to "empty", "hard" to "soft", "early" to "late", "strong" to "weak", "young" to "old",
        "rich" to "poor", "day" to "night", "true" to "false", "love" to "hate", "win" to "lose", "near" to "far",
        "thick" to "thin", "wet" to "dry", "clean" to "dirty", "always" to "never", "ancient" to "modern",
        "expand" to "shrink", "accept" to "reject", "brave" to "cowardly",
    )

    val categories: List<List<String>> = listOf(
        listOf("apple", "banana", "mango", "grape", "cherry", "peach"),
        listOf("tiger", "horse", "rabbit", "eagle", "shark", "zebra"),
        listOf("red", "blue", "green", "yellow", "purple", "orange"),
        listOf("hammer", "wrench", "saw", "drill", "pliers", "chisel"),
        listOf("car", "train", "bicycle", "plane", "boat", "truck"),
        listOf("piano", "guitar", "violin", "drum", "flute", "harp"),
        listOf("india", "japan", "brazil", "kenya", "norway", "egypt"),
        listOf("mars", "venus", "saturn", "jupiter", "mercury", "neptune"),
    )
}

// ---------------------------------------------------------------- Language

class WordScramble(private val minLen: Int, private val maxLen: Int, private val speed: Float = 1f) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val len = p.scale(minLen, maxLen)
        val pool = Words.byLength.getValue(len)
        val word = rng.pick(pool)
        var scrambled = word
        var guard = 0
        while (scrambled == word && guard++ < 20) scrambled = word.toList().shuffled(rng).joinToString("")
        return choice(
            rng, "Unscramble the letters", word, pool, p.choices,
            (p.ms(16_000, 11_000) * speed).toLong(), stimulus = scrambled.uppercase(),
        )
    }
}

class SpellingFix : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val word = rng.pick(Words.spelling)
        val wrong = linkedSetOf<String>()
        var tries = 0
        while (wrong.size < 4 && tries++ < 60) {
            val i = rng.nextInt(1, word.length - 1)
            val m = when (rng.nextInt(3)) {
                0 -> word.substring(0, i) + word[i + 1] + word[i] + word.substring(i + 2)
                1 -> word.removeRange(i, i + 1)
                else -> word.substring(0, i) + word[i] + word.substring(i)
            }
            if (m != word) wrong += m
        }
        return choice(rng, "Which spelling is correct?", word, wrong, p.choices, p.ms(14_000, 9_000))
    }
}

enum class Relation { SYNONYM, ANTONYM, CATEGORY }

class WordRelation(private val relation: Relation) : GameTemplate {
    private val distractorPool: List<String> =
        (Words.synonyms + Words.antonyms).flatMap { listOf(it.first, it.second) }.distinct()

    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val time = p.ms(14_000, 9_000)
        return when (relation) {
            Relation.SYNONYM, Relation.ANTONYM -> {
                val list = if (relation == Relation.SYNONYM) Words.synonyms else Words.antonyms
                val (a, b) = rng.pick(list)
                val (word, answer) = if (rng.nextBoolean()) a to b else b to a
                val label = if (relation == Relation.SYNONYM) "means the SAME as" else "means the OPPOSITE of"
                val pairs = list.filter { it.first == word || it.second == word || it.first == answer || it.second == answer }
                val banned = pairs.flatMap { listOf(it.first, it.second) }.toSet()
                choice(rng, "Which word $label", answer, distractorPool.filter { it !in banned }, p.choices, time, stimulus = word.uppercase())
            }
            Relation.CATEGORY -> {
                val cats = Words.categories.shuffled(rng)
                val main = cats[0]; val other = cats[1]
                val odd = rng.pick(other)
                val group = main.shuffled(rng).take(p.choices - 1)
                choiceAll(rng, "Which word does not belong?", odd, group + odd, time)
            }
        }
    }
}

// ---------------------------------------------------------------- Spatial

internal val COMPASS_ARROWS = listOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")

class RotateArrow : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val useEighths = p.level >= 6
        val steps = if (useEighths) rng.nextInt(1, 8) else rng.nextInt(1, 4) * 2
        val start = if (useEighths) rng.nextInt(8) else rng.nextInt(4) * 2
        val clockwise = if (p.level >= 3) rng.nextBoolean() else true
        val result = ((start + if (clockwise) steps else -steps) % 8 + 8) % 8
        val deg = steps * 45
        val dir = if (clockwise) "clockwise" else "counter-clockwise"
        val pool = if (useEighths) COMPASS_ARROWS else listOf("↑", "→", "↓", "←")
        return choice(
            rng, "Rotate $deg° $dir. Where does it point?", COMPASS_ARROWS[result], pool, minOf(p.choices, pool.size),
            p.ms(14_000, 9_000), stimulus = COMPASS_ARROWS[start],
        )
    }
}

class MirrorArrow : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val horizontal = rng.nextBoolean()
        // indices 0..7 clockwise from up; left-right mirror: i -> (8 - i) % 8 ; top-bottom flip: i -> (4 - i + 8) % 8
        var start: Int
        var result: Int
        do {
            start = if (p.level < 5) rng.nextInt(4) * 2 + 1 else rng.nextInt(8)
            result = if (horizontal) (8 - start) % 8 else (4 - start + 8) % 8
        } while (result == start)
        val q = if (horizontal) "Mirror it left ↔ right" else "Flip it top ↕ bottom"
        return choice(rng, q, COMPASS_ARROWS[result], COMPASS_ARROWS, minOf(p.choices, 5), p.ms(14_000, 8_000), stimulus = COMPASS_ARROWS[start])
    }
}

class CompassTurns : GameTemplate {
    private val dirs = listOf("North", "East", "South", "West")
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        var facing = rng.nextInt(4)
        val start = facing
        val turns = List(p.scale(1, 5)) { rng.pick(listOf("right", "left", "around")) }
        for (t in turns) facing = (facing + when (t) { "right" -> 1; "left" -> 3; else -> 2 }) % 4
        val text = "You face ${dirs[start]}.\nTurn " + turns.joinToString(", then ")
        return choiceAll(rng, "Which way do you face now?", dirs[facing], dirs, p.ms(18_000, 12_000), stimulus = text)
    }
}

class PathSteps(private val distanceMode: Boolean) : GameTemplate {
    override fun generate(rng: Random, p: DifficultyParams, index: Int): Round {
        val n = p.scale(3, 9)
        var x = 0; var y = 0
        val moves = List(n) {
            when (rng.nextInt(4)) {
                0 -> { y++; "↑" }
                1 -> { y--; "↓" }
                2 -> { x++; "→" }
                else -> { x--; "←" }
            }
        }
        val stim = moves.joinToString(" ")
        val time = p.ms(20_000, 14_000)
        return if (distanceMode) {
            val d = Math.abs(x) + Math.abs(y)
            choice(rng, "Start at the origin. How many single steps back to the start?", d.toString(), numWrong(rng, d, 3, min = 0), p.choices, time, stimulus = stim)
        } else {
            val answer = "($x, $y)"
            val wrong = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1, 1 to 1, -1 to -1, 2 to 0, 0 to 2, y to x).map { (dx, dy) -> "(${x + dx}, ${y + dy})" }
            choice(rng, "Start at (0, 0): ↑ is +y, → is +x. Where do you end?", answer, wrong, p.choices, time, stimulus = stim)
        }
    }
}
