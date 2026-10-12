package io.github.leroyjg.arabe

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.leroyjg.arabe.data.ThemeMode
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.activity.ActivityScreen
import io.github.leroyjg.arabe.ui.activity.ReviewKind
import io.github.leroyjg.arabe.ui.activity.ReviewSessionScreen
import io.github.leroyjg.arabe.ui.main.MainScreen
import io.github.leroyjg.arabe.ui.text.LocalReadingPrefs
import io.github.leroyjg.arabe.ui.text.ReadingPrefs
import io.github.leroyjg.arabe.ui.theme.ArabeTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as ArabeApp).container
        setContent {
            val settings by container.settings.state.collectAsStateWithLifecycle()
            val dark = when (settings.theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Icônes de la barre d'état lisibles quel que soit le thème choisi dans l'app.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
                )
                onDispose {}
            }
            ArabeTheme(darkTheme = dark) {
                // Interface en français : toujours de gauche à droite, même si le téléphone est en arabe.
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalAppContainer provides container,
                    LocalReadingPrefs provides ReadingPrefs(
                        hideHarakat = !settings.showHarakat,
                        arabicScale = settings.arabicScale,
                        showTranslit = settings.showTranslit,
                        autoPlay = settings.autoPlay,
                    ),
                ) {
                    AppNavHost()
                }
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}

@Serializable
private data object MainRoute

@Serializable
private data class ActivityRoute(val activityId: String)

/** Séance de révision : [kind] « vocabulaire » (avec un [filter] facultatif) ou « grammaire » (avec un [topicId] facultatif). */
@Serializable
private data class ReviewRoute(val kind: String, val topicId: String? = null, val filter: String? = null)

@Composable
private fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = MainRoute) {
        composable<MainRoute> {
            MainScreen(
                onOpenActivity = { nav.navigate(ActivityRoute(it)) { launchSingleTop = true } },
                onStartVocabulary = { filter -> nav.navigate(ReviewRoute(ReviewKind.VOCABULARY, filter = filter)) { launchSingleTop = true } },
                onStartGrammar = { topic -> nav.navigate(ReviewRoute(ReviewKind.GRAMMAR, topicId = topic)) { launchSingleTop = true } },
            )
        }
        composable<ActivityRoute> { entry ->
            ActivityScreen(activityId = entry.toRoute<ActivityRoute>().activityId, onClose = { nav.popBackStack() })
        }
        composable<ReviewRoute> { entry ->
            val route = entry.toRoute<ReviewRoute>()
            ReviewSessionScreen(kind = route.kind, topicId = route.topicId, filter = route.filter, onClose = { nav.popBackStack() })
        }
    }
}
