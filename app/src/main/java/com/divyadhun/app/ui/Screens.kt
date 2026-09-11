package com.divyadhun.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import com.divyadhun.app.*
import com.divyadhun.app.R
import com.divyadhun.app.ui.theme.*
@Composable private fun Screen(title: Int, intro: Int, tag: String, content: @Composable () -> Unit) {
 LazyColumn(Modifier.fillMaxSize().testTag(tag), contentPadding = PaddingValues(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
  item { Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
   Text(stringResource(R.string.brand_hindi), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
   Text(stringResource(title), style = MaterialTheme.typography.headlineLarge)
   Text(stringResource(intro), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
  } }
  item { content() }
 }
}
@Composable fun HomeScreen(onExplore: () -> Unit) {
 Screen(R.string.namaste, R.string.home_intro, "screen_HOME") {
  Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
   Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
    Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text(stringResource(R.string.foundation_label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
      DevotionalArtwork()
     }
     Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.displaySmall)
     Text(stringResource(R.string.hero_body), style = MaterialTheme.typography.bodyLarge)
     DivyaButton(stringResource(R.string.explore_action), onExplore)
    }
   }
   EmptyState(stringResource(R.string.welcome_title), stringResource(R.string.welcome_body))
   Text(stringResource(R.string.secondary_tagline), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
@Composable fun ExploreScreen() {
 Screen(R.string.explore, R.string.explore_intro, "screen_EXPLORE") {
  Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
   DevotionalArtwork(Modifier.align(Alignment.CenterHorizontally).size(160.dp))
   EmptyState(stringResource(R.string.collection_title), stringResource(R.string.collection_body))
   Text(stringResource(R.string.coming_soon), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
@Composable fun SearchScreen() {
 Screen(R.string.search, R.string.search_intro, "screen_SEARCH") {
  EmptyState(stringResource(R.string.search_title), stringResource(R.string.search_body))
 }
}
@Composable fun LibraryScreen(state: ShellUiState, onTheme: (ThemeMode) -> Unit) {
 Screen(R.string.library, R.string.library_intro, "screen_LIBRARY") {
  Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
   EmptyState(stringResource(R.string.favorites_title), stringResource(R.string.favorites_body))
   SectionHeader(stringResource(R.string.appearance))
   ThemeMode.entries.forEach { mode ->
    val label = when(mode) { ThemeMode.SYSTEM -> R.string.system; ThemeMode.LIGHT -> R.string.light; ThemeMode.DARK -> R.string.dark }
    OutlinedButton(onClick = { onTheme(mode) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("theme_" + mode.name).semantics { selected = state.theme == mode }) {
     RadioButton(selected = state.theme == mode, onClick = null)
     Spacer(Modifier.width(Space.sm))
     Text(stringResource(label))
    }
   }
   if (state.error) Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error)
  }
 }
}
