package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.TestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CourseProgressTest {
    private val course = TestContent.course
    private val all = course.activities
    private val level = course.levels.first()

    @Test
    fun `rien n'est verrouillé, la première leçon est proposée`() {
        val p = CourseProgress(course, emptySet())
        assertTrue(all.all { p.state(it) == LessonState.AVAILABLE })
        assertEquals(all[0], p.next)
        assertEquals(0f, p.fraction(level))
    }

    @Test
    fun `la leçon proposée est la première non terminée, même dans le désordre`() {
        val p = CourseProgress(course, setOf(all[0].id, all[2].id))
        assertEquals(LessonState.COMPLETED, p.state(all[0]))
        assertEquals(LessonState.AVAILABLE, p.state(all[1]))
        assertEquals(all[1], p.next)
        val chapter2 = course.chapters[1]
        val ahead = CourseProgress(course, chapter2.activities.map { it.id }.toSet())
        assertEquals(all[0], ahead.next)
        assertEquals(chapter2.activities.size, ahead.completedIn(chapter2))
        assertEquals(1f, ahead.fraction(chapter2))
        assertEquals(0, ahead.completedIn(course.chapters[0]))
    }

    @Test
    fun `tout terminé, ids inconnus ignorés`() {
        val done = CourseProgress(course, all.map { it.id }.toSet() + "id.disparu")
        assertNull(done.next)
        assertEquals(1f, done.fraction(level))
        assertEquals(all.size, done.completedIn(level))
    }
}
