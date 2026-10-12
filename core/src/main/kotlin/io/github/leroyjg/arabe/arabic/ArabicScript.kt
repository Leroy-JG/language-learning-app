package io.github.leroyjg.arabe.arabic

import java.text.Normalizer

/** Caractères arabes et règles d'écriture du contenu (docs/content-format.md §3). */
object ArabicScript {
    const val CIRCLE = '\u25CC' // ◌ : support d'un signe montré seul
    const val TATWEEL = '\u0640'
    const val FATHATAN = '\u064B'
    const val FATHA = '\u064E'
    const val SHADDA = '\u0651'
    const val SUKUN = '\u0652'
    const val DAGGER_ALIF = '\u0670' // alif suscrit

    /** Lettres arabes de base (hamza … ghayn, fa … ya) et alif wasla. */
    fun isLetter(c: Char): Boolean = c in '\u0621'..'\u063A' || c in '\u0641'..'\u064A' || c == '\u0671'

    /** Harakat, tanwin, shadda, sukun, signes de hamza combinants et alif suscrit. */
    fun isMark(c: Char): Boolean = c in '\u064B'..'\u0655' || c == DAGGER_ALIF

    /** Signes masqués par le réglage « masquer les harakat ». */
    fun isHarakah(c: Char): Boolean = c in FATHATAN..SUKUN || c == DAGGER_ALIF

    fun isArabicDigit(c: Char): Boolean = c in '\u0660'..'\u0669'

    /** Lettres solaires : l'article ne se prononce pas devant elles et elles portent la shadda. */
    val SOLAR = "تثدذرزسشصضطظلن".toSet()

    /** Lettres lunaires : le lām de l'article porte un sukūn devant elles. */
    val LUNAR = "اأإآبجحخعغفقكموهي".toSet()

    /** Ponctuation admise dans un texte arabe du contenu. */
    private val ALLOWED_PUNCTUATION = "؟،؛.!:()«» -".toSet()

    fun nfc(s: String): String = Normalizer.normalize(s, Normalizer.Form.NFC)

    /** Texte tel qu'affiché avec le réglage « masquer les harakat ». */
    fun withoutHarakat(s: String): String = nfc(s).filterNot { isHarakah(it) }

    /** Seulement les lettres (sans signes, espaces ni ponctuation) : sert à comparer deux textes. */
    fun lettersOnly(s: String): String = nfc(s).filter { isLetter(it) }

    /**
     * Clé de comparaison des distracteurs d'un QCM : les lettres, avec ذلك ramené à هذا. Deux textes
     * qui ne diffèrent que par le démonstratif (« c'est » dans les deux cas en français) ont la même clé.
     */
    fun distractorKey(s: String): String = lettersOnly(s).replace("ذلك", "هذا")

    /**
     * Problèmes d'un texte arabe du contenu (vide si le texte est correct) :
     * caractère interdit, signe sans lettre, espaces superflus, article mal vocalisé.
     */
    fun problems(text: String): List<String> {
        val s = nfc(text)
        val out = mutableListOf<String>()
        if (s.none { isLetter(it) || it == CIRCLE }) out += "texte arabe sans lettre arabe : « $s »"
        var prev: Char? = null
        for ((i, c) in s.withIndex()) {
            when {
                isMark(c) -> if (prev == null || !(isLetter(prev) || isMark(prev) || prev == CIRCLE)) {
                    out += "signe sans lettre avant lui (position $i) : « $s »"
                }
                isLetter(c) || isArabicDigit(c) || c in ALLOWED_PUNCTUATION || c == CIRCLE -> Unit
                else -> out += "caractère interdit U+%04X dans « %s »".format(c.code, s)
            }
            prev = c
        }
        if ("  " in s || s != s.trim()) out += "espaces superflus dans « $s »"
        out += articleProblems(s)
        return out
    }

    /**
     * Article : lām avec sukūn devant une lettre lunaire ; lettre solaire avec shadda.
     * Seuls les mots vocalisés sont vérifiés (une astuce peut montrer un mot sans voyelles).
     */
    fun articleProblems(text: String): List<String> {
        val out = mutableListOf<String>()
        for (word in nfc(text).split(' ')) {
            if (word.none { isHarakah(it) }) continue
            val start = articleStart(word) ?: continue
            val next = word.getOrNull(start + 2) ?: continue
            if (!isLetter(next)) continue // lām déjà vocalisé (sukūn, kasra de liaison…)
            val marks = word.drop(start + 3).takeWhile { isMark(it) }
            if (next in LUNAR) out += "article sans sukūn devant la lettre lunaire $next : « $word »"
            else if (next in SOLAR && SHADDA !in marks) out += "lettre solaire $next sans shadda après l'article : « $word »"
        }
        return out
    }

    /** Position de « ال » en tête de mot, après un éventuel préfixe وَ فَ بِ كَ لِ ; null sinon. */
    private fun articleStart(word: String): Int? {
        if (word.startsWith("ال")) return 0
        if (word.length >= 4 && word[0] in "وفبكل" && word[1] in "\u064E\u064F\u0650" && word.startsWith("ال", 2)) return 2
        return null
    }
}
