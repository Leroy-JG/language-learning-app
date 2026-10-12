import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Numéro de version : fourni par la CI (numéro du build), 1 en local.
val buildNumber = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 1

// Signature : la CI déchiffre la clé (signing/release.keystore.enc) et donne son chemin et son mot de passe.
// Sans eux, l'APK de release est signé avec la clé de debug (installation de test seulement).
val signingKeystore: String? = System.getenv("SIGNING_KEYSTORE")
val signingPassphrase: String? = System.getenv("SIGNING_PASSPHRASE")
val hasReleaseKey = !signingKeystore.isNullOrBlank() && !signingPassphrase.isNullOrBlank()

android {
    namespace = "io.github.leroyjg.arabe"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.leroyjg.arabe"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "0.1.$buildNumber"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(signingKeystore!!)
                storePassword = signingPassphrase
                keyAlias = "arabe"
                keyPassword = signingPassphrase
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseKey) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Schéma de la base exporté à chaque build : il sert à écrire et tester les migrations.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("io.github.leroyjg.arabe:core:1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
