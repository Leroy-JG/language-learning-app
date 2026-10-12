package io.github.leroyjg.arabe.arabic

import io.github.leroyjg.arabe.arabic.Markup.Segment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkupTest {

    @Test
    fun `texte français avec arabe et gras`() {
        val p = Markup.parse("{ar|هٰذَا} est un **nom démonstratif** ({ar|اِسْمُ إِشَارَةٍ}).")
        assertEquals(emptyList(), p.errors)
        assertEquals(
            listOf(
                Segment("هٰذَا", arabic = true, bold = false),
                Segment(" est un ", arabic = false, bold = false),
                Segment("nom démonstratif", arabic = false, bold = true),
                Segment(" (", arabic = false, bold = false),
                Segment("اِسْمُ إِشَارَةٍ", arabic = true, bold = false),
                Segment(").", arabic = false, bold = false),
            ),
            p.segments,
        )
    }

    @Test
    fun `arabe en gras`() {
        val p = Markup.parse("**{ar|ذٰلِكَ}**")
        assertEquals(listOf(Segment("ذٰلِكَ", arabic = true, bold = true)), p.segments)
    }

    @Test
    fun `erreurs de balisage`() {
        assertTrue(Markup.parse("{ar|هٰذَا").errors.isNotEmpty())
        assertTrue(Markup.parse("{ar|}").errors.isNotEmpty())
        assertTrue(Markup.parse("un } isolé").errors.isNotEmpty())
        assertTrue(Markup.parse("**gras").errors.isNotEmpty())
        assertTrue(Markup.parse("arabe هٰذَا nu").errors.isNotEmpty())
        assertTrue(Markup.parse("{fr|bonjour}").errors.isNotEmpty())
    }
}
