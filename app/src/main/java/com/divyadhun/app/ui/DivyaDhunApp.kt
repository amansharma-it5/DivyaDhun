package com.divyadhun.app.ui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.divyadhun.app.LocaleController
import com.divyadhun.app.R
import com.divyadhun.app.ShellUiState
import com.divyadhun.app.ShellViewModel
import com.divyadhun.app.ThemeMode
import com.divyadhun.app.content.CategoryViewModel
import com.divyadhun.app.content.ExploreViewModel
import com.divyadhun.app.content.HomeViewModel
import com.divyadhun.app.content.PreferencesViewModel
import com.divyadhun.app.content.SearchViewModel
import com.divyadhun.app.content.TrackDetailViewModel
import com.divyadhun.app.content.DeityDetailViewModel
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.onboarding.OnboardingFlow
import com.divyadhun.app.onboarding.OnboardingViewModel
import com.divyadhun.app.ui.theme.DivyaTheme
import com.divyadhun.app.ui.theme.usesDark
import com.divyadhun.app.playback.Media3PlaybackController

enum class Destination {
    HOME, EXPLORE, SEARCH, LIBRARY;
    val label: Int get() = when (this) {
        HOME -> R.string.home; EXPLORE -> R.string.explore; SEARCH -> R.string.search; LIBRARY -> R.string.library
    }
    val icon get() = when (this) {
        HOME -> Icons.Outlined.Home; EXPLORE -> Icons.AutoMirrored.Outlined.List
        SEARCH -> Icons.Outlined.Search; LIBRARY -> Icons.Outlined.FavoriteBorder
    }
}

private const val DEITY_ROUTE = "deity/{deityId}"
private const val CATEGORY_ROUTE = "category/{categoryName}"
private const val TRACK_ROUTE = "track/{trackId}"

@Composable
fun DivyaDhunApp(model: ShellViewModel) {
    val shellState by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dark = shellState.theme.usesDark(isSystemInDarkTheme())
    SideEffect {
        val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        (context as? ComponentActivity)?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
    DivyaTheme(shellState.theme) {
        if (!shellState.loaded) {
            Surface(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else {
            val exploreViewModel: ExploreViewModel = viewModel()
            val exploreState by exploreViewModel.state.collectAsStateWithLifecycle()
            if (!shellState.preferences.onboardingComplete) {
                if (!exploreState.loaded) Surface(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                else {
                    val onboardingViewModel: OnboardingViewModel = viewModel()
                    OnboardingFlow(onboardingViewModel, exploreState.deities)
                }
            } else {
                LaunchedEffect(shellState.preferences.language) { LocaleController.apply(context, shellState.preferences.language) }
                AppShell(shellState, exploreViewModel)
            }
        }
    }
}

@Composable
private fun AppShell(shellState: ShellUiState, exploreViewModel: ExploreViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Destination.HOME.name
    val homeViewModel: HomeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val preferencesViewModel: PreferencesViewModel = viewModel()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    val exploreState by exploreViewModel.state.collectAsStateWithLifecycle()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val preferencesState by preferencesViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val playbackController = remember(context) { Media3PlaybackController(context) }
    DisposableEffect(playbackController) { onDispose { playbackController.release() } }
    val playbackState by playbackController.state.collectAsStateWithLifecycle()
    val currentTrack = remember(playbackState.currentTrackId, exploreState.tracks) { exploreState.tracks.firstOrNull { it.id == playbackState.currentTrackId } }

    Scaffold(bottomBar = {
        androidx.compose.foundation.layout.Column {
            if (currentTrack != null) MiniPlayer(currentTrack, shellState.preferences.language, playbackState, playbackController) { nav.navigate("track/${currentTrack.id}") }
            NavigationBar {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        modifier = Modifier.testTag("tab_" + destination.name),
                        selected = route == destination.name,
                        onClick = { nav.navigate(destination.name) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        } },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.label)) }
                    )
                }
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = Destination.HOME.name, modifier = Modifier.padding(padding)) {
            composable(Destination.HOME.name) { HomeScreen(homeState, shellState.preferences.language, { nav.navigate(Destination.EXPLORE.name) }, { nav.navigate("deity/$it") }, { nav.navigate("track/$it") }, playbackState, playbackController) }
            composable(Destination.EXPLORE.name) { ExploreScreen(exploreState, shellState.preferences.language, { nav.navigate("deity/$it") }, { nav.navigate("category/${it.name}") }, { nav.navigate("track/$it") }, playbackState, playbackController) }
            composable(Destination.SEARCH.name) { SearchScreen(searchViewModel, searchState, shellState.preferences.language, { nav.navigate("track/$it") }, { nav.navigate("deity/$it") }, playbackState, playbackController) }
            composable(Destination.LIBRARY.name) {
                LibraryScreen(preferencesState, exploreState.deities, preferencesViewModel::setTheme,
                    onLanguage = { language -> preferencesViewModel.setLanguage(language); LocaleController.apply(context, language) },
                    onTime = preferencesViewModel::setListeningTime, onDeities = preferencesViewModel::setDeities)
            }
            composable(DEITY_ROUTE, arguments = listOf(navArgument("deityId") { type = NavType.StringType })) { backStackEntry ->
                val deityId = backStackEntry.arguments?.getString("deityId").orEmpty()
                val detailViewModel: DeityDetailViewModel = viewModel()
                DeityDetailScreen(detailViewModel, deityId, shellState.preferences.language, { nav.popBackStack() }, { nav.navigate("track/$it") }, { nav.navigate("category/${it.name}") }, playbackState, playbackController)
            }
            composable(CATEGORY_ROUTE, arguments = listOf(navArgument("categoryName") { type = NavType.StringType })) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("categoryName")?.let { runCatching { DevotionalCategory.valueOf(it) }.getOrNull() }
                val categoryViewModel: CategoryViewModel = viewModel()
                if (category != null) CategoryDetailScreen(categoryViewModel, category, shellState.preferences.language, { nav.popBackStack() }, { nav.navigate("track/$it") }, playbackState, playbackController)
                else NotFoundScreen { nav.popBackStack() }
            }
            composable(TRACK_ROUTE, arguments = listOf(navArgument("trackId") { type = NavType.StringType })) { backStackEntry ->
                val trackId = backStackEntry.arguments?.getString("trackId").orEmpty()
                val trackViewModel: TrackDetailViewModel = viewModel()
                TrackDetailScreen(trackViewModel, trackId, shellState.preferences.language, { nav.popBackStack() }, { nav.navigate("deity/$it") }, { nav.navigate("category/${it.name}") }, playbackState, playbackController)
            }
        }
    }
}
