package com.mindforge101.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge101.app.data.AchievementEntity
import com.mindforge101.app.data.GameProgressEntity
import com.mindforge101.app.data.ProfileEntity
import com.mindforge101.app.data.Repository
import com.mindforge101.app.data.SessionEntity
import com.mindforge101.app.data.SessionOutcome
import com.mindforge101.app.data.Settings
import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.SessionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    val settings: StateFlow<Settings> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    val profiles: StateFlow<List<ProfileEntity>> = repo.profiles.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeProfile: StateFlow<ProfileEntity?> =
        combine(profiles, settings) { list, s -> list.firstOrNull { it.id == s.activeProfileId } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val activeId = settings.map { it.activeProfileId }.distinctUntilChanged()

    val progress: StateFlow<Map<String, GameProgressEntity>> =
        activeId.flatMapLatest { repo.progress(it) }
            .map { list -> list.associateBy { it.gameId } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val sessions: StateFlow<List<SessionEntity>> =
        activeId.flatMapLatest { repo.sessions(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val achievements: StateFlow<List<AchievementEntity>> =
        activeId.flatMapLatest { repo.achievements(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** The session that just ended, read by the result screen. */
    var lastOutcome by mutableStateOf<SessionOutcome?>(null)
        private set

    init {
        viewModelScope.launch { repo.ensureProfile() }
    }

    fun finishSession(game: GameDef, result: SessionResult, onSaved: () -> Unit) {
        viewModelScope.launch {
            val profileId = repo.ensureProfile()
            lastOutcome = repo.recordSession(profileId, game, result)
            onSaved()
        }
    }

    fun switchProfile(id: Long) = launch { repo.setActiveProfile(id) }
    fun addProfile(name: String) = launch { repo.addProfile(name) }
    fun renameProfile(id: Long, name: String) = launch { repo.renameProfile(id, name) }
    fun deleteProfile(id: Long) = launch { repo.deleteProfile(id) }
    fun setLargeText(value: Boolean) = launch { repo.setLargeText(value) }
    fun setReduceMotion(value: Boolean) = launch { repo.setReduceMotion(value) }
    fun setHaptics(value: Boolean) = launch { repo.setHaptics(value) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

/** Average accuracy (0..100) of the last 10 sessions per category; unplayed categories are absent. */
fun categoryScores(sessions: List<SessionEntity>): Map<Category, Int> =
    sessions.groupBy { Category.valueOf(it.category) }.mapValues { (_, list) ->
        val recent = list.sortedByDescending { it.timestamp }.take(10)
        (recent.map { it.accuracy }.average() * 100).toInt()
    }
