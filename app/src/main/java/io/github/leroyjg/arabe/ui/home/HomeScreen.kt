package io.github.leroyjg.arabe.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.content.Chapter
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Level
import io.github.leroyjg.arabe.engine.CourseProgress
import io.github.leroyjg.arabe.engine.LessonState
import io.github.leroyjg.arabe.ui.IconImages
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.icon
import io.github.leroyjg.arabe.ui.label
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlinx.coroutines.launch

/** Une ligne du parcours affiché sur l'accueil. */
private sealed interface PathEntry {
    data class Header(val chapter: Chapter) : PathEntry
    data class Lesson(val activity: Activity, val connectorBelow: Boolean) : PathEntry
    data class Checkpoint(val activity: Activity) : PathEntry
}

private fun pathOf(level: Level): List<PathEntry> = buildList {
    for (chapter in level.chapters) {
        add(PathEntry.Header(chapter))
        val lessons = chapter.activities.filter { it.file.type != ActivityType.QUIZ }
        lessons.forEachIndexed { i, activity ->
            val last = i == lessons.lastIndex
            add(PathEntry.Lesson(activity, connectorBelow = !last || chapter.activities.any { it.file.type == ActivityType.QUIZ }))
        }
        chapter.activities.filter { it.file.type == ActivityType.QUIZ }.forEach { add(PathEntry.Checkpoint(it)) }
    }
}

private fun levelOf(course: Course, activity: Activity?): Level? =
    activity?.let { a -> course.chapter(a.chapterId)?.let { course.levelOf(it) } }

/** Accueil : le tome choisi, sa progression, et le fil des leçons de chaque chapitre. */
@Composable
fun HomeScreen(onOpenActivity: (String) -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        WithContent { ready ->
            val container = LocalAppContainer.current
            val completed by container.progress.completed.collectAsStateWithLifecycle(initialValue = null)
            val done = completed
            if (done == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@WithContent
            }
            val course = ready.course
            val progress = remember(course, done) { CourseProgress(course, done) }
            var chosenLevel by rememberSaveable { mutableStateOf<String?>(null) }
            val level = course.levels.firstOrNull { it.id == chosenLevel }
                ?: levelOf(course, progress.next)
                ?: course.levels.first()
            val entries = remember(level) { pathOf(level) }
            val listState = rememberLazyListState()
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val lockedMessage = stringResource(R.string.lesson_locked)

            // À l'ouverture (et au changement de tome), on montre la prochaine leçon à faire.
            LaunchedEffect(level.id) {
                val next = progress.next
                val index = entries.indexOfFirst {
                    (it is PathEntry.Lesson && it.activity == next) || (it is PathEntry.Checkpoint && it.activity == next)
                }
                if (index > 0) listState.scrollToItem((index - 1).coerceAtLeast(0))
            }

            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    LevelHeader(
                        course = course,
                        level = level,
                        fraction = progress.fraction(level),
                        onChoose = { chosenLevel = it },
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                    ) {
                        items(entries, key = {
                            when (it) {
                                is PathEntry.Header -> "h-" + it.chapter.id
                                is PathEntry.Lesson -> it.activity.id
                                is PathEntry.Checkpoint -> it.activity.id
                            }
                        }) { entry ->
                            when (entry) {
                                is PathEntry.Header -> ChapterHeader(entry.chapter, progress)
                                is PathEntry.Lesson -> {
                                    val state = progress.state(entry.activity)
                                    LessonRow(
                                        activity = entry.activity,
                                        state = state,
                                        isNext = progress.next == entry.activity,
                                        connectorBelow = entry.connectorBelow,
                                        onClick = {
                                            if (state == LessonState.LOCKED) scope.launch { snackbar.showSnackbar(lockedMessage) }
                                            else onOpenActivity(entry.activity.id)
                                        },
                                    )
                                }
                                is PathEntry.Checkpoint -> {
                                    val state = progress.state(entry.activity)
                                    CheckpointCard(state, onClick = {
                                        if (state == LessonState.LOCKED) scope.launch { snackbar.showSnackbar(lockedMessage) }
                                        else onOpenActivity(entry.activity.id)
                                    })
                                }
                            }
                        }
                    }
                }
                SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
            }
        }
    }
}

// ---------------------------------------------------------------- en-tête

@Composable
private fun LevelHeader(course: Course, level: Level, fraction: Float, onChoose: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Box {
            Row(
                Modifier.clip(RoundedCornerShape(12.dp)).clickable { menu = true }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RichText(level.file.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.level_choose), modifier = Modifier.size(32.dp))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                course.levels.forEach { l ->
                    DropdownMenuItem(
                        text = { RichText(l.file.title, style = MaterialTheme.typography.titleMedium) },
                        onClick = {
                            menu = false
                            onChoose(l.id)
                        },
                        trailingIcon = { if (l.id == level.id) Icon(Icons.Filled.Check, contentDescription = null) },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ProgressWithBadge(fraction)
    }
}

/** Barre de progression du tome, avec le pourcentage dans une pastille au bout du remplissage. */
@Composable
private fun ProgressWithBadge(fraction: Float) {
    val green = AppTheme.colors.correct
    val percent = (fraction * 100).toInt()
    BoxWithConstraints(Modifier.fillMaxWidth().height(34.dp)) {
        val badgeWidth = 64.dp
        Box(
            Modifier.align(Alignment.CenterStart).fillMaxWidth().height(14.dp)
                .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Box(
            Modifier.align(Alignment.CenterStart).fillMaxWidth(fraction.coerceIn(0f, 1f)).height(14.dp)
                .clip(RoundedCornerShape(50)).background(green),
        )
        val x = (maxWidth * fraction - badgeWidth / 2).coerceIn(0.dp, (maxWidth - badgeWidth).coerceAtLeast(0.dp))
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = x).width(badgeWidth).height(30.dp)
                .background(green, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.level_progress, percent), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ---------------------------------------------------------------- chapitres et leçons

@Composable
private fun ChapterHeader(chapter: Chapter, progress: CourseProgress) {
    val lessons = chapter.activities.filter { it.file.type != ActivityType.QUIZ }
    val doneCount = lessons.count { progress.state(it) == LessonState.COMPLETED }
    Column(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 16.dp)) {
        Text(
            stringResource(R.string.chapter_label, chapter.file.number),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            stringResource(R.string.lessons_done, doneCount, lessons.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        RichText(chapter.file.title.fr, style = MaterialTheme.typography.bodyLarge)
        ArabicText(
            chapter.file.title.ar,
            size = 20.sp,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val NodeSize = 84.dp
private val ImageSize = 68.dp

@Composable
private fun LessonRow(activity: Activity, state: LessonState, isNext: Boolean, connectorBelow: Boolean, onClick: () -> Unit) {
    val green = AppTheme.colors.correct
    val locked = state == LessonState.LOCKED
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LessonNode(activity, state, isNext)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f).alpha(if (locked) 0.45f else 1f)) {
                TypePill(activity.file.type)
                Spacer(Modifier.height(4.dp))
                RichText(activity.file.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 21.sp))
            }
        }
        if (connectorBelow) {
            Box(
                Modifier.padding(start = NodeSize / 2 - 3.dp, top = 2.dp, bottom = 2.dp)
                    .width(6.dp).height(30.dp)
                    .background(if (state == LessonState.COMPLETED) green else MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
            )
        }
    }
}

/** Image ronde de la leçon : anneau vert si terminée, cadenas si verrouillée. */
@Composable
private fun LessonNode(activity: Activity, state: LessonState, isNext: Boolean) {
    val colors = AppTheme.colors
    val typeColor = colors.forType(activity.file.type)
    val ring = when {
        state == LessonState.COMPLETED -> colors.correct
        isNext -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    Box(Modifier.size(NodeSize), contentAlignment = Alignment.Center) {
        Box(Modifier.size(NodeSize).border(6.dp, ring, CircleShape))
        Box(
            Modifier.size(ImageSize).clip(CircleShape).background(typeColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            val image = IconImages.of(activity.file.icon)
            if (image != null) {
                Image(painterResource(image), contentDescription = null, modifier = Modifier.size(46.dp))
            } else {
                Icon(activity.file.type.icon(), contentDescription = null, tint = typeColor, modifier = Modifier.size(34.dp))
            }
            if (state == LessonState.LOCKED) {
                Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)))
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
            }
        }
        if (state == LessonState.COMPLETED) {
            Box(
                Modifier.align(Alignment.BottomCenter).offset(y = 4.dp).size(30.dp)
                    .background(colors.correct, CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.lesson_done), tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TypePill(type: ActivityType) {
    val color = AppTheme.colors.forType(type)
    Box(Modifier.background(color.copy(alpha = 0.14f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(
            stringResource(type.label()).uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
        )
    }
}

/** Bilan du chapitre : le quiz final, qui débloque le chapitre suivant. */
@Composable
private fun CheckpointCard(state: LessonState, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val done = state == LessonState.COMPLETED
    val locked = state == LessonState.LOCKED
    Row(
        Modifier.fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (done) colors.correctContainer else colors.remarkContainer)
            .clickable(onClick = onClick)
            .padding(18.dp)
            .alpha(if (locked) 0.55f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(76.dp)
                .border(6.dp, if (done) colors.correct else MaterialTheme.colorScheme.secondary, CircleShape)
                .padding(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(if (done) R.drawable.ui_trophy else R.drawable.ui_flag),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )
            if (locked) Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.checkpoint_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                stringResource(if (done) R.string.checkpoint_done else R.string.checkpoint_text),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
