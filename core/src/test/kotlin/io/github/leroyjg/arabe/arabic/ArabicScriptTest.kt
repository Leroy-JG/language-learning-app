package io.github.leroyjg.arabe.arabic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArabicScriptTest {

    @Test
    fun `texte correct`() {
        assertEquals(emptyList(), ArabicScript.problems("مَا هٰذَا؟ هٰذَا بَيْتٌ."))
        assertEquals(emptyList(), ArabicScript.problems("◌ٌ"))
        assertEquals(emptyList(), ArabicScript.problems("١٠"), "un chiffre arabe seul est un texte arabe valide")
        assertEquals(emptyList(), ArabicScript.problems("الْوَلَدُ جَالِسٌ، وَالْمُدَرِّسُ وَاقِفٌ."))
        assertEquals(emptyList(), ArabicScript.problems("اَلْكِتَابُ جَدِيدٌ."), "alif de l'article vocalisé en début de phrase")
        assertEquals(emptyList(), ArabicScript.problems("الِاسْتِفْهَامُ"), "lām vocalisé devant une hamza de liaison")
        assertEquals(emptyList(), ArabicScript.problems("الكتاب"), "mot non vocalisé : l'article n'est pas vérifié")
    }

    @Test
    fun `caractères interdits`() {
        assertTrue(ArabicScript.problems("مَا هٰذَا?").isNotEmpty(), "? latin")
        assertTrue(ArabicScript.problems("لَا, هٰذَا").isNotEmpty(), ", latine")
        assertTrue(ArabicScript.problems("هٰـذَا").isNotEmpty(), "tatweel")
        assertTrue(ArabicScript.problems("\uFEFBلا").isNotEmpty(), "forme de présentation")
        assertTrue(ArabicScript.problems("هٰذَا\u200Fبَيْتٌ").isNotEmpty(), "caractère invisible")
        assertTrue(ArabicScript.problems("ی").isNotEmpty(), "lettre persane")
        assertTrue(ArabicScript.problems("\u064C").isNotEmpty(), "signe sans lettre")
        assertTrue(ArabicScript.problems("هٰذَا  بَيْتٌ").isNotEmpty(), "double espace")
        assertTrue(ArabicScript.problems("hello").isNotEmpty(), "aucune lettre arabe")
    }

    @Test
    fun `article lunaire et solaire`() {
        assertTrue(ArabicScript.articleProblems("الكِتَابُ").isNotEmpty(), "sukūn manquant")
        assertTrue(ArabicScript.articleProblems("وَالمُدَرِّسُ").isNotEmpty(), "sukūn manquant après وَ")
        assertTrue(ArabicScript.articleProblems("الشَمْسُ").isNotEmpty(), "shadda manquante")
        assertEquals(emptyList(), ArabicScript.articleProblems("الشَّمْسُ اللَّحْمُ الْقَمَرُ بِالْقَلَمِ"))
    }

    @Test
    fun `masquer les harakat et comparer les lettres`() {
        assertEquals("هذا بيت.", ArabicScript.withoutHarakat("هٰذَا بَيْتٌ."))
        assertEquals("هذابيت", ArabicScript.lettersOnly("هٰذَا بَيْتٌ."))
    }

    @Test
    fun `deux textes qui ne diffèrent que par le démonstratif ont la même clé de distracteur`() {
        assertEquals(ArabicScript.distractorKey("مَا هٰذَا؟"), ArabicScript.distractorKey("مَا ذٰلِكَ؟"))
        assertEquals(ArabicScript.distractorKey("هٰذَا"), ArabicScript.distractorKey("ذٰلِكَ"))
        assertEquals(ArabicScript.distractorKey("مَنْ هٰذَا؟"), ArabicScript.distractorKey("مَنْ هٰذِهِ؟"))
        assertEquals(ArabicScript.distractorKey("مَنْ هٰذِهِ؟"), ArabicScript.distractorKey("مَنْ تِلْكَ؟"))
        assertTrue(ArabicScript.distractorKey("مَنْ ذٰلِكَ؟") != ArabicScript.distractorKey("مَا ذٰلِكَ؟"))
    }
}
