package io.github.leroyjg.arabe.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.leroyjg.arabe.arabic.AnswerMatcher
import io.github.leroyjg.arabe.audio.Sound
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.Screen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.TrueFalseScreen
import io.github.leroyjg.arabe.content.TypingScreen
import io.github.leroyjg.arabe.engine.ActivitySession
import io.github.leroyjg.arabe.engine.ExerciseFactory
import io.github.leroyjg.arabe.engine.Outcome
import io.github.leroyjg.arabe.engine.PreparedExercise
import io.github.leroyjg.arabe.engine.PreparedFillGap
import io.github.leroyjg.arabe.engine.PreparedMatch
import io.github.leroyjg.arabe.engine.PreparedMcq
import io.github.leroyjg.arabe.engine.PreparedOrder
import io.github.leroyjg.arabe.engine.PreparedQuestion
import io.github.leroyjg.arabe.engine.PreparedTrueFalse
import io.github.leroyjg.arabe.engine.PreparedTyping
import io.github.leroyjg.arabe.engine.QUIZ_PASS_SCORE

/** Résultat affiché dans le panneau de correction. */
data class Feedback(
    val verdict: AnswerMatcher.Verdict,
    /** Réponse attendue en arabe (affichée et écoutable). */
    val answerAr: String? = null,
    /** Réponse attendue ou traduction en français. */
    val answerFr: String? = null,
    val sound: Sound? = null,
    val remarks: Set<AnswerMatcher.Remark> = emptySet(),
    val explain: String? = null,
    /** Association terminée avec des erreurs (nombre d'erreurs). */
    val matchMistakes: Int = 0,
    /** Vrai ou faux : la bonne réponse, à rappeler. */
    val trueFalseAnswer: Boolean? = null,
)

/** Bilan affiché à la fin d'une activité. */
data class ActivityResult(
    val type: ActivityType,
    val exerciseCount: Int,
    val firstTryCorrect: Int,
    val score: Double,
    val passed: Boolean,
    val xp: Int,
    val items: List<Item>,
)

/**
 * État d'une activité en cours. Il survit à la rotation de l'écran ; si Android ferme l'app,
 * l'activité recommence proprement au début.
 */
class PlayerViewModel(
    private val course: Course,
    index: CourseIndex,
    val activity: Activity,
    /** Appelé une fois à chaque fin d'activité (enregistrement de la progression). */
    private val onFinished: (ActivityResult) -> Unit,
) : ViewModel() {
    private val factory = ExerciseFactory(course, index)
    private var session = ActivitySession(activity.screens)
    private var pendingOutcome: Outcome = Outcome.VIEWED

    /** Change à chaque nouvel écran : sert de clé pour réinitialiser l'affichage. */
    var turn by mutableIntStateOf(0)
        private set
    var screen by mutableStateOf<Screen?>(null)
        private set
    var prepared by mutableStateOf<PreparedExercise?>(null)
        private set
    var isRetry by mutableStateOf(false)
        private set
    var progress by mutableFloatStateOf(0f)
        private set
    var feedback by mutableStateOf<Feedback?>(null)
        private set
    var result by mutableStateOf<ActivityResult?>(null)
        private set

    // Saisies de l'écran courant.
    var choice by mutableStateOf<Int?>(null)
    var typed by mutableStateOf("")
    var trueFalseChoice by mutableStateOf<Boolean?>(null)
    val gapFill = mutableStateListOf<String?>()
    val orderPicked = mutableStateListOf<Int>()
    var matchLeft by mutableStateOf<Int?>(null)
    var matchRight by mutableStateOf<Int?>(null)
    val matchDone = mutableStateListOf<String>()
    var matchMistakes by mutableIntStateOf(0)
        private set
    var matchWrong by mutableStateOf<Pair<Int, Int>?>(null)

    init {
        showCurrent()
    }

    private fun showCurrent() {
        val current = session.current
        screen = current
        isRetry = session.isRetry
        progress = session.progress
        feedback = null
        choice = null
        typed = ""
        trueFalseChoice = null
        orderPicked.clear()
        matchLeft = null
        matchRight = null
        matchDone.clear()
        matchMistakes = 0
        matchWrong = null
        prepared = current?.let { prepare(it) }
        gapFill.clear()
        (prepared as? PreparedFillGap)?.let { p -> repeat(p.gapCount) { gapFill.add(null) } }
        turn++
        if (current == null) finish()
    }

    private fun prepare(screen: Screen): PreparedExercise? = when (screen) {
        is McqScreen -> factory.mcq(screen)
        is MatchScreen -> factory.match(screen)
        is FillGapScreen -> factory.fillGap(screen)
        is OrderScreen -> factory.order(screen)
        is TypingScreen -> factory.typing(screen)
        is TrueFalseScreen -> factory.trueFalse(screen)
        is QuestionScreen -> factory.question(screen)
        is PresentScreen, is TipScreen, is DialogueScreen -> null
    }

    /** Le bouton « Vérifier » est-il actif ? */
    val canCheck: Boolean
        get() = feedback == null && when (prepared) {
            is PreparedMcq, is PreparedQuestion -> choice != null
            is PreparedTyping -> typed.isNotBlank()
            is PreparedTrueFalse -> trueFalseChoice != null
            is PreparedFillGap -> gapFill.all { it != null }
            is PreparedOrder -> orderPicked.isNotEmpty()
            is PreparedMatch, null -> false
        }

    fun check() {
        if (!canCheck) return
        val fb = when (val p = prepared) {
            is PreparedMcq -> Feedback(
                verdict = verdict(p.check(choice!!)),
                answerAr = p.target.ar, answerFr = p.target.fr, sound = Sound.OfItem(p.target),
                explain = p.screen.explain,
            )
            is PreparedQuestion -> {
                val right = p.screen.choices[p.screen.answer]
                Feedback(
                    verdict = verdict(p.check(choice!!)),
                    answerAr = right.ar, answerFr = right.fr,
                    sound = right.ar?.let { Sound.OfText(it) },
                    explain = p.screen.explain,
                )
            }
            is PreparedTrueFalse -> Feedback(
                verdict = verdict(p.check(trueFalseChoice!!)),
                answerAr = p.screen.statement.ar,
                answerFr = p.screen.statement.fr.takeIf { p.screen.statement.ar != null },
                sound = p.screen.statement.ar?.let { Sound.OfText(it) },
                explain = p.screen.explain,
                trueFalseAnswer = p.screen.answer,
            )
            is PreparedTyping -> {
                val r = p.check(typed)
                val item = p.screen.item?.let { course.item(it) }
                val sound = when {
                    item != null -> Sound.OfItem(item)
                    r.expected == p.answers.first() -> Sound.OfText(r.expected)
                    else -> null
                }
                Feedback(
                    verdict = r.verdict, answerAr = r.expected, answerFr = p.translation, sound = sound,
                    remarks = r.remarks, explain = p.screen.explain,
                )
            }
            is PreparedFillGap -> Feedback(
                verdict = verdict(p.check(gapFill.toList())),
                answerAr = p.fullSentence, answerFr = p.screen.fr, sound = Sound.OfText(p.fullSentence),
                explain = p.screen.explain,
            )
            is PreparedOrder -> Feedback(
                verdict = verdict(p.check(orderPicked.map { p.tokens[it] })),
                answerAr = p.fullSentence, answerFr = p.screen.fr, sound = Sound.OfText(p.fullSentence),
                explain = p.screen.explain,
            )
            is PreparedMatch, null -> return
        }
        pendingOutcome = if (fb.verdict == AnswerMatcher.Verdict.WRONG) Outcome.WRONG else Outcome.CORRECT
        feedback = fb
    }

    private fun verdict(ok: Boolean) = if (ok) AnswerMatcher.Verdict.CORRECT else AnswerMatcher.Verdict.WRONG

    /** Association : touche une case de gauche ou de droite. */
    fun tapMatch(left: Int?, right: Int?) {
        val p = prepared as? PreparedMatch ?: return
        if (feedback != null) return
        if (left != null) matchLeft = if (matchLeft == left) null else left
        if (right != null) matchRight = if (matchRight == right) null else right
        val l = matchLeft ?: return
        val r = matchRight ?: return
        if (p.isPair(p.left[l], p.right[r])) {
            matchDone += p.left[l].id
            matchWrong = null
        } else {
            matchMistakes++
            matchWrong = l to r
        }
        matchLeft = null
        matchRight = null
        if (matchDone.size == p.left.size) {
            val clean = matchMistakes == 0
            pendingOutcome = if (clean) Outcome.CORRECT else Outcome.COMPLETED_WITH_MISTAKES
            feedback = Feedback(
                verdict = if (clean) AnswerMatcher.Verdict.CORRECT else AnswerMatcher.Verdict.CORRECT_WITH_REMARK,
                matchMistakes = matchMistakes,
                explain = p.screen.explain,
            )
        }
    }

    /** Écran suivant (après la correction, ou pour un écran sans exercice). */
    fun next() {
        if (screen == null) return
        val outcome = if (prepared == null) Outcome.VIEWED else if (feedback == null) return else pendingOutcome
        session.advance(outcome)
        showCurrent()
    }

    fun restart() {
        session = ActivitySession(activity.screens)
        result = null
        showCurrent()
    }

    private fun finish() {
        val s = session
        val type = activity.file.type
        val xp = s.firstTryCorrect * 10 + (s.exerciseCount - s.firstTryCorrect) * 5
        val r = ActivityResult(
            type = type,
            exerciseCount = s.exerciseCount,
            firstTryCorrect = s.firstTryCorrect,
            score = s.score,
            passed = type != ActivityType.QUIZ || s.score >= QUIZ_PASS_SCORE,
            xp = xp,
            items = activity.screens.filterIsInstance<PresentScreen>().mapNotNull { course.item(it.item) },
        )
        result = r
        onFinished(r)
    }
}
