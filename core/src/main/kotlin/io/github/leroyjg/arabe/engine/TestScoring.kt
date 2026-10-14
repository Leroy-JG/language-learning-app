package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.MultiScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.Screen
import kotlin.math.floor

/**
 * Notation des quiz du professeur et de l'examen final : chaque question vaut ses points (ceux du
 * professeur), la note est ramenée sur 20, et le quiz est réussi à 12/20 (60 % des points).
 * Une question à réponses multiples est juste seulement si toute la sélection est exacte.
 */
object TestScoring {
    const val OUT_OF = 20
    const val PASS_MARK = 12

    /** Points d'un écran : ceux d'une question notée, 1 pour tout autre exercice. */
    fun points(screen: Screen): Int = when (screen) {
        is QuestionScreen -> screen.points
        is MultiScreen -> screen.points
        else -> 1
    }

    /** Total des points des questions de [screens]. */
    fun totalPoints(screens: List<Screen>): Int = screens.filterIsInstance<Exercise>().sumOf { points(it) }

    /** Points gagnés : ceux des questions de [screens] absentes de [missed]. */
    fun earnedPoints(screens: List<Screen>, missed: Set<String>): Int =
        screens.filterIsInstance<Exercise>().filter { it.id !in missed }.sumOf { points(it) }

    /** Note sur 20 (0 s'il n'y a aucun point). */
    fun mark(earned: Int, total: Int): Double = if (total == 0) 0.0 else earned.toDouble() * OUT_OF / total

    /** Réussi à 12/20 ou plus (calcul entier : 60 % des points, sans erreur d'arrondi). */
    fun passed(earned: Int, total: Int): Boolean = total > 0 && earned * OUT_OF >= total * PASS_MARK

    /** Note à la française : une décimale au plus, jamais arrondie au-dessus (« 11,9 », « 12 »). */
    fun format(mark: Double): String {
        val tenths = floor(mark * 10 + 1e-9).toInt()
        val whole = tenths / 10
        val decimal = tenths % 10
        return if (decimal == 0) "$whole" else "$whole,$decimal"
    }
}
