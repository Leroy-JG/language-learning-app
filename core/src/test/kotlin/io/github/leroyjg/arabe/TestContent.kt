package io.github.leroyjg.arabe

import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.ContentLoader
import io.github.leroyjg.arabe.content.FileContentSource
import java.io.File
import java.nio.file.Files

/** Accès au vrai contenu de l'app (app/src/main/assets/content) depuis les tests. */
object TestContent {
    val dir: File = File(System.getProperty("contentDir") ?: error("propriété contentDir absente"))

    val course: Course by lazy {
        val load = ContentLoader(FileContentSource(dir)).load("ar")
        check(load.issues.isEmpty()) { "contenu illisible : ${load.issues}" }
        load.course!!
    }

    /** Copie modifiable du contenu, pour y injecter des erreurs. */
    fun copy(): File {
        val target = Files.createTempDirectory("content").toFile()
        dir.copyRecursively(target)
        return target
    }
}

/** Remplace la première occurrence de [old] par [new] dans [file] ; échoue si [old] est absent. */
fun File.replaceOnce(old: String, new: String) {
    val text = readText()
    require(old in text) { "« $old » introuvable dans $this" }
    writeText(text.replaceFirst(old, new))
}
