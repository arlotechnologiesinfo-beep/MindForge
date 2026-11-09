package com.mindforge101.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastPlayDay: Long = -1,
)

@Entity(tableName = "game_progress", primaryKeys = ["profileId", "gameId"])
data class GameProgressEntity(
    val profileId: Long,
    val gameId: String,
    val level: Float = 1f,
    val bestScore: Int = 0,
    val plays: Int = 0,
    val lastPlayed: Long = 0,
)

@Entity(tableName = "session", indices = [Index("profileId")])
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val gameId: String,
    val category: String,
    val level: Int,
    val score: Int,
    val correct: Int,
    val accuracy: Float,
    val xp: Int,
    val perfect: Boolean,
    val day: Long,
    val timestamp: Long,
)

@Entity(tableName = "achievement", primaryKeys = ["profileId", "id"])
data class AchievementEntity(
    val profileId: Long,
    val id: String,
    val unlockedAt: Long,
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile ORDER BY id")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profile WHERE id = :id")
    suspend fun get(id: Long): ProfileEntity?

    @Query("SELECT * FROM profile ORDER BY id LIMIT 1")
    suspend fun first(): ProfileEntity?

    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    @Upsert
    suspend fun upsert(profile: ProfileEntity)

    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM game_progress WHERE profileId = :profileId")
    fun observe(profileId: Long): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE profileId = :profileId AND gameId = :gameId")
    suspend fun get(profileId: Long, gameId: String): GameProgressEntity?

    @Upsert
    suspend fun upsert(progress: GameProgressEntity)

    @Query("DELETE FROM game_progress WHERE profileId = :profileId")
    suspend fun deleteFor(profileId: Long)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun observe(profileId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM session WHERE profileId = :profileId")
    suspend fun all(profileId: Long): List<SessionEntity>

    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("DELETE FROM session WHERE profileId = :profileId")
    suspend fun deleteFor(profileId: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement WHERE profileId = :profileId ORDER BY unlockedAt DESC")
    fun observe(profileId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievement WHERE profileId = :profileId")
    suspend fun ids(profileId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity)

    @Query("DELETE FROM achievement WHERE profileId = :profileId")
    suspend fun deleteFor(profileId: Long)
}

@Database(
    entities = [ProfileEntity::class, GameProgressEntity::class, SessionEntity::class, AchievementEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MindForgeDb : RoomDatabase() {
    abstract fun profiles(): ProfileDao
    abstract fun progress(): ProgressDao
    abstract fun sessions(): SessionDao
    abstract fun achievements(): AchievementDao

    companion object {
        fun create(context: Context): MindForgeDb =
            Room.databaseBuilder(context, MindForgeDb::class.java, "mindforge.db").build()
    }
}
