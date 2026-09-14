package com.divyadhun.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.divyadhun.app.ThemeMode
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val onboardingStep: Int = 0,
    val language: Language = Language.ENGLISH,
    val preferredDeityIds: Set<String> = emptySet(),
    val listeningTime: ListeningTime = ListeningTime.ANYTIME,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val simpleMode: Boolean = false,
    val favoriteTrackIds: Set<String> = emptySet(),
    val recentTrackIds: List<String> = emptyList(),
    val lastPlayedTrackId: String? = null
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val onboardingStep = androidx.datastore.preferences.core.intPreferencesKey("onboarding_step")
        val language = stringPreferencesKey("language")
        val preferredDeityIds = stringSetPreferencesKey("preferred_deity_ids")
        val listeningTime = stringPreferencesKey("listening_time")
        val theme = stringPreferencesKey("theme")
        val simpleMode = booleanPreferencesKey("simple_mode")
        val favoriteTrackIds = stringSetPreferencesKey("favorite_track_ids")
        val recentTrackIds = stringPreferencesKey("recent_track_ids")
        val lastPlayedTrackId = stringPreferencesKey("last_played_track_id")
    }

    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { values ->
            UserPreferences(
                onboardingComplete = values[Keys.onboardingComplete] ?: false,
                onboardingStep = values[Keys.onboardingStep] ?: 0,
                language = Language.fromStored(values[Keys.language]),
                preferredDeityIds = values[Keys.preferredDeityIds] ?: emptySet(),
                listeningTime = ListeningTime.fromStored(values[Keys.listeningTime]),
                theme = ThemeMode.fromStored(values[Keys.theme]),
                simpleMode = values[Keys.simpleMode] ?: false,
                favoriteTrackIds = values[Keys.favoriteTrackIds] ?: emptySet(),
                recentTrackIds = decodeIds(values[Keys.recentTrackIds]),
                lastPlayedTrackId = values[Keys.lastPlayedTrackId]
            )
        }

    suspend fun completeOnboarding(language: Language, deityIds: Set<String>, listeningTime: ListeningTime) {
        context.userPreferencesDataStore.edit { values ->
            values[Keys.onboardingComplete] = true
            values[Keys.onboardingStep] = 0
            values[Keys.language] = language.name
            values[Keys.preferredDeityIds] = deityIds
            values[Keys.listeningTime] = listeningTime.name
        }
    }

    suspend fun setLanguage(language: Language) = context.userPreferencesDataStore.edit { it[Keys.language] = language.name }
    suspend fun setOnboardingStep(step: Int) = context.userPreferencesDataStore.edit { it[Keys.onboardingStep] = step }
    suspend fun setPreferredDeityIds(ids: Set<String>) = context.userPreferencesDataStore.edit { it[Keys.preferredDeityIds] = ids }
    suspend fun setListeningTime(time: ListeningTime) = context.userPreferencesDataStore.edit { it[Keys.listeningTime] = time.name }
    suspend fun setTheme(theme: ThemeMode) = context.userPreferencesDataStore.edit { it[Keys.theme] = theme.name }
    suspend fun setSimpleMode(enabled: Boolean) = context.userPreferencesDataStore.edit { it[Keys.simpleMode] = enabled }

    suspend fun setFavoriteTrack(trackId: String, favorite: Boolean) = context.userPreferencesDataStore.edit { values ->
        val updated = (values[Keys.favoriteTrackIds] ?: emptySet()).toMutableSet()
        if (favorite) updated += trackId else updated -= trackId
        values[Keys.favoriteTrackIds] = updated
    }

    suspend fun recordPlayedTrack(trackId: String, maxHistory: Int = MAX_RECENT_TRACKS) = context.userPreferencesDataStore.edit { values ->
        val recent = listOf(trackId) + decodeIds(values[Keys.recentTrackIds]).filterNot { it == trackId }
        values[Keys.recentTrackIds] = encodeIds(recent.take(maxHistory))
        values[Keys.lastPlayedTrackId] = trackId
    }

    suspend fun clearHistory() = context.userPreferencesDataStore.edit { values ->
        values.remove(Keys.recentTrackIds)
        values.remove(Keys.lastPlayedTrackId)
    }

    suspend fun resetForTest() = context.userPreferencesDataStore.edit { it.clear() }

    companion object {
        private const val MAX_RECENT_TRACKS = 20

        internal fun encodeIds(ids: List<String>) = ids.joinToString(ID_SEPARATOR)

        internal fun decodeIds(value: String?): List<String> = value
            ?.split(ID_SEPARATOR)
            ?.filter(String::isNotBlank)
            ?.distinct()
            ?: emptyList()

        private const val ID_SEPARATOR = "|"
    }
}
