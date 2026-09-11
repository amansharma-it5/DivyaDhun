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
    val theme: ThemeMode = ThemeMode.SYSTEM
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val onboardingStep = androidx.datastore.preferences.core.intPreferencesKey("onboarding_step")
        val language = stringPreferencesKey("language")
        val preferredDeityIds = stringSetPreferencesKey("preferred_deity_ids")
        val listeningTime = stringPreferencesKey("listening_time")
        val theme = stringPreferencesKey("theme")
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
                theme = ThemeMode.fromStored(values[Keys.theme])
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

    suspend fun resetForTest() = context.userPreferencesDataStore.edit { it.clear() }
}
