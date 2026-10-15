package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.TestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeEstimateTest {
    private val course = TestContent.course

    @Test
    fun `chaque leçon a une durée positive`() {
        assertTrue(course.activities.all { TimeEstimate.seconds(it) > 0 })
    }

    @Test
    fun `un chapitre dure la somme de ses leçons et un tome la somme de ses chapitres`() {
        for (chapter in course.chapters) {
            assertEquals(chapter.activities.sumOf { TimeEstimate.seconds(it) }, TimeEstimate.seconds(chapter))
        }
        val level = course.levels.first()
        assertEquals(level.chapters.sumOf { TimeEstimate.seconds(it) }, TimeEstimate.seconds(level))
    }

    @Test
    fun `l'arrondi affiché reste raisonnable`() {
        assertEquals(1, TimeEstimate.displayMinutes(0))
        assertEquals(1, TimeEstimate.displayMinutes(40))
        assertEquals(1, TimeEstimate.displayMinutes(60))
        assertEquals(2, TimeEstimate.displayMinutes(61))
        assertEquals(9, TimeEstimate.displayMinutes(9 * 60))
        assertEquals(10, TimeEstimate.displayMinutes(9 * 60 + 1))
        assertEquals(10, TimeEstimate.displayMinutes(12 * 60))
        assertEquals(15, TimeEstimate.displayMinutes(13 * 60))
        assertEquals(35, TimeEstimate.displayMinutes(34 * 60))
    }
}
