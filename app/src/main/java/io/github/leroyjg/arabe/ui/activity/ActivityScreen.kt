@file:OptIn(ExperimentalLayoutApi::class)

package io.github.leroyjg.arabe.ui.activity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.arabic.AnswerMatcher.Verdict
import io.github.leroyjg.arabe.audio.PhoneSpeech
import io.github.leroyjg.arabe.audio.SoundEffects
import io.github.leroyjg.arabe.audio.rememberHaptics
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.isGraded
import io.github.leroyjg.arabe.data.ProgressRepository
import io.github.leroyjg.arabe.data.ReviewRepository
import io.github.leroyjg.arabe.data.Settings
import io.github.leroyjg.arabe.engine.PreparedBuild
import io.github.leroyjg.arabe.engine.PreparedFillGap
import io.github.leroyjg.arabe.engine.PreparedMatch
import io.github.leroyjg.arabe.engine.PreparedMcq
import io.github.leroyjg.arabe.engine.PreparedMemory
import io.github.leroyjg.arabe.engine.PreparedMulti
import io.github.leroyjg.arabe.engine.PreparedOrder
import io.github.leroyjg.arabe.engine.PreparedQuestion
import io.github.leroyjg.arabe.engine.PreparedSort
import io.github.leroyjg.arabe.engine.PreparedSpot
import io.github.leroyjg.arabe.engine.PreparedTrueFalse
import io.github.leroyjg.arabe.engine.PreparedTyping
import io.github.leroyjg.arabe.engine.TestScoring
import io.github.leroyjg.arabe.review.CardKind
import io.github.leroyjg.arabe.review.Grade
import io.github.leroyjg.arabe.review.GradedKey
import io.github.leroyjg.arabe.review.Mastery
import io.github.leroyjg.arabe.review.ReviewCard
import io.github.leroyjg.arabe.review.ReviewExercise
import io.github.leroyjg.arabe.review.ReviewPlanner
import io.github.leroyjg.arabe.review.ReviewRules
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.review.VocabularyFilter
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.LocalReadingPrefs
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** Types de séances de révision (route de navigation). */
object ReviewKind {
    const val VOCABULARY = "vocabulaire"
    const val GRAMMAR = "grammaire"
}

/** Une leçon du parcours, écran par écran, à la Busuu. */
@Composable
fun ActivityScreen(activityId: String, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        WithContent { ready ->
            val activity = ready.course.activity(activityId)
            if (activity == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.activity_not_found)) }
                return@WithContent
            }
            val container = LocalAppContainer.current
            val vm: PlayerViewModel = viewModel(key = "lecon-$activityId") {
                PlayerViewModel(ready.course, ready.index, activity.screens, SessionKind.Lesson(activity)) { r ->
                    container.progress.record(
                        activityId = activity.id,
                        score = r.score,
                        firstTryCorrect = r.firstTryCorrect,
                        exerciseCount = r.exerciseCount,
                        xp = r.xp,
                        passed = r.passed,
                    )
                    container.review.afterActivity(ready.course, activity, r.itemResults, r.score)
                }
            }
            // Dans un quiz noté, les voyelles sont toujours affichées : beaucoup de questions portent dessus.
            val prefs = LocalReadingPrefs.current
            val shown = if (activity.file.type.isGraded && prefs.hideHarakat) prefs.copy(hideHarakat = false) else prefs
            CompositionLocalProvider(LocalReadingPrefs provides shown) {
                Player(vm, Playback(ready.course, container.audio), onClose)
            }
        }
    }
}

/** Séance de révision : vocabulaire (avec un filtre facultatif) ou grammaire (un point, ou le plus urgent). */
@Composable
fun ReviewSessionScreen(kind: String, topicId: String?, filter: String?, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        WithContent { ready ->
            val container = LocalAppContainer.current
            val cards by container.review.cards.collectAsStateWithLifecycle(initialValue = null)
            val saved by container.review.saved.collectAsStateWithLifecycle(initialValue = null)
            val all = cards
            val favorites = saved
            if (all == null || favorites == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@WithContent
            }
            val course = ready.course
            val review = container.review
            // La séance est composée une fois, à l'ouverture, et garde ses exercices même si l'écran tourne.
            val holder: ReviewPlanHolder = viewModel(key = "plan-$kind-$topicId-$filter") {
                ReviewPlanHolder(planReview(course, review, container.settings.current, kind, topicId, filter, all, favorites))
            }
            val plan = holder.plan
            if (plan == null || plan.second.none { it.screen is Exercise }) {
                NothingToReview(onClose)
                return@WithContent
            }
            val (sessionKind, exercises) = plan
            val vm: PlayerViewModel = viewModel(key = "revision-$kind-$topicId-$filter") {
                PlayerViewModel(course, ready.index, exercises.map { it.screen }, sessionKind) { r ->
                    val graded = when (sessionKind) {
                        is SessionKind.GrammarReview -> listOf(GradedKey(sessionKind.topic.id, CardKind.GRAMMAR, ReviewRules.gradeForScore(r.score)))
                        else -> exercises.filter { it.screen is Exercise }.map { ex ->
                            GradedKey(
                                ex.cardKey,
                                CardKind.ITEM,
                                ReviewRules.itemGrade(missed = ex.screen.id in r.missedScreens, remark = ex.screen.id in r.remarkScreens),
                            )
                        }
                    }
                    review.afterReview(graded)
                    container.progress.record(
                        activityId = ProgressRepository.REVIEW_PREFIX + (if (sessionKind is SessionKind.GrammarReview) sessionKind.topic.id else ReviewKind.VOCABULARY),
                        score = r.score,
                        firstTryCorrect = r.firstTryCorrect,
                        exerciseCount = r.exerciseCount,
                        xp = r.xp,
                        passed = true,
                    )
                }
            }
            Player(vm, Playback(course, container.audio), onClose)
        }
    }
}

/** Garde la séance de révision composée à l'ouverture. */
class ReviewPlanHolder(val plan: Pair<SessionKind, List<ReviewExercise>>?) : ViewModel()

private fun planReview(
    course: Course,
    review: ReviewRepository,
    settings: Settings,
    kind: String,
    topicId: String?,
    filter: String?,
    all: List<ReviewCard>,
    favorites: Set<String>,
): Pair<SessionKind, List<ReviewExercise>>? {
    val planner = ReviewPlanner(course, review.scheduler)
    val today = review.today()
    if (kind == ReviewKind.GRAMMAR) {
        val topic = topicId?.let { course.activity(it) } ?: planner.mostNeededTopic(all, today) ?: return null
        val card = planner.grammarCards(all).firstOrNull { it.key == topic.id }
        // Point fragile ou jamais révisé : on rappelle la règle avant les exercices.
        val weak = card == null || review.scheduler.mastery(card, today) == Mastery.WEAK
        return SessionKind.GrammarReview(topic) to planner.grammarSession(topic, withReminder = weak)
    }
    val itemCards = planner.itemCards(all)
    val wanted = VocabularyFilter.mastery(filter)
    val chosen = when {
        filter == VocabularyFilter.SAVED -> {
            val byKey = itemCards.associateBy { it.key }
            favorites.filter { course.item(it)?.review == true }
                .map { byKey[it] ?: review.scheduler.create(it, CardKind.ITEM, Grade.GOOD, today) }
        }
        wanted != null -> itemCards.filter { review.scheduler.mastery(it, today) == wanted }
        else -> itemCards
    }
    return SessionKind.VocabularyReview to planner.vocabularySession(chosen, today, settings.reviewSize, settings.typingInReview)
}

@Composable
private fun NothingToReview(onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Image(painterResource(R.drawable.icon_books), contentDescription = null, modifier = Modifier.size(96.dp))
        Text(stringResource(R.string.review_nothing), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Button(onClick = onClose, shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.back)) }
    }
}

/** Le lecteur commun aux leçons et aux révisions. */
@Composable
private fun Player(vm: PlayerViewModel, playback: Playback, onClose: () -> Unit) {
    val audio = playback.audio
    DisposableEffect(Unit) { onDispose { audio.stop() } }

    val result = vm.result
    if (result != null) {
        EndScreen(result, onContinue = onClose, onRetry = { vm.restart() })
        return
    }

    var confirmQuit by rememberSaveable { mutableStateOf(false) }
    BackHandler { confirmQuit = true }
    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text(stringResource(R.string.quit_title)) },
            text = { Text(stringResource(R.string.quit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmQuit = false
                    onClose()
                }) { Text(stringResource(R.string.quit_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.quit_cancel)) } },
        )
    }

    // Le haut et les côtés ici ; le bas est géré par la zone des boutons (clavier compris).
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
        PlayerTopBar(progress = vm.progress, combo = vm.combo, onClose = { confirmQuit = true })
        key(vm.turn) {
            Column(
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                if (vm.isRetry) {
                    Text(
                        stringResource(R.string.retry_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.remark,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                ScreenBody(vm, playback)
                Spacer(Modifier.height(24.dp))
            }
        }
        BottomArea(vm, playback)
    }
    NoVoiceWarning(audio.speechStatus)
}

@Composable
private fun PlayerTopBar(progress: Float, combo: Int, onClose: () -> Unit) {
    val animated by animateFloatAsState(progress, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow), label = "avancement")
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close)) }
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier.weight(1f).height(12.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        ComboSlot(combo)
    }
}

/** Place de la série à droite de la barre (fonction à part : hors de la portée de la ligne). */
@Composable
private fun ComboSlot(combo: Int) {
    Box(Modifier.width(64.dp), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = combo >= 2, enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn(), exit = scaleOut() + fadeOut()) {
            ComboBadge(combo)
        }
    }
}

/** Série de bonnes réponses d'affilée (à partir de 2). */
@Composable
private fun ComboBadge(combo: Int) {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(combo) {
        pulse.snapTo(1.25f)
        pulse.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
    }
    Row(
        Modifier.scale(pulse.value)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔥", fontSize = 15.sp)
        Spacer(Modifier.width(2.dp))
        Text("$combo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun ScreenBody(vm: PlayerViewModel, playback: Playback) {
    val screen = vm.screen ?: return
    when (val p = vm.prepared) {
        is PreparedMcq -> McqContent(p, vm, playback)
        is PreparedMatch -> MatchContent(p, vm, playback)
        is PreparedFillGap -> FillGapContent(p, vm, playback)
        is PreparedOrder -> OrderContent(p, vm, playback)
        is PreparedTyping -> TypingContent(p, vm, playback)
        is PreparedTrueFalse -> TrueFalseContent(p, vm, playback)
        is PreparedQuestion -> QuestionContent(p, vm, playback)
        is PreparedMulti -> MultiContent(p, vm, playback)
        is PreparedSort -> SortContent(p, vm, playback)
        is PreparedSpot -> SpotContent(p, vm, playback)
        is PreparedBuild -> BuildContent(p, vm, playback)
        is PreparedMemory -> MemoryContent(p, vm, playback)
        null -> when (screen) {
            is PresentScreen -> playback.course.item(screen.item)?.let { item ->
                val container = LocalAppContainer.current
                val saved by container.review.saved.collectAsStateWithLifecycle(initialValue = emptySet())
                PresentContent(
                    item = item,
                    playback = playback,
                    turn = vm.turn,
                    saved = item.id in saved,
                    onToggleSaved = { container.review.setSaved(item.id, item.id !in saved) },
                )
            }
            is TipScreen -> TipContent(screen, playback)
            is DialogueScreen -> DialogueContent(screen, playback)
            else -> Unit
        }
    }
}

@Composable
private fun BottomArea(vm: PlayerViewModel, playback: Playback) {
    val feedback = vm.feedback
    val autoPlay = LocalReadingPrefs.current.autoPlay
    val sounds = LocalAppContainer.current.sounds
    val haptics = rememberHaptics()
    LaunchedEffect(feedback) {
        if (feedback == null) return@LaunchedEffect
        if (feedback.verdict == Verdict.WRONG) {
            sounds.play(SoundEffects.Effect.WRONG)
            haptics.error()
        } else {
            sounds.play(SoundEffects.Effect.CORRECT)
            haptics.success()
        }
        // La réponse est lue après le bruitage, pour ne pas se superposer.
        if (autoPlay) feedback.sound?.let {
            delay(450)
            playback.play(it)
        }
    }
    Box(Modifier.fillMaxWidth().imePadding()) {
        if (feedback == null) {
            val exercise = vm.prepared != null
            // Les jeux qui se corrigent au fil des gestes n'ont pas de bouton « Vérifier ».
            val selfChecking = vm.prepared is PreparedMatch || vm.prepared is PreparedSort || vm.prepared is PreparedMemory
            if (!selfChecking) {
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
                    BigButton(
                        text = stringResource(if (exercise) R.string.check else R.string.continue_),
                        enabled = !exercise || vm.canCheck,
                        onClick = { if (exercise) vm.check() else vm.next() },
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = feedback != null,
            enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(),
        ) {
            feedback?.let { FeedbackPanel(it, vm.turn, playback, onContinue = { vm.next() }) }
        }
    }
}

@Composable
private fun BigButton(text: String, enabled: Boolean, onClick: () -> Unit, color: Color? = null) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = if (color != null) ButtonDefaults.buttonColors(containerColor = color, contentColor = MaterialTheme.colorScheme.surface) else ButtonDefaults.buttonColors(),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
private fun FeedbackPanel(feedback: Feedback, turn: Int, playback: Playback, onContinue: () -> Unit) {
    val colors = AppTheme.colors
    val (accent, background) = when (feedback.verdict) {
        Verdict.CORRECT -> colors.correct to colors.correctContainer
        Verdict.CORRECT_WITH_REMARK -> colors.remark to colors.remarkContainer
        Verdict.WRONG -> colors.wrong to colors.wrongContainer
    }
    val praise = stringArrayResource(R.array.praise)
    val title = when {
        feedback.matchMistakes > 0 -> pluralStringResource(R.plurals.feedback_match_mistakes, feedback.matchMistakes, feedback.matchMistakes)
        feedback.verdict == Verdict.CORRECT -> praise[turn.mod(praise.size)]
        feedback.verdict == Verdict.CORRECT_WITH_REMARK -> stringResource(R.string.feedback_remark)
        else -> stringResource(R.string.feedback_wrong)
    }
    val iconScale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { iconScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)) }
    Column(
        Modifier.fillMaxWidth()
            .background(background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).scale(iconScale.value).background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (feedback.verdict == Verdict.WRONG) Icons.Filled.Close else Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = accent, modifier = Modifier.weight(1f))
        }
        if (feedback.combo == 3 || (feedback.combo >= 5 && feedback.combo % 5 == 0)) {
            Text(
                stringResource(R.string.feedback_combo, feedback.combo),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        feedback.remarks.forEach { Text(stringResource(it.label()), color = accent, style = MaterialTheme.typography.bodyMedium) }
        feedback.trueFalseAnswer?.let {
            Text(stringResource(if (it) R.string.feedback_it_was_true else R.string.feedback_it_was_false), style = MaterialTheme.typography.bodyMedium)
        }
        if (feedback.answerAr != null || feedback.answerFr != null) {
            if (feedback.verdict != Verdict.CORRECT) {
                Text(stringResource(R.string.feedback_answer), style = MaterialTheme.typography.labelLarge, color = accent)
            }
            feedback.answerAr?.let { ar ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    feedback.sound?.let { s -> SpeakerButton(onClick = { playback.play(s) }, size = 38.dp) }
                    Spacer(Modifier.width(10.dp))
                    ArabicText(ar, size = 26.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
            }
            feedback.answerFr?.let { RichText(it, style = MaterialTheme.typography.bodyLarge) }
        }
        // L'explication parle souvent des signes eux-mêmes : on les montre toujours.
        feedback.explain?.let { RichText(it, style = MaterialTheme.typography.bodyMedium, keepHarakat = true) }
        Spacer(Modifier.height(4.dp))
        BigButton(stringResource(R.string.continue_), enabled = true, onClick = onContinue, color = accent)
    }
}

@Composable
private fun EndScreen(result: ActivityResult, onContinue: () -> Unit, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val failedQuiz = (result.isQuiz || result.isGraded) && !result.passed
    val sounds = LocalAppContainer.current.sounds
    val haptics = rememberHaptics()
    LaunchedEffect(Unit) {
        if (!failedQuiz) {
            sounds.play(SoundEffects.Effect.COMPLETE)
            haptics.success()
        }
    }
    val trophyScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) { trophyScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)) }
    val xp = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(350)
        xp.animateTo(result.xp.toFloat(), tween(900))
    }
    val title = when {
        result.kind is SessionKind.VocabularyReview || result.kind is SessionKind.GrammarReview -> R.string.end_review_title
        result.isExam -> if (result.passed) R.string.end_exam_passed else R.string.end_exam_failed
        result.isGraded -> if (result.passed) R.string.end_test_passed else R.string.end_test_failed
        !result.isQuiz -> R.string.end_title
        result.passed -> R.string.end_quiz_passed
        else -> R.string.end_quiz_failed
    }
    val encouragement = when {
        failedQuiz && result.isGraded -> R.string.end_test_failed_hint
        failedQuiz -> R.string.end_quiz_failed_hint
        result.isGraded -> R.string.end_test_passed_hint
        result.score >= 0.9 -> R.string.end_great
        result.score >= 0.7 -> R.string.end_good
        else -> R.string.end_keep_going
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.size(150.dp).scale(trophyScale.value)
                    .background(if (failedQuiz) colors.remarkContainer else scheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(if (failedQuiz) R.drawable.ui_flag else R.drawable.ui_trophy),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
            }
            Text(
                stringResource(title),
                style = MaterialTheme.typography.headlineMedium,
                color = if (failedQuiz) colors.wrong else colors.correct,
                textAlign = TextAlign.Center,
            )
            Text(stringResource(encouragement), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(stringResource(R.string.end_stat_xp), "+${xp.value.toInt()}", scheme.secondary, Modifier.weight(1f))
                if (result.exerciseCount > 0) {
                    if (result.isGraded) {
                        StatTile(
                            stringResource(R.string.end_stat_mark),
                            stringResource(R.string.end_mark_value, TestScoring.format(result.mark)),
                            if (result.passed) colors.correct else colors.wrong,
                            Modifier.weight(1f),
                        )
                    } else {
                        StatTile(stringResource(R.string.end_stat_accuracy), "${(result.score * 100).toInt()} %", colors.correct, Modifier.weight(1f))
                    }
                    StatTile(stringResource(R.string.end_stat_streak), "🔥 ${result.bestCombo}", colors.dialogue, Modifier.weight(1f))
                }
            }
            if (result.exerciseCount > 0) {
                Text(
                    stringResource(if (result.isGraded) R.string.end_test_score else R.string.end_score, result.firstTryCorrect, result.exerciseCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            if (result.isQuiz && result.passed) {
                Text(stringResource(R.string.end_chapter_done), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
            if (result.isExam && result.passed) {
                Text(stringResource(R.string.end_exam_done), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
            if (result.items.isNotEmpty()) {
                Text(
                    stringResource(if (result.kind is SessionKind.VocabularyReview) R.string.end_words_reviewed else R.string.end_words),
                    style = MaterialTheme.typography.titleMedium,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.items.forEach { item ->
                        Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                ArabicText(item.ar, size = 22.sp)
                                RichText(item.fr, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (failedQuiz) {
                BigButton(
                    stringResource(if (result.isGraded) R.string.end_retry_test else R.string.end_retry_quiz),
                    enabled = true,
                    onClick = onRetry,
                )
                OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.continue_))
                }
            } else {
                BigButton(stringResource(R.string.continue_), enabled = true, onClick = onContinue)
            }
        }
        if (!failedQuiz) {
            Confetti(
                colors = listOf(scheme.primary, scheme.secondary, colors.vocabulary, colors.dialogue, colors.grammar, colors.correct),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(18.dp)).padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NoVoiceWarning(status: PhoneSpeech.Status) {
    var dismissed by rememberSaveable { mutableStateOf(false) }
    if (status == PhoneSpeech.Status.NO_ARABIC_VOICE && !dismissed) {
        AlertDialog(
            onDismissRequest = { dismissed = true },
            text = { Text(stringResource(R.string.no_arabic_voice)) },
            confirmButton = { TextButton(onClick = { dismissed = true }) { Text(stringResource(android.R.string.ok)) } },
        )
    }
}
