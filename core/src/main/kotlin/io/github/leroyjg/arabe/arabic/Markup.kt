package io.github.leroyjg.arabe.arabic

/**
 * Texte français enrichi : `{ar|…}` marque un passage arabe, `**…**` du gras (docs/content-format.md §3).
 * Le même analyseur sert à l'affichage dans l'app et au validateur.
 */
object Markup {
    data class Segment(val text: String, val arabic: Boolean, val bold: Boolean)

    data class Parsed(val segments: List<Segment>, val errors: List<String>)

    fun parse(text: String): Parsed {
        val segments = mutableListOf<Segment>()
        val errors = mutableListOf<String>()
        val plain = StringBuilder()
        var bold = false
        var i = 0

        fun flush() {
            if (plain.isNotEmpty()) segments += Segment(plain.toString(), arabic = false, bold = bold)
            plain.clear()
        }

        while (i < text.length) {
            when {
                text.startsWith("{ar|", i) -> {
                    val end = text.indexOf('}', i)
                    if (end < 0) {
                        errors += "balise {ar|…} non fermée : « $text »"
                        break
                    }
                    val ar = text.substring(i + 4, end)
                    when {
                        ar.isEmpty() -> errors += "balise {ar|} vide : « $text »"
                        '{' in ar -> errors += "balise {ar|…} imbriquée : « $text »"
                        else -> {
                            flush()
                            segments += Segment(ArabicScript.nfc(ar), arabic = true, bold = bold)
                        }
                    }
                    i = end + 1
                }
                text.startsWith("**", i) -> {
                    flush()
                    bold = !bold
                    i += 2
                }
                else -> {
                    val c = text[i]
                    if (c == '{' || c == '}') errors += "accolade isolée ou balise mal formée : « $text »"
                    else if (c in '\u0600'..'\u06FF' || c == ArabicScript.CIRCLE) errors += "arabe hors balise {ar|…} : « $text »"
                    plain.append(c)
                    i++
                }
            }
        }
        flush()
        if (bold) errors += "gras ** non fermé : « $text »"
        return Parsed(segments, errors.distinct())
    }

    /** Texte sans balises (pour l'accessibilité ou les journaux). */
    fun plainText(text: String): String = parse(text).segments.joinToString("") { it.text }
}
