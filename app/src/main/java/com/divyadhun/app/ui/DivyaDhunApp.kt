package com.divyadhun.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.divyadhun.app.*
import com.divyadhun.app.R
import com.divyadhun.app.ui.theme.*
enum class Destination {
 HOME, EXPLORE, SEARCH, LIBRARY;
 val label: Int get() = when(this) {
  HOME -> R.string.home; EXPLORE -> R.string.explore; SEARCH -> R.string.search; LIBRARY -> R.string.library
 }
 val icon: ImageVector get() = when(this) {
  HOME -> Icons.Outlined.Home; EXPLORE -> Icons.AutoMirrored.Outlined.List
  SEARCH -> Icons.Outlined.Search; LIBRARY -> Icons.Outlined.FavoriteBorder
 }
}
@Composable fun DivyaDhunApp(model: ShellViewModel) {
 val state by model.state.collectAsStateWithLifecycle()
 val dark = state.theme.usesDark(isSystemInDarkTheme())
 val activity = LocalContext.current as? ComponentActivity
 SideEffect {
  val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
   else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
  activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
 }
 DivyaTheme(state.theme) {
  if (!state.loaded) { Surface(Modifier.fillMaxSize()) { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() } } }
  else AppShell(state, model::setTheme)
 }
}
@Composable fun AppShell(state: ShellUiState = ShellUiState(loaded = true), onTheme: (ThemeMode) -> Unit = {}) {
 val nav = rememberNavController()
 val entry by nav.currentBackStackEntryAsState()
 val route = entry?.destination?.route ?: Destination.HOME.name
 Scaffold(bottomBar = {
  NavigationBar {
   Destination.entries.forEach { destination ->
    NavigationBarItem(modifier = Modifier.testTag("tab_" + destination.name), selected = route == destination.name,
     onClick = { nav.navigate(destination.name) {
      popUpTo(nav.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true; restoreState = true
     } }, icon = { Icon(destination.icon, contentDescription = null) }, label = { Text(stringResource(destination.label)) })
   }
  }
 }) { padding ->
  NavHost(nav, startDestination = Destination.HOME.name, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
   composable(Destination.HOME.name) { HomeScreen { nav.navigate(Destination.EXPLORE.name) { launchSingleTop = true } } }
   composable(Destination.EXPLORE.name) { ExploreScreen() }
   composable(Destination.SEARCH.name) { SearchScreen() }
   composable(Destination.LIBRARY.name) { LibraryScreen(state, onTheme) }
  }
 }
}
