package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.Chapter
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Level

/** État d'une leçon (activité) dans le parcours. Rien n'est jamais verrouillé. */
enum class LessonState { AVAILABLE, COMPLETED }

/**
 * Progression dans un cours, à partir des activités terminées (réussies).
 * Toutes les leçons sont ouvertes : on fait ce qu'on veut, dans l'ordre qu'on veut. Le parcours
 * propose seulement la suivante : la première leçon non terminée, dans l'ordre du cours.
 * Un id d'activité terminée qui n'existe plus dans le contenu est simplement ignoré.
 */
class CourseProgress(private val course: Course, completed: Set<String>) {
    private val order: List<Activity> = course.activities
    private val completed: Set<String> = completed.filterTo(mutableSetOf()) { id -> order.any { it.id == id } }

    fun state(activity: Activity): LessonState =
        if (activity.id in completed) LessonState.COMPLETED else LessonState.AVAILABLE

    fun isCompleted(activity: Activity): Boolean = activity.id in completed

    /** Prochaine leçon proposée : la première non terminée (null si tout est fini). */
    val next: Activity? = order.firstOrNull { it.id !in completed }

    fun completedIn(chapter: Chapter): Int = chapter.activities.count { it.id in completed }

    fun completedIn(level: Level): Int = level.chapters.sumOf { completedIn(it) }

    fun totalIn(level: Level): Int = level.chapters.sumOf { it.activities.size }

    /** Avancement du niveau, de 0 à 1. */
    fun fraction(level: Level): Float = totalIn(level).let { if (it == 0) 0f else completedIn(level).toFloat() / it }

    /** Avancement du chapitre, de 0 à 1. */
    fun fraction(chapter: Chapter): Float =
        chapter.activities.size.let { if (it == 0) 0f else completedIn(chapter).toFloat() / it }
}
