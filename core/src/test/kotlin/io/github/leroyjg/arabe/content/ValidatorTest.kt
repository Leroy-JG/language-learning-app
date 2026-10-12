package io.github.leroyjg.arabe.content

import io.github.leroyjg.arabe.TestContent
import io.github.leroyjg.arabe.replaceOnce
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidatorTest {

    @Test
    fun `le contenu de l'app est valide`() {
        val report = Validator(FileContentSource(TestContent.dir)).validate("ar")
        assertTrue(report.ok, "erreurs :\n" + report.errors.joinToString("\n"))
        assertTrue(report.itemCount > 0 && report.screenCount > 0)
    }

    /** Une erreur injectée dans une copie du contenu, et un extrait du message attendu. */
    private class Mutation(val name: String, val file: String, val old: String, val new: String, val expected: String)

    private val mutations = listOf(
        Mutation("champ inconnu (faute de frappe)", "medine-t1/l01/vocab-1.json",
            "\"mode\": \"ar-fr\", \"item\": \"w:bayt\"", "\"mode\": \"ar-fr\", \"itme\": \"w:bayt\"", "JSON non conforme"),
        Mutation("type d'écran inconnu", "medine-t1/l01/vocab-1.json",
            "\"type\": \"mcq\", \"mode\": \"icon-ar\", \"item\": \"w:masjid\"", "\"type\": \"qcm\", \"mode\": \"icon-ar\", \"item\": \"w:masjid\"", "JSON non conforme"),
        Mutation("mode non permis", "medine-t1/l01/vocab-1.json",
            "\"mode\": \"icon-ar\", \"item\": \"w:masjid\"", "\"mode\": \"image-ar\", \"item\": \"w:masjid\"", "JSON non conforme"),
        Mutation("élément utilisé avant d'être présenté", "medine-t1/l01/vocab-1.json",
            "\"mode\": \"ar-fr\", \"item\": \"w:bayt\"", "\"mode\": \"ar-fr\", \"item\": \"w:najm\"", "pas encore présenté"),
        Mutation("élément inconnu", "medine-t1/l01/vocab-1.json",
            "\"mode\": \"ar-fr\", \"item\": \"w:bayt\"", "\"mode\": \"ar-fr\", \"item\": \"w:inconnu\"", "élément inconnu"),
        Mutation("id d'écran en double", "medine-t1/l01/vocab-1.json",
            "\"id\": \"medine-t1.l01.vocab-1#2\"", "\"id\": \"medine-t1.l01.vocab-1#1\"", "en double"),
        Mutation("balise arabe non fermée", "medine-t1/l01/quiz.json",
            "désigne ce qui est proche ({ar|قَرِيبٌ}).", "désigne ce qui est proche ({ar|قَرِيبٌ).", "non fermée"),
        Mutation("arabe hors balise", "medine-t1/l01/quiz.json",
            "désigne ce qui est proche ({ar|قَرِيبٌ}).", "désigne ce qui est proche (قَرِيبٌ).", "hors balise"),
        Mutation("point d'interrogation latin", "medine-t1/l01/quiz.json",
            "\"question\": \"مَا هٰذَا؟\"", "\"question\": \"مَا هٰذَا?\"", "caractère interdit"),
        Mutation("tatweel", "medine-t1/l01/quiz.json",
            "\"question\": \"مَا هٰذَا؟\"", "\"question\": \"مَا هٰـذَا؟\"", "caractère interdit"),
        Mutation("lettre persane", "medine-t1/l01/items.json",
            "\"ar\": \"كِتَابٌ\"", "\"ar\": \"کِتَابٌ\"", "caractère interdit"),
        Mutation("mot non vocalisé", "medine-t1/l01/items.json",
            "\"ar\": \"كِتَابٌ\"", "\"ar\": \"كتاب\"", "non vocalisé"),
        Mutation("sukūn manquant devant une lettre lunaire", "medine-t1/l02/items.json",
            "إِمَامُ الْمَسْجِدِ", "إِمَامُ المَسْجِدِ", "sans sukūn"),
        Mutation("shadda manquante sur une lettre solaire", "medine-t1/l03/grammar-5.json",
            "\"ar\": \"الشَّمْسُ\", \"fr\": \"le soleil", "\"ar\": \"الشَمْسُ\", \"fr\": \"le soleil", "sans shadda"),
        Mutation("démonstratif masculin devant un nom féminin", "medine-t1/l03/vocab-3.json",
            "\"answers\": [\"السَّمَكَةُ\", \"السَّمَكُ\"]", "\"answers\": [\"هٰذَا سَمَكَةٌ.\"]", "devant un nom féminin"),
        Mutation("distracteur qui ne diffère que par le démonstratif", "medine-t1/l02/vocab-1.json",
            "\"distractors\": [\"p:man-dhalika\", \"p:man-hadha\"]", "\"distractors\": [\"p:man-dhalika\", \"p:ma-hadha\"]", "démonstratif"),
        Mutation("étiquettes qui ne diffèrent que par les harakat", "medine-t1/l03/grammar-3.json",
            "\"distractors\": [\"قَلَمٌ\"]", "\"distractors\": [\"الْقَلَمِ\"]", "harakat"),
        Mutation("options identiques sans harakat", "medine-t1/l03/quiz.json",
            "\"options\": [\"الْوَلَدُ\", \"وَلَدٌ\", \"مَفْتُوحٌ\"]", "\"options\": [\"الْوَلَدُ\", \"الْوَلَدَ\", \"مَفْتُوحٌ\"]", "options identiques"),
        Mutation("choix identiques sans harakat", "medine-t1/l03/quiz.json",
            "{ \"ar\": \"الْكِتَابُ\" }, { \"ar\": \"كُتُبٌ\" }", "{ \"ar\": \"الْكِتَابُ\" }, { \"ar\": \"الْكِتَابٌ\" }", "choix en double"),
        Mutation("réponse hors des options", "medine-t1/l01/grammar-1.json",
            "\"gaps\": [{ \"answers\": [\"بَابٌ\"] }]", "\"gaps\": [{ \"answers\": [\"سَرِيرٌ\"] }]", "absente des options"),
        Mutation("index de réponse hors bornes", "medine-t1/l01/quiz.json",
            "{ \"ar\": \"مَنْ هٰذَا؟\" }, { \"ar\": \"مَا هٰذَا؟\" }, { \"ar\": \"أَهٰذَا؟\" } ],\n      \"answer\": 0",
            "{ \"ar\": \"مَنْ هٰذَا؟\" }, { \"ar\": \"مَا هٰذَا؟\" }, { \"ar\": \"أَهٰذَا؟\" } ],\n      \"answer\": 3", "hors bornes"),
        Mutation("remise en ordre incohérente", "medine-t1/l01/grammar-1.json",
            "\"answers\": [[\"هٰذَا\", \"كِتَابٌ\"]]", "\"answers\": [[\"هٰذَا\", \"كِتَابٌ\"], [\"كِتَابٌ\", \"بَيْتٌ\"]]", "mêmes étiquettes"),
        Mutation("QCM image sur un élément sans icône", "medine-t1/l01/quiz.json",
            "\"mode\": \"icon-ar\", \"item\": \"w:miftah\"", "\"mode\": \"icon-ar\", \"item\": \"w:tajir\"", "sans icône"),
        Mutation("icône inconnue", "medine-t1/l01/items.json",
            "\"icon\": \"house\"", "\"icon\": \"maison\"", "icône inconnue"),
        Mutation("voix inconnue", "medine-t1/l01/dialogue-1.json",
            "\"voice\": \"m2\"", "\"voice\": \"m9\"", "voix inconnue"),
        Mutation("tableau irrégulier", "medine-t1/l01/intro.json",
            "[\"{ar|اِسْمٌ}\", \"un nom\"]", "[\"{ar|اِسْمٌ}\"]", "cellules"),
        Mutation("traduction en double dans un chapitre", "medine-t1/l01/items.json",
            "\"fr\": \"une porte\"", "\"fr\": \"une maison\"", "traduction identique"),
        Mutation("trous incohérents", "medine-t1/l01/grammar-1.json",
            "\"sentence\": \"هٰذَا {0}.\"", "\"sentence\": \"هٰذَا {1}.\"", "trous"),
    )

    @TestFactory
    fun `chaque erreur injectée est détectée`(): List<DynamicTest> = mutations.map { m ->
        DynamicTest.dynamicTest(m.name) {
            val copy = TestContent.copy()
            try {
                File(copy, "ar/${m.file}").replaceOnce(m.old, m.new)
                val report = Validator(FileContentSource(copy)).validate("ar")
                assertTrue(
                    report.errors.any { m.expected in it.message },
                    "« ${m.expected} » attendu, erreurs obtenues :\n" + report.errors.joinToString("\n"),
                )
            } finally {
                copy.deleteRecursively()
            }
        }
    }

    @Test
    fun `un fichier d'activité non listé est signalé`() {
        val copy = TestContent.copy()
        try {
            File(copy, "ar/medine-t1/l01/oubli.json").writeText("{}")
            val report = Validator(FileContentSource(copy)).validate("ar")
            assertTrue(report.errors.any { "non listés" in it.message }, report.errors.joinToString("\n"))
        } finally {
            copy.deleteRecursively()
        }
    }

    @Test
    fun `un audio manquant est une erreur dès que des audios existent`() {
        val report = Validator(FileContentSource(TestContent.dir), audioExists = { false }).validate("ar")
        assertTrue(report.errors.any { "manquant" in it.message })
    }
}
