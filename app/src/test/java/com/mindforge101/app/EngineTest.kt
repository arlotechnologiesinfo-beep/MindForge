package com.mindforge101.app

import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import com.mindforge101.app.domain.Achievements
import com.mindforge101.app.domain.DailyMix
import com.mindforge101.app.domain.GameSession
import com.mindforge101.app.domain.Performance
import com.mindforge101.app.domain.PlayerStats
import com.mindforge101.app.domain.Ranks
import com.mindforge101.app.domain.Scoring
import com.mindforge101.app.games.Catalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    @Test
    fun strongPerformanceRaisesLevelAndWeakLowersIt() {
        val strong = Performance(1f, 1_000, 0, 8, 10_000)
        val weak = Performance(0.25f, 9_000, 6, 1, 10_000)
        assertTrue(AdaptiveDifficultyEngine.nextLevel(5f, strong) > 5f)
        assertTrue(AdaptiveDifficultyEngine.nextLevel(5f, weak) < 5f)
    }

    @Test
    fun levelStaysInRange() {
        val strong = Performance(1f, 100, 0, 8, 10_000)
        val weak = Performance(0f, 10_000, 8, 0, 10_000)
        assertEquals(10f, AdaptiveDifficultyEngine.nextLevel(10f, strong), 0f)
        assertEquals(1f, AdaptiveDifficultyEngine.nextLevel(1f, weak), 0f)
    }

    @Test
    fun wrongAnswerScoresZeroAndFasterScoresMore() {
        assertEquals(0, Scoring.roundScore(false, 100, 1000, 3))
        assertTrue(Scoring.roundScore(true, 100, 1000, 0) > Scoring.roundScore(true, 900, 1000, 0))
    }

    @Test
    fun sessionTracksResultsAndStaysUnderScoreCap() {
        val session = GameSession(Catalog.games.first(), 10, 1L)
        while (!session.finished) session.submit(session.nextRound(), true, 1)
        val r = session.result()
        assertEquals(8, r.correct)
        assertEquals(1f, r.accuracy, 0f)
        assertTrue(r.perfect)
        assertEquals(8, r.bestStreak)
        assertTrue(r.score <= Scoring.maxScore(8, 10))
    }

    @Test
    fun mistakeResetsStreak() {
        val session = GameSession(Catalog.games.first(), 3, 1L, totalRounds = 4)
        listOf(true, true, false, true).forEach { session.submit(session.nextRound(), it, 500) }
        val r = session.result()
        assertEquals(3, r.correct)
        assertEquals(2, r.bestStreak)
        assertFalse(r.perfect)
    }

    @Test
    fun ranksClimbWithXp() {
        assertEquals("Beginner", Ranks.forXp(0).name)
        assertEquals("Thinker", Ranks.forXp(300).name)
        assertEquals("Mind Forge", Ranks.forXp(50_000).name)
        assertEquals(1f, Ranks.progress(50_000), 0f)
    }

    @Test
    fun dailyMixIsStableAndDiverse() {
        val a = DailyMix.forDay(20_000, Catalog.games)
        assertEquals(a, DailyMix.forDay(20_000, Catalog.games))
        assertEquals(5, a.size)
        assertEquals(5, a.map { it.category }.toSet().size)
    }

    @Test
    fun achievementsUnlockOnceFromStats() {
        val stats = PlayerStats(sessions = 1)
        assertEquals(listOf("first_session"), Achievements.newlyUnlocked(stats, emptySet()).map { it.id })
        assertTrue(Achievements.newlyUnlocked(stats, setOf("first_session")).isEmpty())
    }
}
