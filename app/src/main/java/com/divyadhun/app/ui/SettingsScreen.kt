package com.divyadhun.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.divyadhun.app.R
import com.divyadhun.app.ThemeMode
import com.divyadhun.app.content.PreferencesUiState
import com.divyadhun.app.content.LibraryUiState
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.ui.theme.Space

@Composable
fun LibraryScreen(
    state: PreferencesUiState,
    library: LibraryUiState,
    tracks: List<Track>,
    deities: List<Deity>,
    onTheme: (ThemeMode) -> Unit,
    onSimpleMode: (Boolean) -> Unit,
    onLanguage: (Language) -> Unit,
    onTime: (ListeningTime) -> Unit,
    onDeities: (Set<String>) -> Unit,
    onTrack: (String) -> Unit,
    onFavorite: (String) -> Unit,
    onClearHistory: () -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    var showDeities by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val favorites = tracks.filter { it.id in library.favoriteTrackIds }
    val recent = library.recentTrackIds.mapNotNull { id -> tracks.firstOrNull { it.id == id } }
    val lastPlayed = library.lastPlayedTrackId?.let { id -> tracks.firstOrNull { it.id == id } }
    ContentList(R.string.library, R.string.library_intro, "screen_LIBRARY") {
        item { SectionHeader(stringResource(R.string.favorites_title)) }
        if (favorites.isEmpty()) item { EmptyState(stringResource(R.string.favorites_empty_title), stringResource(R.string.favorites_empty_body), Modifier.testTag("favorites_empty_title")) }
        else item { TrackGroup(favorites, state.preferences.language, playbackState, playbackController, onTrack, library.favoriteTrackIds, onFavorite) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.recently_played), style = MaterialTheme.typography.titleLarge)
                if (recent.isNotEmpty()) TextButton(onClick = onClearHistory, modifier = Modifier.testTag("clear_history")) { Text(stringResource(R.string.clear_history)) }
            }
        }
        if (recent.isEmpty()) item { EmptyState(stringResource(R.string.recently_played), stringResource(R.string.recently_played_empty), Modifier.testTag("recently_played_empty")) }
        else item { TrackGroup(recent, state.preferences.language, playbackState, playbackController, onTrack, library.favoriteTrackIds, onFavorite) }
        lastPlayed?.let { track ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Text(stringResource(R.string.last_played), style = MaterialTheme.typography.titleLarge)
                    TrackRow(track, state.preferences.language, playbackState, playbackController, listOf(track), onClick = { onTrack(track.id) }, isFavorite = track.id in library.favoriteTrackIds, onFavorite = { onFavorite(track.id) })
                }
            }
        }
        item { Text(stringResource(R.string.preferences), style = MaterialTheme.typography.titleLarge) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    FilterChip(selected = state.preferences.language == Language.ENGLISH, onClick = { onLanguage(Language.ENGLISH) }, label = { Text(stringResource(R.string.language_english)) }, modifier = Modifier.testTag("setting_language_ENGLISH"))
                    FilterChip(selected = state.preferences.language == Language.HINDI, onClick = { onLanguage(Language.HINDI) }, label = { Text(stringResource(R.string.language_hindi)) }, modifier = Modifier.testTag("setting_language_HINDI"))
                }
            }
        }
        item {
            PreferenceButton(stringResource(R.string.your_gods), selectedSummary(deities, state.preferences.preferredDeityIds), "setting_deities") { showDeities = true }
        }
        item {
            PreferenceButton(stringResource(R.string.listening_time), stringResource(listeningTimeNameRes(state.preferences.listeningTime)), "setting_time") { showTime = true }
        }
        item { Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge) }
        item {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Text(stringResource(R.string.simple_mode), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.simple_mode_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.preferences.simpleMode, onCheckedChange = onSimpleMode, modifier = Modifier.testTag("simple_mode_toggle"))
            }
        }
        items(ThemeMode.entries.toList(), key = { it.name }) { mode ->
            val label = when (mode) { ThemeMode.SYSTEM -> R.string.system; ThemeMode.LIGHT -> R.string.light; ThemeMode.DARK -> R.string.dark }
            OutlinedButton(onClick = { onTheme(mode) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("theme_" + mode.name).semantics { selected = state.preferences.theme == mode }) {
                RadioButton(selected = state.preferences.theme == mode, onClick = null)
                Spacer(Modifier.width(Space.sm))
                Text(stringResource(label))
            }
        }
        if (state.error || library.error) item { Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error) }
    }
    if (showDeities) DeityDialog(deities, state.preferences.preferredDeityIds, { onDeities(it) }, { showDeities = false })
    if (showTime) TimeDialog(state.preferences.listeningTime, onTime, { showTime = false })
}

@Composable private fun PreferenceButton(title: String, summary: String, tag: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(tag), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Space.md, vertical = Space.sm)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun selectedSummary(deities: List<Deity>, ids: Set<String>): String {
    val names = deities.filter { it.id in ids }.map { stringResource(deityNameRes(it.id)) }
    return if (names.isEmpty()) stringResource(R.string.no_preference_deities) else names.joinToString(", ")
}

@Composable private fun DeityDialog(deities: List<Deity>, selected: Set<String>, onSelected: (Set<String>) -> Unit, onDismiss: () -> Unit) {
    var draft by remember(selected) { mutableStateOf(selected) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.choose_gods)) }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Space.xs)) { items(deities, key = { it.id }) { deity ->
            val checked = deity.id in draft
            OutlinedButton(onClick = { draft = if (checked) draft - deity.id else draft + deity.id }, modifier = Modifier.fillMaxWidth().testTag("setting_deity_${deity.id}")) { RadioButton(checked, onClick = null); Spacer(Modifier.width(Space.sm)); Text(stringResource(deityNameRes(deity.id))) }
        } }
    }, confirmButton = { TextButton(onClick = { onSelected(draft); onDismiss() }, modifier = Modifier.testTag("setting_deities_done")) { Text(stringResource(R.string.done)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) } })
}

@Composable private fun TimeDialog(selected: ListeningTime, onSelected: (ListeningTime) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.choose_time)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) { ListeningTime.entries.forEach { time ->
            OutlinedButton(onClick = { onSelected(time); onDismiss() }, modifier = Modifier.fillMaxWidth().testTag("setting_time_${time.name}")) { RadioButton(selected == time, onClick = null); Spacer(Modifier.width(Space.sm)); Text(stringResource(listeningTimeNameRes(time))) }
        } }
    }, confirmButton = {})
}
