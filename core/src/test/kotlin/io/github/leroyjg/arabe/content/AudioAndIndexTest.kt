package io.github.leroyjg.arabe.content

import io.github.leroyjg.arabe.TestContent
import io.github.leroyjg.arabe.arabic.ArabicScript
import java.text.Normalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioAndIndexTest {
    private val hamed = Voice("edge", "ar-SA-HamedNeural", "-10%")

    @Test
    fun `nom de fichier audio = empreinte stable`() {
        // Valeurs calculées indépendamment (Python : sha256("edge|ar-SA-HamedNeural|-10%|<texte>")[:20]).
        assertEquals("6969e8d95d64c2fba7bd.mp3", Audio.fileName(hamed, "كِتَابٌ"))
        assertEquals("d820ca790ba69b31a5ed.mp3", Audio.fileName(hamed, "مَا هٰذَا؟"))
    }

    @Test
    fun `l'ordre de saisie des signes ne change pas l'empreinte`() {
        val a = "قِطٌّ"
        val b = "قِطٌّ"
        assertEquals(Audio.fileName(hamed, a), Audio.fileName(hamed, b))
        assertEquals(Normalizer.normalize(a, Normalizer.Form.NFC), Normalizer.normalize(b, Normalizer.Form.NFC))
    }

    @Test
    fun `chaque texte lu a son audio`() {
        val course = TestContent.course
        val needs = Audio.needs(course).map { it.text }.toSet()
        for (item in course.itemsById.values) assertTrue(ArabicScript.nfc(item.tts ?: item.ar) in needs, item.id)
        val dialogueLine = course.activity("medine-t1.l01.dialogue-1")!!.screens.filterIsInstance<DialogueScreen>().first().lines.first()
        assertTrue(Audio.needs(course).any { it.text == ArabicScript.nfc(dialogueLine.ar) && it.voiceKey == "m2" })
    }

    @Test
    fun `distracteurs automatiques déjà présentés, de même sorte, activité d'abord`() {
        val course = TestContent.course
        val index = CourseIndex(course)
        val target = course.item("w:bayt")!!
        val tiers = index.distractorTiers(target, "medine-t1.l01.vocab-1#4")
        // Avant cet écran : مَسْجِدٌ et بَابٌ dans l'activité, puis les trois sortes de mots de l'introduction.
        assertEquals(
            listOf(listOf("w:masjid", "w:bab"), listOf("w:ism", "w:fil", "w:harf")),
            tiers.map { tier -> tier.map { it.id } },
        )

        val late = index.distractorTiers(course.item("w:imam")!!, "medine-t1.l02.vocab-2#3")
        assertEquals(listOf("w:hajar"), late.first().map { it.id }, "même activité en premier")
        assertTrue(late.flatten().none { it.kind != ItemKind.WORD })
        assertTrue(late.flatten().map { it.fr }.toSet().size == late.flatten().size)
    }

    @Test
    fun `présenté avant`() {
        val index = CourseIndex(TestContent.course)
        val pos = index.position("medine-t1.l01.vocab-1#4")!!
        assertTrue(index.presentedBefore("w:bayt", pos))
        assertTrue(!index.presentedBefore("w:kitab", pos))
    }
}
