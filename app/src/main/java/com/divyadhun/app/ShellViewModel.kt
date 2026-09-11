package com.divyadhun.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divyadhun.app.data.UserPreferences
import com.divyadhun.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

enum class ThemeMode { SYSTEM, LIGHT, DARK;
    companion object { fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: SYSTEM }
}

data class ShellUiState(
    val preferences: UserPreferences = UserPreferences(),
    val loaded: Boolean = false,
    val error: Boolean = false
) {
    val theme: ThemeMode get() = preferences.theme
}

class ShellViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)
    private val mutableState = MutableStateFlow(ShellUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferences
                .catch { error ->
                    if (error !is IOException) throw error
                    mutableState.update { it.copy(loaded = true, error = true) }
                }
                .collect { preferences ->
                    mutableState.value = ShellUiState(preferences = preferences, loaded = true)
                }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                repository.setTheme(mode)
                mutableState.update { it.copy(error = false) }
            } catch (_: IOException) {
                mutableState.update { it.copy(error = true) }
            }
        }
    }
}
