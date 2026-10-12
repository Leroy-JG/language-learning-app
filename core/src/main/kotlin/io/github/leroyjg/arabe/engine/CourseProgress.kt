package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.Chapter
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Level

/** État d'une leçon (activité) dans le parcours. */
enum class LessonState { LOCKED, AVAILABLE, COMPLETED }

/**
 * Progression dans un cours, à partir des activités terminées (réussies).
 * Le parcours est linéaire : une activité s'ouvre quand la précédente est terminée ; le chapitre
 * suivant s'ouvre donc quand le bilan (dernière activité du chapitre) est réussi.
 * Un id d'activité terminée qui n'existe plus dans le contenu est simplement ignoré.
 */
class CourseProgress(
    private val course: Course,
    completed: Set<String>,
    private val unlockAll: Boolean = false,
) {
    private val order: List<Activity> = course.activities
    private val completed: Set<String> = completed.filterTo(mutableSetOf()) { id -> order.any { it.id == id } }
    private val indexOf: Map<String, Int> = order.withIndex().associate { (i, a) -> a.id to i }

    fun state(activity: Activity): LessonState = when {
        activity.id in completed -> LessonState.COMPLETED
        unlockAll -> LessonState.AVAILABLE
        else -> {
            val i = indexOf[activity.id] ?: return LessonState.LOCKED
            if (i == 0 || order[i - 1].id in completed) LessonState.AVAILABLE else LessonState.LOCKED
        }
    }

    /** Prochaine activité à faire : la première non terminée et ouverte (null si tout est fini). */
    val next: Activity? get() = order.firstOrNull { state(it) == LessonState.AVAILABLE }

    fun completedIn(chapter: Chapter): Int = chapter.activities.count { it.id in completed }

    fun completedIn(level: Level): Int = level.chapters.sumOf { completedIn(it) }

    fun totalIn(level: Level): Int = level.chapters.sumOf { it.activities.size }

    /** Avancement du niveau, de 0 à 1. */
    fun fraction(level: Level): Float = totalIn(level).let { if (it == 0) 0f else completedIn(level).toFloat() / it }
}
