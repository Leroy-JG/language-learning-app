package io.github.leroyjg.arabe.review

import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.McqMode
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.Screen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.TypingAsk
import io.github.leroyjg.arabe.content.TypingScreen
import kotlin.random.Random

/** Un écran d'une séance de révision et la carte qu'il fait réviser. */
data class ReviewExercise(val screen: Screen, val cardKey: String)

/** Une carte à créer ou à réviser, avec sa note. */
data class GradedKey(val key: String, val kind: CardKind, val grade: Grade)

/** Une activité terminée : premier jour de réussite et meilleur score. */
data class Completion(val activityId: String, val day: Long, val bestScore: Double)

/** Comment les réponses deviennent des notes de révision. */
object ReviewRules {
    /** Note d'un point de grammaire selon la part de réponses justes du premier coup. */
    fun gradeForScore(score: Double): Grade = when {
        score >= 0.85 -> Grade.GOOD
        score >= 0.6 -> Grade.HARD
        else -> Grade.AGAIN
    }

    /** Note d'un mot révisé : raté au premier essai, juste avec une remarque, ou juste. */
    fun itemGrade(missed: Boolean, remark: Boolean): Grade = when {
        missed -> Grade.AGAIN
        remark -> Grade.HARD
        else -> Grade.GOOD
    }

    /**
     * Ce qu'une leçon terminée fait entrer en mémoire ou réviser :
     * - un mot présenté entre dans la mémoire (noté « difficile » s'il a été raté pendant la leçon) ;
     * - un mot déjà en mémoire et interrogé pendant la leçon est révisé (juste ou oublié) ;
     * - une leçon de grammaire crée ou révise son point de grammaire, noté selon le score.
     * [itemResults] : pour chaque élément interrogé, réussi du premier coup ou non ;
     * [known] : clés des cartes existantes.
     */
    fun afterActivity(
        course: Course,
        activity: Activity,
        itemResults: Map<String, Boolean>,
        score: Double,
        known: Set<String>,
    ): List<GradedKey> {
        val presented = activity.screens.filterIsInstance<PresentScreen>().map { it.item }.toSet()
        val out = mutableListOf<GradedKey>()
        for (id in (presented + itemResults.keys)) {
            if (course.item(id)?.review != true) continue
            val ok = itemResults[id]
            val grade = when {
                id !in known -> when (ok) {
                    false -> if (id in presented) Grade.HARD else Grade.AGAIN
                    else -> Grade.GOOD
                }
                ok == null -> continue
                ok -> Grade.GOOD
                else -> Grade.AGAIN
            }
            out += GradedKey(id, CardKind.ITEM, grade)
        }
        if (activity.file.type == ActivityType.GRAMMAR) out += GradedKey(activity.id, CardKind.GRAMMAR, gradeForScore(score))
        return out
    }

    /**
     * Cartes à créer pour des leçons terminées avant l'arrivée des révisions : chaque mot présenté
     * et chaque point de grammaire, au jour de la première réussite. Les cartes existantes restent intactes.
     */
    fun backfill(course: Course, completions: List<Completion>, known: Set<String>): List<Pair<GradedKey, Long>> {
        val added = known.toMutableSet()
        val out = mutableListOf<Pair<GradedKey, Long>>()
        for (completion in completions.sortedBy { it.day }) {
            val activity = course.activity(completion.activityId) ?: continue
            for (screen in activity.screens) {
                if (screen !is PresentScreen || course.item(screen.item)?.review != true) continue
                if (added.add(screen.item)) out += GradedKey(screen.item, CardKind.ITEM, Grade.GOOD) to completion.day
            }
            if (activity.file.type == ActivityType.GRAMMAR && added.add(activity.id)) {
                out += GradedKey(activity.id, CardKind.GRAMMAR, gradeForScore(completion.bestScore)) to completion.day
            }
        }
        return out
    }
}

/** Composition des séances de révision. */
class ReviewPlanner(
    private val course: Course,
    private val scheduler: ReviewScheduler = ReviewScheduler(),
    private val random: Random = Random.Default,
) {
    /** Points de grammaire : les activités de type `grammar`, dans l'ordre du cours. */
    val grammarTopics: List<Activity> = course.activities.filter { it.file.type == ActivityType.GRAMMAR }

    /** Les cartes dues d'abord (les plus effacées en premier), puis les plus fragiles. */
    fun prioritize(cards: List<ReviewCard>, today: Long): List<ReviewCard> {
        val (due, notDue) = cards.partition { scheduler.isDue(it, today) }
        return due.sortedBy { scheduler.retrievability(it, today) } +
            notDue.sortedBy { scheduler.retrievability(it, today) }
    }

    fun dueCount(cards: List<ReviewCard>, today: Long): Int = cards.count { scheduler.isDue(it, today) }

    fun masteryCounts(cards: List<ReviewCard>, today: Long): Map<Mastery, Int> =
        Mastery.entries.associateWith { m -> cards.count { scheduler.mastery(it, today) == m } }

    /** Cartes de mots encore présents dans le cours. */
    fun itemCards(cards: List<ReviewCard>): List<ReviewCard> =
        cards.filter { it.kind == CardKind.ITEM && course.item(it.key)?.review == true }

    /** Cartes de grammaire encore présentes dans le cours. */
    fun grammarCards(cards: List<ReviewCard>): List<ReviewCard> {
        val topics = grammarTopics.map { it.id }.toSet()
        return cards.filter { it.kind == CardKind.GRAMMAR && it.key in topics }
    }

    /** Point de grammaire le plus urgent à réviser (null si aucun n'a encore été étudié). */
    fun mostNeededTopic(cards: List<ReviewCard>, today: Long): Activity? =
        prioritize(grammarCards(cards), today).firstOrNull()?.let { course.activity(it.key) }

    /**
     * Séance de vocabulaire : jusqu'à [size] mots, choisis par priorité puis mélangés. Le type
     * d'exercice suit la solidité du souvenir : reconnaître (arabe → français) quand il est fragile,
     * retrouver l'arabe ensuite, l'écrire quand il est solide.
     */
    fun vocabularySession(cards: List<ReviewCard>, today: Long, size: Int, allowTyping: Boolean): List<ReviewExercise> =
        prioritize(itemCards(cards), today)
            .take(size)
            .shuffled(random)
            .mapIndexedNotNull { n, card -> vocabularyExercise(card, "review#${n + 1}", allowTyping) }

    private enum class Kind { AR_FR, FR_AR, AUDIO_AR, ICON_AR, TYPING }

    private fun vocabularyExercise(card: ReviewCard, screenId: String, allowTyping: Boolean): ReviewExercise? {
        val item = course.item(card.key) ?: return null
        val s = card.memory.stability
        val kinds = when {
            card.lastGrade == Grade.AGAIN || s < RECOGNITION_UNTIL -> listOfNotNull(Kind.AR_FR, Kind.ICON_AR.takeIf { item.icon != null })
            s < TYPING_FROM || !allowTyping -> listOfNotNull(Kind.FR_AR, Kind.AUDIO_AR, Kind.ICON_AR.takeIf { item.icon != null })
            else -> listOf(Kind.TYPING, Kind.TYPING, Kind.AUDIO_AR)
        }
        val kind = kinds.random(random)
        if (kind == Kind.TYPING) return ReviewExercise(TypingScreen(id = screenId, ask = TypingAsk.FR, item = item.id), item.id)
        val distractors = distractors(item, 3)
        if (distractors.size < 2) {
            return if (allowTyping) ReviewExercise(TypingScreen(id = screenId, ask = TypingAsk.FR, item = item.id), item.id) else null
        }
        val mode = when (kind) {
            Kind.AR_FR -> McqMode.AR_FR
            Kind.FR_AR -> McqMode.FR_AR
            Kind.AUDIO_AR -> McqMode.AUDIO_AR
            else -> McqMode.ICON_AR
        }
        return ReviewExercise(McqScreen(id = screenId, mode = mode, item = item.id, distractors = distractors.map { it.id }), item.id)
    }

    /**
     * Distracteurs d'un QCM de révision : même sorte (mot / phrase), d'abord du même chapitre, puis
     * du même niveau, sans doublon de traduction ni de texte arabe (même sans harakat).
     */
    internal fun distractors(target: Item, count: Int): List<Item> {
        val chapter = course.chapterOfItem(target.id)
        val sameKind = course.chapters.flatMap { it.items }.filter { it.kind == target.kind && it.id != target.id }
        val sameChapter = sameKind.filter { course.chapterOfItem(it.id) == chapter }.shuffled(random)
        val sameLevel = sameKind.filter {
            val c = course.chapterOfItem(it.id)
            c != chapter && c?.levelId == chapter?.levelId
        }.shuffled(random)
        val seenFr = mutableSetOf(target.fr)
        val seenAr = mutableSetOf(ArabicScript.lettersOnly(target.ar))
        val picked = mutableListOf<Item>()
        for (candidate in sameChapter + sameLevel) {
            if (picked.size >= count) break
            val letters = ArabicScript.lettersOnly(candidate.ar)
            if (candidate.fr in seenFr || letters in seenAr) continue
            seenFr += candidate.fr
            seenAr += letters
            picked += candidate
        }
        return picked
    }

    /**
     * Séance de grammaire sur un point : jusqu'à [size] exercices de sa leçon, mélangés, précédés du
     * rappel de la règle (ses astuces) si [withReminder].
     */
    fun grammarSession(topic: Activity, withReminder: Boolean, size: Int = GRAMMAR_SESSION_SIZE): List<ReviewExercise> {
        val exercises = topic.screens.filterIsInstance<Exercise>().filter { it.context?.dialogue == null }
        val reminder = if (withReminder) topic.screens.filterIsInstance<TipScreen>() else emptyList()
        return (reminder + exercises.shuffled(random).take(size)).map { ReviewExercise(it, topic.id) }
    }

    companion object {
        /** En dessous de cette stabilité (jours), on reconnaît le mot plutôt que de le retrouver. */
        const val RECOGNITION_UNTIL = 4.0

        /** À partir de cette stabilité (jours), on écrit le mot. */
        const val TYPING_FROM = 15.0

        const val VOCABULARY_SESSION_SIZE = 12
        const val GRAMMAR_SESSION_SIZE = 8
    }
}
