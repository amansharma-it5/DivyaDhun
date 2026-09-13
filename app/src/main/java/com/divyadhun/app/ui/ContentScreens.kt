package com.divyadhun.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyadhun.app.R
import com.divyadhun.app.content.CategoryViewModel
import com.divyadhun.app.content.DeityDetailViewModel
import com.divyadhun.app.content.ExploreUiState
import com.divyadhun.app.content.HomeUiState
import com.divyadhun.app.content.SearchUiState
import com.divyadhun.app.content.SearchViewModel
import com.divyadhun.app.content.TrackDetailViewModel
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.ui.theme.LocalDevotionalColors
import com.divyadhun.app.ui.theme.Space
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.playback.playableQueue

@Composable
fun HomeScreen(
    state: HomeUiState,
    language: Language,
    onExplore: () -> Unit,
    onDeity: (String) -> Unit,
    onTrack: (String) -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    if (!state.loaded) { LoadingState(); return }
    val content = state.content
    ContentList(title = R.string.namaste, intro = R.string.home_intro, tag = "screen_HOME") {
        item { HomeHeroCard(onExplore) }
        item { SectionHeader(stringResource(R.string.your_gods_section)) }
        item {
            if (content.yourGods.isEmpty()) EmptyState(stringResource(R.string.your_gods_section), stringResource(R.string.no_preference_deities))
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), contentPadding = PaddingValues(end = Space.lg)) {
                items(content.yourGods, key = { it.id }) { deity -> DeityShortcut(deity) { onDeity(deity.id) } }
            }
        }
        item { SectionHeader(stringResource(R.string.recommended_for_you)) }
        item { TrackGroup(content.recommended, language, playbackState, playbackController, onTrack) }
        item { SectionHeader(stringResource(R.string.featured_today)) }
        item { TrackGroup(content.today, language, playbackState, playbackController, onTrack) }
        item { SectionHeader(stringResource(R.string.popular_aartis)) }
        item { TrackGroup(content.popularAartis, language, playbackState, playbackController, onTrack) }
        item { SectionHeader(stringResource(R.string.mantras_section)) }
        item { TrackGroup(content.mantras, language, playbackState, playbackController, onTrack) }
    }
}

@Composable
private fun HomeHeroCard(onExplore: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("home_hero"),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.foundation_label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                DevotionalArtwork(Modifier.size(104.dp))
            }
            Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.hero_body), style = MaterialTheme.typography.bodyLarge)
            DivyaButton(stringResource(R.string.explore_action), onExplore, Modifier.testTag("home_explore_cta"))
        }
    }
}

@Composable
fun ExploreScreen(
    state: ExploreUiState,
    language: Language,
    onDeity: (String) -> Unit,
    onCategory: (DevotionalCategory) -> Unit,
    onTrack: (String) -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    if (!state.loaded) { LoadingState(); return }
    ContentList(R.string.explore, R.string.explore_intro, "screen_EXPLORE") {
        item { SectionHeader(stringResource(R.string.browse_by_type)) }
        itemsIndexed(DevotionalCategory.entries.chunked(2), key = { _, pair -> pair.first().name }) { _, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                pair.forEach { category ->
                    CategoryCard(category, state.categoryCounts[category] ?: 0, Modifier.weight(1f)) { onCategory(category) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { SectionHeader(stringResource(R.string.browse_by_deity)) }
        itemsIndexed(state.deities.chunked(2), key = { _, pair -> pair.first().id }) { _, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                pair.forEach { deity -> DeityDiscoveryCard(deity, Modifier.weight(1f), onDeity) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { SectionHeader(stringResource(R.string.featured_today)) }
        item { TrackGroup(state.tracks.take(5), language, playbackState, playbackController, onTrack) }
    }
}

@Composable
fun DeityDetailScreen(
    viewModel: DeityDetailViewModel,
    deityId: String,
    language: Language,
    onBack: () -> Unit,
    onTrack: (String) -> Unit,
    onCategory: (DevotionalCategory) -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    LaunchedEffect(deityId) { viewModel.load(deityId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    val deity = state.deity
    ContentList(
        title = if (deity == null) R.string.unknown_deity else deityNameRes(deity.id),
        intro = if (deity == null) R.string.deity_detail_missing else deityDescriptionRes(deity.id),
        tag = "screen_DEITY_$deityId",
        leading = { BackButton(onBack) }
    ) {
        if (deity == null) {
            item { EmptyState(stringResource(R.string.unknown_deity), stringResource(R.string.deity_detail_missing)) }
            return@ContentList
        }
        item { DeityHero(deity, state.tracks) }
        if (state.availableCategories.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Text(stringResource(R.string.explore_by_type), style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        items(state.availableCategories, key = { it.name }) { category ->
                            FilterChip(false, { onCategory(category) }, label = { Text(stringResource(categoryNameRes(category))) }, modifier = Modifier.testTag("deity_category_${category.name}"))
                        }
                    }
                }
            }
        }
        state.sections.forEach { section ->
            item { SectionHeader(stringResource(deitySectionNameRes(section.type))) }
            item { TrackGroup(section.tracks, language, playbackState, playbackController, onTrack) }
        }
    }
}

@Composable
private fun DeityHero(deity: Deity, tracks: List<Track>) {
    val accent = deityVisualStyle(deity.id).accent
    Surface(color = accent.copy(alpha = .16f), shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md), horizontalAlignment = Alignment.CenterHorizontally) {
            DeityArtwork(deity, Modifier.size(144.dp))
            Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(deityInvocationRes(deity.id)), style = MaterialTheme.typography.titleLarge, color = accent)
            Text(stringResource(deityDescriptionRes(deity.id)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(pluralStringResource(R.plurals.track_count, tracks.size, tracks.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Reusable detail destination for every devotional category. */
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryViewModel,
    category: DevotionalCategory,
    language: Language,
    onBack: () -> Unit,
    onTrack: (String) -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    LaunchedEffect(category) { viewModel.load(category) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    ContentList(categoryNameRes(category), categoryDescriptionRes(category), "screen_CATEGORY_${category.name}", leading = { BackButton(onBack) }) {
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md), horizontalAlignment = Alignment.CenterHorizontally) {
                    DevotionalArtwork(Modifier.size(112.dp), LocalDevotionalColors.current.gold)
                    Text(stringResource(categoryNameRes(category)), style = MaterialTheme.typography.headlineSmall)
                    Text(pluralStringResource(R.plurals.track_count, state.tracks.size, state.tracks.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (state.tracks.isEmpty()) item { EmptyState(stringResource(R.string.empty_category_title), stringResource(R.string.empty_category_body)) }
        else item { TrackGroup(state.tracks, language, playbackState, playbackController, onTrack) }
    }
}

@Composable
fun CategoryScreen(viewModel: CategoryViewModel, category: DevotionalCategory, language: Language, onBack: () -> Unit, onTrack: (String) -> Unit) =
    CategoryDetailScreen(viewModel, category, language, onBack, onTrack)

@Composable
fun TrackDetailScreen(
    viewModel: TrackDetailViewModel,
    trackId: String,
    language: Language,
    onBack: () -> Unit,
    onDeity: (String) -> Unit,
    onCategory: (DevotionalCategory) -> Unit,
    playbackState: PlaybackState = PlaybackState(),
    playbackController: PlaybackController? = null
) {
    LaunchedEffect(trackId) { viewModel.load(trackId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    ContentList(R.string.track_info, R.string.track_detail_intro, "screen_TRACK_$trackId", leading = { BackButton(onBack) }) {
        val track = state.track
        if (track == null) {
            item { EmptyState(stringResource(R.string.track_info), stringResource(R.string.track_not_found)) }
        } else {
            item {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(Space.lg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.md)) {
                        TrackArtwork(track, Modifier.size(144.dp))
                        LocalizedText(track.title, language, style = MaterialTheme.typography.headlineSmall)
                        LocalizedText(track.subtitle, language, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        MetadataChip(stringResource(categoryNameRes(track.category)))
                        MetadataChip(stringResource(R.string.deity_label) + ": " + stringResource(deityNameRes(track.deityId)))
                        MetadataChip(stringResource(R.string.source_label) + ": " + localized(track.artistOrSource, language))
                        MetadataChip(stringResource(R.string.language_label) + ": " + stringResource(if (track.language == Language.HINDI) R.string.language_hindi else R.string.language_english))
                        track.durationMs?.let { duration -> MetadataChip(stringResource(R.string.duration_label) + ": " + formatDuration(duration)) }
                        MetadataChip(stringResource(if (track.lyricsId == null) R.string.lyrics_unavailable else R.string.lyrics_available))
                        if (track.isPlayable && playbackController != null) {
                            val current = playbackState.currentTrackId == track.id
                            val playing = current && playbackState.isPlaying
                            androidx.compose.material3.Button(
                                onClick = { if (playing) playbackController.pause() else if (current) playbackController.resume() else playbackController.playTrack(track) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("track_play_action")
                            ) { Text(stringResource(if (playing) R.string.pause else if (current) R.string.resume else R.string.play)) }
                            Text(stringResource(R.string.demo_audio), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(stringResource(R.string.demo_audio_unavailable), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { onDeity(track.deityId) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("track_view_deity")) { Text(stringResource(R.string.view_deity)) }
                        OutlinedButton(onClick = { onCategory(track.category) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("track_view_category")) { Text(stringResource(R.string.view_category)) }
                    }
                }
            }
        }
    }
}

private fun localized(text: com.divyadhun.app.domain.model.LocalizedText, language: Language) = if (language == Language.HINDI) text.hindi else text.english
private fun formatDuration(durationMs: Long): String = "%d:%02d".format(durationMs / 60_000, (durationMs / 1_000) % 60)

@Composable
fun NotFoundScreen(onBack: () -> Unit) {
    ContentList(R.string.not_found_title, R.string.not_found_body, "screen_NOT_FOUND", leading = { BackButton(onBack) }) {
        item { EmptyState(stringResource(R.string.not_found_title), stringResource(R.string.not_found_body)) }
    }
}

@Composable
fun ContentList(title: Int, intro: Int, tag: String, leading: @Composable (() -> Unit)? = null, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxWidth().testTag(tag), contentPadding = PaddingValues(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    leading?.invoke()
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.brand_hindi), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(title), style = MaterialTheme.typography.headlineLarge)
                    }
                }
                Text(stringResource(intro), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
private fun CategoryCard(category: DevotionalCategory, count: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth().heightIn(min = 88.dp).clickable(onClick = onClick).semantics { role = Role.Button }.testTag("category_${category.name}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))) {
        Column(Modifier.fillMaxWidth().padding(Space.md), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Text(stringResource(categoryNameRes(category)), style = MaterialTheme.typography.titleMedium)
            Text(pluralStringResource(R.plurals.track_count, count, count), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun LoadingState() { Column(Modifier.fillMaxWidth().padding(Space.xl), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator() } }
@Composable private fun BackButton(onBack: () -> Unit) { IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("back_button")) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back)) } }
@Composable private fun TrackGroup(tracks: List<Track>, language: Language, playbackState: PlaybackState, playbackController: PlaybackController?, onTrack: (String) -> Unit) {
    val queue = playableQueue(tracks)
    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        tracks.forEach { track ->
            TrackRow(track, language, playbackState, playbackController, queue, onClick = { onTrack(track.id) })
        }
    }
}

@Composable private fun DeityShortcut(deity: Deity, onClick: () -> Unit) {
    Column(Modifier.width(94.dp).heightIn(min = 112.dp).clickable(onClick = onClick).semantics { role = Role.Button }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        DeityArtwork(deity, Modifier.size(72.dp))
        Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable private fun DeityDiscoveryCard(deity: Deity, modifier: Modifier = Modifier, onDeity: (String) -> Unit) {
    Card(modifier = modifier.fillMaxWidth().heightIn(min = 72.dp).clickable { onDeity(deity.id) }.semantics { role = Role.Button }.testTag("deity_${deity.id}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))) {
        Row(Modifier.fillMaxWidth().padding(Space.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) { DeityArtwork(deity, Modifier.size(50.dp)); Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
fun SearchScreen(viewModel: SearchViewModel, state: SearchUiState, language: Language, onTrack: (String) -> Unit, onDeity: (String) -> Unit, playbackState: PlaybackState = PlaybackState(), playbackController: PlaybackController? = null) {
    ContentList(R.string.search, R.string.search_intro, "screen_SEARCH") {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().testTag("search_field"),
                singleLine = true,
                label = { Text(stringResource(R.string.search)) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                trailingIcon = if (state.query.isNotEmpty()) ({ IconButton(onClick = viewModel::clearQuery, modifier = Modifier.testTag("clear_search")) { Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear_search)) } }) else null
            )
        }
        if (state.query.isBlank()) {
            item { EmptyState(stringResource(R.string.search_title), stringResource(R.string.search_body)) }
        } else {
            if (state.deityResults.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                        Text(stringResource(R.string.deities_found), style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            items(state.deityResults, key = { it.id }) { deity ->
                                FilterChip(false, { onDeity(deity.id) }, label = { Text(stringResource(deityNameRes(deity.id))) }, modifier = Modifier.testTag("search_deity_${deity.id}"))
                            }
                        }
                    }
                }
            }
            if (state.results.isEmpty() && !state.isSearching) {
                item { EmptyState(stringResource(R.string.no_results_title), stringResource(R.string.no_results_body)) }
            } else {
                item { Text(pluralStringResource(R.plurals.search_result_count, state.results.size, state.results.size), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                item { TrackGroup(state.results, language, playbackState, playbackController, onTrack) }
            }
        }
        if (state.error) item { Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error) }
    }
}
