package com.divyadhun.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.playback.PlaybackStatus
import com.divyadhun.app.playback.RepeatMode

@Composable
fun MiniPlayer(
    track: Track,
    language: Language,
    state: PlaybackState,
    controller: PlaybackController,
    onOpen: () -> Unit
) {
    val title = if (language == Language.HINDI) track.title.hindi else track.title.english
    val progress = if (state.durationMs > 0L) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
    val playPauseDescription = stringResource(if (state.isPlaying) R.string.pause else R.string.resume)
    val nextDescription = stringResource(R.string.next)
    val stopDescription = stringResource(R.string.stop)
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("mini_player"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrackArtwork(track, Modifier.size(42.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (state.status == PlaybackStatus.ERROR) R.string.playback_error else R.string.now_playing),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.status == PlaybackStatus.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                }
                IconButton(
                    onClick = { if (state.isPlaying) controller.pause() else controller.resume() },
                    modifier = Modifier.size(48.dp).semantics { contentDescription = playPauseDescription }.testTag("mini_player_play_pause")
                ) { Icon(if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null) }
                IconButton(
                    onClick = controller::skipNext,
                    modifier = Modifier.size(48.dp).semantics { contentDescription = nextDescription }.testTag("mini_player_next")
                ) { Icon(Icons.Filled.SkipNext, contentDescription = null) }
                IconButton(
                    onClick = controller::stop,
                    modifier = Modifier.size(48.dp).semantics { contentDescription = stopDescription }.testTag("mini_player_stop")
                ) { Icon(Icons.Outlined.Close, contentDescription = null) }
            }
        }
    }
}

@Composable
fun FullPlayerScreen(
    track: Track?,
    queueTracks: List<Track>,
    language: Language,
    state: PlaybackState,
    controller: PlaybackController,
    onBack: () -> Unit,
    onSelectTrack: (Track) -> Unit
) {
    val title = track?.let { if (language == Language.HINDI) it.title.hindi else it.title.english }
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(state.positionMs.toFloat()) }
    val backDescription = stringResource(R.string.back)
    val seekDescription = stringResource(R.string.seek_position)
    val previousDescription = stringResource(R.string.previous)
    val nextDescription = stringResource(R.string.next)
    val playPauseDescription = stringResource(if (state.isPlaying) R.string.pause else R.string.resume)
    val repeatLabel = repeatDescription(state.repeatMode)
    LaunchedEffect(state.positionMs, isDragging) {
        if (!isDragging) sliderPosition = state.positionMs.toFloat()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("screen_PLAYER"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp).semantics { contentDescription = backDescription }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.brand_hindi), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.full_player_title), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        if (track == null) {
            item { EmptyState(stringResource(R.string.full_player_title), stringResource(R.string.player_no_selection)) }
            return@LazyColumn
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrackArtwork(track, Modifier.size(220.dp).testTag("player_artwork"))
                    Text(title.orEmpty(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("player_title"))
                    Text(localizedSource(track, language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.status == PlaybackStatus.ERROR) {
                        Text(stringResource(R.string.playback_error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("player_error"))
                    }
                    val duration = state.durationMs.coerceAtLeast(0L)
                    Slider(
                        value = sliderPosition.coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                        onValueChange = { value -> isDragging = true; sliderPosition = value },
                        onValueChangeFinished = { isDragging = false; controller.seekTo(sliderPosition.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = seekDescription }.testTag("player_seek")
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatPlaybackTime(sliderPosition.toLong()))
                        Text(formatPlaybackTime(duration))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = controller::skipPrevious, modifier = Modifier.size(56.dp).semantics { contentDescription = previousDescription }.testTag("player_previous")) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = null)
                        }
                        IconButton(
                            onClick = { if (state.isPlaying) controller.pause() else controller.resume() },
                            modifier = Modifier.size(72.dp).semantics { contentDescription = playPauseDescription }.testTag("player_play_pause")
                        ) { Icon(if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null) }
                        IconButton(onClick = controller::skipNext, modifier = Modifier.size(56.dp).semantics { contentDescription = nextDescription }.testTag("player_next")) {
                            Icon(Icons.Filled.SkipNext, contentDescription = null)
                        }
                    }
                    IconButton(onClick = controller::toggleRepeatMode, modifier = Modifier.size(52.dp).semantics { contentDescription = repeatLabel }.testTag("player_repeat")) {
                        Icon(if (state.repeatMode == RepeatMode.ONE) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat, contentDescription = null)
                    }
                }
            }
        }
        item { Text(stringResource(R.string.queue), style = MaterialTheme.typography.titleLarge) }
        if (queueTracks.isEmpty()) item { Text(stringResource(R.string.queue_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else items(queueTracks, key = { it.id }) { queueTrack ->
            QueueTrackRow(queueTrack, language, queueTrack.id == state.currentTrackId, onClick = { onSelectTrack(queueTrack) })
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QueueTrackRow(track: Track, language: Language, current: Boolean, onClick: () -> Unit) {
    val title = if (language == Language.HINDI) track.title.hindi else track.title.english
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("queue_${track.id}"),
        color = if (current) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TrackArtwork(track, Modifier.size(48.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(if (current) stringResource(R.string.now_playing) else localizedSource(track, language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun repeatDescription(mode: RepeatMode): String = stringResource(when (mode) {
    RepeatMode.OFF -> R.string.repeat_off
    RepeatMode.ALL -> R.string.repeat_all
    RepeatMode.ONE -> R.string.repeat_one
})

private fun localizedSource(track: Track, language: Language) = if (language == Language.HINDI) track.artistOrSource.hindi else track.artistOrSource.english

private fun formatPlaybackTime(positionMs: Long): String = "%d:%02d".format(positionMs / 60_000, (positionMs / 1_000) % 60)
