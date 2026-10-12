@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.leroyjg.arabe.ui.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.audio.Sound
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.review.Mastery
import io.github.leroyjg.arabe.review.ReviewCard
import io.github.leroyjg.arabe.review.ReviewPlanner
import io.github.leroyjg.arabe.review.ReviewScheduler
import io.github.leroyjg.arabe.ui.GenderTag
import io.github.leroyjg.arabe.ui.IconImages
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.MasteryBars
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.activity.SpeakerButton
import io.github.leroyjg.arabe.ui.eightPointStar
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.LocalReadingPrefs
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme
import io.github.leroyjg.arabe.ui.theme.Brand
import io.github.leroyjg.arabe.ui.theme.NotoNaskh
import java.text.Normalizer

/** Filtres de la liste de mots (et de la séance qui suit). */
object VocabularyFilter {
    const val WEAK = "faible"
    const val MEDIUM = "moyen"
    const val STRONG = "fort"
    const val SAVED = "favoris"

    fun mastery(filter: String?): Mastery? = when (filter) {
        WEAK -> Mastery.WEAK
        MEDIUM -> Mastery.MEDIUM
        STRONG -> Mastery.STRONG
        else -> null
    }
}

/** Onglet « Réviser » : vocabulaire et grammaire, avec la maîtrise de chaque élément. */
@Composable
fun ReviewScreen(
    onStartVocabulary: (String?) -> Unit,
    onStartGrammar: (String?) -> Unit,
    onOpenActivity: (String) -> Unit,
    onGoLearn: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        WithContent { ready ->
            val container = LocalAppContainer.current
            val cards by container.review.cards.collectAsStateWithLifecycle(initialValue = null)
            val saved by container.review.saved.collectAsStateWithLifecycle(initialValue = emptySet())
            val all = cards
            if (all == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@WithContent
            }
            val course = ready.course
            val scheduler = container.review.scheduler
            val planner = remember(course) { ReviewPlanner(course, scheduler) }
            val today = container.review.today()
            var tab by rememberSaveable { mutableIntStateOf(0) }
            var searching by rememberSaveable { mutableStateOf(false) }
            var query by rememberSaveable { mutableStateOf("") }

            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(if (tab == 0) R.string.review_vocabulary else R.string.review_grammar),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (tab == 0) {
                        IconButton(onClick = {
                            searching = !searching
                            if (!searching) query = ""
                        }) {
                            Icon(
                                if (searching) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = stringResource(R.string.review_search),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.review_vocabulary).uppercase(), fontWeight = FontWeight.Bold) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.review_grammar).uppercase(), fontWeight = FontWeight.Bold) })
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (tab == 0) {
                        VocabularyTab(
                            course = course,
                            planner = planner,
                            scheduler = scheduler,
                            cards = planner.itemCards(all),
                            saved = saved,
                            today = today,
                            searching = searching,
                            query = query,
                            onQuery = { query = it },
                            onStart = onStartVocabulary,
                            onToggleSaved = { id, value -> container.review.setSaved(id, value) },
                            onGoLearn = onGoLearn,
                        )
                    } else {
                        GrammarTab(
                            course = course,
                            planner = planner,
                            scheduler = scheduler,
                            cards = planner.grammarCards(all),
                            today = today,
                            onStart = onStartGrammar,
                            onOpenActivity = onOpenActivity,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- vocabulaire

private class WordEntry(val item: Item, val card: ReviewCard?, val mastery: Mastery?, val retrievability: Double)

@Composable
private fun VocabularyTab(
    course: Course,
    planner: ReviewPlanner,
    scheduler: ReviewScheduler,
    cards: List<ReviewCard>,
    saved: Set<String>,
    today: Long,
    searching: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onStart: (String?) -> Unit,
    onToggleSaved: (String, Boolean) -> Unit,
    onGoLearn: () -> Unit,
) {
    val byKey = cards.associateBy { it.key }
    val entries = remember(cards, saved, today) {
        val keys = cards.map { it.key } + saved.filter { it !in byKey && course.item(it) != null }
        keys.mapNotNull { key ->
            val item = course.item(key) ?: return@mapNotNull null
            val card = byKey[key]
            WordEntry(
                item = item,
                card = card,
                mastery = card?.let { scheduler.mastery(it, today) },
                retrievability = card?.let { scheduler.retrievability(it, today) } ?: 0.0,
            )
        }.sortedBy { it.retrievability }
    }
    if (entries.isEmpty()) {
        EmptyState(text = stringResource(R.string.review_empty_vocabulary), action = stringResource(R.string.review_go_learn), onAction = onGoLearn)
        return
    }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    val counts = planner.masteryCounts(cards, today)
    val wanted = VocabularyFilter.mastery(filter)
    val needle = normalize(query)
    val arabicNeedle = ArabicScript.lettersOnly(query)
    val shown = entries.filter { e ->
        val inFilter = when {
            filter == VocabularyFilter.SAVED -> e.item.id in saved
            wanted != null -> e.mastery == wanted
            else -> true
        }
        val inSearch = query.isBlank() ||
            (needle.isNotEmpty() && normalize(e.item.fr).contains(needle)) ||
            (arabicNeedle.isNotEmpty() && ArabicScript.lettersOnly(e.item.ar).contains(arabicNeedle))
        inFilter && inSearch
    }
    val canStart = when (filter) {
        VocabularyFilter.SAVED -> saved.any { course.item(it) != null }
        null -> cards.isNotEmpty()
        else -> (counts[wanted] ?: 0) > 0
    }
    val due = planner.dueCount(cards, today)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (searching) {
                item(key = "search") {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.review_search_hint)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
            item(key = "banner") {
                DueBanner(
                    title = if (due > 0) pluralStringResource(R.plurals.review_due_words, due, due) else stringResource(R.string.review_up_to_date),
                    text = stringResource(R.string.review_banner_text),
                )
            }
            item(key = "chart") {
                MasteryChart(
                    entries = listOf(
                        ChartEntry(stringResource(R.string.mastery_weak), counts[Mastery.WEAK] ?: 0, AppTheme.colors.weak, VocabularyFilter.WEAK),
                        ChartEntry(stringResource(R.string.mastery_medium), counts[Mastery.MEDIUM] ?: 0, AppTheme.colors.medium, VocabularyFilter.MEDIUM),
                        ChartEntry(stringResource(R.string.mastery_strong), counts[Mastery.STRONG] ?: 0, AppTheme.colors.strong, VocabularyFilter.STRONG),
                    ),
                    selected = filter,
                    onSelect = { filter = if (filter == it) null else it },
                )
            }
            item(key = "saved") {
                SavedRow(
                    count = saved.count { course.item(it) != null },
                    selected = filter == VocabularyFilter.SAVED,
                    onClick = { filter = if (filter == VocabularyFilter.SAVED) null else VocabularyFilter.SAVED },
                )
            }
            item(key = "title") {
                Text(
                    pluralStringResource(R.plurals.review_word_count, shown.size, shown.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }
            items(shown, key = { it.item.id }) { entry ->
                WordRow(
                    course = course,
                    entry = entry,
                    today = today,
                    saved = entry.item.id in saved,
                    expanded = expanded == entry.item.id,
                    onToggle = { expanded = if (expanded == entry.item.id) null else entry.item.id },
                    onToggleSaved = { onToggleSaved(entry.item.id, entry.item.id !in saved) },
                )
            }
        }
        if (canStart) {
            StartButton(
                text = stringResource(if (filter == null) R.string.review_start_vocabulary else R.string.review_start_filtered),
                onClick = { onStart(filter) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}

private fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").trim()

@Composable
private fun DueBanner(title: String, text: String) {
    val star = Color.White.copy(alpha = 0.13f)
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Brand.Emerald, Brand.EmeraldDeep)))
            .drawBehind { eightPointStar(Offset(size.width - 40.dp.toPx(), size.height / 2), 70.dp.toPx(), star, 2.dp.toPx()) }
            .padding(20.dp),
    ) {
        Column(Modifier.padding(end = 56.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

private class ChartEntry(val label: String, val count: Int, val color: Color, val filter: String)

/** Trois colonnes dont la hauteur suit le nombre d'éléments ; toucher une colonne filtre la liste. */
@Composable
private fun MasteryChart(entries: List<ChartEntry>, selected: String?, onSelect: ((String) -> Unit)?) {
    val max = entries.maxOf { it.count }.coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
        entries.forEach { e ->
            val target = 48.dp + 66.dp * (e.count.toFloat() / max)
            val height by animateDpAsState(target, spring(stiffness = 200f), label = "barre")
            val isSelected = selected == e.filter
            Column(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(if (onSelect != null) Modifier.clickable { onSelect(e.filter) } else Modifier)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(height)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (e.count > 0) e.color else e.color.copy(alpha = 0.12f))
                        .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(14.dp)) else Modifier),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    if (e.count == 0) Hatching(e.color)
                    Text(
                        "${e.count}",
                        color = if (e.count > 0) Color.White else e.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                    )
                }
                Text(
                    e.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Hachures d'une colonne vide. */
@Composable
private fun Hatching(color: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val step = 9.dp.toPx()
        val line = color.copy(alpha = 0.35f)
        var x = -size.height
        while (x < size.width) {
            drawLine(line, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 1.5.dp.toPx())
            x += step
        }
    }
}

@Composable
private fun SavedRow(count: Int, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) scheme.primaryContainer else scheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(scheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(stringResource(R.string.review_saved_words), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text("$count", style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
    }
}

@Composable
private fun WordRow(
    course: Course,
    entry: WordEntry,
    today: Long,
    saved: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val container = LocalAppContainer.current
    val prefs = LocalReadingPrefs.current
    val scheme = MaterialTheme.colorScheme
    val item = entry.item
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "flèche")
    Surface(shape = RoundedCornerShape(18.dp), color = scheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WordThumb(item)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    ArabicText(item.ar, size = 23.sp, textAlign = TextAlign.Start)
                    RichText(item.fr, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, maxLines = 2)
                }
                Spacer(Modifier.width(8.dp))
                if (entry.mastery != null) MasteryBars(entry.mastery) else NewChip()
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp).rotate(rotation),
                )
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = scheme.outlineVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SpeakerButton(onClick = { container.audio.play(course, Sound.OfItem(item)) }, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            if (prefs.showTranslit && item.translit != null) {
                                Text(item.translit.orEmpty(), fontStyle = FontStyle.Italic, color = scheme.onSurfaceVariant)
                            }
                            Text(nextReviewText(entry.card, today), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onToggleSaved) {
                            Icon(
                                if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(if (saved) R.string.saved_remove else R.string.saved_add),
                                tint = if (saved) AppTheme.colors.weak else scheme.onSurfaceVariant,
                            )
                        }
                    }
                    item.gender?.let { GenderTag(it) }
                    item.plural?.let { plural ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.plural_label), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            ArabicText(plural, size = 22.sp)
                        }
                    }
                    item.example?.let { ex ->
                        Column(
                            Modifier.fillMaxWidth().background(scheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(10.dp),
                        ) {
                            ArabicText(ex.ar, size = 21.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            RichText(ex.fr, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun nextReviewText(card: ReviewCard?, today: Long): String {
    if (card == null) return stringResource(R.string.review_not_studied)
    val days = (card.dueDay - today).toInt()
    return when {
        days <= 0 -> stringResource(R.string.review_next_today)
        days == 1 -> stringResource(R.string.review_next_tomorrow)
        else -> pluralStringResource(R.plurals.review_next_in_days, days, days)
    }
}

@Composable
private fun WordThumb(item: Item) {
    val image = IconImages.of(item.icon)
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(painterResource(image), contentDescription = null, modifier = Modifier.size(40.dp))
        } else {
            Text(
                ArabicScript.lettersOnly(item.ar).take(1),
                fontFamily = NotoNaskh,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NewChip() {
    Box(Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(stringResource(R.string.review_new), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun StartButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(50),
        text = { Text(text, style = MaterialTheme.typography.titleMedium) },
        icon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        expanded = true,
    )
}

@Composable
private fun EmptyState(text: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Image(painterResource(R.drawable.icon_books), contentDescription = null, modifier = Modifier.size(96.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) { Text(action) }
    }
}

// ---------------------------------------------------------------- grammaire

@Composable
private fun GrammarTab(
    course: Course,
    planner: ReviewPlanner,
    scheduler: ReviewScheduler,
    cards: List<ReviewCard>,
    today: Long,
    onStart: (String?) -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val byKey = cards.associateBy { it.key }
    val counts = planner.masteryCounts(cards, today)
    val topics = planner.grammarTopics
    var sheetTopic by rememberSaveable { mutableStateOf<String?>(null) }
    val chapters = course.chapters.filter { c -> topics.any { it.chapterId == c.id } }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "chart") {
                MasteryChart(
                    entries = listOf(
                        ChartEntry(stringResource(R.string.mastery_grammar_weak), counts[Mastery.WEAK] ?: 0, AppTheme.colors.weak, VocabularyFilter.WEAK),
                        ChartEntry(stringResource(R.string.mastery_grammar_medium), counts[Mastery.MEDIUM] ?: 0, AppTheme.colors.medium, VocabularyFilter.MEDIUM),
                        ChartEntry(stringResource(R.string.mastery_strong), counts[Mastery.STRONG] ?: 0, AppTheme.colors.strong, VocabularyFilter.STRONG),
                    ),
                    selected = null,
                    onSelect = null,
                )
            }
            for (chapter in chapters) {
                item(key = "c-" + chapter.id) {
                    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.icon_puzzle), contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.chapter_label, chapter.file.number), style = MaterialTheme.typography.titleLarge)
                            RichText(chapter.file.title.fr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                items(topics.filter { it.chapterId == chapter.id }, key = { it.id }) { topic ->
                    val card = byKey[topic.id]
                    TopicRow(topic, card?.let { scheduler.mastery(it, today) }, onClick = { sheetTopic = topic.id })
                }
            }
        }
        val needed = planner.mostNeededTopic(cards, today)
        if (needed != null) {
            StartButton(
                text = stringResource(R.string.review_start_grammar),
                onClick = { onStart(needed.id) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    val topic = sheetTopic?.let { id -> topics.firstOrNull { it.id == id } }
    if (topic != null) {
        TopicSheet(
            topic = topic,
            card = byKey[topic.id],
            mastery = byKey[topic.id]?.let { scheduler.mastery(it, today) },
            today = today,
            onDismiss = { sheetTopic = null },
            onPractice = {
                sheetTopic = null
                onStart(topic.id)
            },
            onOpenLesson = {
                sheetTopic = null
                onOpenActivity(topic.id)
            },
        )
    }
}

private fun topicSummary(topic: Activity): List<String> = topic.screens.filterIsInstance<TipScreen>().map { it.title }

@Composable
private fun TopicRow(topic: Activity, mastery: Mastery?, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = scheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RichText(topic.file.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), keepHarakat = true)
                RichText(
                    topicSummary(topic).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    keepHarakat = true,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.width(12.dp))
            if (mastery != null) MasteryBars(mastery) else NewChip()
        }
    }
}

@Composable
private fun TopicSheet(
    topic: Activity,
    card: ReviewCard?,
    mastery: Mastery?,
    today: Long,
    onDismiss: () -> Unit,
    onPractice: () -> Unit,
    onOpenLesson: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = scheme.surface) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RichText(topic.file.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), keepHarakat = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (mastery != null) MasteryBars(mastery) else NewChip()
                Text(
                    when (mastery) {
                        null -> stringResource(R.string.review_not_studied)
                        Mastery.WEAK -> stringResource(R.string.mastery_grammar_weak)
                        Mastery.MEDIUM -> stringResource(R.string.mastery_grammar_medium)
                        Mastery.STRONG -> stringResource(R.string.mastery_strong)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (card != null) Text(nextReviewText(card, today), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                topicSummary(topic).forEach { title ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("•  ", style = MaterialTheme.typography.bodyLarge, color = scheme.primary)
                        RichText(title, style = MaterialTheme.typography.bodyLarge, keepHarakat = true)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(),
            ) { Text(stringResource(R.string.review_practice), style = MaterialTheme.typography.labelLarge) }
            OutlinedButton(onClick = onOpenLesson, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.review_open_lesson), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
