package io.github.leroyjg.arabe.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Réglages de l'app (écran Paramètres). */
data class Settings(
    val showHarakat: Boolean = true,
    val showTranslit: Boolean = true,
    val arabicScale: Float = 1f,
    val autoPlay: Boolean = true,
    val speechRate: Float = 1f,
    val soundEffects: Boolean = true,
    val haptics: Boolean = true,
    val typingInReview: Boolean = true,
    val reviewSize: Int = 12,
    val theme: ThemeMode = ThemeMode.SYSTEM,
) {
    companion object {
        val ARABIC_SCALES = listOf(0.85f, 1f, 1.15f, 1.3f)
        val SPEECH_RATES = listOf(0.75f, 1f, 1.25f)
        val REVIEW_SIZES = listOf(8, 12, 20)
    }
}

/** Réglages enregistrés sur le téléphone ; chaque changement est appliqué tout de suite. */
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("reglages", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<Settings> = _state.asStateFlow()

    val current: Settings get() = _state.value

    fun update(transform: (Settings) -> Settings) {
        val next = transform(_state.value)
        _state.value = next
        write(next)
    }

    private fun read(): Settings {
        val d = Settings()
        return Settings(
            showHarakat = prefs.getBoolean(HARAKAT, d.showHarakat),
            showTranslit = prefs.getBoolean(TRANSLIT, d.showTranslit),
            arabicScale = prefs.getFloat(ARABIC_SCALE, d.arabicScale).takeIf { it in Settings.ARABIC_SCALES } ?: d.arabicScale,
            autoPlay = prefs.getBoolean(AUTO_PLAY, d.autoPlay),
            speechRate = prefs.getFloat(SPEECH_RATE, d.speechRate).takeIf { it in Settings.SPEECH_RATES } ?: d.speechRate,
            soundEffects = prefs.getBoolean(SOUND_EFFECTS, d.soundEffects),
            haptics = prefs.getBoolean(HAPTICS, d.haptics),
            typingInReview = prefs.getBoolean(TYPING_IN_REVIEW, d.typingInReview),
            reviewSize = prefs.getInt(REVIEW_SIZE, d.reviewSize).takeIf { it in Settings.REVIEW_SIZES } ?: d.reviewSize,
            theme = ThemeMode.entries.firstOrNull { it.name == prefs.getString(THEME, null) } ?: d.theme,
        )
    }

    private fun write(s: Settings) {
        prefs.edit()
            .putBoolean(HARAKAT, s.showHarakat)
            .putBoolean(TRANSLIT, s.showTranslit)
            .putFloat(ARABIC_SCALE, s.arabicScale)
            .putBoolean(AUTO_PLAY, s.autoPlay)
            .putFloat(SPEECH_RATE, s.speechRate)
            .putBoolean(SOUND_EFFECTS, s.soundEffects)
            .putBoolean(HAPTICS, s.haptics)
            .putBoolean(TYPING_IN_REVIEW, s.typingInReview)
            .putInt(REVIEW_SIZE, s.reviewSize)
            .putString(THEME, s.theme.name)
            .apply()
    }

    private companion object {
        const val HARAKAT = "harakat"
        const val TRANSLIT = "translitteration"
        const val ARABIC_SCALE = "taille_arabe"
        const val AUTO_PLAY = "lecture_auto"
        const val SPEECH_RATE = "vitesse_voix"
        const val SOUND_EFFECTS = "effets_sonores"
        const val HAPTICS = "vibrations"
        const val TYPING_IN_REVIEW = "ecriture_revisions"
        const val REVIEW_SIZE = "taille_revisions"
        const val THEME = "theme"
    }
}
