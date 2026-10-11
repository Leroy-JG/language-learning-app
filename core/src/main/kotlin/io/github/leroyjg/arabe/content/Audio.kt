package io.github.leroyjg.arabe.content

import io.github.leroyjg.arabe.arabic.ArabicScript
import java.security.MessageDigest

/** Un audio attendu : le texte envoyé à la synthèse vocale et la voix (docs/content-format.md §10). */
data class AudioNeed(val text: String, val voiceKey: String)

object Audio {
    const val DIR = "audio"

    /** Nom du fichier : empreinte de la voix et du texte, donc régénéré dès que l'un d'eux change. */
    fun fileName(voice: Voice, text: String): String {
        val key = "${voice.provider}|${voice.name}|${voice.rate}|${ArabicScript.nfc(text)}"
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(20) + ".mp3"
    }

    /** Chemin dans les assets de l'audio d'un texte. */
    fun path(course: Course, need: AudioNeed): String = "$DIR/" + fileName(course.voice(need.voiceKey), need.text)

    /** Audio d'un élément : enregistrement manuel, ou synthèse de `tts` (sinon `ar`). */
    fun itemPath(course: Course, item: Item): String =
        item.audioFile ?: path(course, AudioNeed(item.tts ?: item.ar, item.voice ?: course.file.defaultVoice))

    /** Phrase complète d'un texte à trous (premier choix de chaque trou). */
    fun fillGapSentence(screen: FillGapScreen): String =
        screen.gaps.foldIndexed(screen.sentence) { i, s, gap -> s.replace("{$i}", gap.answers.first()) }

    /** Phrase complète d'une remise en ordre (première suite + ponctuation finale). */
    fun orderSentence(screen: OrderScreen): String =
        screen.answers.first().joinToString(" ") + (screen.punctuation ?: "")

    /** Tous les audios que le contenu attend (hors enregistrements manuels). */
    fun needs(course: Course): Set<AudioNeed> {
        val default = course.file.defaultVoice
        val out = linkedSetOf<AudioNeed>()
        fun add(text: String?, voice: String? = null) {
            if (text != null) out += AudioNeed(ArabicScript.nfc(text), voice ?: default)
        }
        for (item in course.itemsById.values) {
            if (item.audioFile == null) add(item.tts ?: item.ar, item.voice)
            add(item.plural, item.voice)
            add(item.example?.ar, item.voice)
        }
        for (activity in course.activities) for (screen in activity.screens) when (screen) {
            is TipScreen -> screen.blocks.filterIsInstance<ExampleBlock>().forEach { add(it.ar) }
            is FillGapScreen -> add(fillGapSentence(screen))
            is OrderScreen -> add(orderSentence(screen))
            is TypingScreen -> {
                add(screen.question)
                add(screen.answers?.firstOrNull())
            }
            is TrueFalseScreen -> add(screen.statement.ar)
            is QuestionScreen -> {
                add(screen.prompt.ar)
                screen.choices.forEach { add(it.ar) }
            }
            is DialogueScreen -> screen.lines.forEach { line -> add(line.ar, screen.speakers[line.speaker]?.voice) }
            is PresentScreen, is McqScreen, is MatchScreen -> Unit
        }
        return out
    }
}
