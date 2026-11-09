package com.mindforge101.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.graphics.Color as AndroidColor
import com.mindforge101.app.ui.MfBottomBar
import com.mindforge101.app.ui.NavIcon
import com.mindforge101.app.ui.NavTab
import com.mindforge101.app.ui.MfMotion
import com.mindforge101.app.ui.SplashOverlay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

private val tabs = listOf(
    NavTab("home", "Home", NavIcon.Home),
    NavTab("games", "Games", NavIcon.Games),
    NavTab("progress", "Progress", NavIcon.Progress),
    NavTab("profile", "Profile", NavIcon.Profile),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        setContent {
            val vm: AppViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            MindForgeTheme(largeText = settings.largeText, reduceMotion = settings.reduceMotion) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    App(vm)
                    if (showSplash) SplashOverlay(settings.reduceMotion) { showSplash = false }
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
                MfBottomBar(tabs, route) { tab ->
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
            enterTransition = { fadeIn(tween(MfMotion.Medium)) + if (settings.reduceMotion) androidx.compose.animation.EnterTransition.None else slideInHorizontally(tween(MfMotion.Medium)) { it / 24 } },
            exitTransition = { fadeOut(tween(MfMotion.Short)) },
            popEnterTransition = { fadeIn(tween(MfMotion.Medium)) },
            popExitTransition = { fadeOut(tween(MfMotion.Short)) + if (settings.reduceMotion) androidx.compose.animation.ExitTransition.None else slideOutHorizontally(tween(MfMotion.Short)) { it / 24 } },
        ) {
            composable("home") { HomeScreen(profile, sessions, achievements.size) { nav.openPlay(it) } }
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
                        bestScore = progress[game.id]?.bestScore?.takeIf { it > 0 },
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
