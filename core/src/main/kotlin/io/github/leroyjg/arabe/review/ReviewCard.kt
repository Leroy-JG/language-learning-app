package io.github.leroyjg.arabe.review

/** Ce qu'on révise. */
enum class CardKind(val code: Int) {
    /** Un mot ou une phrase du cours (clé : id de l'élément, `w:…` ou `p:…`). */
    ITEM(0),

    /** Un point de grammaire (clé : id d'une activité de type `grammar`). */
    GRAMMAR(1),
    ;

    companion object {
        fun of(code: Int): CardKind? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Carte de révision : la mémoire d'un élément. Les dates sont des jours locaux (nombre de jours
 * depuis le 1er janvier 1970), pour que les révisions suivent le calendrier de l'utilisateur.
 */
data class ReviewCard(
    val key: String,
    val kind: CardKind,
    val memory: Memory,
    val lastReviewDay: Long,
    val dueDay: Long,
    val reps: Int,
    val lapses: Int,
    val lastGrade: Grade,
)

/** Niveau de maîtrise affiché (trois barres, à la Busuu). */
enum class Mastery {
    /** À travailler / non acquis. */
    WEAK,

    /** À revoir / en progrès. */
    MEDIUM,

    /** Acquis. */
    STRONG,
}

/** Planification des révisions avec FSRS. */
class ReviewScheduler(val fsrs: Fsrs = Fsrs()) {

    /** Première rencontre avec un élément. */
    fun create(key: String, kind: CardKind, grade: Grade, today: Long): ReviewCard {
        val memory = fsrs.initial(grade)
        return ReviewCard(
            key = key,
            kind = kind,
            memory = memory,
            lastReviewDay = today,
            dueDay = today + fsrs.interval(memory.stability),
            reps = 1,
            lapses = if (grade == Grade.AGAIN) 1 else 0,
            lastGrade = grade,
        )
    }

    /** Révision d'une carte existante. Une date antérieure à la dernière révision compte comme le même jour. */
    fun review(card: ReviewCard, grade: Grade, today: Long): ReviewCard {
        val elapsed = (today - card.lastReviewDay).coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val memory = fsrs.review(card.memory, elapsed, grade)
        val day = maxOf(today, card.lastReviewDay)
        return card.copy(
            memory = memory,
            lastReviewDay = day,
            dueDay = day + fsrs.interval(memory.stability),
            reps = card.reps + 1,
            lapses = card.lapses + if (grade == Grade.AGAIN) 1 else 0,
            lastGrade = grade,
        )
    }

    /** Probabilité de s'en souvenir aujourd'hui. */
    fun retrievability(card: ReviewCard, today: Long): Double =
        fsrs.retrievability((today - card.lastReviewDay).toDouble(), card.memory.stability)

    fun isDue(card: ReviewCard, today: Long): Boolean = card.dueDay <= today

    /**
     * Maîtrise : **acquis** quand le souvenir tient au moins trois semaines et reste frais ;
     * **à travailler** après un oubli, quand le souvenir est très fragile ou déjà bien effacé ;
     * **à revoir** entre les deux.
     */
    fun mastery(card: ReviewCard, today: Long): Mastery {
        val r = retrievability(card, today)
        return when {
            card.lastGrade == Grade.AGAIN || card.memory.stability < WEAK_STABILITY || r < WEAK_RETRIEVABILITY -> Mastery.WEAK
            card.memory.stability >= STRONG_STABILITY && r >= STRONG_RETRIEVABILITY -> Mastery.STRONG
            else -> Mastery.MEDIUM
        }
    }

    companion object {
        const val WEAK_STABILITY = 1.5
        const val WEAK_RETRIEVABILITY = 0.75
        const val STRONG_STABILITY = 21.0
        const val STRONG_RETRIEVABILITY = 0.85
    }
}
