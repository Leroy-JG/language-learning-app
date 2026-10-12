package io.github.leroyjg.arabe.tools

import io.github.leroyjg.arabe.content.Audio
import io.github.leroyjg.arabe.content.ContentLoader
import io.github.leroyjg.arabe.content.FileContentSource
import io.github.leroyjg.arabe.content.Validator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

/**
 * Outil en ligne de commande sur le contenu (utilisé par Gradle et la CI) :
 *   validate <dossier content>               valide tous les cours
 *   audio-manifest <dossier content> <sortie> liste les audios attendus (pour tools/generate_audio.py)
 */
fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "validate" -> exitProcess(validate(File(args[1])))
        "audio-manifest" -> exitProcess(audioManifest(File(args[1]), File(args[2])))
        else -> {
            System.err.println("usage : validate <content> | audio-manifest <content> <sortie.json>")
            exitProcess(2)
        }
    }
}

/** Dossiers des cours : ceux qui contiennent un course.json. */
private fun courseDirs(content: File): List<String> =
    content.listFiles().orEmpty().filter { File(it, "course.json").isFile }.map { it.name }.sorted()

private fun audioRoot(content: File): File = File(content.parentFile, Audio.DIR)

private fun validate(content: File): Int {
    val audioRoot = audioRoot(content)
    val audioExists: ((String) -> Boolean)? =
        if (audioRoot.isDirectory) { path -> File(content.parentFile, path).isFile } else null
    var failed = false
    val courses = courseDirs(content)
    if (courses.isEmpty()) {
        println("ERREUR aucun cours dans $content")
        return 1
    }
    for (dir in courses) {
        val report = Validator(FileContentSource(content), audioExists).validate(dir)
        println("Cours « $dir » : ${report.itemCount} éléments, ${report.screenCount} écrans, ${report.audioNeeds} audios attendus")
        println("  écrans par type : ${report.screensByType}")
        report.warnings.forEach { println("  AVERTISSEMENT $it") }
        report.errors.forEach { println("  ERREUR $it") }
        println(if (report.ok) "  OK" else "  ${report.errors.size} erreur(s)")
        failed = failed || !report.ok
    }
    return if (failed) 1 else 0
}

@Serializable
private data class ManifestEntry(
    val file: String,
    val provider: String,
    val voice: String,
    val rate: String,
    val text: String,
)

private fun audioManifest(content: File, out: File): Int {
    val entries = mutableListOf<ManifestEntry>()
    for (dir in courseDirs(content)) {
        val load = ContentLoader(FileContentSource(content)).load(dir)
        val course = load.course
        if (course == null || load.issues.isNotEmpty()) {
            load.issues.forEach { System.err.println("ERREUR $it") }
            return 1
        }
        for (need in Audio.needs(course)) {
            val voice = course.voice(need.voiceKey)
            entries += ManifestEntry(Audio.path(course, need), voice.provider, voice.name, voice.rate, need.text)
        }
    }
    out.parentFile?.mkdirs()
    val json = Json { prettyPrint = true }
    out.writeText(json.encodeToString(entries.distinctBy { it.file }.sortedBy { it.file }) + "\n")
    println("${entries.size} audios listés dans $out")
    return 0
}
