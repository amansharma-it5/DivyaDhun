package com.divyadhun.app.content

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divyadhun.app.data.ContentRepository
import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.data.UserPreferences
import com.divyadhun.app.data.UserPreferencesRepository
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val deities: List<Deity> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val preferredDeityIds: Set<String> = emptySet(),
    val content: HomeContent = HomeContent(),
    val loaded: Boolean = false,
    val error: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val preferences = UserPreferencesRepository(application)
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val deities = content.getDeities()
                val tracks = content.getTracks()
                val assembler = HomeContentAssembler(deities, tracks)
                preferences.preferences.collect { user ->
                    mutableState.value = HomeUiState(
                        deities = deities,
                        tracks = tracks,
                        preferredDeityIds = user.preferredDeityIds,
                        content = assembler.assemble(user.preferredDeityIds),
                        loaded = true
                    )
                }
            } catch (_: Exception) {
                mutableState.update { it.copy(loaded = true, error = true) }
            }
        }
    }
}

data class ExploreUiState(
    val deities: List<Deity> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val categoryCounts: Map<DevotionalCategory, Int> = emptyMap(),
    val loaded: Boolean = false,
    val error: Boolean = false
)

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val mutableState = MutableStateFlow(ExploreUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val tracks = content.getTracks()
                mutableState.value = ExploreUiState(
                    deities = content.getDeities(),
                    tracks = tracks,
                    categoryCounts = tracks.groupingBy { it.category }.eachCount(),
                    loaded = true
                )
            }
            catch (_: Exception) { mutableState.update { it.copy(loaded = true, error = true) } }
        }
    }
}

data class SearchUiState(
    val query: String = "",
    val results: List<Track> = emptyList(),
    val deityResults: List<Deity> = emptyList(),
    val isSearching: Boolean = false,
    val loaded: Boolean = false,
    val error: Boolean = false
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val mutableState = MutableStateFlow(SearchUiState())
    val state = mutableState.asStateFlow()

    init { viewModelScope.launch { mutableState.update { it.copy(loaded = true) } } }

    fun setQuery(query: String) {
        mutableState.update { it.copy(query = query, isSearching = query.isNotBlank(), error = false) }
        viewModelScope.launch {
            try {
                mutableState.update {
                    it.copy(
                        results = if (query.isBlank()) emptyList() else content.search(query),
                        deityResults = if (query.isBlank()) emptyList() else content.searchDeities(query),
                        isSearching = false
                    )
                }
            }
            catch (_: Exception) { mutableState.update { it.copy(isSearching = false, error = true) } }
        }
    }

    fun clearQuery() = setQuery("")
}

data class DeityDetailUiState(
    val deity: Deity? = null,
    val tracks: List<Track> = emptyList(),
    val sections: List<DeityContentSection> = emptyList(),
    val availableCategories: List<DevotionalCategory> = emptyList(),
    val loaded: Boolean = false,
    val error: Boolean = false
)

class DeityDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val mutableState = MutableStateFlow(DeityDetailUiState())
    val state = mutableState.asStateFlow()

    fun load(deityId: String) {
        if (state.value.loaded && state.value.deity?.id == deityId) return
        viewModelScope.launch {
            try {
                val deity = content.getDeities().firstOrNull { it.id == deityId }
                val tracks = content.getTracksForDeity(deityId)
                mutableState.value = DeityDetailUiState(
                    deity = deity,
                    tracks = tracks,
                    sections = deitySections(tracks),
                    availableCategories = tracks.map { it.category }.distinct(),
                    loaded = true
                )
            }
            catch (_: Exception) { mutableState.update { it.copy(loaded = true, error = true) } }
        }
    }
}

enum class DeitySection { POPULAR, AARTIS, MANTRAS, CHALISA_STOTRA, BHAJANS }

data class DeityContentSection(val type: DeitySection, val tracks: List<Track>)

private fun deitySections(tracks: List<Track>): List<DeityContentSection> {
    if (tracks.isEmpty()) return emptyList()
    val groups = listOf(
        DeitySection.POPULAR to tracks.take(3),
        DeitySection.AARTIS to tracks.filter { it.category == DevotionalCategory.AARTI },
        DeitySection.MANTRAS to tracks.filter { it.category == DevotionalCategory.MANTRA },
        DeitySection.CHALISA_STOTRA to tracks.filter { it.category == DevotionalCategory.CHALISA || it.category == DevotionalCategory.STOTRA },
        DeitySection.BHAJANS to tracks.filter { it.category == DevotionalCategory.BHAJAN }
    )
    return groups.filter { it.second.isNotEmpty() }.map { DeityContentSection(it.first, it.second) }
}

data class ContentUiState(
    val category: DevotionalCategory? = null,
    val tracks: List<Track> = emptyList(),
    val loaded: Boolean = false,
    val error: Boolean = false
)

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val mutableState = MutableStateFlow(ContentUiState())
    val state = mutableState.asStateFlow()
    fun load(category: DevotionalCategory) {
        if (state.value.loaded) return
        viewModelScope.launch {
            try { mutableState.value = ContentUiState(category, content.getTracksByCategory(category), loaded = true) }
            catch (_: Exception) { mutableState.update { it.copy(loaded = true, error = true) } }
        }
    }
}

data class TrackDetailUiState(val track: Track? = null, val loaded: Boolean = false, val error: Boolean = false)

class TrackDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val content: ContentRepository = LocalContentRepository()
    private val mutableState = MutableStateFlow(TrackDetailUiState())
    val state = mutableState.asStateFlow()
    fun load(trackId: String) {
        if (state.value.loaded && state.value.track?.id == trackId) return
        viewModelScope.launch {
            try { mutableState.value = TrackDetailUiState(content.getTrack(trackId), loaded = true) }
            catch (_: Exception) { mutableState.update { it.copy(loaded = true, error = true) } }
        }
    }
}

data class PreferencesUiState(val preferences: UserPreferences = UserPreferences(), val loaded: Boolean = false, val error: Boolean = false)

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)
    private val mutableState = MutableStateFlow(PreferencesUiState())
    val state = mutableState.asStateFlow()

    init { viewModelScope.launch { repository.preferences.collect { mutableState.value = PreferencesUiState(it, loaded = true) } } }
    fun setTheme(theme: com.divyadhun.app.ThemeMode) = update { repository.setTheme(theme) }
    fun setLanguage(language: com.divyadhun.app.domain.model.Language) = update { repository.setLanguage(language) }
    fun setListeningTime(time: com.divyadhun.app.domain.model.ListeningTime) = update { repository.setListeningTime(time) }
    fun setDeities(ids: Set<String>) = update { repository.setPreferredDeityIds(ids) }
    private fun update(action: suspend () -> Unit) { viewModelScope.launch { try { action(); mutableState.update { it.copy(error = false) } } catch (_: Exception) { mutableState.update { it.copy(error = true) } } } }
}
