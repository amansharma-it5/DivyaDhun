package com.divyadhun.app.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divyadhun.app.data.UserPreferencesRepository
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 0,
    val language: Language = Language.ENGLISH,
    val selectedDeityIds: Set<String> = emptySet(),
    val listeningTime: ListeningTime = ListeningTime.ANYTIME,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)
    private val mutableState = MutableStateFlow(OnboardingUiState())
    // DataStore emits an update for each intermediate onboarding write. Keep the
    // in-progress UI choices authoritative until the final atomic completion write,
    // otherwise an older emission can replace a just-selected deity before Finish.
    private var hasLocalEdits = false
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferences.collect { preferences ->
                if (!hasLocalEdits) {
                    mutableState.update {
                        it.copy(
                            language = preferences.language,
                            step = preferences.onboardingStep,
                            selectedDeityIds = preferences.preferredDeityIds,
                            listeningTime = preferences.listeningTime,
                            loaded = true
                        )
                    }
                } else {
                    mutableState.update { it.copy(loaded = true) }
                }
            }
        }
    }

    fun next() = setStep((state.value.step + 1).coerceAtMost(3))
    fun back() = setStep((state.value.step - 1).coerceAtLeast(0))
    private fun setStep(step: Int) {
        hasLocalEdits = true
        mutableState.update { it.copy(step = step) }
        viewModelScope.launch { repository.setOnboardingStep(step) }
    }
    fun selectLanguage(language: Language, onSaved: () -> Unit = {}) {
        hasLocalEdits = true
        mutableState.update { it.copy(language = language) }
        viewModelScope.launch { repository.setLanguage(language); onSaved() }
    }
    fun toggleDeity(id: String) {
        hasLocalEdits = true
        val ids = state.value.selectedDeityIds.toMutableSet()
        if (!ids.add(id)) ids.remove(id)
        mutableState.update { it.copy(selectedDeityIds = ids) }
        viewModelScope.launch { repository.setPreferredDeityIds(ids) }
    }
    fun selectListeningTime(time: ListeningTime) {
        hasLocalEdits = true
        mutableState.update { it.copy(listeningTime = time) }
        viewModelScope.launch { repository.setListeningTime(time) }
    }

    fun complete(onComplete: () -> Unit) {
        val current = state.value
        mutableState.update { it.copy(saving = true, error = false) }
        viewModelScope.launch {
            try {
                repository.completeOnboarding(current.language, current.selectedDeityIds, current.listeningTime)
                mutableState.update { it.copy(saving = false) }
                onComplete()
            } catch (_: Exception) {
                mutableState.update { it.copy(saving = false, error = true) }
            }
        }
    }
}
