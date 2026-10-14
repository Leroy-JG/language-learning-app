package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.MultiScreen
import io.github.leroyjg.arabe.content.isGraded
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.Txt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestScoringTest {
    private fun question(id: String, points: Int) =
        QuestionScreen(id, Txt(fr = "?"), listOf(Txt(fr = "a"), Txt(fr = "b")), answer = 0, points = points)

    private fun multi(id: String, points: Int, answers: List<Int>) =
        MultiScreen(id, Txt(fr = "?"), List(4) { Txt(fr = "choix $it") }, answers = answers, points = points)

    private val screens = listOf(question("q1", 1), question("q2", 5), multi("m1", 5, listOf(0, 2)), PresentScreen("p", "w:x"))

    @Test
    fun `les points sont ceux des questions, 1 pour un autre exercice`() {
        assertEquals(11, TestScoring.totalPoints(screens))
        assertEquals(5, TestScoring.points(screens[1]))
        assertEquals(1, TestScoring.points(PresentScreen("p", "w:x")))
    }

    @Test
    fun `les points gagnés sont ceux des questions non ratées`() {
        assertEquals(11, TestScoring.earnedPoints(screens, emptySet()))
        assertEquals(6, TestScoring.earnedPoints(screens, setOf("m1")))
        assertEquals(0, TestScoring.earnedPoints(screens, setOf("q1", "q2", "m1")))
    }

    @Test
    fun `la note est ramenée sur 20 et le seuil est 12 sur 20`() {
        assertEquals(20.0, TestScoring.mark(18, 18), 1e-9)
        assertEquals(12.0, TestScoring.mark(3, 5), 1e-9)
        assertEquals(0.0, TestScoring.mark(0, 0), 1e-9)
        assertTrue(TestScoring.passed(3, 5), "60 % pile : réussi")
        assertTrue(TestScoring.passed(60, 100))
        assertFalse(TestScoring.passed(59, 100))
        assertFalse(TestScoring.passed(10, 18), "11,1 sur 20")
        assertTrue(TestScoring.passed(11, 18), "12,2 sur 20")
        assertFalse(TestScoring.passed(0, 0))
    }

    @Test
    fun `la note s'écrit à la française sans jamais arrondir au-dessus`() {
        assertEquals("12", TestScoring.format(12.0))
        assertEquals("14,5", TestScoring.format(14.5))
        assertEquals("11,9", TestScoring.format(11.96))
        assertEquals("0", TestScoring.format(0.0))
        assertEquals("13,3", TestScoring.format(20.0 * 2 / 3))
        assertEquals("20", TestScoring.format(20.0))
    }

    @Test
    fun `dans un quiz noté une question ratée ne revient pas`() {
        val quiz = listOf(question("q1", 1), question("q2", 5))
        val session = ActivitySession(quiz, retryMissed = false)
        session.advance(Outcome.WRONG)
        assertEquals("q2", session.current?.id)
        session.advance(Outcome.CORRECT)
        assertTrue(session.finished)
        assertEquals(setOf("q1"), session.missedScreens)
        assertEquals(1f, session.progress)
        assertEquals(5, TestScoring.earnedPoints(quiz, session.missedScreens))
    }

    @Test
    fun `une leçon ordinaire reprend toujours les erreurs`() {
        val session = ActivitySession(listOf(question("q1", 1)))
        session.advance(Outcome.WRONG)
        assertFalse(session.finished)
        assertEquals("q1", session.current?.id)
    }

    @Test
    fun `une question à réponses multiples exige exactement la bonne sélection`() {
        val screen = multi("m1", 5, listOf(0, 2))
        val ordered = ExerciseFactory(io.github.leroyjg.arabe.TestContent.course, io.github.leroyjg.arabe.content.CourseIndex(io.github.leroyjg.arabe.TestContent.course), Random(1))
        val p = ordered.multi(screen.copy(shuffle = false))
        assertEquals(setOf(0, 2), p.correctPositions)
        assertTrue(p.check(listOf(2, 0)))
        assertFalse(p.check(listOf(0)))
        assertFalse(p.check(listOf(0, 1, 2)))
        assertFalse(p.check(emptyList()))
        val shuffled = ordered.multi(screen)
        assertEquals(shuffled.correctPositions, shuffled.order.indices.filter { shuffled.order[it] in screen.answers }.toSet())
        assertTrue(shuffled.check(shuffled.correctPositions))
    }

    @Test
    fun `chaque quiz noté du cours est réussi avec toutes les bonnes réponses et raté sans aucune`() {
        val course = io.github.leroyjg.arabe.TestContent.course
        val graded = course.activities.filter { it.file.type.isGraded }
        assertEquals(10, graded.size, "neuf quiz du professeur et l'examen final")
        for (activity in graded) {
            val screens = activity.screens
            val total = TestScoring.totalPoints(screens)
            assertTrue(total > 0, activity.id)
            assertEquals(20.0, TestScoring.mark(TestScoring.earnedPoints(screens, emptySet()), total), 1e-9, activity.id)
            assertTrue(TestScoring.passed(total, total), activity.id)
            assertFalse(TestScoring.passed(TestScoring.earnedPoints(screens, screens.map { it.id }.toSet()), total), activity.id)
        }
        assertEquals(io.github.leroyjg.arabe.content.ActivityType.EXAM, graded.last().file.type, "l'examen final est le dernier")
    }

    @Test
    fun `le quiz de la leçon 4 se joue sans reprise`() {
        val course = io.github.leroyjg.arabe.TestContent.course
        val activity = course.activity("medine-t1.q01.test")!!
        val session = ActivitySession(activity.screens, retryMissed = false)
        var steps = 0
        while (!session.finished) {
            session.advance(if (steps % 2 == 0) Outcome.WRONG else Outcome.CORRECT)
            steps++
        }
        assertEquals(activity.screens.size, steps, "chaque question est posée une seule fois")
        assertEquals(1f, session.progress)
    }
}
