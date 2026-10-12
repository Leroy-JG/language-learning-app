package io.github.leroyjg.arabe.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.data.ContentState
import io.github.leroyjg.arabe.ui.AppIcons
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.home.LearnScreen
import io.github.leroyjg.arabe.ui.review.ReviewScreen
import io.github.leroyjg.arabe.ui.settings.SettingsScreen

private enum class Tab(val label: Int, val icon: () -> ImageVector) {
    LEARN(R.string.tab_learn, { AppIcons.Book }),
    REVIEW(R.string.tab_review, { AppIcons.Chart }),
    SETTINGS(R.string.tab_settings, { Icons.Filled.Settings }),
}

/** Écran principal : trois onglets en bas (Apprendre, Réviser, Paramètres). */
@Composable
fun MainScreen(
    onOpenActivity: (String) -> Unit,
    onStartVocabulary: (String?) -> Unit,
    onStartGrammar: (String?) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(Tab.LEARN.ordinal) }
    // Chaque onglet garde son état (défilement, sections ouvertes) quand on passe à un autre.
    val states = rememberSaveableStateHolder()
    BackHandler(enabled = tab != Tab.LEARN.ordinal) { tab = Tab.LEARN.ordinal }

    // Leçons terminées avant l'arrivée des révisions : leurs mots entrent dans la mémoire une fois.
    val container = LocalAppContainer.current
    val content by container.content.state.collectAsStateWithLifecycle()
    LaunchedEffect(content) {
        (content as? ContentState.Ready)?.let { container.review.backfill(it.course, container.progress.dao) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t.ordinal,
                        onClick = { tab = t.ordinal },
                        icon = { Icon(t.icon(), contentDescription = null) },
                        label = { Text(stringResource(t.label), fontWeight = if (tab == t.ordinal) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            states.SaveableStateProvider(tab) {
                when (Tab.entries[tab]) {
                    Tab.LEARN -> LearnScreen(onOpenActivity = onOpenActivity)
                    Tab.REVIEW -> ReviewScreen(
                        onStartVocabulary = onStartVocabulary,
                        onStartGrammar = onStartGrammar,
                        onOpenActivity = onOpenActivity,
                        onGoLearn = { tab = Tab.LEARN.ordinal },
                    )
                    Tab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}
