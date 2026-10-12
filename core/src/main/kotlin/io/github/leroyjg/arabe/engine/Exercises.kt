package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.arabic.AnswerMatcher
import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.content.Audio
import io.github.leroyjg.arabe.content.BuildScreen
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.MemoryMode
import io.github.leroyjg.arabe.content.MemoryScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.SortCard
import io.github.leroyjg.arabe.content.SortScreen
import io.github.leroyjg.arabe.content.SpotScreen
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

/** Cartes à ranger, dans un ordre mélangé ; l'élément éventuel de chaque carte. */
class PreparedSort(val screen: SortScreen, val cards: List<SortCard>, val items: List<Item?>) : PreparedExercise {
    fun check(card: Int, category: Int): Boolean = cards[card].category == category
}

class PreparedSpot(val screen: SpotScreen) : PreparedExercise {
    val sentence: String get() = Audio.spotSentence(screen)
    fun check(selected: Collection<Int>): Boolean = selected.toSet() == screen.answers.toSet()
}

/** [tiles] : les lettres du mot et les lettres en plus, mélangées. */
class PreparedBuild(val screen: BuildScreen, val target: Item, val tiles: List<String>) : PreparedExercise {
    /** Lettres du mot, sans harakat, dans l'ordre. */
    val letters: String = Audio.buildLetters(target.ar)

    /** [picked] : indices des étiquettes touchées, dans l'ordre. */
    fun check(picked: List<Int>): Boolean = picked.joinToString("") { tiles[it] } == letters
}

/** Une carte du jeu de mémoire : l'élément et la face montrée une fois retournée. */
data class MemoryCard(val item: Item, val face: MemoryFace)

enum class MemoryFace { ARABIC, FRENCH, ICON }

class PreparedMemory(val screen: MemoryScreen, val cards: List<MemoryCard>) : PreparedExercise {
    val pairCount: Int get() = cards.size / 2
    fun isPair(a: Int, b: Int): Boolean = a != b && cards[a].item.id == cards[b].item.id
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

    fun sort(screen: SortScreen): PreparedSort {
        val cards = screen.cards.shuffled(random)
        return PreparedSort(screen, cards, cards.map { c -> c.item?.let { course.item(it) } })
    }

    fun spot(screen: SpotScreen) = PreparedSpot(screen)

    fun build(screen: BuildScreen): PreparedBuild {
        val target = course.item(screen.item) ?: error("élément inconnu ${screen.item}")
        val tiles = Audio.buildLetters(target.ar).map { it.toString() } + screen.extra
        var shuffled = tiles.shuffled(random)
        // Évite de proposer les lettres déjà dans l'ordre.
        repeat(5) { if (shuffled.joinToString("").startsWith(Audio.buildLetters(target.ar))) shuffled = tiles.shuffled(random) }
        return PreparedBuild(screen, target, shuffled)
    }

    fun memory(screen: MemoryScreen): PreparedMemory {
        val items = screen.items.mapNotNull { course.item(it) }
        val other = if (screen.mode == MemoryMode.ICON_AR) MemoryFace.ICON else MemoryFace.FRENCH
        val cards = items.flatMap { listOf(MemoryCard(it, MemoryFace.ARABIC), MemoryCard(it, other)) }
        return PreparedMemory(screen, cards.shuffled(random))
    }

    fun question(screen: QuestionScreen): PreparedQuestion {
        val indices = screen.choices.indices.toList()
        return PreparedQuestion(screen, if (screen.shuffle) indices.shuffled(random) else indices)
    }
}
