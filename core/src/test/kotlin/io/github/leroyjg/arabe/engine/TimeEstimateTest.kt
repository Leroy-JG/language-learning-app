package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.TestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeEstimateTest {
    private val course = TestContent.course

    @Test
    fun `chaque leçon dure au moins une minute`() {
        assertTrue(course.activities.all { TimeEstimate.minutes(it) >= 1 })
    }

    @Test
    fun `une leçon est arrondie à la minute supérieure`() {
        for (activity in course.activities) {
            val seconds = TimeEstimate.seconds(activity)
            assertTrue(seconds > 0)
            assertEquals(maxOf((seconds + 59) / 60, 1), TimeEstimate.minutes(activity))
        }
    }

    @Test
    fun `un chapitre dure la somme des minutes affichées de ses leçons`() {
        for (chapter in course.chapters) {
            assertEquals(chapter.activities.sumOf { TimeEstimate.minutes(it) }, TimeEstimate.minutes(chapter))
        }
    }
}
