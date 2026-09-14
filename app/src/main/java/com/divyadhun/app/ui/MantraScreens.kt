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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.divyadhun.app.content.MantraViewModel
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.MantraSessionStatus
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.domain.model.Track

@Composable
fun MantraScreen(
    viewModel: MantraViewModel,
    trackId: String,
    language: Language,
    playbackState: PlaybackState,
    playbackController: PlaybackController?,
    onBack: () -> Unit
) {
    LaunchedEffect(trackId) { viewModel.load(trackId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) {
        LoadingState()
        return
    }
    val track = state.track
    val backDescription = stringResource(R.string.back)
    ContentList(
        title = R.string.mantra_mode,
        intro = R.string.mantra_mode_intro,
        tag = "screen_MANTRA_$trackId",
        leading = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("back_button").semantics { contentDescription = backDescription }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) } }
    ) {
        if (track == null) {
            item { EmptyState(stringResource(R.string.mantra_mode), stringResource(R.string.track_not_found)) }
            return@ContentList
        }
        item { MantraTrackHeader(track, language) }
        item { MantraTargetPicker(state.progress.targetCount, viewModel::selectTarget) }
        item { MantraProgressCard(state.progress.completedCount, state.progress.targetCount, state.progress.status, viewModel::toggleRunning, viewModel::increment, viewModel::reset) }
        if (track.isPlayable && playbackController != null) item { MantraPlaybackControls(track, playbackState, playbackController) }
        else item { Text(stringResource(R.string.mantra_metadata_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        state.lyrics?.let { item { Text(stringResource(R.string.mantra_text_available), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun MantraTrackHeader(track: Track, language: Language) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TrackArtwork(track, Modifier.size(112.dp))
            LocalizedText(track.title, language, style = MaterialTheme.typography.headlineSmall)
            Text(if (language == Language.HINDI) track.artistOrSource.hindi else track.artistOrSource.english, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MantraTargetPicker(selected: Int, onSelected: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.mantra_choose_count), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(11, 21, 51, 108).forEach { target ->
                FilterChip(
                    selected = selected == target,
                    onClick = { onSelected(target) },
                    label = { Text(target.toString()) },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("mantra_target_$target")
                )
            }
        }
    }
}

@Composable
private fun MantraProgressCard(
    completed: Int,
    target: Int,
    status: MantraSessionStatus,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    val progress = (completed.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val actionLabel = when (status) {
        MantraSessionStatus.RUNNING -> R.string.mantra_pause
        MantraSessionStatus.PAUSED -> R.string.mantra_resume
        MantraSessionStatus.IDLE -> R.string.mantra_start
        MantraSessionStatus.COMPLETE -> R.string.mantra_complete
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f), shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.mantra_progress, completed, target), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("mantra_progress"))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().testTag("mantra_progress_bar"))
            Button(onClick = onToggle, enabled = status != MantraSessionStatus.COMPLETE, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("mantra_toggle_running")) { Text(stringResource(actionLabel)) }
            Button(onClick = onIncrement, enabled = status == MantraSessionStatus.RUNNING, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("mantra_count_repetition")) { Text(stringResource(R.string.mantra_count_repetition)) }
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("mantra_reset")) { Text(stringResource(R.string.mantra_reset)) }
            if (status == MantraSessionStatus.COMPLETE) Text(stringResource(R.string.mantra_complete_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("mantra_complete"))
        }
    }
}

@Composable
private fun MantraPlaybackControls(track: Track, state: PlaybackState, controller: PlaybackController) {
    val current = state.currentTrackId == track.id
    val playing = current && state.isPlaying
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = controller::skipPrevious, modifier = Modifier.size(56.dp).testTag("mantra_previous")) { Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.previous)) }
        IconButton(onClick = { if (playing) controller.pause() else if (current) controller.resume() else controller.playTrack(track) }, modifier = Modifier.size(64.dp).testTag("mantra_play_pause")) { Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = stringResource(if (playing) R.string.pause else R.string.resume)) }
        IconButton(onClick = controller::skipNext, modifier = Modifier.size(56.dp).testTag("mantra_next")) { Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.next)) }
    }
}
