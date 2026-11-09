package com.mindforge101.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.mindforge101.app.domain.Achievement
import com.mindforge101.app.domain.Achievements
import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.PlayerStats
import com.mindforge101.app.domain.Scoring
import com.mindforge101.app.domain.SessionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val activeProfileId: Long = 0,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val haptics: Boolean = true,
)

/** What changed as a result of finishing a session; shown on the result screen. */
data class SessionOutcome(
    val result: SessionResult,
    val gameName: String,
    val previousBest: Int,
    val newBest: Boolean,
    val previousLevel: Float,
    val nextLevel: Float,
    val unlocked: List<Achievement>,
)

class Repository(context: Context) {
    private val appContext = context.applicationContext
    private val db = MindForgeDb.create(appContext)

    private object Keys {
        val activeProfile = longPreferencesKey("active_profile")
        val largeText = booleanPreferencesKey("large_text")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val haptics = booleanPreferencesKey("haptics")
    }

    val settings: Flow<Settings> = appContext.dataStore.data.map {
        Settings(
            activeProfileId = it[Keys.activeProfile] ?: 0,
            largeText = it[Keys.largeText] ?: false,
            reduceMotion = it[Keys.reduceMotion] ?: false,
            haptics = it[Keys.haptics] ?: true,
        )
    }

    val profiles: Flow<List<ProfileEntity>> = db.profiles().observeAll()
    fun progress(profileId: Long) = db.progress().observe(profileId)
    fun sessions(profileId: Long) = db.sessions().observe(profileId)
    fun achievements(profileId: Long) = db.achievements().observe(profileId)

    /** Guarantees a usable active profile on first launch or after one is deleted. */
    suspend fun ensureProfile(): Long {
        val active = settings.first().activeProfileId
        db.profiles().get(active)?.let { return it.id }
        val id = db.profiles().first()?.id ?: db.profiles().insert(ProfileEntity(name = "Player"))
        setActiveProfile(id)
        return id
    }

    suspend fun setActiveProfile(id: Long) {
        appContext.dataStore.edit { it[Keys.activeProfile] = id }
    }

    suspend fun addProfile(name: String) {
        val id = db.profiles().insert(ProfileEntity(name = name.trim().ifEmpty { "Player" }))
        setActiveProfile(id)
    }

    suspend fun renameProfile(id: Long, name: String) {
        val current = db.profiles().get(id) ?: return
        db.profiles().upsert(current.copy(name = name.trim().ifEmpty { current.name }))
    }

    suspend fun deleteProfile(id: Long) {
        db.withTransaction {
            db.sessions().deleteFor(id)
            db.progress().deleteFor(id)
            db.achievements().deleteFor(id)
            db.profiles().delete(id)
        }
        ensureProfile()
    }

    suspend fun setLargeText(value: Boolean) {
        appContext.dataStore.edit { it[Keys.largeText] = value }
    }

    suspend fun setReduceMotion(value: Boolean) {
        appContext.dataStore.edit { it[Keys.reduceMotion] = value }
    }

    suspend fun setHaptics(value: Boolean) {
        appContext.dataStore.edit { it[Keys.haptics] = value }
    }

    suspend fun levelFor(profileId: Long, gameId: String): Float =
        db.progress().get(profileId, gameId)?.level ?: 1f

    suspend fun recordSession(profileId: Long, game: GameDef, result: SessionResult): SessionOutcome {
        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        // Sanity check: a result beyond what a real session can reach is stored without rewards.
        val trusted = result.score <= Scoring.maxScore(result.rounds, result.level)
        val xp = if (trusted) result.xp else 0
        val score = if (trusted) result.score else 0

        return db.withTransaction {
            val old = db.progress().get(profileId, game.id) ?: GameProgressEntity(profileId, game.id)
            val nextLevel = AdaptiveDifficultyEngine.nextLevel(old.level, result.performance)
            db.progress().upsert(
                old.copy(level = nextLevel, bestScore = maxOf(old.bestScore, score), plays = old.plays + 1, lastPlayed = now),
            )
            db.sessions().insert(
                SessionEntity(
                    profileId = profileId, gameId = game.id, category = game.category.name, level = result.level,
                    score = score, correct = result.correct, accuracy = result.accuracy, xp = xp,
                    perfect = result.perfect, day = today, timestamp = now,
                ),
            )
            val profile = db.profiles().get(profileId) ?: error("missing profile $profileId")
            val streak = when (profile.lastPlayDay) {
                today -> profile.streakDays
                today - 1 -> profile.streakDays + 1
                else -> 1
            }
            val updated = profile.copy(
                totalXp = profile.totalXp + xp,
                streakDays = streak,
                longestStreakDays = maxOf(profile.longestStreakDays, streak),
                lastPlayDay = today,
            )
            db.profiles().upsert(updated)

            val stats = statsFor(updated, db.sessions().all(profileId), today)
            val unlocked = Achievements.newlyUnlocked(stats, db.achievements().ids(profileId).toSet())
            unlocked.forEach { db.achievements().insert(AchievementEntity(profileId, it.id, now)) }

            SessionOutcome(
                result = result.copy(score = score, xp = xp),
                gameName = game.name,
                previousBest = old.bestScore,
                newBest = score > old.bestScore,
                previousLevel = old.level,
                nextLevel = nextLevel,
                unlocked = unlocked,
            )
        }
    }

    private fun statsFor(profile: ProfileEntity, sessions: List<SessionEntity>, today: Long) = PlayerStats(
        sessions = sessions.size,
        totalCorrect = sessions.sumOf { it.correct },
        perfectSessions = sessions.count { it.perfect },
        streakDays = profile.streakDays,
        longestStreakDays = profile.longestStreakDays,
        distinctGames = sessions.map { it.gameId }.toSet().size,
        maxLevelPlayed = sessions.maxOfOrNull { it.level } ?: 0,
        totalXp = profile.totalXp,
        sessionsByCategory = sessions.groupingBy { Category.valueOf(it.category) }.eachCount(),
        sessionsToday = sessions.count { it.day == today },
    )
}
