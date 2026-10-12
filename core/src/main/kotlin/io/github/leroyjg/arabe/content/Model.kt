package io.github.leroyjg.arabe.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modèles des fichiers JSON décrits dans docs/content-format.md.
// Tout champ inconnu fait échouer la lecture : une faute de frappe n'est jamais ignorée.

const val SUPPORTED_FORMAT_VERSION = 1

@Serializable
data class CourseFile(
    val formatVersion: Int,
    val id: String,
    val title: String,
    val voices: Map<String, Voice>,
    val defaultVoice: String,
    val levels: List<String>,
)

@Serializable
data class Voice(val provider: String, val name: String, val rate: String)

@Serializable
data class LevelFile(
    val id: String,
    val title: String,
    val description: String? = null,
    val source: String? = null,
    val chapters: List<String>,
)

@Serializable
data class ChapterFile(
    val id: String,
    val number: Int,
    val title: BiText,
    val description: String? = null,
    val source: String? = null,
    val sections: List<Section>,
)

@Serializable
data class BiText(val ar: String, val fr: String)

@Serializable
data class Section(val title: String, val activities: List<String>)

@Serializable
enum class ItemKind {
    @SerialName("word") WORD,
    @SerialName("phrase") PHRASE,
}

@Serializable
enum class Gender {
    @SerialName("m") MASCULINE,
    @SerialName("f") FEMININE,
}

@Serializable
data class Item(
    val id: String,
    val kind: ItemKind,
    val ar: String,
    val fr: String,
    val translit: String? = null,
    val plural: String? = null,
    val icon: String? = null,
    val gender: Gender? = null,
    val note: String? = null,
    val example: BiText? = null,
    val voice: String? = null,
    val tts: String? = null,
    val audioFile: String? = null,
    val review: Boolean = true,
)

@Serializable
enum class ActivityType {
    @SerialName("vocabulary") VOCABULARY,
    @SerialName("grammar") GRAMMAR,
    @SerialName("dialogue") DIALOGUE,
    @SerialName("review") REVIEW,
    @SerialName("quiz") QUIZ,
}

@Serializable
data class ActivityFile(
    val id: String,
    val type: ActivityType,
    val title: String,
    /** Image de la leçon dans le parcours (nom d'icône) ; sinon le symbole de son type. */
    val icon: String? = null,
    val screens: List<Screen>,
)

/** Support affiché au-dessus d'une question : exactement un des trois champs. */
@Serializable
data class ScreenContext(
    val icon: String? = null,
    val item: String? = null,
    val dialogue: String? = null,
)

/** Texte soit français, soit arabe (exactement un des deux). */
@Serializable
data class Txt(val fr: String? = null, val ar: String? = null)

@Serializable
sealed interface Screen {
    val id: String
}

/** Écran qui attend une réponse (tous sauf présentation, astuce et dialogue). */
sealed interface Exercise : Screen {
    val instruction: String?
    val context: ScreenContext?
    val explain: String?
}

@Serializable
@SerialName("present")
data class PresentScreen(override val id: String, val item: String) : Screen

@Serializable
@SerialName("tip")
data class TipScreen(override val id: String, val title: String, val blocks: List<Block>) : Screen

@Serializable
enum class McqMode {
    @SerialName("ar-fr") AR_FR,
    @SerialName("fr-ar") FR_AR,
    @SerialName("audio-ar") AUDIO_AR,
    @SerialName("icon-ar") ICON_AR,
}

@Serializable
@SerialName("mcq")
data class McqScreen(
    override val id: String,
    val mode: McqMode,
    val item: String,
    val distractors: List<String>? = null,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
enum class MatchMode {
    @SerialName("ar-fr") AR_FR,
    @SerialName("audio-ar") AUDIO_AR,
    @SerialName("sing-plur") SING_PLUR,
}

@Serializable
@SerialName("match")
data class MatchScreen(
    override val id: String,
    val mode: MatchMode,
    val items: List<String>,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
data class Gap(val answers: List<String>)

@Serializable
@SerialName("fill_gap")
data class FillGapScreen(
    override val id: String,
    val sentence: String,
    val fr: String,
    val gaps: List<Gap>,
    val options: List<String>,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
@SerialName("order")
data class OrderScreen(
    override val id: String,
    val fr: String,
    val answers: List<List<String>>,
    val punctuation: String? = null,
    val distractors: List<String>? = null,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
enum class TypingAsk {
    @SerialName("fr") FR,
    @SerialName("audio") AUDIO,
    @SerialName("question") QUESTION,
}

@Serializable
@SerialName("typing")
data class TypingScreen(
    override val id: String,
    val ask: TypingAsk,
    val item: String? = null,
    val answers: List<String>? = null,
    val fr: String? = null,
    val question: String? = null,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
data class Statement(val fr: String, val ar: String? = null)

@Serializable
@SerialName("true_false")
data class TrueFalseScreen(
    override val id: String,
    val statement: Statement,
    val answer: Boolean,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
@SerialName("question")
data class QuestionScreen(
    override val id: String,
    val prompt: Txt,
    val choices: List<Txt>,
    val answer: Int,
    val shuffle: Boolean = true,
    override val instruction: String? = null,
    override val context: ScreenContext? = null,
    override val explain: String? = null,
) : Exercise

@Serializable
data class Speaker(val name: String, val voice: String)

@Serializable
data class DialogueLine(val speaker: String, val ar: String, val fr: String)

@Serializable
@SerialName("dialogue")
data class DialogueScreen(
    override val id: String,
    val title: String,
    val intro: String? = null,
    val speakers: Map<String, Speaker>,
    val lines: List<DialogueLine>,
) : Screen

@Serializable
sealed interface Block

@Serializable
@SerialName("paragraph")
data class ParagraphBlock(val text: String) : Block

@Serializable
@SerialName("example")
data class ExampleBlock(val ar: String, val fr: String) : Block

@Serializable
@SerialName("table")
data class TableBlock(val header: List<String>, val rows: List<List<String>>) : Block

@Serializable
@SerialName("note")
data class NoteBlock(val text: String) : Block
