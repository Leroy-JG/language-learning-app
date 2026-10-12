package io.github.leroyjg.arabe.arabic

import io.github.leroyjg.arabe.arabic.AnswerMatcher.Remark
import io.github.leroyjg.arabe.arabic.AnswerMatcher.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerMatcherTest {
    private fun verdict(input: String, vararg answers: String) = AnswerMatcher.check(input, answers.toList()).verdict
    private fun remarks(input: String, vararg answers: String) = AnswerMatcher.check(input, answers.toList()).remarks

    @Test
    fun `sans harakat, c'est correct`() {
        assertEquals(Verdict.CORRECT, verdict("هذا بيت", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.CORRECT, verdict("كتاب", "كِتَابٌ"))
        assertEquals(Verdict.CORRECT, verdict("ذلك", "ذٰلِكَ"))
    }

    @Test
    fun `ponctuation, espaces et tatweel sont ignorés`() {
        assertEquals(Verdict.CORRECT, verdict("  هذا   بيت  ", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.CORRECT, verdict("ما هذا?", "مَا هٰذَا؟"))
        assertEquals(Verdict.CORRECT, verdict("لا, هذا قلم.", "لَا، هٰذَا قَلَمٌ."))
        assertEquals(Verdict.CORRECT, verdict("هـذا", "هٰذَا"))
        assertEquals(Verdict.WRONG, verdict("هذا\u200Fبيت", "هٰذَا بَيْتٌ"), "sans espace, les deux mots sont collés")
        assertEquals(Verdict.CORRECT, verdict("هذا \u200Fبيت", "هٰذَا بَيْتٌ"))
    }

    @Test
    fun `harakat justes ou partielles acceptées`() {
        assertEquals(Verdict.CORRECT, verdict("هٰذَا بَيْتٌ", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.CORRECT, verdict("بَيت", "بَيْتٌ"))
        assertEquals(Verdict.CORRECT, verdict("هَذَا", "هٰذَا"), "fatha à la place de l'alif suscrit")
        assertEquals(Verdict.CORRECT, verdict("كُرْسيّ", "كُرْسِيٌّ"))
        assertEquals(Verdict.CORRECT, verdict("الشَّمْسُ", "الشَّمْسُ"))
        assertEquals(Verdict.CORRECT, verdict("الشّمس", "الشَّمْسُ"), "shadda sans voyelle")
    }

    @Test
    fun `l'ordre de saisie shadda et voyelle est sans importance`() {
        assertEquals(Verdict.CORRECT, verdict("قِطٌّ", "قِطٌّ"))
        assertEquals(Verdict.CORRECT, verdict("قِطٌّ", "قِطٌّ"))
        assertEquals(Verdict.CORRECT, verdict("قِطٌّ", "قِطٌّ"))
    }

    @Test
    fun `tanwin sur l'alif ou sur la lettre d'avant`() {
        assertEquals(Verdict.CORRECT, verdict("كتاباً", "كِتَابًا"))
        assertEquals(Verdict.CORRECT, verdict("كتابًا", "كِتَابًا"))
    }

    @Test
    fun `harakat fausses donnent une remarque`() {
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("بِيت", "بَيْتٌ"))
        assertEquals(setOf(Remark.HARAKAT), remarks("بِيت", "بَيْتٌ"))
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("الكتابُ", "الْكِتَابٌ"))
    }

    @Test
    fun `hamza et alif donnent une remarque`() {
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("اهذا بيت", "أَهٰذَا بَيْتٌ؟"))
        assertEquals(setOf(Remark.HAMZA_ALIF), remarks("اهذا بيت", "أَهٰذَا بَيْتٌ؟"))
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("امام", "إِمَامٌ"))
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("القران", "الْقُرْآنُ"))
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("ائمة", "أَئِمَّةٌ"))
    }

    @Test
    fun `ta marbuta et alif maqsura donnent une remarque`() {
        assertEquals(setOf(Remark.TA_MARBUTA), remarks("سمكه", "سَمَكَةٌ"))
        assertEquals(Verdict.CORRECT_WITH_REMARK, verdict("سمكه", "سَمَكَةٌ"))
        assertEquals(setOf(Remark.ALIF_MAQSURA), remarks("الي", "إِلَى").minus(Remark.HAMZA_ALIF))
    }

    @Test
    fun `lettres différentes, mots manquants ou en trop sont faux`() {
        assertEquals(Verdict.WRONG, verdict("هذا قلم", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.WRONG, verdict("هذا", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.WRONG, verdict("هذا بيت كبير", "هٰذَا بَيْتٌ."))
        assertEquals(Verdict.WRONG, verdict("هاذا", "هٰذَا"), "l'alif de هٰذَا ne s'écrit pas")
        assertEquals(Verdict.WRONG, verdict("الكتاب", "كِتَابٌ"))
        assertEquals(Verdict.WRONG, verdict("", "كِتَابٌ"))
        assertEquals(Verdict.WRONG, verdict("   ", "كِتَابٌ"))
        assertEquals(Verdict.WRONG, verdict("kitab", "كِتَابٌ"))
    }

    @Test
    fun `variantes de clavier persan et formes de présentation`() {
        assertEquals(Verdict.CORRECT, verdict("کتاب", "كِتَابٌ"))
        assertEquals(Verdict.CORRECT, verdict("كرسی", "كُرْسِيٌّ"))
        assertEquals(Verdict.CORRECT, verdict("\uFEFB", "لَا"), "lam-alif en forme de présentation")
    }

    @Test
    fun `la meilleure des réponses acceptées est retenue`() {
        val r = AnswerMatcher.check("نعم", listOf("نَعَمْ، هٰذَا نَجْمٌ.", "نَعَمْ."))
        assertEquals(Verdict.CORRECT, r.verdict)
        assertEquals("نَعَمْ.", r.expected)
        assertEquals("نَعَمْ، هٰذَا نَجْمٌ.", AnswerMatcher.check("لا", listOf("نَعَمْ، هٰذَا نَجْمٌ.", "نَعَمْ.")).expected)
    }
}
