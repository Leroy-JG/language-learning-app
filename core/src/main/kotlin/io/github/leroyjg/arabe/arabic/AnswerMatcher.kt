package io.github.leroyjg.arabe.arabic

import io.github.leroyjg.arabe.arabic.ArabicScript.isLetter
import io.github.leroyjg.arabe.arabic.ArabicScript.isMark
import java.text.Normalizer

/**
 * Correction tolérante d'une réponse écrite en arabe (docs/content-format.md §9.7) :
 * - correct : mêmes lettres, en ignorant harakat, alif suscrit, ponctuation et espaces en trop ;
 * - correct avec remarque : seule différence sur la hamza ou l'alif, ة/ه, ى/ي,
 *   ou des harakat saisies mais fausses ;
 * - faux sinon.
 */
object AnswerMatcher {
    enum class Verdict { CORRECT, CORRECT_WITH_REMARK, WRONG }

    enum class Remark { HAMZA_ALIF, TA_MARBUTA, ALIF_MAQSURA, HARAKAT }

    data class Result(
        val verdict: Verdict,
        /** Réponse attendue la plus proche (à afficher dans la correction). */
        val expected: String,
        val remarks: Set<Remark> = emptySet(),
    ) {
        val accepted: Boolean get() = verdict != Verdict.WRONG
    }

    fun check(input: String, answers: List<String>): Result {
        require(answers.isNotEmpty()) { "aucune réponse attendue" }
        val results = answers.map { checkOne(input, it) }
        return results.firstOrNull { it.verdict == Verdict.CORRECT }
            ?: results.firstOrNull { it.verdict == Verdict.CORRECT_WITH_REMARK }
            ?: Result(Verdict.WRONG, answers.first())
    }

    private fun checkOne(input: String, answer: String): Result {
        val typed = prepare(input)
        val expected = prepare(answer)
        if (typed.isEmpty()) return Result(Verdict.WRONG, answer)

        val typedLetters = baseLetters(typed)
        val expectedLetters = baseLetters(expected)
        if (typedLetters == expectedLetters) {
            return if (harakatAgree(typed, expected)) Result(Verdict.CORRECT, answer)
            else Result(Verdict.CORRECT_WITH_REMARK, answer, setOf(Remark.HARAKAT))
        }
        if (typedLetters.length == expectedLetters.length) {
            val remarks = mutableSetOf<Remark>()
            for (i in typedLetters.indices) {
                val a = typedLetters[i]
                val b = expectedLetters[i]
                if (a == b) continue
                val kind = lenientKind(a, b) ?: return Result(Verdict.WRONG, answer)
                remarks += kind
            }
            if (!harakatAgree(typed, expected)) remarks += Remark.HARAKAT
            return Result(Verdict.CORRECT_WITH_REMARK, answer, remarks)
        }
        return Result(Verdict.WRONG, answer)
    }

    /** Lettres confondues tolérées, et la remarque correspondante. */
    private fun lenientKind(a: Char, b: Char): Remark? {
        fun group(c: Char): Pair<Char, Remark>? = when (c) {
            'ا', 'أ', 'إ', 'آ', 'ٱ' -> 'ا' to Remark.HAMZA_ALIF
            'ؤ', 'و' -> 'و' to Remark.HAMZA_ALIF
            'ئ' -> 'ي' to Remark.HAMZA_ALIF
            'ة', 'ه' -> 'ه' to Remark.TA_MARBUTA
            'ى', 'ي' -> 'ي' to Remark.ALIF_MAQSURA
            else -> null
        }
        val ga = group(a) ?: return null
        val gb = group(b) ?: return null
        if (ga.first != gb.first) return null
        // ئ/ي : écart de hamza ; ى/ي : écart d'alif maqsura.
        return if (a == 'ئ' || b == 'ئ') Remark.HAMZA_ALIF else ga.second
    }

    /**
     * Mise en forme commune : formes de présentation et variantes de clavier ramenées aux lettres
     * de base, tatweel et caractères invisibles supprimés, ponctuation retirée, espaces normalisés.
     */
    internal fun prepare(s: String): String {
        val decomposed = Normalizer.normalize(s, Normalizer.Form.NFKC)
        val sb = StringBuilder()
        for (c in decomposed) {
            when {
                c == ArabicScript.TATWEEL -> Unit
                c in '\u200B'..'\u200F' || c in '\u202A'..'\u202E' || c in '\u2066'..'\u2069' || c == '\uFEFF' -> Unit
                c == 'ی' || c == 'ې' -> sb.append('ي')
                c == 'ک' -> sb.append('ك')
                c == 'ە' || c == 'ہ' -> sb.append('ه')
                c == 'ۀ' -> sb.append('ة')
                isLetter(c) || isMark(c) -> sb.append(c)
                c in '\u0656'..'\u065F' -> Unit
                else -> sb.append(' ') // ponctuation, chiffres latins, espaces…
            }
        }
        val compact = sb.toString().trim().replace(Regex(" +"), " ")
        return moveTanwinFromAlif(ArabicScript.nfc(compact))
    }

    /** « كتاباً » (tanwin posé sur l'alif) équivaut à « كتابًا » (tanwin sur la lettre d'avant). */
    private fun moveTanwinFromAlif(s: String): String =
        s.replace(Regex("([\\u0621-\\u064A])([\\u064B-\\u0655\\u0670]*)ا\\u064B"), "$1$2\u064Bا")

    private fun baseLetters(s: String): String = s.filterNot { isMark(it) }

    /**
     * Les harakat saisies sont-elles compatibles avec celles attendues ? On accepte une saisie
     * partielle (signes manquants) mais pas un signe faux. Les deux textes ont les mêmes lettres.
     */
    private fun harakatAgree(typed: String, expected: String): Boolean {
        val a = marksPerLetter(typed)
        val b = marksPerLetter(expected)
        if (a.size != b.size) return false
        return a.indices.all { b[it].containsAll(a[it]) }
    }

    /** Pour chaque lettre ou espace, l'ensemble des signes qui la suivent (alif suscrit compté comme fatha). */
    private fun marksPerLetter(s: String): List<Set<Char>> {
        val out = mutableListOf<MutableSet<Char>>()
        for (c in s) {
            if (isMark(c)) {
                out.lastOrNull()?.add(if (c == ArabicScript.DAGGER_ALIF) ArabicScript.FATHA else c)
            } else {
                out.add(mutableSetOf())
            }
        }
        return out
    }
}
