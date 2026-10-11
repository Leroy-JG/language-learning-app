import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.leroyjg.arabe"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val contentDir = layout.projectDirectory.dir("../app/src/main/assets/content")

tasks.test {
    useJUnitPlatform()
    systemProperty("contentDir", contentDir.asFile.absolutePath)
    inputs.dir(contentDir).withPropertyName("content")
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Valide tout le contenu des cours (le même code que celui qu'utilise l'app).
tasks.register<JavaExec>("validateContent") {
    group = "verification"
    description = "Valide le contenu des cours selon docs/content-format.md."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.leroyjg.arabe.tools.ContentToolKt")
    jvmArgs("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
    args("validate", contentDir.asFile.absolutePath)
}

// Liste les audios attendus (texte, voix, fichier) pour tools/generate_audio.py.
tasks.register<JavaExec>("audioManifest") {
    group = "content"
    description = "Écrit build/audio-manifest.json : les audios que le contenu attend."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.leroyjg.arabe.tools.ContentToolKt")
    jvmArgs("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
    args("audio-manifest", contentDir.asFile.absolutePath, layout.buildDirectory.file("audio-manifest.json").get().asFile.absolutePath)
}
