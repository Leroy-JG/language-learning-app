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
import io.github.leroyjg.arabe.content.BuildScreen
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.MemoryScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.Screen
import io.github.leroyjg.arabe.content.SortScreen
import io.github.leroyjg.arabe.content.SpotScreen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.TrueFalseScreen
import io.github.leroyjg.arabe.content.TypingScreen
import io.github.leroyjg.arabe.engine.ActivitySession
import io.github.leroyjg.arabe.engine.ExerciseFactory
import io.github.leroyjg.arabe.engine.Outcome
import io.github.leroyjg.arabe.engine.PreparedBuild
import io.github.leroyjg.arabe.engine.PreparedExercise
import io.github.leroyjg.arabe.engine.PreparedFillGap
import io.github.leroyjg.arabe.engine.PreparedMatch
import io.github.leroyjg.arabe.engine.PreparedMcq
import io.github.leroyjg.arabe.engine.PreparedMemory
import io.github.leroyjg.arabe.engine.PreparedOrder
import io.github.leroyjg.arabe.engine.PreparedQuestion
import io.github.leroyjg.arabe.engine.PreparedSort
import io.github.leroyjg.arabe.engine.PreparedSpot
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
    /** Association ou rangement terminé avec des erreurs (nombre d'erreurs). */
    val matchMistakes: Int = 0,
    /** Vrai ou faux : la bonne réponse, à rappeler. */
    val trueFalseAnswer: Boolean? = null,
    /** Bonnes réponses d'affilée, celle-ci comprise (0 si la réponse est fausse). */
    val combo: Int = 0,
)

/** Ce que joue le lecteur : une leçon du parcours, ou une séance de révision. */
sealed interface SessionKind {
    data class Lesson(val activity: Activity) : SessionKind
    data object VocabularyReview : SessionKind
    data class GrammarReview(val topic: Activity) : SessionKind
}

/** Bilan affiché à la fin d'une activité ou d'une révision. */
data class ActivityResult(
    val kind: SessionKind,
    val exerciseCount: Int,
    val firstTryCorrect: Int,
    val score: Double,
    val passed: Boolean,
    val xp: Int,
    /** Mots vus (leçon) ou révisés (révision de vocabulaire). */
    val items: List<Item>,
    /** Exercices ratés au premier essai. */
    val missedScreens: Set<String>,
    /** Exercices justes au premier essai, mais avec une remarque. */
    val remarkScreens: Set<String>,
    /** Pour chaque mot interrogé : réussi du premier coup ou non. */
    val itemResults: Map<String, Boolean>,
    /** Meilleure série de bonnes réponses d'affilée. */
    val bestCombo: Int,
) {
    val isQuiz: Boolean get() = (kind as? SessionKind.Lesson)?.activity?.file?.type == ActivityType.QUIZ
}

/**
 * État d'une séance en cours. Il survit à la rotation de l'écran ; si Android ferme l'app,
 * la séance recommence proprement au début.
 */
class PlayerViewModel(
    private val course: Course,
    index: CourseIndex,
    private val screens: List<Screen>,
    val kind: SessionKind,
    /** Appelé une fois à chaque fin de séance (enregistrement de la progression). */
    private val onFinished: (ActivityResult) -> Unit,
) : ViewModel() {
    private val factory = ExerciseFactory(course, index)
    private var session = ActivitySession(screens)
    private var pendingOutcome: Outcome = Outcome.VIEWED
    private val itemResults = mutableMapOf<String, Boolean>()
    private val remarkScreens = mutableSetOf<String>()
    private var bestCombo = 0

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

    /** Bonnes réponses d'affilée. */
    var combo by mutableIntStateOf(0)
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

    /** Rangement : catégorie choisie pour chaque carte déjà rangée (dans l'ordre des cartes). */
    val sortDone = mutableStateListOf<Int>()
    var sortMistakes by mutableIntStateOf(0)
        private set

    /** Catégorie touchée à tort (secouée un instant). */
    var sortWrong by mutableStateOf<Int?>(null)

    /** Repérer : mots sélectionnés. */
    val spotSelected = mutableStateListOf<Int>()

    /** Écrire un mot : étiquettes touchées, dans l'ordre. */
    val buildPicked = mutableStateListOf<Int>()

    /** Mémoire : cartes retournées pas encore appariées (au plus deux) et éléments trouvés. */
    val memoryOpen = mutableStateListOf<Int>()
    val memoryFound = mutableStateListOf<String>()

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
        sortDone.clear()
        sortMistakes = 0
        sortWrong = null
        spotSelected.clear()
        buildPicked.clear()
        memoryOpen.clear()
        memoryFound.clear()
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
        is SortScreen -> factory.sort(screen)
        is SpotScreen -> factory.spot(screen)
        is BuildScreen -> factory.build(screen)
        is MemoryScreen -> factory.memory(screen)
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
            is PreparedSpot -> spotSelected.isNotEmpty()
            is PreparedBuild -> buildPicked.isNotEmpty()
            is PreparedMatch, is PreparedSort, is PreparedMemory, null -> false
        }

    fun check() {
        if (!canCheck) return
        val fb = when (val p = prepared) {
            is PreparedMcq -> Feedback(
                verdict = verdict(p.check(choice!!)),
                answerAr = p.target.ar, answerFr = p.target.fr, sound = Sound.OfItem(p.target),
                explain = p.screen.explain,
            ).also { recordItem(p.target.id, it.verdict != AnswerMatcher.Verdict.WRONG) }
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
                item?.let { recordItem(it.id, r.verdict != AnswerMatcher.Verdict.WRONG) }
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
            is PreparedSpot -> Feedback(
                verdict = verdict(p.check(spotSelected.toList())),
                answerAr = p.sentence, answerFr = p.screen.fr, sound = Sound.OfText(p.sentence),
                explain = p.screen.explain,
            )
            is PreparedBuild -> Feedback(
                verdict = verdict(p.check(buildPicked.toList())),
                answerAr = p.target.ar, answerFr = p.target.fr, sound = Sound.OfItem(p.target),
                explain = p.screen.explain,
            ).also { recordItem(p.target.id, it.verdict != AnswerMatcher.Verdict.WRONG) }
            is PreparedMatch, is PreparedSort, is PreparedMemory, null -> return
        }
        val wrong = fb.verdict == AnswerMatcher.Verdict.WRONG
        if (!isRetry && fb.verdict == AnswerMatcher.Verdict.CORRECT_WITH_REMARK) screen?.let { remarkScreens += it.id }
        pendingOutcome = if (wrong) Outcome.WRONG else Outcome.CORRECT
        feedback = fb.copy(combo = updateCombo(!wrong))
    }

    private fun verdict(ok: Boolean) = if (ok) AnswerMatcher.Verdict.CORRECT else AnswerMatcher.Verdict.WRONG

    /** Résultat du premier essai sur un mot (les reprises ne comptent pas). */
    private fun recordItem(itemId: String, ok: Boolean) {
        if (isRetry) return
        itemResults[itemId] = (itemResults[itemId] ?: true) && ok
    }

    private fun updateCombo(ok: Boolean): Int {
        combo = if (ok) combo + 1 else 0
        bestCombo = maxOf(bestCombo, combo)
        return combo
    }

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
            recordItem(p.left[l].id, false)
        }
        matchLeft = null
        matchRight = null
        if (matchDone.size == p.left.size) {
            p.left.forEach { recordItem(it.id, true) }
            val clean = matchMistakes == 0
            pendingOutcome = if (clean) Outcome.CORRECT else Outcome.COMPLETED_WITH_MISTAKES
            feedback = Feedback(
                verdict = if (clean) AnswerMatcher.Verdict.CORRECT else AnswerMatcher.Verdict.CORRECT_WITH_REMARK,
                matchMistakes = matchMistakes,
                explain = p.screen.explain,
                combo = updateCombo(clean),
            )
        }
    }

    /** Rangement : range la carte du dessus dans la catégorie [category]. */
    fun tapSort(category: Int) {
        val p = prepared as? PreparedSort ?: return
        if (feedback != null) return
        val card = sortDone.size
        if (card >= p.cards.size) return
        val item = p.items[card]
        if (p.check(card, category)) {
            sortDone += category
            sortWrong = null
            item?.let { recordItem(it.id, true) }
        } else {
            sortMistakes++
            sortWrong = category
            item?.let { recordItem(it.id, false) }
        }
        if (sortDone.size == p.cards.size) {
            val clean = sortMistakes == 0
            pendingOutcome = if (clean) Outcome.CORRECT else Outcome.COMPLETED_WITH_MISTAKES
            feedback = Feedback(
                verdict = if (clean) AnswerMatcher.Verdict.CORRECT else AnswerMatcher.Verdict.CORRECT_WITH_REMARK,
                matchMistakes = sortMistakes,
                explain = p.screen.explain,
                combo = updateCombo(clean),
            )
        }
    }

    /** Repérer : sélectionne ou désélectionne un mot. */
    fun toggleSpot(word: Int) {
        if (feedback != null) return
        if (word in spotSelected) spotSelected.remove(word) else spotSelected.add(word)
    }

    /** Mémoire : retourne une carte. Renvoie vrai si elle complète une paire. */
    fun tapMemory(card: Int): Boolean {
        val p = prepared as? PreparedMemory ?: return false
        if (feedback != null || card in memoryOpen || memoryOpen.size >= 2) return false
        if (p.cards[card].item.id in memoryFound) return false
        memoryOpen += card
        if (memoryOpen.size < 2) return false
        val (a, b) = memoryOpen[0] to memoryOpen[1]
        if (!p.isPair(a, b)) return false
        memoryFound += p.cards[a].item.id
        memoryOpen.clear()
        if (memoryFound.size == p.pairCount) {
            // Un jeu de mémoire se finit toujours : retourner des cartes au hasard fait partie du jeu.
            pendingOutcome = Outcome.CORRECT
            feedback = Feedback(
                verdict = AnswerMatcher.Verdict.CORRECT,
                explain = p.screen.explain,
                combo = updateCombo(true),
            )
        }
        return true
    }

    /** Mémoire : referme les deux cartes qui ne vont pas ensemble. */
    fun closeMemory() {
        memoryOpen.clear()
    }

    /** Écran suivant (après la correction, ou pour un écran sans exercice). */
    fun next() {
        if (screen == null) return
        val outcome = if (prepared == null) Outcome.VIEWED else if (feedback == null) return else pendingOutcome
        session.advance(outcome)
        showCurrent()
    }

    fun restart() {
        session = ActivitySession(screens)
        itemResults.clear()
        remarkScreens.clear()
        combo = 0
        bestCombo = 0
        result = null
        showCurrent()
    }

    private fun finish() {
        val s = session
        val xp = s.firstTryCorrect * 10 + (s.exerciseCount - s.firstTryCorrect) * 5
        val isQuiz = (kind as? SessionKind.Lesson)?.activity?.file?.type == ActivityType.QUIZ
        val items = when (kind) {
            is SessionKind.Lesson -> screens.filterIsInstance<PresentScreen>().mapNotNull { course.item(it.item) }
            SessionKind.VocabularyReview -> screens.mapNotNull { sc ->
                when (sc) {
                    is McqScreen -> course.item(sc.item)
                    is TypingScreen -> sc.item?.let { course.item(it) }
                    else -> null
                }
            }
            is SessionKind.GrammarReview -> emptyList()
        }
        val r = ActivityResult(
            kind = kind,
            exerciseCount = s.exerciseCount,
            firstTryCorrect = s.firstTryCorrect,
            score = s.score,
            passed = !isQuiz || s.score >= QUIZ_PASS_SCORE,
            xp = xp,
            items = items,
            missedScreens = s.missedScreens.toSet(),
            remarkScreens = remarkScreens.toSet(),
            itemResults = itemResults.toMap(),
            bestCombo = bestCombo,
        )
        result = r
        onFinished(r)
    }
}
