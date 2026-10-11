package io.github.leroyjg.arabe.ui.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.icon
import io.github.leroyjg.arabe.ui.label
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(chapterId: String, onBack: () -> Unit, onOpenActivity: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        WithContent { ready ->
            val chapter = ready.course.chapter(chapterId) ?: return@WithContent
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            stringResource(R.string.chapter_label, chapter.file.number),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        ArabicText(chapter.file.title.ar, size = 28.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        RichText(chapter.file.title.fr, style = MaterialTheme.typography.titleLarge)
                        chapter.file.description?.let {
                            RichText(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                chapter.file.sections.forEachIndexed { sectionIndex, section ->
                    item(key = "section-$sectionIndex") {
                        RichText(
                            section.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    items(chapter.activities.filter { it.sectionIndex == sectionIndex }, key = { it.id }) { activity ->
                        ActivityCard(activity, onClick = { onOpenActivity(activity.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity, onClick: () -> Unit) {
    val type = activity.file.type
    val color = AppTheme.colors.forType(type)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Icon(type.icon(), contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(type.label()), style = MaterialTheme.typography.labelMedium, color = color)
                RichText(activity.file.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.activity_screen_count, activity.screens.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
