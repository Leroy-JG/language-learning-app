package io.github.leroyjg.arabe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.activity.ActivityScreen
import io.github.leroyjg.arabe.ui.chapter.ChapterScreen
import io.github.leroyjg.arabe.ui.home.HomeScreen
import io.github.leroyjg.arabe.ui.theme.ArabeTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as ArabeApp).container
        setContent {
            ArabeTheme {
                // Interface en français : toujours de gauche à droite, même si le téléphone est en arabe.
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalAppContainer provides container,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}

@Serializable
private data object HomeRoute

@Serializable
private data class ChapterRoute(val chapterId: String)

@Serializable
private data class ActivityRoute(val activityId: String)

@Composable
private fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(onOpenChapter = { nav.navigate(ChapterRoute(it)) })
        }
        composable<ChapterRoute> { entry ->
            ChapterScreen(
                chapterId = entry.toRoute<ChapterRoute>().chapterId,
                onBack = { nav.popBackStack() },
                onOpenActivity = { nav.navigate(ActivityRoute(it)) },
            )
        }
        composable<ActivityRoute> { entry ->
            ActivityScreen(activityId = entry.toRoute<ActivityRoute>().activityId, onClose = { nav.popBackStack() })
        }
    }
}
