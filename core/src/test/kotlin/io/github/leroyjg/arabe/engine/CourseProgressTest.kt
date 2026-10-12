package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.TestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CourseProgressTest {
    private val course = TestContent.course
    private val all = course.activities
    private val level = course.levels.first()

    @Test
    fun `au départ seule la première leçon est ouverte`() {
        val p = CourseProgress(course, emptySet())
        assertEquals(LessonState.AVAILABLE, p.state(all[0]))
        assertEquals(LessonState.LOCKED, p.state(all[1]))
        assertEquals(all[0], p.next)
        assertEquals(0f, p.fraction(level))
    }

    @Test
    fun `terminer une leçon ouvre la suivante`() {
        val p = CourseProgress(course, setOf(all[0].id))
        assertEquals(LessonState.COMPLETED, p.state(all[0]))
        assertEquals(LessonState.AVAILABLE, p.state(all[1]))
        assertEquals(LessonState.LOCKED, p.state(all[2]))
        assertEquals(all[1], p.next)
    }

    @Test
    fun `le chapitre suivant s'ouvre après le bilan`() {
        val chapter1 = course.chapters[0]
        val chapter2 = course.chapters[1]
        val beforeQuiz = chapter1.activities.dropLast(1).map { it.id }.toSet()
        assertEquals(LessonState.LOCKED, CourseProgress(course, beforeQuiz).state(chapter2.activities.first()))
        val withQuiz = chapter1.activities.map { it.id }.toSet()
        val p = CourseProgress(course, withQuiz)
        assertEquals(LessonState.AVAILABLE, p.state(chapter2.activities.first()))
        assertEquals(chapter1.activities.size, p.completedIn(chapter1))
        assertEquals(0, p.completedIn(chapter2))
    }

    @Test
    fun `tout terminé, tout déverrouillé, ids inconnus ignorés`() {
        val done = CourseProgress(course, all.map { it.id }.toSet() + "id.disparu")
        assertNull(done.next)
        assertEquals(1f, done.fraction(level))
        assertEquals(all.size, done.completedIn(level))
        val open = CourseProgress(course, emptySet(), unlockAll = true)
        assertEquals(LessonState.AVAILABLE, open.state(all.last()))
    }
}
