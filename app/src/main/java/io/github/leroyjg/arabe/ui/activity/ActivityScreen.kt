package io.github.leroyjg.arabe.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.text.RichText

/** Déroulé d'une activité (moteur d'exercices : prochaine étape). */
@Composable
fun ActivityScreen(activityId: String, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        WithContent { ready ->
            val activity = ready.course.activity(activityId)
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (activity == null) {
                    Text(stringResource(R.string.activity_not_found))
                } else {
                    RichText(activity.file.title, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.coming_soon), modifier = Modifier.padding(top = 12.dp))
                }
                TextButton(onClick = onClose, modifier = Modifier.padding(top = 24.dp)) { Text(stringResource(R.string.back)) }
            }
        }
    }
}
