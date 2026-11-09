package com.mindforge101.app.domain

/** Aggregated lifetime numbers for one profile; achievements are pure functions of this. */
data class PlayerStats(
    val sessions: Int = 0,
    val totalCorrect: Int = 0,
    val perfectSessions: Int = 0,
    val streakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val distinctGames: Int = 0,
    val maxLevelPlayed: Int = 0,
    val totalXp: Int = 0,
    val sessionsByCategory: Map<Category, Int> = emptyMap(),
    val sessionsToday: Int = 0,
)

class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: (PlayerStats) -> Boolean,
)

object Achievements {
    val all: List<Achievement> = listOf(
        Achievement("first_session", "First Spark", "Finish your first game") { it.sessions >= 1 },
        Achievement("ten_sessions", "Warming Up", "Finish 10 games") { it.sessions >= 10 },
        Achievement("hundred_sessions", "Centurion", "Finish 100 games") { it.sessions >= 100 },
        Achievement("streak_3", "On a Roll", "Play 3 days in a row") { it.longestStreakDays >= 3 },
        Achievement("streak_7", "Week Warrior", "Play 7 days in a row") { it.longestStreakDays >= 7 },
        Achievement("streak_30", "Habit Formed", "Play 30 days in a row") { it.longestStreakDays >= 30 },
        Achievement("perfect", "Flawless", "Get every round right in a game") { it.perfectSessions >= 1 },
        Achievement("perfect_5", "Sharp Mind", "Get 5 perfect games") { it.perfectSessions >= 5 },
        Achievement("correct_1000", "Thousand Right", "Give 1,000 correct answers") { it.totalCorrect >= 1000 },
        Achievement("explorer", "Explorer", "Try 20 different games") { it.distinctGames >= 20 },
        Achievement("level_5", "Rising", "Play a game at level 5") { it.maxLevelPlayed >= 5 },
        Achievement("level_10", "Summit", "Play a game at level 10") { it.maxLevelPlayed >= 10 },
        Achievement("memory_master", "Memory Master", "Finish 15 Memory games") { (it.sessionsByCategory[Category.MEMORY] ?: 0) >= 15 },
        Achievement("logic_master", "Logic Master", "Finish 15 Logic games") { (it.sessionsByCategory[Category.LOGIC] ?: 0) >= 15 },
        Achievement("speed_demon", "Speed Demon", "Finish 15 Speed games") { (it.sessionsByCategory[Category.SPEED] ?: 0) >= 15 },
        Achievement("marathon", "Brain Marathon", "Finish 10 games in one day") { it.sessionsToday >= 10 },
    )

    fun newlyUnlocked(stats: PlayerStats, alreadyUnlocked: Set<String>): List<Achievement> =
        all.filter { it.id !in alreadyUnlocked && it.unlocked(stats) }
}
