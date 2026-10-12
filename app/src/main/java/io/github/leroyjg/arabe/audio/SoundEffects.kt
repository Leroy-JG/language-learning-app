package io.github.leroyjg.arabe.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.data.SettingsRepository
import io.github.leroyjg.arabe.ui.LocalAppContainer

/** Bruitages de l'app (bonne réponse, erreur, paire trouvée, fin de leçon), selon le réglage. */
class SoundEffects(context: Context, private val settings: SettingsRepository) {
    enum class Effect { CORRECT, WRONG, PAIR, COMPLETE }

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val loaded = mutableSetOf<Int>()
    private val ids: Map<Effect, Int>

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) synchronized(loaded) { loaded += id } }
        val app = context.applicationContext
        ids = mapOf(
            Effect.CORRECT to pool.load(app, R.raw.sfx_correct, 1),
            Effect.WRONG to pool.load(app, R.raw.sfx_wrong, 1),
            Effect.PAIR to pool.load(app, R.raw.sfx_pair, 1),
            Effect.COMPLETE to pool.load(app, R.raw.sfx_complete, 1),
        )
    }

    fun play(effect: Effect) {
        if (!settings.current.soundEffects) return
        val id = ids[effect] ?: return
        if (synchronized(loaded) { id in loaded }) pool.play(id, VOLUME, VOLUME, 1, 0, 1f)
    }

    private companion object {
        const val VOLUME = 0.8f
    }
}

/** Retour haptique (vibration légère), selon le réglage ; aucune permission n'est nécessaire. */
class Haptics(private val view: View, private val settings: SettingsRepository) {
    fun success() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)

    fun error() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)

    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

    private fun perform(constant: Int) {
        if (settings.current.haptics) view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    val settings = LocalAppContainer.current.settings
    return remember(view, settings) { Haptics(view, settings) }
}
