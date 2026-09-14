package com.divyadhun.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyadhun.app.R
import com.divyadhun.app.content.LyricsViewModel
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState

@Composable
fun LyricsScreen(
    viewModel: LyricsViewModel,
    trackId: String,
    language: Language,
    playbackState: PlaybackState,
    playbackController: PlaybackController?,
    onBack: () -> Unit
) {
    LaunchedEffect(trackId) { viewModel.load(trackId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) {
        Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }
    val track = state.track
    val lyrics = state.lyrics
    val title = track?.let { if (language == Language.HINDI) it.title.hindi else it.title.english }
    val aartiMode = track?.category == DevotionalCategory.AARTI
    val backDescription = stringResource(R.string.back)
    ContentList(
        title = if (aartiMode) R.string.aarti_mode else R.string.lyrics_title,
        intro = if (aartiMode) R.string.aarti_mode_intro else R.string.lyrics_intro,
        tag = "screen_LYRICS_$trackId",
        leading = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("back_button").semantics { contentDescription = backDescription }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) } }
    ) {
        if (track == null || lyrics == null) {
            item { EmptyState(stringResource(R.string.lyrics_title), stringResource(R.string.lyrics_not_found)) }
            return@ContentList
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackArtwork(track, Modifier.size(112.dp).align(Alignment.CenterHorizontally))
                    Text(title.orEmpty(), style = MaterialTheme.typography.headlineSmall)
                    Text(localizedSource(track, language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (playbackController != null && track.isPlayable) {
            item { LyricsPlaybackControls(track, playbackState, playbackController) }
        }
        item { LyricsText(lyrics.devanagari, R.string.devanagari_label, MaterialTheme.typography.headlineSmall, "lyrics_devanagari") }
        item { LyricsText(lyrics.romanized, R.string.romanized_label, MaterialTheme.typography.bodyLarge, "lyrics_romanized") }
        item { LyricsText(lyrics.meaning, R.string.meaning_label, MaterialTheme.typography.bodyLarge, "lyrics_meaning") }
        item { KeepAwakeControl() }
        item { Text(stringResource(R.string.traditional_text_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun LyricsText(text: String, label: Int, style: androidx.compose.ui.text.TextStyle, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Text(stringResource(label), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f), shape = MaterialTheme.shapes.medium) {
            Text(text, Modifier.fillMaxWidth().padding(16.dp), style = style)
        }
    }
}

@Composable
private fun LyricsPlaybackControls(track: Track, state: PlaybackState, controller: PlaybackController) {
    val current = state.currentTrackId == track.id
    val playing = current && state.isPlaying
    val action = stringResource(if (playing) R.string.pause else R.string.resume)
    val previous = stringResource(R.string.previous)
    val next = stringResource(R.string.next)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = controller::skipPrevious, modifier = Modifier.size(56.dp).semantics { contentDescription = previous }.testTag("lyrics_previous")) { Icon(Icons.Filled.SkipPrevious, contentDescription = null) }
        IconButton(
            onClick = { if (playing) controller.pause() else if (current) controller.resume() else controller.playTrack(track) },
            modifier = Modifier.size(64.dp).semantics { contentDescription = action }.testTag("lyrics_play_pause")
        ) { Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null) }
        IconButton(onClick = controller::skipNext, modifier = Modifier.size(56.dp).semantics { contentDescription = next }.testTag("lyrics_next")) { Icon(Icons.Filled.SkipNext, contentDescription = null) }
    }
}

@Composable
private fun KeepAwakeControl() {
    val view = LocalView.current
    var keepAwake by remember { mutableStateOf(false) }
    DisposableEffect(view, keepAwake) {
        view.keepScreenOn = keepAwake
        onDispose { view.keepScreenOn = false }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.keep_screen_awake), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.keep_screen_awake_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = keepAwake, onCheckedChange = { keepAwake = it }, modifier = Modifier.testTag("keep_screen_awake"))
    }
}

private fun localizedSource(track: Track, language: Language) = if (language == Language.HINDI) track.artistOrSource.hindi else track.artistOrSource.english
