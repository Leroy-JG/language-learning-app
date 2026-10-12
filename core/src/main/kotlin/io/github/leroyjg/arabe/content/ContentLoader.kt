package io.github.leroyjg.arabe.content

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

/** Accès aux fichiers du contenu : les assets dans l'app, le disque dans les tests et les outils. */
interface ContentSource {
    /** Texte du fichier [path] (relatif à la racine du contenu), ou null s'il n'existe pas. */
    fun read(path: String): String?

    /** Noms des fichiers du dossier [dir], ou null si le dossier n'existe pas. */
    fun list(dir: String): List<String>?
}

class FileContentSource(private val root: File) : ContentSource {
    override fun read(path: String): String? = File(root, path).takeIf { it.isFile }?.readText(Charsets.UTF_8)
    override fun list(dir: String): List<String>? = File(root, dir).takeIf { it.isDirectory }?.list()?.sorted()
}

/** Lecteur JSON strict : un champ inconnu ou une valeur non permise est une erreur. */
val ContentJson: Json = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = false
    isLenient = false
}

data class ContentIssue(val where: String, val message: String) {
    override fun toString(): String = "$where: $message"
}

class Course(
    val dir: String,
    val file: CourseFile,
    val levels: List<Level>,
) {
    val id: String get() = file.id
    val chapters: List<Chapter> = levels.flatMap { it.chapters }

    /** Toutes les activités, dans l'ordre du parcours. */
    val activities: List<Activity> = chapters.flatMap { it.activities }

    val itemsById: Map<String, Item> = chapters.flatMap { it.items }.associateBy { it.id }
    private val chapterOfItem: Map<String, Chapter> = chapters.flatMap { c -> c.items.map { it.id to c } }.toMap()
    private val activitiesById: Map<String, Activity> = activities.associateBy { it.id }
    private val chaptersById: Map<String, Chapter> = chapters.associateBy { it.id }

    fun item(id: String): Item? = itemsById[id]
    fun chapterOfItem(itemId: String): Chapter? = chapterOfItem[itemId]
    fun activity(id: String): Activity? = activitiesById[id]
    fun chapter(id: String): Chapter? = chaptersById[id]
    fun levelOf(chapter: Chapter): Level = levels.first { chapter in it.chapters }

    fun voice(key: String?): Voice = file.voices.getValue(key ?: file.defaultVoice)
}

class Level(val dir: String, val file: LevelFile, val chapters: List<Chapter>) {
    val id: String get() = file.id
}

class Chapter(
    val dir: String,
    val levelId: String,
    val file: ChapterFile,
    val items: List<Item>,
    /** Activités dans l'ordre (parties puis activités). */
    val activities: List<Activity>,
) {
    val id: String get() = file.id
}

class Activity(
    val code: String,
    val chapterId: String,
    val sectionIndex: Int,
    val file: ActivityFile,
) {
    val id: String get() = file.id
    val screens: List<Screen> get() = file.screens
}

class LoadResult(val course: Course?, val issues: List<ContentIssue>)

/** Charge un cours complet ; chaque fichier illisible devient une erreur, sans arrêter le reste. */
class ContentLoader(private val source: ContentSource) {

    fun load(courseDir: String): LoadResult {
        val issues = mutableListOf<ContentIssue>()
        val courseFile = parse<CourseFile>("$courseDir/course.json", issues) ?: return LoadResult(null, issues)
        val levels = courseFile.levels.mapNotNull { levelDir ->
            val levelPath = "$courseDir/$levelDir"
            val levelFile = parse<LevelFile>("$levelPath/level.json", issues) ?: return@mapNotNull null
            val chapters = levelFile.chapters.mapNotNull { chapterDir ->
                loadChapter("$levelPath/$chapterDir", chapterDir, levelFile.id, issues)
            }
            Level(levelDir, levelFile, chapters)
        }
        return LoadResult(Course(courseDir, courseFile, levels), issues)
    }

    private fun loadChapter(path: String, dir: String, levelId: String, issues: MutableList<ContentIssue>): Chapter? {
        val chapterFile = parse<ChapterFile>("$path/chapter.json", issues) ?: return null
        val items = parse<List<Item>>("$path/items.json", issues) ?: emptyList()
        val activities = mutableListOf<Activity>()
        chapterFile.sections.forEachIndexed { sectionIndex, section ->
            for (code in section.activities) {
                val file = parse<ActivityFile>("$path/$code.json", issues) ?: continue
                activities += Activity(code, chapterFile.id, sectionIndex, file)
            }
        }
        return Chapter(dir, levelId, chapterFile, items, activities)
    }

    private inline fun <reified T> parse(path: String, issues: MutableList<ContentIssue>): T? {
        val text = source.read(path)
        if (text == null) {
            issues += ContentIssue(path, "fichier introuvable")
            return null
        }
        return try {
            ContentJson.decodeFromString<T>(text)
        } catch (e: SerializationException) {
            issues += ContentIssue(path, "JSON non conforme : ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            issues += ContentIssue(path, "JSON non conforme : ${e.message}")
            null
        }
    }
}
