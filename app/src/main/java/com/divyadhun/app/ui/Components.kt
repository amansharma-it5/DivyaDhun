package com.divyadhun.app.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.LocalizedText
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.ui.theme.*
@Composable fun DevotionalArtwork(modifier: Modifier, tint: Color = LocalDevotionalColors.current.gold) {
 Canvas(modifier) {
  val gold = tint
  val w = size.width; val h = size.height
  drawCircle(gold.copy(alpha = 0.16f), w * .47f, style = Stroke(1.dp.toPx()))
  val petal = Path().apply { moveTo(w*.5f,h*.16f); cubicTo(w*.18f,h*.47f,w*.38f,h*.63f,w*.5f,h*.7f); cubicTo(w*.62f,h*.63f,w*.82f,h*.47f,w*.5f,h*.16f) }
  drawPath(petal,gold,style=Stroke(2.dp.toPx()))
  drawArc(gold,0f,180f,false,topLeft=Offset(w*.2f,h*.47f),size=androidx.compose.ui.geometry.Size(w*.6f,h*.38f),style=Stroke(2.dp.toPx()))
 }
}
@Composable fun TrackArtwork(track: Track, modifier: Modifier) {
 DevotionalArtwork(modifier, deityVisualStyle(track.deityId).accent)
}
@Composable fun DeityArtwork(deity: Deity, modifier: Modifier) {
 DevotionalArtwork(modifier, deityVisualStyle(deity.id).accent)
}
@Composable fun SectionHeader(title: String) { Text(title, style = MaterialTheme.typography.titleLarge) }
@Composable fun MetadataChip(label: String, modifier: Modifier = Modifier) {
 Surface(modifier = modifier, color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
  Text(label, Modifier.padding(horizontal = Space.sm, vertical = Space.xs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
 }
}
@Composable fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
 Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = MaterialTheme.shapes.large) {
  Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
   Text(title, style = MaterialTheme.typography.titleLarge)
   Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
@Composable fun DivyaButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
 Button(onClick, modifier = modifier.fillMaxWidth().heightIn(min = 52.dp), contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) { Text(label) }
}
@Composable fun LocalizedText(text: LocalizedText, language: Language, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.bodyLarge, color: Color = Color.Unspecified) = Text(if (language == Language.HINDI) text.hindi else text.english, modifier, style = style, color = color)
@Composable fun TrackRow(
 track: Track,
 language: Language,
 playbackState: PlaybackState = PlaybackState(),
 playbackController: PlaybackController? = null,
 playbackQueue: List<Track> = listOf(track),
 onClick: () -> Unit,
 isFavorite: Boolean = false,
 onFavorite: (() -> Unit)? = null
) {
 val trackInfoDescription = stringResource(R.string.open_track_info)
 val current = playbackState.currentTrackId == track.id
 val title = if (language == Language.HINDI) track.title.hindi else track.title.english
 val actionDescription = stringResource(if (current && playbackState.isPlaying) R.string.pause_track else R.string.play_track, title)
 val favoriteDescription = stringResource(if (isFavorite) R.string.unfavorite_track else R.string.favorite_track, title)
 Card(
  modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp).clickable(onClick = onClick).semantics { contentDescription = trackInfoDescription; role = Role.Button }.testTag("track_${track.id}"),
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))
 ) {
  Row(Modifier.fillMaxWidth().padding(Space.md), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
   TrackArtwork(track, Modifier.size(56.dp))
   Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
    LocalizedText(
     track.title,
     language,
     modifier = Modifier.clickable(onClick = onClick).semantics { role = Role.Button }.testTag("track_title_${track.id}"),
     style = MaterialTheme.typography.titleMedium
    )
    LocalizedText(track.subtitle, language, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    if (current) Text(stringResource(if (playbackState.isPlaying) R.string.now_playing else R.string.pause), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
   }
   if (track.isPlayable && playbackController != null) {
    IconButton(
     onClick = { if (current && playbackState.isPlaying) playbackController.pause() else if (current) playbackController.resume() else playbackController.playTrack(track, playbackQueue) },
     modifier = Modifier.size(48.dp).semantics { contentDescription = actionDescription }.testTag("play_${track.id}")
    ) { Icon(if (current && playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null) }
   } else if (!track.isPlayable) {
    Text(stringResource(R.string.demo_audio_unavailable), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
   }
   if (onFavorite != null) {
    IconButton(
     onClick = onFavorite,
     modifier = Modifier.size(48.dp).semantics { contentDescription = favoriteDescription }.testTag("favorite_${track.id}")
    ) { Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
   }
   MetadataChip(stringResource(categoryNameRes(track.category)))
  }
 }
}

@Composable
fun FavoriteToggle(
 trackTitle: String,
 isFavorite: Boolean,
 onClick: () -> Unit,
 modifier: Modifier = Modifier
) {
 val favoriteDescription = stringResource(if (isFavorite) R.string.unfavorite_track else R.string.favorite_track, trackTitle)
 IconButton(
  onClick = onClick,
  modifier = modifier.size(52.dp).semantics { contentDescription = favoriteDescription }
 ) {
  Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
 }
}
