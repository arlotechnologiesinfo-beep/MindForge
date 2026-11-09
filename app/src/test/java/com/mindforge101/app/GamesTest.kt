package com.mindforge101.app

import com.mindforge101.app.domain.ChoiceRound
import com.mindforge101.app.domain.DifficultyParams
import com.mindforge101.app.domain.GlyphGridRound
import com.mindforge101.app.domain.GridRound
import com.mindforge101.app.domain.NumberRound
import com.mindforge101.app.domain.Round
import com.mindforge101.app.domain.SequenceRound
import com.mindforge101.app.games.Catalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GamesTest {
    @Test(timeout = 90_000)
    fun catalogHasSixtyUniqueGamesInAllCategories() {
        val games = Catalog.games
        assertTrue("got ${games.size}", games.size >= 60)
        assertEquals(games.size, games.map { it.id }.toSet().size)
        assertEquals(games.size, games.map { it.name }.toSet().size)
        assertEquals(10, games.map { it.category }.toSet().size)
    }

    private fun validate(id: String, r: Round) {
        assertTrue("$id prompt", r.prompt.isNotBlank())
        assertTrue("$id limit", r.timeLimitMs > 0)
        when (r) {
            is ChoiceRound -> {
                assertTrue("$id needs 2+ options: ${r.options}", r.options.size >= 2)
                assertEquals("$id duplicate options ${r.options}", r.options.size, r.options.toSet().size)
                assertTrue("$id correct index ${r.correctIndex} ${r.options}", r.correctIndex in r.options.indices)
            }
            is NumberRound -> {
                assertTrue(id, r.answer.all { it.isDigit() } && r.answer.isNotEmpty())
                assertEquals(id, r.answer, r.memorize)
            }
            is GridRound -> {
                assertTrue(id, r.targets.isNotEmpty())
                assertTrue(id, r.targets.all { it in 0 until r.size * r.size })
            }
            is SequenceRound -> {
                assertTrue(id, r.sequence.isNotEmpty())
                assertTrue(id, r.sequence.all { it in 0 until r.pads })
            }
            is GlyphGridRound -> {
                assertEquals(id, r.columns * r.columns, r.glyphs.size)
                val odd = r.glyphs[r.oddIndex]
                assertEquals("$id exactly one odd", 1, r.glyphs.count { it == odd })
                assertEquals("$id two glyphs", 2, r.glyphs.toSet().size)
            }
        }
    }

    @Test(timeout = 90_000)
    fun everyGameGeneratesValidRoundsAtEveryLevel() {
        for (game in Catalog.games) for (level in 1..10) for (seed in 0L until 40L) {
            val rng = Random(seed * 31 + level)
            repeat(8) { i -> validate("${game.id}@L$level#$seed", game.template.generate(rng, DifficultyParams(level), i)) }
        }
    }

    @Test(timeout = 90_000)
    fun generationIsDeterministicForASeed() {
        for (game in Catalog.games) {
            val a = game.template.generate(Random(99), DifficultyParams(5), 0)
            val b = game.template.generate(Random(99), DifficultyParams(5), 0)
            assertEquals(game.id, a, b)
        }
    }
}
