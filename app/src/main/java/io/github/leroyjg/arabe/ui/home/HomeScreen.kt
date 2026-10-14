package io.github.leroyjg.arabe.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import io.github.leroyjg.arabe.content.isGraded
import io.github.leroyjg.arabe.engine.CourseProgress
import io.github.leroyjg.arabe.ui.IconImages
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.eightPointStar
import io.github.leroyjg.arabe.ui.icon
import io.github.leroyjg.arabe.ui.label
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme

private fun levelOf(course: Course, activity: Activity?): Level? =
    activity?.let { a -> course.chapter(a.chapterId)?.let { course.levelOf(it) } }

/**
 * Onglet « Apprendre » : le tome choisi, sa progression, et ses chapitres en accordéon.
 * Rien n'est verrouillé : chaque leçon s'ouvre quand on veut.
 */
@Composable
fun LearnScreen(onOpenActivity: (String) -> Unit) {
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
            val next = progress.next?.takeIf { it.chapterId in level.chapters.map { c -> c.id } }
            val nextChapter = next?.chapterId ?: level.chapters.firstOrNull()?.id

            // Chapitres ouverts (ids séparés par des virgules, pour survivre à la rotation) ;
            // au départ, celui de la prochaine leçon.
            var openIds by rememberSaveable(level.id) { mutableStateOf(nextChapter.orEmpty()) }
            val open = openIds.split(',').filter { it.isNotEmpty() }.toSet()
            val listState = rememberLazyListState()
            var scrolledFor by rememberSaveable { mutableStateOf<String?>(null) }
            LaunchedEffect(level.id) {
                if (scrolledFor != level.id) {
                    scrolledFor = level.id
                    val index = level.chapters.indexOfFirst { it.id == nextChapter }
                    if (index > 0) listState.scrollToItem(index)
                }
            }

            Column(Modifier.fillMaxSize()) {
                // Le tome est validé quand son examen final est réussi.
                val validated = level.chapters.any { chapter ->
                    chapter.activities.any { it.file.type == ActivityType.EXAM && progress.isCompleted(it) }
                }
                LevelHeader(
                    course = course,
                    level = level,
                    fraction = progress.fraction(level),
                    validated = validated,
                    onChoose = { chosenLevel = it },
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(level.chapters, key = { it.id }) { chapter ->
                        ChapterCard(
                            chapter = chapter,
                            progress = progress,
                            next = next,
                            expanded = chapter.id in open,
                            onToggle = {
                                openIds = (if (chapter.id in open) open - chapter.id else open + chapter.id).joinToString(",")
                            },
                            onOpenActivity = onOpenActivity,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- en-tête

@Composable
private fun LevelHeader(course: Course, level: Level, fraction: Float, validated: Boolean, onChoose: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
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
        if (validated) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = AppTheme.colors.correct, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(R.string.level_validated),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.correct,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        ProgressWithBadge(fraction)
    }
}

/** Barre de progression du tome, avec le pourcentage dans une pastille au bout du remplissage. */
@Composable
private fun ProgressWithBadge(fraction: Float) {
    val green = MaterialTheme.colorScheme.primary
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), spring(stiffness = 120f), label = "tome")
    val percent = (fraction * 100).toInt()
    BoxWithConstraints(Modifier.fillMaxWidth().height(34.dp)) {
        val badgeWidth = 64.dp
        Box(
            Modifier.align(Alignment.CenterStart).fillMaxWidth().height(14.dp)
                .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Box(
            Modifier.align(Alignment.CenterStart).fillMaxWidth(animated).height(14.dp)
                .clip(RoundedCornerShape(50)).background(green),
        )
        val x = (maxWidth * animated - badgeWidth / 2).coerceIn(0.dp, (maxWidth - badgeWidth).coerceAtLeast(0.dp))
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = x).width(badgeWidth).height(30.dp)
                .background(green, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.level_progress, percent), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ---------------------------------------------------------------- chapitres

/** Carte d'un chapitre : son en-tête, et le fil de ses leçons quand elle est ouverte. */
@Composable
private fun ChapterCard(
    chapter: Chapter,
    progress: CourseProgress,
    next: Activity?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val lessons = chapter.activities.filter { it.file.type != ActivityType.QUIZ }
    val doneCount = lessons.count { progress.isCompleted(it) }
    val chapterDone = chapter.activities.all { progress.isCompleted(it) }
    // Un chapitre « Défi » ou « Le grand défi » ne contient qu'un test noté.
    val graded = chapter.activities.isNotEmpty() && chapter.activities.all { it.file.type.isGraded }
    val scheme = MaterialTheme.colorScheme
    val star = scheme.primary.copy(alpha = 0.10f)
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, if (expanded) scheme.primary.copy(alpha = 0.35f) else scheme.outlineVariant),
        shadowElevation = if (expanded) 3.dp else 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .drawBehind {
                        // Étoile à huit branches en filigrane, dans le coin.
                        eightPointStar(Offset(size.width - 34.dp.toPx(), 30.dp.toPx()), 64.dp.toPx(), star, 2.dp.toPx())
                    }
                    .padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        (chapter.file.label ?: stringResource(R.string.chapter_label, chapter.file.number)),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        if (graded) {
                            stringResource(if (chapterDone) R.string.exam_or_quiz_passed else R.string.test_to_pass)
                        } else {
                            stringResource(R.string.lessons_done, doneCount, lessons.size)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (chapterDone) AppTheme.colors.correct else scheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    RichText(chapter.file.title.fr, style = MaterialTheme.typography.bodyLarge)
                    ArabicText(
                        chapter.file.title.ar,
                        size = 21.sp,
                        textAlign = TextAlign.Start,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                ChapterRing(fraction = progress.fraction(chapter), done = chapterDone, expanded = expanded)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(stiffness = 380f)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = 500f)) + fadeOut(),
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {
                    HorizontalDivider(color = scheme.outlineVariant, modifier = Modifier.padding(bottom = 12.dp))
                    ChapterPath(chapter, progress, next, onOpenActivity)
                }
            }
        }
    }
}

/** Anneau d'avancement du chapitre, avec la flèche de l'accordéon au centre. */
@Composable
private fun ChapterRing(fraction: Float, done: Boolean, expanded: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val animated by animateFloatAsState(fraction, spring(stiffness = 120f), label = "chapitre")
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, spring(stiffness = 400f), label = "flèche")
    val track = scheme.surfaceVariant
    val fill = scheme.primary
    Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(58.dp)) {
            val w = 6.dp.toPx()
            val inset = Offset(w / 2, w / 2)
            val arcSize = Size(size.width - w, size.height - w)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = inset, size = arcSize, style = Stroke(w))
            if (animated > 0f) {
                drawArc(fill, -90f, 360f * animated, useCenter = false, topLeft = inset, size = arcSize, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        if (done) {
            Box(Modifier.size(38.dp).background(fill, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.chapter_done), tint = scheme.onPrimary, modifier = Modifier.size(22.dp))
            }
        } else {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(if (expanded) R.string.chapter_collapse else R.string.chapter_expand),
                modifier = Modifier.size(30.dp).rotate(rotation),
                tint = scheme.onSurface,
            )
        }
    }
}

/** Fil des leçons d'un chapitre, partie par partie, puis le bilan. */
@Composable
private fun ChapterPath(chapter: Chapter, progress: CourseProgress, next: Activity?, onOpenActivity: (String) -> Unit) {
    val lessons = chapter.activities.filter { it.file.type != ActivityType.QUIZ }
    val quizzes = chapter.activities.filter { it.file.type == ActivityType.QUIZ }
    var previousSection = -1
    lessons.forEachIndexed { i, activity ->
        if (activity.sectionIndex != previousSection) {
            previousSection = activity.sectionIndex
            chapter.file.sections.getOrNull(activity.sectionIndex)?.let { section ->
                RichText(
                    section.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = if (i == 0) 0.dp else 6.dp, bottom = 8.dp),
                )
            }
        }
        val nextInSection = lessons.getOrNull(i + 1)?.sectionIndex == activity.sectionIndex
        LessonRow(
            activity = activity,
            completed = progress.isCompleted(activity),
            isNext = next == activity,
            connectorBelow = nextInSection,
            onClick = { onOpenActivity(activity.id) },
        )
    }
    quizzes.forEach { quiz ->
        Spacer(Modifier.height(10.dp))
        CheckpointCard(done = progress.isCompleted(quiz), isNext = next == quiz, onClick = { onOpenActivity(quiz.id) })
    }
}

private val NodeSize = 74.dp
private val ImageSize = 60.dp

@Composable
private fun LessonRow(activity: Activity, completed: Boolean, isNext: Boolean, connectorBelow: Boolean, onClick: () -> Unit) {
    val green = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LessonNode(activity, completed, isNext)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypePill(activity.file.type)
                    if (isNext) NextPill()
                }
                Spacer(Modifier.height(4.dp))
                RichText(activity.file.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 19.sp))
            }
        }
        if (connectorBelow) {
            Box(
                Modifier.padding(start = NodeSize / 2 - 3.dp, top = 2.dp, bottom = 2.dp)
                    .width(6.dp).height(22.dp)
                    .background(if (completed) green else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(3.dp)),
            )
        }
    }
}

/** Image ronde de la leçon : anneau vert si terminée, doré si c'est la leçon proposée. */
@Composable
private fun LessonNode(activity: Activity, completed: Boolean, isNext: Boolean) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val typeColor = colors.forType(activity.file.type)
    val ring = when {
        completed -> scheme.primary
        isNext -> scheme.secondary
        else -> scheme.outline
    }
    Box(Modifier.size(NodeSize), contentAlignment = Alignment.Center) {
        Box(Modifier.size(NodeSize).border(5.dp, ring, CircleShape))
        Box(
            Modifier.size(ImageSize).clip(CircleShape).background(typeColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            val image = IconImages.of(activity.file.icon)
            if (image != null) {
                Image(painterResource(image), contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Icon(activity.file.type.icon(), contentDescription = null, tint = typeColor, modifier = Modifier.size(30.dp))
            }
        }
        val badge = when {
            completed -> scheme.primary
            isNext -> scheme.secondary
            else -> null
        }
        if (badge != null) {
            Box(
                Modifier.align(Alignment.BottomCenter).offset(y = 4.dp).size(28.dp)
                    .background(badge, CircleShape)
                    .border(3.dp, scheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (completed) Icons.Filled.Check else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (completed) R.string.lesson_done else R.string.lesson_next),
                    tint = if (completed) scheme.onPrimary else scheme.onSecondary,
                    modifier = Modifier.size(17.dp),
                )
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

@Composable
private fun NextPill() {
    Box(Modifier.background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(
            stringResource(R.string.lesson_next).uppercase(),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
        )
    }
}

/** Bilan du chapitre : le quiz final. */
@Composable
private fun CheckpointCard(done: Boolean, isNext: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (done) colors.correctContainer else colors.remarkContainer)
            .then(if (isNext) Modifier.border(2.dp, scheme.secondary, RoundedCornerShape(22.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(70.dp)
                .border(5.dp, if (done) colors.correct else scheme.secondary, CircleShape)
                .padding(7.dp)
                .clip(CircleShape)
                .background(scheme.secondary.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(if (done) R.drawable.ui_trophy else R.drawable.ui_flag),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.checkpoint_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                stringResource(if (done) R.string.checkpoint_done else R.string.checkpoint_text),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
