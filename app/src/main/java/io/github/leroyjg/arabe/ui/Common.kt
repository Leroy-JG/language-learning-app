package io.github.leroyjg.arabe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.leroyjg.arabe.AppContainer
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.data.ContentState

val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer absent") }

/** Affiche [content] quand le cours est chargé ; sinon un indicateur ou l'erreur de chargement. */
@Composable
fun WithContent(content: @Composable (ContentState.Ready) -> Unit) {
    val state by LocalAppContainer.current.content.state.collectAsStateWithLifecycle()
    when (val s = state) {
        ContentState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is ContentState.Failed -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.content_error_title), style = MaterialTheme.typography.titleLarge)
            s.issues.forEach { Text(it.toString(), style = MaterialTheme.typography.bodySmall) }
        }
        is ContentState.Ready -> content(s)
    }
}

fun ActivityType.label(): Int = when (this) {
    ActivityType.VOCABULARY -> R.string.activity_type_vocabulary
    ActivityType.GRAMMAR -> R.string.activity_type_grammar
    ActivityType.DIALOGUE -> R.string.activity_type_dialogue
    ActivityType.REVIEW -> R.string.activity_type_review
    ActivityType.QUIZ -> R.string.activity_type_quiz
    ActivityType.TEST -> R.string.activity_type_test
    ActivityType.EXAM -> R.string.activity_type_exam
}

fun ActivityType.icon(): ImageVector = when (this) {
    ActivityType.VOCABULARY -> Icons.Filled.Star
    ActivityType.GRAMMAR -> Icons.Filled.Build
    ActivityType.DIALOGUE -> Icons.Filled.Face
    ActivityType.REVIEW -> Icons.Filled.Refresh
    ActivityType.QUIZ, ActivityType.TEST, ActivityType.EXAM -> Icons.Filled.CheckCircle
}
