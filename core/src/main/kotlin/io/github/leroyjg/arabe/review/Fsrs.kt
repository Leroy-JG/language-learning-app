package io.github.leroyjg.arabe.review

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Note d'une révision, déduite de la réponse donnée (l'utilisateur ne se note jamais lui-même). */
enum class Grade(val value: Int) {
    /** Oublié : réponse fausse. */
    AGAIN(1),

    /** Retrouvé avec difficulté : juste avec une remarque, ou juste après une erreur. */
    HARD(2),

    /** Retrouvé : juste du premier coup. */
    GOOD(3),

    /** Évident. */
    EASY(4),
    ;

    companion object {
        fun of(value: Int): Grade? = entries.firstOrNull { it.value == value }
    }
}

/** Mémoire d'un élément selon FSRS : stabilité (en jours) et difficulté (de 1 à 10). */
data class Memory(val stability: Double, val difficulty: Double)

/**
 * FSRS-5 (Free Spaced Repetition Scheduler, projet open-spaced-repetition), l'algorithme de
 * répétition espacée d'Anki. Il modélise la courbe d'oubli de chaque élément :
 * - la **stabilité** S est le nombre de jours au bout duquel la probabilité de s'en souvenir
 *   tombe à 90 % ;
 * - chaque révision réussie l'augmente, d'autant plus que l'élément était près d'être oublié
 *   (effet de l'effort de rappel) ; un oubli la fait chuter ;
 * - la prochaine révision tombe quand la probabilité de souvenir atteint [desiredRetention].
 * Les paramètres par défaut sont ceux publiés par le projet, ajustés sur des millions de révisions.
 */
class Fsrs(
    private val w: DoubleArray = DEFAULT_WEIGHTS,
    val desiredRetention: Double = 0.9,
    val maximumInterval: Int = 365,
) {
    init {
        require(w.size == 19) { "FSRS-5 attend 19 paramètres" }
        require(desiredRetention > 0.0 && desiredRetention < 1.0)
    }

    /** Probabilité de se souvenir, [elapsedDays] jours après la dernière révision. */
    fun retrievability(elapsedDays: Double, stability: Double): Double =
        (1 + FACTOR * max(0.0, elapsedDays) / stability).pow(DECAY)

    /** Mémoire après la toute première rencontre notée [grade]. */
    fun initial(grade: Grade): Memory = Memory(
        stability = w[grade.value - 1].coerceIn(MIN_STABILITY, MAX_STABILITY),
        difficulty = clampDifficulty(initialDifficulty(grade)),
    )

    /** Mémoire après une révision faite [elapsedDays] jours après la précédente (0 = le même jour). */
    fun review(memory: Memory, elapsedDays: Int, grade: Grade): Memory {
        val s = memory.stability
        val d = memory.difficulty
        val next = when {
            elapsedDays <= 0 -> shortTermStability(s, grade)
            grade == Grade.AGAIN -> forgetStability(d, s, retrievability(elapsedDays.toDouble(), s))
            else -> recallStability(d, s, retrievability(elapsedDays.toDouble(), s), grade)
        }
        return Memory(next.coerceIn(MIN_STABILITY, MAX_STABILITY), nextDifficulty(d, grade))
    }

    /** Nombre de jours avant la prochaine révision (de 1 à [maximumInterval]). */
    fun interval(stability: Double): Int {
        val days = stability / FACTOR * (desiredRetention.pow(1 / DECAY) - 1)
        return days.roundToInt().coerceIn(1, maximumInterval)
    }

    private fun initialDifficulty(grade: Grade): Double = w[4] - exp(w[5] * (grade.value - 1)) + 1

    private fun nextDifficulty(d: Double, grade: Grade): Double {
        val delta = -w[6] * (grade.value - 3)
        val damped = d + delta * (10 - d) / 9
        return clampDifficulty(w[7] * initialDifficulty(Grade.EASY) + (1 - w[7]) * damped)
    }

    private fun recallStability(d: Double, s: Double, r: Double, grade: Grade): Double {
        val hardPenalty = if (grade == Grade.HARD) w[15] else 1.0
        val easyBonus = if (grade == Grade.EASY) w[16] else 1.0
        return s * (1 + exp(w[8]) * (11 - d) * s.pow(-w[9]) * (exp((1 - r) * w[10]) - 1) * hardPenalty * easyBonus)
    }

    private fun forgetStability(d: Double, s: Double, r: Double): Double {
        val longTerm = w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp((1 - r) * w[14])
        val shortTerm = s / exp(w[17] * w[18])
        return min(longTerm, shortTerm)
    }

    private fun shortTermStability(s: Double, grade: Grade): Double = s * exp(w[17] * (grade.value - 3 + w[18]))

    private fun clampDifficulty(d: Double): Double = d.coerceIn(1.0, 10.0)

    companion object {
        const val DECAY = -0.5
        const val FACTOR = 19.0 / 81.0
        const val MIN_STABILITY = 0.01
        const val MAX_STABILITY = 36500.0

        /** Paramètres par défaut de FSRS-5. */
        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.40255, 1.18385, 3.173, 15.69105, 7.1949, 0.5345, 1.4604, 0.0046, 1.54575, 0.1192,
            1.01925, 1.9395, 0.11, 0.29605, 2.2698, 0.2315, 2.9898, 0.51655, 0.6621,
        )
    }
}
