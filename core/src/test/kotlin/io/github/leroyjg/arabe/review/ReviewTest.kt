package io.github.leroyjg.arabe.review

import io.github.leroyjg.arabe.TestContent
import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.content.ActivityType
import io.github.leroyjg.arabe.content.CourseIndex
import io.github.leroyjg.arabe.content.ItemKind
import io.github.leroyjg.arabe.content.McqMode
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.TypingScreen
import io.github.leroyjg.arabe.engine.ExerciseFactory
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FsrsTest {
    private val fsrs = Fsrs()

    @Test
    fun `la probabilité de souvenir vaut 90 pour cent au bout de la stabilité`() {
        assertEquals(1.0, fsrs.retrievability(0.0, 5.0), 1e-9)
        assertEquals(0.9, fsrs.retrievability(5.0, 5.0), 1e-9)
        assertTrue(fsrs.retrievability(30.0, 5.0) < fsrs.retrievability(10.0, 5.0))
        assertEquals(5, fsrs.interval(5.0))
        assertEquals(1, fsrs.interval(0.2))
        assertEquals(365, fsrs.interval(10_000.0))
    }

    @Test
    fun `première rencontre, meilleure note, souvenir plus stable et élément plus facile`() {
        val again = fsrs.initial(Grade.AGAIN)
        val hard = fsrs.initial(Grade.HARD)
        val good = fsrs.initial(Grade.GOOD)
        val easy = fsrs.initial(Grade.EASY)
        assertTrue(again.stability < hard.stability && hard.stability < good.stability && good.stability < easy.stability)
        assertTrue(again.difficulty > hard.difficulty && hard.difficulty > good.difficulty && good.difficulty > easy.difficulty)
        assertEquals(3.173, good.stability, 1e-9)
    }

    @Test
    fun `une révision réussie à temps allonge l'intervalle, un oubli le raccourcit`() {
        val start = fsrs.initial(Grade.GOOD)
        val elapsed = fsrs.interval(start.stability)
        val good = fsrs.review(start, elapsed, Grade.GOOD)
        val hard = fsrs.review(start, elapsed, Grade.HARD)
        val again = fsrs.review(start, elapsed, Grade.AGAIN)
        assertTrue(good.stability > 2 * start.stability, "stabilité ${good.stability}")
        assertTrue(hard.stability > start.stability && hard.stability < good.stability)
        assertTrue(again.stability < start.stability)
        assertTrue(again.difficulty > start.difficulty)
        // Réussir après un long oubli (souvenir presque effacé) consolide davantage.
        val late = fsrs.review(start, elapsed * 4, Grade.GOOD)
        assertTrue(late.stability > good.stability)
    }

    @Test
    fun `réviser le même jour change peu le souvenir`() {
        val start = fsrs.initial(Grade.GOOD)
        val same = fsrs.review(start, 0, Grade.GOOD)
        assertTrue(same.stability >= start.stability && same.stability < 2 * start.stability)
        val forgot = fsrs.review(start, 0, Grade.AGAIN)
        assertTrue(forgot.stability < start.stability)
    }

    @Test
    fun `les valeurs restent dans les bornes après de nombreux oublis et réussites`() {
        var m = fsrs.initial(Grade.AGAIN)
        repeat(30) { m = fsrs.review(m, 1, Grade.AGAIN) }
        assertTrue(m.difficulty <= 10.0 && m.stability >= Fsrs.MIN_STABILITY)
        repeat(30) { m = fsrs.review(m, fsrs.interval(m.stability), Grade.EASY) }
        assertTrue(m.difficulty >= 1.0 && m.stability <= Fsrs.MAX_STABILITY)
        assertTrue(m.stability.isFinite())
    }
}

class ReviewSchedulerTest {
    private val scheduler = ReviewScheduler()

    @Test
    fun `une carte apprise revient à sa date puis s'éloigne`() {
        val card = scheduler.create("w:kitab", CardKind.ITEM, Grade.GOOD, today = 100)
        assertEquals(103, card.dueDay)
        assertFalse(scheduler.isDue(card, 102))
        assertTrue(scheduler.isDue(card, 103))
        val next = scheduler.review(card, Grade.GOOD, today = 103)
        assertTrue(next.dueDay - 103 > 3)
        assertEquals(2, next.reps)
        assertEquals(0, next.lapses)
        val forgot = scheduler.review(next, Grade.AGAIN, today = next.dueDay)
        assertEquals(1, forgot.lapses)
        assertEquals(Grade.AGAIN, forgot.lastGrade)
    }

    @Test
    fun `une date dans le passé (horloge changée) compte comme le même jour`() {
        val card = scheduler.create("w:bab", CardKind.ITEM, Grade.GOOD, today = 200)
        val back = scheduler.review(card, Grade.GOOD, today = 150)
        assertEquals(200, back.lastReviewDay)
        assertTrue(back.dueDay > 200)
    }

    @Test
    fun `maîtrise, fragile après un oubli, à revoir ensuite, acquise après plusieurs réussites`() {
        val weak = scheduler.create("w:a", CardKind.ITEM, Grade.AGAIN, today = 0)
        assertEquals(Mastery.WEAK, scheduler.mastery(weak, 0))
        val learned = scheduler.create("w:b", CardKind.ITEM, Grade.GOOD, today = 0)
        assertEquals(Mastery.MEDIUM, scheduler.mastery(learned, 0))
        // Oubliée bien après sa date : de nouveau fragile.
        assertEquals(Mastery.WEAK, scheduler.mastery(learned, 60))
        var c = learned
        while (c.memory.stability < ReviewScheduler.STRONG_STABILITY) c = scheduler.review(c, Grade.GOOD, c.dueDay)
        assertEquals(Mastery.STRONG, scheduler.mastery(c, c.lastReviewDay))
        assertTrue(c.reps <= 5, "acquis en ${c.reps} révisions")
    }
}

class ReviewPlannerTest {
    private val course = TestContent.course
    private val scheduler = ReviewScheduler()
    private val planner = ReviewPlanner(course, scheduler, Random(7))
    private val reviewable = course.chapters.flatMap { it.items }.filter { it.review }

    private fun cards(grade: Grade, day: Long) = reviewable.map { scheduler.create(it.id, CardKind.ITEM, grade, day) }

    @Test
    fun `les cartes dues passent en premier, les plus effacées d'abord`() {
        val old = scheduler.create("w:old", CardKind.ITEM, Grade.GOOD, today = 0)
        val recent = scheduler.create("w:recent", CardKind.ITEM, Grade.GOOD, today = 8)
        val fresh = scheduler.create("w:fresh", CardKind.ITEM, Grade.GOOD, today = 10)
        val order = planner.prioritize(listOf(fresh, recent, old), today = 11)
        assertEquals(listOf("w:old", "w:recent", "w:fresh"), order.map { it.key })
        assertEquals(2, planner.dueCount(listOf(fresh, recent, old), 11))
    }

    @Test
    fun `séance de vocabulaire, exercices valides, sans doublon de réponse`() {
        val index = CourseIndex(course)
        val factory = ExerciseFactory(course, index, Random(3))
        for (grade in listOf(Grade.AGAIN, Grade.GOOD)) {
            for (allowTyping in listOf(true, false)) {
                var all = cards(grade, 0)
                // Différents niveaux de solidité, pour voir tous les types d'exercices.
                all = all.mapIndexed { i, c -> (0 until i % 4).fold(c) { acc, _ -> scheduler.review(acc, Grade.GOOD, acc.dueDay) } }
                val session = planner.vocabularySession(all, today = 400, size = 40, allowTyping = allowTyping)
                assertEquals(minOf(40, all.size), session.size)
                assertEquals(session.size, session.map { it.screen.id }.toSet().size, "ids d'écrans uniques")
                assertEquals(session.size, session.map { it.cardKey }.toSet().size, "un exercice par mot")
                for (ex in session) {
                    val item = assertNotNull(course.item(ex.cardKey))
                    when (val s = ex.screen) {
                        is McqScreen -> {
                            assertEquals(item.id, s.item)
                            if (s.mode == McqMode.ICON_AR) assertNotNull(item.icon)
                            val card = all.first { it.key == item.id }
                            if (card.lastGrade == Grade.AGAIN) assertTrue(s.mode == McqMode.AR_FR || s.mode == McqMode.ICON_AR)
                            val prepared = factory.mcq(s)
                            assertTrue(prepared.choices.size in 3..4)
                            assertEquals(prepared.choices.size, prepared.choices.map { it.fr }.toSet().size)
                            assertEquals(prepared.choices.size, prepared.choices.map { ArabicScript.lettersOnly(it.ar) }.toSet().size)
                            if (item.kind == ItemKind.WORD) assertTrue(prepared.choices.all { it.kind == item.kind })
                            assertTrue(prepared.choices.none { it.id != item.id && ArabicScript.distractorKey(it.ar) == ArabicScript.distractorKey(item.ar) })
                        }
                        is TypingScreen -> {
                            assertTrue(allowTyping)
                            assertEquals(item.id, s.item)
                        }
                        else -> error("écran inattendu ${s.id}")
                    }
                }
            }
        }
    }

    @Test
    fun `séance de grammaire, exercices du point choisi, rappel de la règle si demandé`() {
        assertTrue(planner.grammarTopics.isNotEmpty())
        assertTrue(planner.grammarTopics.all { it.file.type == ActivityType.GRAMMAR })
        for (topic in planner.grammarTopics) {
            val plain = planner.grammarSession(topic, withReminder = false)
            assertTrue(plain.isNotEmpty(), "aucun exercice pour ${topic.id}")
            assertTrue(plain.size <= ReviewPlanner.GRAMMAR_SESSION_SIZE)
            assertTrue(plain.all { it.cardKey == topic.id && it.screen in topic.screens })
            assertTrue(plain.none { it.screen is TipScreen || it.screen is PresentScreen })
            val withTips = planner.grammarSession(topic, withReminder = true)
            assertEquals(topic.screens.count { it is TipScreen }, withTips.count { it.screen is TipScreen })
        }
    }

    @Test
    fun `point de grammaire le plus urgent`() {
        val topics = planner.grammarTopics
        val a = scheduler.create(topics[0].id, CardKind.GRAMMAR, Grade.GOOD, today = 0)
        val b = scheduler.create(topics[1].id, CardKind.GRAMMAR, Grade.AGAIN, today = 0)
        assertEquals(topics[1], planner.mostNeededTopic(listOf(a, b), today = 5))
        assertEquals(null, planner.mostNeededTopic(emptyList(), today = 5))
        val gone = scheduler.create("medine-t1.l99.grammar-1", CardKind.GRAMMAR, Grade.AGAIN, today = 0)
        assertEquals(topics[0], planner.mostNeededTopic(listOf(a, gone), today = 5))
    }

    @Test
    fun `fin de leçon, mots présentés mémorisés, mots ratés notés, grammaire notée selon le score`() {
        val vocab = course.activity("medine-t1.l01.vocab-1")!!
        val presented = vocab.screens.filterIsInstance<PresentScreen>().map { it.item }
        val missed = presented.first()
        val graded = ReviewRules.afterActivity(course, vocab, mapOf(missed to false, presented[1] to true), score = 0.9, known = emptySet())
        assertEquals(presented.toSet(), graded.map { it.key }.toSet())
        assertEquals(Grade.HARD, graded.first { it.key == missed }.grade)
        assertTrue(graded.filter { it.key != missed }.all { it.grade == Grade.GOOD })
        // Déjà connus : seuls les mots interrogés sont révisés.
        val again = ReviewRules.afterActivity(course, vocab, mapOf(missed to false), score = 0.9, known = presented.toSet())
        assertEquals(listOf(GradedKey(missed, CardKind.ITEM, Grade.AGAIN)), again)

        val grammar = course.activity("medine-t1.l01.grammar-1")!!
        val g = ReviewRules.afterActivity(course, grammar, emptyMap(), score = 0.5, known = emptySet())
        assertEquals(GradedKey(grammar.id, CardKind.GRAMMAR, Grade.AGAIN), g.last())
        assertEquals(Grade.GOOD, ReviewRules.gradeForScore(0.9))
        assertEquals(Grade.HARD, ReviewRules.gradeForScore(0.7))
    }

    @Test
    fun `reprise des leçons déjà faites, cartes créées une fois, au premier jour de réussite`() {
        val vocab = course.activity("medine-t1.l01.vocab-1")!!
        val grammar = course.activity("medine-t1.l01.grammar-1")!!
        val presented = vocab.screens.filterIsInstance<PresentScreen>().map { it.item }
        val known = setOf(presented.first())
        val out = ReviewRules.backfill(
            course,
            listOf(Completion(grammar.id, day = 20, bestScore = 1.0), Completion(vocab.id, day = 10, bestScore = 1.0), Completion("disparu", 5, 1.0)),
            known,
        )
        val keys = out.map { it.first.key }
        assertEquals(keys.size, keys.toSet().size)
        assertFalse(presented.first() in keys)
        assertTrue(presented.drop(1).all { it in keys })
        assertEquals(10L, out.first { it.first.key == presented[1] }.second)
        assertEquals(20L, out.first { it.first.key == grammar.id }.second)
        assertTrue(out.all { abs(it.second) < 1000 })
    }
}
