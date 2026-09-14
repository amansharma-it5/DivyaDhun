package com.divyadhun.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyadhun.app.R
import com.divyadhun.app.content.DailyBhaktiViewModel
import com.divyadhun.app.domain.model.DailyBhaktiRoutine
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState

@Composable
fun DailyBhaktiScreen(
    viewModel: DailyBhaktiViewModel,
    routine: DailyBhaktiRoutine,
    tracks: List<Track>,
    language: Language,
    playbackState: PlaybackState,
    playbackController: PlaybackController?,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val routineTracks = routine.trackIds.mapNotNull { id -> tracks.firstOrNull { it.id == id } }.filter { it.isPlayable }
    LaunchedEffect(routine.id) { viewModel.reset() }
    val current = routineTracks.getOrNull(state.currentIndex)
    val backDescription = stringResource(R.string.back)
    ContentList(
        title = R.string.daily_bhakti,
        intro = R.string.daily_bhakti_intro,
        tag = "screen_DAILY_BHAKTI",
        leading = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("back_button").semantics { contentDescription = backDescription }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) } }
    ) {
        if (routineTracks.isEmpty()) {
            item { EmptyState(stringResource(R.string.daily_bhakti), stringResource(R.string.no_playable_daily_bhakti)) }
            return@ContentList
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    current?.let { TrackArtwork(it, Modifier.size(132.dp)) }
                    Text(stringResource(R.string.daily_bhakti_for_time, listeningTimeRes(routine)), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.routine_progress, state.currentIndex + 1, routineTracks.size), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("routine_progress"))
                }
            }
        }
        current?.let { track ->
            item { DailyBhaktiCurrentTrack(track, language) }
            item { DailyBhaktiControls(viewModel, routineTracks, state.currentIndex, state.isRunning, track, playbackState, playbackController) }
        }
        item {
            Text(stringResource(R.string.daily_bhakti_queue), style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                routineTracks.forEachIndexed { index, track ->
                    Text(
                        text = "${index + 1}. ${if (language == Language.HINDI) track.title.hindi else track.title.english}",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("routine_track_${track.id}"),
                        style = if (index == state.currentIndex) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        color = if (index == state.currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyBhaktiCurrentTrack(track: Track, language: Language) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (language == Language.HINDI) track.title.hindi else track.title.english, style = MaterialTheme.typography.titleLarge)
            Text(if (language == Language.HINDI) track.subtitle.hindi else track.subtitle.english, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyBhaktiControls(
    viewModel: DailyBhaktiViewModel,
    tracks: List<Track>,
    index: Int,
    running: Boolean,
    track: Track,
    playbackState: PlaybackState,
    playbackController: PlaybackController?
) {
    val currentPlaying = playbackState.currentTrackId == track.id && playbackState.isPlaying
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = {
                if (playbackController != null) {
                    if (currentPlaying) playbackController.pause()
                    else if (playbackState.currentTrackId == track.id) playbackController.resume()
                    else playbackController.playTrack(track, tracks)
                }
                viewModel.setRunning(!running)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("routine_play_pause")
        ) { Text(stringResource(if (running) R.string.pause else R.string.start_daily_bhakti)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.previous(); if (playbackController != null && index > 0) playbackController.playTrack(tracks[index - 1], tracks) }, modifier = Modifier.size(56.dp).testTag("routine_previous")) { Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.previous)) }
            IconButton(onClick = { viewModel.next(tracks.size); if (playbackController != null && index < tracks.lastIndex) playbackController.playTrack(tracks[index + 1], tracks) }, modifier = Modifier.size(56.dp).testTag("routine_next")) { Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.next)) }
        }
        OutlinedButton(onClick = { viewModel.reset(); playbackController?.stop() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("routine_reset")) { Text(stringResource(R.string.mantra_reset)) }
    }
}

private fun listeningTimeRes(routine: DailyBhaktiRoutine) = listeningTimeNameRes(routine.listeningTime)
