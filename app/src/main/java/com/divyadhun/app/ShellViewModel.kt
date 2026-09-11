package com.divyadhun.app
import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
val android.content.Context.appearanceStore by preferencesDataStore("appearance")
enum class ThemeMode { SYSTEM, LIGHT, DARK;
 companion object { fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: SYSTEM }
}
data class ShellUiState(val theme: ThemeMode = ThemeMode.SYSTEM, val loaded: Boolean = false, val error: Boolean = false)
class ShellViewModel(application: Application) : AndroidViewModel(application) {
 private val store = application.appearanceStore
 private val key = stringPreferencesKey("theme")
 private val mutableState = MutableStateFlow(ShellUiState())
 val state = mutableState.asStateFlow()
 init { viewModelScope.launch {
  store.data.catch { error ->
   if (error !is IOException) throw error
   mutableState.update { it.copy(loaded = true, error = true) }
  }.collect { prefs -> mutableState.value = ShellUiState(ThemeMode.fromStored(prefs[key]), true) }
 } }
 fun setTheme(mode: ThemeMode) { viewModelScope.launch {
  try { store.edit { it[key] = mode.name }; mutableState.update { it.copy(error = false) } }
  catch (_: IOException) { mutableState.update { it.copy(error = true) } }
 } }
}

