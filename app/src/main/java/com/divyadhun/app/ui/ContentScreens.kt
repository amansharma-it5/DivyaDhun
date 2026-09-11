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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyadhun.app.R
import com.divyadhun.app.content.CategoryViewModel
import com.divyadhun.app.content.DeityDetailViewModel
import com.divyadhun.app.content.HomeUiState
import com.divyadhun.app.content.SearchUiState
import com.divyadhun.app.content.SearchViewModel
import com.divyadhun.app.content.TrackDetailViewModel
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.ui.theme.Space

@Composable
fun HomeScreen(state: HomeUiState, language: Language, onExplore: () -> Unit, onDeity: (String) -> Unit, onTrack: (String) -> Unit) {
    if (!state.loaded) { LoadingState(); return }
    ContentList(title = R.string.namaste, intro = R.string.home_intro, tag = "screen_HOME") {
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.foundation_label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        DevotionalArtwork(Modifier.size(104.dp))
                    }
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.displaySmall)
                    Text(stringResource(R.string.hero_body), style = MaterialTheme.typography.bodyLarge)
                    DivyaButton(stringResource(R.string.explore_action), onExplore)
                }
            }
        }
        item { SectionHeader(stringResource(R.string.your_gods_section)) }
        item {
            val preferred = state.deities.filter { it.id in state.preferredDeityIds }.ifEmpty { state.deities.take(4) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), contentPadding = PaddingValues(end = Space.lg)) {
                items(preferred, key = { it.id }) { deity -> DeityShortcut(deity, onClick = { onDeity(deity.id) }) }
            }
        }
        item { SectionHeader(stringResource(R.string.featured_today)) }
        item { TrackGroup(state.tracks.take(4), language, onTrack) }
        item { SectionHeader(stringResource(R.string.popular_aartis)) }
        item { TrackGroup(state.tracks.filter { it.category == DevotionalCategory.AARTI }.take(4), language, onTrack) }
        item { SectionHeader(stringResource(R.string.mantras_section)) }
        item { TrackGroup(state.tracks.filter { it.category == DevotionalCategory.MANTRA }.take(4), language, onTrack) }
    }
}

@Composable
fun ExploreScreen(state: com.divyadhun.app.content.ExploreUiState, language: Language, onDeity: (String) -> Unit, onCategory: (DevotionalCategory) -> Unit, onTrack: (String) -> Unit) {
    if (!state.loaded) { LoadingState(); return }
    ContentList(R.string.explore, R.string.explore_intro, "screen_EXPLORE") {
        item { SectionHeader(stringResource(R.string.browse_by_type)) }
        itemsIndexed(DevotionalCategory.entries.chunked(2), key = { _, pair -> pair.first().name }) { _, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                pair.forEach { category ->
                    DiscoveryCard(stringResource(categoryNameRes(category)), Modifier.weight(1f)) { onCategory(category) }
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
        item { TrackGroup(state.tracks.take(5), language, onTrack) }
    }
}

@Composable
fun DeityDetailScreen(viewModel: DeityDetailViewModel, deityId: String, language: Language, onBack: () -> Unit, onTrack: (String) -> Unit) {
    LaunchedEffect(deityId) { viewModel.load(deityId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    val deity = state.deity
    ContentList(title = R.string.deity_tracks, intro = R.string.explore_intro, tag = "screen_DEITY_$deityId", leading = { BackButton(onBack) }) {
        if (deity == null) {
            item { EmptyState(stringResource(R.string.unknown_deity), stringResource(R.string.deity_detail_missing)) }
            return@ContentList
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md), horizontalAlignment = Alignment.CenterHorizontally) {
                    DeityArtwork(deity, Modifier.size(132.dp))
                    Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(deityDescriptionRes(deity.id)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text(stringResource(R.string.deity_tracks), style = MaterialTheme.typography.titleLarge) }
        item { TrackGroup(state.tracks, language, onTrack) }
    }
}

@Composable
fun CategoryScreen(viewModel: CategoryViewModel, category: DevotionalCategory, language: Language, onBack: () -> Unit, onTrack: (String) -> Unit) {
    LaunchedEffect(category) { viewModel.load(category) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    ContentList(categoryNameRes(category), R.string.explore_intro, "screen_CATEGORY_${category.name}", leading = { BackButton(onBack) }) {
        item { TrackGroup(state.tracks, language, onTrack) }
        if (state.tracks.isEmpty()) item { EmptyState(stringResource(R.string.no_results_title), stringResource(R.string.no_results_body)) }
    }
}

@Composable
fun TrackDetailScreen(viewModel: TrackDetailViewModel, trackId: String, language: Language, onBack: () -> Unit) {
    LaunchedEffect(trackId) { viewModel.load(trackId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.loaded) { LoadingState(); return }
    ContentList(R.string.track_info, R.string.explore_intro, "screen_TRACK_$trackId", leading = { BackButton(onBack) }) {
        val track = state.track
        if (track == null) item { EmptyState(stringResource(R.string.track_info), stringResource(R.string.no_results_body)) }
        else item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(Space.lg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    TrackArtwork(track, Modifier.size(144.dp))
                    LocalizedText(track.title, language, style = MaterialTheme.typography.headlineSmall)
                    LocalizedText(track.subtitle, language, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(categoryNameRes(track.category)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.playback_deferred), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.source_label) + ": " + if (language == Language.HINDI) track.artistOrSource.hindi else track.artistOrSource.english, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable fun ContentList(title: Int, intro: Int, tag: String, leading: @Composable (() -> Unit)? = null, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
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

@Composable private fun LoadingState() { Column(Modifier.fillMaxWidth().padding(Space.xl), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator() } }
@Composable private fun BackButton(onBack: () -> Unit) { IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back)) } }
@Composable private fun TrackGroup(tracks: List<Track>, language: Language, onTrack: (String) -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) { tracks.forEach { TrackRow(it, language) { onTrack(it.id) } } } }

@Composable private fun DeityShortcut(deity: Deity, onClick: () -> Unit) {
    Column(Modifier.width(94.dp).clickable(onClick = onClick).semantics { role = Role.Button }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        DeityArtwork(deity, Modifier.size(72.dp))
        Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable private fun DiscoveryCard(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier.fillMaxWidth().heightIn(min = 70.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))) { Text(label, Modifier.padding(Space.md), style = MaterialTheme.typography.titleMedium) }
}
@Composable private fun DeityDiscoveryCard(deity: Deity, modifier: Modifier = Modifier, onDeity: (String) -> Unit) {
    Card(modifier.fillMaxWidth().clickable { onDeity(deity.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))) {
        Row(Modifier.fillMaxWidth().padding(Space.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) { DeityArtwork(deity, Modifier.size(50.dp)); Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
fun SearchScreen(viewModel: SearchViewModel, state: SearchUiState, language: Language, onTrack: (String) -> Unit) {
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
        } else if (state.results.isEmpty() && !state.isSearching) {
            item { EmptyState(stringResource(R.string.no_results_title), stringResource(R.string.no_results_body)) }
        } else {
            item { Text(pluralStringResource(R.plurals.search_result_count, state.results.size, state.results.size), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            item { TrackGroup(state.results, language, onTrack) }
        }
        if (state.error) item { Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error) }
    }
}
