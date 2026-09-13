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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
