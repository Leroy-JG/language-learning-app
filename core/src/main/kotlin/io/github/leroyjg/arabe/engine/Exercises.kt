package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.arabic.AnswerMatcher
import io.github.leroyjg.arabe.content.Audio
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.TrueFalseScreen
import io.github.leroyjg.arabe.content.TypingScreen
import kotlin.random.Random

/** Exercice prêt à afficher : choix tirés et mélangés une fois pour toutes, et sa correction. */
sealed interface PreparedExercise

class PreparedMcq(val screen: McqScreen, val target: Item, val choices: List<Item>) : PreparedExercise {
    val correctIndex: Int = choices.indexOf(target)
    fun check(choice: Int): Boolean = choice == correctIndex
}

class PreparedMatch(val screen: MatchScreen, val left: List<Item>, val right: List<Item>) : PreparedExercise {
    fun isPair(leftItem: Item, rightItem: Item): Boolean = leftItem.id == rightItem.id
}

class PreparedFillGap(val screen: FillGapScreen, val parts: List<SentencePart>, val options: List<String>) : PreparedExercise {
    sealed interface SentencePart {
        data class Text(val text: String) : SentencePart
        data class Gap(val index: Int) : SentencePart
    }

    val gapCount: Int get() = screen.gaps.size
    val fullSentence: String get() = Audio.fillGapSentence(screen)

    /** [filled] : l'option placée dans chaque trou (null = trou vide). */
    fun check(filled: List<String?>): Boolean =
        filled.size == gapCount && filled.withIndex().all { (i, s) -> s != null && s in screen.gaps[i].answers }
}

class PreparedOrder(val screen: OrderScreen, val tokens: List<String>) : PreparedExercise {
    val fullSentence: String get() = Audio.orderSentence(screen)
    fun check(sequence: List<String>): Boolean = screen.answers.any { it == sequence }
}

class PreparedTyping(val screen: TypingScreen, val answers: List<String>, val translation: String) : PreparedExercise {
    fun check(input: String): AnswerMatcher.Result = AnswerMatcher.check(input, answers)
}

class PreparedTrueFalse(val screen: TrueFalseScreen) : PreparedExercise {
    fun check(answer: Boolean): Boolean = answer == screen.answer
}

class PreparedQuestion(val screen: QuestionScreen, val order: List<Int>) : PreparedExercise {
    /** Position affichée du bon choix. */
    val correctPosition: Int = order.indexOf(screen.answer)
    fun check(position: Int): Boolean = order.getOrNull(position) == screen.answer
}

/** Prépare les exercices : distracteurs, mélange des choix, découpage des phrases à trous. */
class ExerciseFactory(
    private val course: Course,
    private val index: CourseIndex,
    private val random: Random = Random.Default,
) {
    fun mcq(screen: McqScreen): PreparedMcq {
        val target = course.item(screen.item) ?: error("élément inconnu ${screen.item}")
        val distractors = screen.distractors?.mapNotNull { course.item(it) } ?: automaticDistractors(target, screen.id)
        return PreparedMcq(screen, target, (distractors + target).shuffled(random))
    }

    /** Deux distracteurs, pris d'abord dans la même activité, puis le chapitre, puis le niveau. */
    private fun automaticDistractors(target: Item, screenId: String, count: Int = 2): List<Item> {
        val picked = mutableListOf<Item>()
        for (tier in index.distractorTiers(target, screenId)) {
            if (picked.size >= count) break
            picked += tier.shuffled(random).take(count - picked.size)
        }
        return picked
    }

    fun match(screen: MatchScreen): PreparedMatch {
        val items = screen.items.mapNotNull { course.item(it) }
        val left = items.shuffled(random)
        var right = items.shuffled(random)
        // Évite que les deux colonnes soient alignées (associations trop évidentes).
        repeat(5) { if (right == left) right = items.shuffled(random) }
        return PreparedMatch(screen, left, right)
    }

    fun fillGap(screen: FillGapScreen): PreparedFillGap {
        val parts = mutableListOf<PreparedFillGap.SentencePart>()
        var last = 0
        for (m in Regex("\\{(\\d+)\\}").findAll(screen.sentence)) {
            if (m.range.first > last) parts += PreparedFillGap.SentencePart.Text(screen.sentence.substring(last, m.range.first))
            parts += PreparedFillGap.SentencePart.Gap(m.groupValues[1].toInt())
            last = m.range.last + 1
        }
        if (last < screen.sentence.length) parts += PreparedFillGap.SentencePart.Text(screen.sentence.substring(last))
        return PreparedFillGap(screen, parts, screen.options.shuffled(random))
    }

    fun order(screen: OrderScreen): PreparedOrder {
        val tokens = screen.answers.first() + screen.distractors.orEmpty()
        var shuffled = tokens.shuffled(random)
        repeat(5) { if (screen.answers.any { it == shuffled.take(it.size) }) shuffled = tokens.shuffled(random) }
        return PreparedOrder(screen, shuffled)
    }

    fun typing(screen: TypingScreen): PreparedTyping {
        val item = screen.item?.let { course.item(it) }
        val answers = item?.let { listOf(it.ar) } ?: screen.answers.orEmpty()
        val translation = item?.fr ?: screen.fr.orEmpty()
        return PreparedTyping(screen, answers, translation)
    }

    fun trueFalse(screen: TrueFalseScreen) = PreparedTrueFalse(screen)

    fun question(screen: QuestionScreen): PreparedQuestion {
        val indices = screen.choices.indices.toList()
        return PreparedQuestion(screen, if (screen.shuffle) indices.shuffled(random) else indices)
    }
}
