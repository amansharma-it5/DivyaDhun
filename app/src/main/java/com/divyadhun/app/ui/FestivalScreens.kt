package com.divyadhun.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.Festival
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState

@Composable
fun FestivalDetailScreen(
    festival: Festival?,
    tracks: List<Track>,
    language: Language,
    playbackState: PlaybackState,
    playbackController: PlaybackController?,
    onBack: () -> Unit,
    onTrack: (String) -> Unit
) {
    val backDescription = stringResource(R.string.back)
    ContentList(
        title = festival?.let { festivalNameRes(it.id) } ?: R.string.festival_unknown,
        intro = festival?.let { festivalDescriptionRes(it.id) } ?: R.string.festival_unknown_description,
        tag = "screen_FESTIVAL_${festival?.id.orEmpty()}",
        leading = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("back_button").semantics { contentDescription = backDescription }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) } }
    ) {
        if (festival == null) {
            item { EmptyState(stringResource(R.string.festival_unknown), stringResource(R.string.festival_unknown_description)) }
            return@ContentList
        }
        val festivalTracks = tracks.filter { it.deityId in festival.associatedDeityIds }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large, modifier = Modifier.testTag("festival_collection")) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(festivalNameRes(festival.id)), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(festivalDescriptionRes(festival.id)), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.festival_local_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (festivalTracks.isEmpty()) item { EmptyState(stringResource(R.string.festival_collection), stringResource(R.string.no_festival_tracks)) }
        else item { TrackGroup(festivalTracks, language, playbackState, playbackController, onTrack) }
    }
}
