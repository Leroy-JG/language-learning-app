@file:OptIn(ExperimentalLayoutApi::class)

package io.github.leroyjg.arabe.ui.activity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.arabic.AnswerMatcher.Verdict
import io.github.leroyjg.arabe.audio.PhoneSpeech
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.engine.PreparedFillGap
import io.github.leroyjg.arabe.engine.PreparedMatch
import io.github.leroyjg.arabe.engine.PreparedMcq
import io.github.leroyjg.arabe.engine.PreparedOrder
import io.github.leroyjg.arabe.engine.PreparedQuestion
import io.github.leroyjg.arabe.engine.PreparedTrueFalse
import io.github.leroyjg.arabe.engine.PreparedTyping
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.WithContent
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.LocalReadingPrefs
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme

/** Déroulé d'une activité, écran par écran, à la Busuu. */
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
            val vm: PlayerViewModel = viewModel(key = activityId) {
                PlayerViewModel(ready.course, ready.index, activity) { r ->
                    container.progress.record(
                        activityId = activity.id,
                        score = r.score,
                        firstTryCorrect = r.firstTryCorrect,
                        exerciseCount = r.exerciseCount,
                        xp = r.xp,
                        passed = r.passed,
                    )
                }
            }
            val audio = container.audio
            val playback = remember(ready.course) { Playback(ready.course, audio) }
            DisposableEffect(Unit) { onDispose { audio.stop() } }

            val result = vm.result
            if (result != null) {
                EndScreen(result, onContinue = onClose, onRetry = { vm.restart() })
                return@WithContent
            }

            var confirmQuit by rememberSaveable { mutableStateOf(false) }
            BackHandler { confirmQuit = true }
            if (confirmQuit) {
                AlertDialog(
                    onDismissRequest = { confirmQuit = false },
                    title = { Text(stringResource(R.string.quit_title)) },
                    text = { Text(stringResource(R.string.quit_message)) },
                    confirmButton = { TextButton(onClick = { confirmQuit = false; onClose() }) { Text(stringResource(R.string.quit_confirm)) } },
                    dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.quit_cancel)) } },
                )
            }

            // Le haut et les côtés ici ; le bas est géré par la zone des boutons (clavier compris).
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
                PlayerTopBar(progress = vm.progress, onClose = { confirmQuit = true })
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
    }
}

@Composable
private fun PlayerTopBar(progress: Float, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close)) }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(10.dp),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Spacer(Modifier.width(16.dp))
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
        null -> when (screen) {
            is PresentScreen -> playback.course.item(screen.item)?.let { PresentContent(it, playback, vm.turn) }
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
    LaunchedEffect(feedback) {
        if (feedback != null && autoPlay) feedback.sound?.let { playback.play(it) }
    }
    Box(Modifier.fillMaxWidth().imePadding()) {
        if (feedback == null) {
            val exercise = vm.prepared != null
            if (vm.prepared !is PreparedMatch) {
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
                    BigButton(
                        text = stringResource(if (exercise) R.string.check else R.string.continue_),
                        enabled = !exercise || vm.canCheck,
                        onClick = { if (exercise) vm.check() else vm.next() },
                    )
                }
            }
        }
        AnimatedVisibility(visible = feedback != null, enter = slideInVertically { it }) {
            feedback?.let { FeedbackPanel(it, playback, onContinue = { vm.next() }) }
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
private fun FeedbackPanel(feedback: Feedback, playback: Playback, onContinue: () -> Unit) {
    val colors = AppTheme.colors
    val (accent, background) = when (feedback.verdict) {
        Verdict.CORRECT -> colors.correct to colors.correctContainer
        Verdict.CORRECT_WITH_REMARK -> colors.remark to colors.remarkContainer
        Verdict.WRONG -> colors.wrong to colors.wrongContainer
    }
    val title = when {
        feedback.matchMistakes > 0 -> stringResource(R.string.feedback_match_mistakes, feedback.matchMistakes)
        feedback.verdict == Verdict.CORRECT -> stringResource(R.string.feedback_correct)
        feedback.verdict == Verdict.CORRECT_WITH_REMARK -> stringResource(R.string.feedback_remark)
        else -> stringResource(R.string.feedback_wrong)
    }
    Column(
        Modifier.fillMaxWidth()
            .background(background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = accent)
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
        feedback.explain?.let { RichText(it, style = MaterialTheme.typography.bodyMedium) }
        Spacer(Modifier.height(4.dp))
        BigButton(stringResource(R.string.continue_), enabled = true, onClick = onContinue, color = accent)
    }
}

@Composable
private fun EndScreen(result: ActivityResult, onContinue: () -> Unit, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    val failedQuiz = result.type == ActivityType.QUIZ && !result.passed
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(
                when {
                    result.type != ActivityType.QUIZ -> R.string.end_title
                    result.passed -> R.string.end_quiz_passed
                    else -> R.string.end_quiz_failed
                },
            ),
            style = MaterialTheme.typography.headlineMedium,
            color = if (failedQuiz) colors.wrong else colors.correct,
            textAlign = TextAlign.Center,
        )
        if (result.exerciseCount > 0) {
            Text(
                "${(result.score * 100).toInt()} %",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(stringResource(R.string.end_score, result.firstTryCorrect, result.exerciseCount), style = MaterialTheme.typography.titleMedium)
        }
        if (failedQuiz) Text(stringResource(R.string.end_quiz_failed_hint), textAlign = TextAlign.Center)
        if (result.type == ActivityType.QUIZ && result.passed) {
            Text(stringResource(R.string.end_next_chapter), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
        Text(stringResource(R.string.end_xp, result.xp), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        if (result.items.isNotEmpty()) {
            Text(stringResource(R.string.end_words), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.items.forEach { item ->
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
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
            BigButton(stringResource(R.string.end_retry_quiz), enabled = true, onClick = onRetry)
            OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.continue_))
            }
        } else {
            BigButton(stringResource(R.string.continue_), enabled = true, onClick = onContinue)
        }
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
