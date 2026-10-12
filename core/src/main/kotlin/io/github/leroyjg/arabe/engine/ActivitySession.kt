package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.Screen

/** Seuil de réussite d'un quiz (docs/content-format.md §8). */
const val QUIZ_PASS_SCORE = 0.7

/** Ce qui s'est passé sur l'écran qu'on quitte. */
enum class Outcome {
    /** Écran sans réponse (présentation, astuce, dialogue). */
    VIEWED,

    /** Réponse juste (avec ou sans remarque). */
    CORRECT,

    /** Réponse fausse : l'exercice revient en fin d'activité. */
    WRONG,

    /** Paires associées, mais avec des erreurs en chemin : compté raté, sans revenir. */
    COMPLETED_WITH_MISTAKES,
}

/**
 * Déroulé d'une activité, à la Busuu : les écrans dans l'ordre, chaque exercice raté revient en fin
 * d'activité jusqu'à sa réussite ; le score compte les réussites du premier coup.
 */
class ActivitySession(val screens: List<Screen>) {
    private val queue = ArrayDeque<Int>(screens.indices.toList())
    private val missed = mutableSetOf<String>()

    /** Nombre d'écrans terminés (un exercice repris compte une seule fois, quand il est réussi). */
    var completed: Int = 0
        private set

    val current: Screen? get() = queue.firstOrNull()?.let { screens[it] }

    /** L'écran courant est-il un exercice raté qui revient ? */
    val isRetry: Boolean get() = current?.id?.let { it in missed } ?: false

    val finished: Boolean get() = queue.isEmpty()

    /** Avancement de 0 à 1 ; un exercice raté rallonge le chemin restant. */
    val progress: Float
        get() = if (completed + queue.size == 0) 1f else completed.toFloat() / (completed + queue.size)

    val exerciseCount: Int = screens.count { it is Exercise }

    val firstTryCorrect: Int get() = exerciseCount - missed.size

    /** Ids des exercices ratés au premier essai. */
    val missedScreens: Set<String> get() = missed

    /** Part des exercices réussis du premier coup (1 s'il n'y a pas d'exercice). */
    val score: Double get() = if (exerciseCount == 0) 1.0 else firstTryCorrect.toDouble() / exerciseCount

    fun advance(outcome: Outcome) {
        val index = queue.removeFirstOrNull() ?: return
        val screen = screens[index]
        when (outcome) {
            Outcome.WRONG -> {
                missed += screen.id
                queue.addLast(index)
            }
            Outcome.COMPLETED_WITH_MISTAKES -> {
                missed += screen.id
                completed++
            }
            Outcome.VIEWED, Outcome.CORRECT -> completed++
        }
    }
}
