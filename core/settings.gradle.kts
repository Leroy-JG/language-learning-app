// Build autonome : la logique de l'app (contenu, arabe, moteur) se compile et se teste sans le SDK Android.
// L'app l'utilise via includeBuild("core") dans settings.gradle.kts à la racine.
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "core"
