package com.mindforge101.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import com.mindforge101.app.games.Catalog
import com.mindforge101.app.ui.AppViewModel
import com.mindforge101.app.ui.GamesScreen
import com.mindforge101.app.ui.HomeScreen
import com.mindforge101.app.ui.MindForgeTheme
import com.mindforge101.app.ui.PlayScreen
import com.mindforge101.app.ui.ProfileScreen
import com.mindforge101.app.ui.ProgressScreen
import com.mindforge101.app.ui.ResultScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Filled.Home),
    Tab("games", "Games", Icons.AutoMirrored.Filled.List),
    Tab("progress", "Progress", Icons.Filled.Star),
    Tab("profile", "Profile", Icons.Filled.Person),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            MindForgeTheme(largeText = settings.largeText) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    App(vm)
                }
            }
        }
    }
}

private fun NavHostController.openPlay(id: String) = navigate("play/$id")

@Composable
private fun App(vm: AppViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val settings by vm.settings.collectAsStateWithLifecycle()
    val profile by vm.activeProfile.collectAsStateWithLifecycle()
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val achievements by vm.achievements.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val isTab = tabs.any { it.route == route }
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = if (isTab) Modifier.padding(padding) else Modifier,
        ) {
            composable("home") { HomeScreen(profile, sessions) { nav.openPlay(it) } }
            composable("games") { GamesScreen(progress) { nav.openPlay(it) } }
            composable("progress") { ProgressScreen(profile, sessions, achievements) }
            composable("profile") { ProfileScreen(profiles, profile, settings, vm) }
            composable("play/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id")
                val game = id?.let { Catalog.find(it) }
                if (game == null) {
                    LaunchedEffect(Unit) { nav.popBackStack() }
                } else {
                    val level = AdaptiveDifficultyEngine.levelForPlay(progress[game.id]?.level ?: 1f)
                    PlayScreen(
                        game = game,
                        level = level,
                        reduceMotion = settings.reduceMotion,
                        haptics = settings.haptics,
                        onFinished = { result ->
                            vm.finishSession(game, result) {
                                nav.navigate("result/${game.id}") {
                                    popUpTo("play/{id}") { inclusive = true }
                                }
                            }
                        },
                        onExit = { nav.popBackStack() },
                    )
                }
            }
            composable("result/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val outcome = vm.lastOutcome
                if (outcome == null) {
                    LaunchedEffect(Unit) { nav.popBackStack() }
                } else {
                    ResultScreen(
                        outcome = outcome,
                        onAgain = {
                            nav.navigate("play/$id") { popUpTo("result/{id}") { inclusive = true } }
                        },
                        onDone = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}
