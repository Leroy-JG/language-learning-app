package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.TestContent
import io.github.leroyjg.arabe.arabic.AnswerMatcher.Verdict
import io.github.leroyjg.arabe.content.BuildScreen
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.MemoryScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.SortScreen
import io.github.leroyjg.arabe.content.SpotScreen
import io.github.leroyjg.arabe.content.TrueFalseScreen
import io.github.leroyjg.arabe.content.TypingScreen
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EngineTest {
    private val course = TestContent.course
    private val factory = ExerciseFactory(course, CourseIndex(course), Random(42))

    private inline fun <reified T> screen(id: String): T =
        course.activities.flatMap { it.screens }.first { it.id == id } as T

    @Test
    fun `un exercice raté revient en fin d'activité jusqu'à sa réussite`() {
        val activity = course.activity("medine-t1.l01.vocab-1")!!
        val session = ActivitySession(activity.screens)
        val total = activity.screens.size
        var steps = 0
        var failedOnce = false
        while (!session.finished) {
            val s = session.current!!
            val outcome = when {
                s !is Exercise -> Outcome.VIEWED
                s.id.endsWith("#4") && !failedOnce -> Outcome.WRONG.also { failedOnce = true }
                else -> Outcome.CORRECT
            }
            if (s.id.endsWith("#4") && failedOnce && outcome == Outcome.CORRECT) assertTrue(session.isRetry)
            session.advance(outcome)
            steps++
        }
        assertEquals(total + 1, steps, "l'exercice raté est repassé une fois")
        assertEquals(session.exerciseCount - 1, session.firstTryCorrect)
        assertEquals(1f, session.progress)
        assertTrue(session.score < 1.0)
    }

    @Test
    fun `l'avancement recule quand un exercice revient`() {
        val activity = course.activity("medine-t1.l01.quiz")!!
        val session = ActivitySession(activity.screens)
        session.advance(Outcome.CORRECT)
        val before = session.progress
        session.advance(Outcome.WRONG)
        assertTrue(session.progress <= before + 1e-6)
    }

    @Test
    fun `seuil du quiz`() {
        val activity = course.activity("medine-t1.l01.quiz")!!
        val session = ActivitySession(activity.screens)
        val toFail = (activity.screens.size * 0.31).toInt() + 1
        var failed = 0
        val seen = mutableSetOf<String>()
        while (!session.finished) {
            val id = session.current!!.id
            if (failed < toFail && seen.add(id)) {
                failed++
                session.advance(Outcome.WRONG)
            } else {
                session.advance(Outcome.CORRECT)
            }
        }
        assertTrue(session.score < QUIZ_PASS_SCORE)
    }

    @Test
    fun `QCM automatique - trois choix distincts dont la bonne réponse`() {
        val s = screen<McqScreen>("medine-t1.l01.vocab-1#4")
        repeat(20) {
            val p = factory.mcq(s)
            assertEquals(3, p.choices.size)
            assertEquals(3, p.choices.map { it.fr }.toSet().size)
            assertTrue(p.check(p.correctIndex))
            assertFalse(p.check((p.correctIndex + 1) % 3))
        }
    }

    @Test
    fun `chaque QCM du cours se prépare avec 3 ou 4 choix distincts`() {
        for (s in course.activities.flatMap { it.screens }.filterIsInstance<McqScreen>()) {
            val p = factory.mcq(s)
            assertTrue(p.choices.size in 3..4, s.id)
            assertEquals(p.choices.size, p.choices.map { it.id }.toSet().size, s.id)
            assertTrue(p.correctIndex >= 0, s.id)
        }
    }

    @Test
    fun `association`() {
        val p = factory.match(screen<MatchScreen>("medine-t1.l01.vocab-1#11"))
        assertEquals(p.left.toSet(), p.right.toSet())
        assertTrue(p.isPair(p.left[0], p.left[0]))
        assertFalse(p.isPair(p.left[0], p.left[1]))
    }

    @Test
    fun `texte à trous`() {
        val p = factory.fillGap(screen<FillGapScreen>("medine-t1.l02.grammar-2#7"))
        assertEquals(2, p.parts.count { it is PreparedFillGap.SentencePart.Gap })
        assertTrue(p.check(listOf("مَنْ", "وَمَنْ")))
        assertFalse(p.check(listOf("وَمَنْ", "مَنْ")))
        assertFalse(p.check(listOf("مَنْ", null)))
        assertEquals("مَنْ هٰذَا وَمَنْ ذٰلِكَ؟", p.fullSentence)
    }

    @Test
    fun `remise en ordre`() {
        val s = screen<OrderScreen>("medine-t1.l01.grammar-1#5")
        val p = factory.order(s)
        assertEquals(setOf("هٰذَا", "كِتَابٌ", "بَيْتٌ"), p.tokens.toSet())
        assertTrue(p.check(listOf("هٰذَا", "كِتَابٌ")))
        assertFalse(p.check(listOf("كِتَابٌ", "هٰذَا")))
        assertFalse(p.check(listOf("هٰذَا", "بَيْتٌ")))
        assertEquals("هٰذَا كِتَابٌ.", p.fullSentence)
    }

    @Test
    fun `saisie avec élément ou réponses`() {
        val withItem = factory.typing(screen<TypingScreen>("medine-t1.l01.vocab-1#12"))
        assertEquals("une maison", withItem.translation)
        assertEquals(Verdict.CORRECT, withItem.check("بيت").verdict)
        val withAnswers = factory.typing(screen<TypingScreen>("medine-t1.l01.grammar-1#7"))
        assertEquals(Verdict.CORRECT, withAnswers.check("هذا بيت").verdict)
        assertEquals(Verdict.WRONG, withAnswers.check("هذا قلم").verdict)
    }

    @Test
    fun `tout le cours se joue - chaque bonne réponse est acceptée`() {
        for (activity in course.activities) {
            val session = ActivitySession(activity.screens)
            var guard = 0
            while (!session.finished) {
                val s = session.current!!
                val ok = when (s) {
                    is McqScreen -> factory.mcq(s).let { it.check(it.correctIndex) }
                    is MatchScreen -> factory.match(s).let { p -> p.left.all { l -> p.right.count { r -> p.isPair(l, r) } == 1 } }
                    is FillGapScreen -> factory.fillGap(s).check(s.gaps.map { it.answers.first() })
                    is OrderScreen -> factory.order(s).let { p -> p.tokens.sorted() == (s.answers.first() + s.distractors.orEmpty()).sorted() && p.check(s.answers.first()) }
                    is TypingScreen -> factory.typing(s).let { p -> p.answers.all { p.check(it).verdict == Verdict.CORRECT } }
                    is TrueFalseScreen -> factory.trueFalse(s).check(s.answer)
                    is QuestionScreen -> factory.question(s).let { it.check(it.correctPosition) }
                    is SortScreen -> factory.sort(s).let { p -> p.cards.indices.all { p.check(it, p.cards[it].category) } }
                    is SpotScreen -> factory.spot(s).check(s.answers)
                    is BuildScreen -> factory.build(s).let { p -> p.check(buildSolution(p)) }
                    is MemoryScreen -> factory.memory(s).let { p -> p.cards.indices.all { a -> p.cards.indices.count { b -> p.isPair(a, b) } == 1 } }
                    else -> true
                }
                assertTrue(ok, "bonne réponse refusée : ${s.id}")
                session.advance(if (s is Exercise) Outcome.CORRECT else Outcome.VIEWED)
                assertTrue(++guard <= activity.screens.size, "boucle infinie dans ${activity.id}")
            }
            assertEquals(1.0, session.score, activity.id)
        }
    }

    /** Indices des étiquettes à toucher pour écrire le mot. */
    private fun buildSolution(p: PreparedBuild): List<Int> {
        val used = mutableSetOf<Int>()
        return p.letters.map { c -> p.tiles.indices.first { it !in used && p.tiles[it] == c.toString() }.also { used += it } }
    }

    @Test
    fun `ranger - chaque carte n'a qu'une bonne catégorie`() {
        val p = factory.sort(screen<SortScreen>("medine-t1.l03.practice-1#18"))
        assertEquals(8, p.cards.size)
        p.cards.indices.forEach { i ->
            assertTrue(p.check(i, p.cards[i].category))
            assertFalse(p.check(i, 1 - p.cards[i].category))
        }
        val withItems = factory.sort(screen<SortScreen>("medine-t1.l01.grammar-3#6b"))
        assertTrue(withItems.items.all { it != null }, "les cartes « item » trouvent leur élément")
    }

    @Test
    fun `repérer - il faut tous les bons mots et seulement eux`() {
        val p = factory.spot(screen<SpotScreen>("medine-t1.l03.grammar-3#8c"))
        assertTrue(p.check(listOf(2, 0)))
        assertFalse(p.check(listOf(0)))
        assertFalse(p.check(listOf(0, 1, 2)))
        assertEquals("الْبَابُ مَفْتُوحٌ، وَالْقَلَمُ مَكْسُورٌ.", p.sentence)
    }

    @Test
    fun `écrire un mot - lettres sans harakat, lettre en plus ignorée`() {
        val p = factory.build(screen<BuildScreen>("medine-t1.l01.vocab-2#13"))
        assertEquals("مفتاح", p.letters)
        assertEquals(6, p.tiles.size, "5 lettres + 1 en plus")
        assertTrue(p.check(buildSolution(p)))
        assertFalse(p.check(buildSolution(p).reversed()))
        assertEquals("هذا", factory.build(BuildScreen("t", "w:hadha")).letters, "l'alif suscrit n'est pas une étiquette")
    }

    @Test
    fun `mémoire - deux cartes par élément, une arabe et une image`() {
        val p = factory.memory(screen<MemoryScreen>("medine-t1.l01.vocab-5#14b"))
        assertEquals(12, p.cards.size)
        assertEquals(6, p.pairCount)
        p.cards.groupBy { it.item.id }.values.forEach { pair ->
            assertEquals(setOf(MemoryFace.ARABIC, MemoryFace.ICON), pair.map { it.face }.toSet())
        }
        val a = p.cards.indexOfFirst { it.face == MemoryFace.ARABIC }
        val b = p.cards.indexOfFirst { it.face == MemoryFace.ICON && it.item.id == p.cards[a].item.id }
        assertTrue(p.isPair(a, b))
        assertFalse(p.isPair(a, a))
    }

    @Test
    fun `vrai ou faux et question`() {
        val tf = factory.trueFalse(screen<TrueFalseScreen>("medine-t1.l01.grammar-1#9"))
        assertTrue(tf.check(false))
        val q = factory.question(screen<QuestionScreen>("medine-t1.l03.practice-1#2"))
        assertEquals(listOf(0, 1), q.order, "shuffle false : ordre du fichier")
        assertTrue(q.check(1))
        assertEquals(1, q.correctPosition)
        val shuffled = factory.question(screen<QuestionScreen>("medine-t1.l01.quiz#5"))
        assertTrue(shuffled.check(shuffled.correctPosition))
    }
}
