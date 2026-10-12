package io.github.leroyjg.arabe.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.content.Audio
import io.github.leroyjg.arabe.content.AudioNeed
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.data.SettingsRepository
import java.util.Locale
import java.util.UUID

/** Un son à jouer : celui d'un élément, ou d'un texte arabe avec une voix. */
sealed interface Sound {
    data class OfItem(val item: Item) : Sound
    data class OfText(val text: String, val voiceKey: String? = null) : Sound
}

/**
 * Joue les audios du cours : le MP3 généré s'il est dans l'APK (assets/audio), sinon la synthèse
 * vocale arabe du téléphone en dernier recours.
 */
class AudioPlayer(context: Context, private val settings: SettingsRepository) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val bundled: Set<String> by lazy { app.assets.list(Audio.DIR)?.toSet().orEmpty() }
    private val speech = PhoneSpeech(app)
    private var media: MediaPlayer? = null

    /** État de la voix du téléphone (utile seulement quand un MP3 manque). */
    val speechStatus: PhoneSpeech.Status get() = speech.status

    fun play(course: Course, sound: Sound, onDone: (() -> Unit)? = null) {
        stop()
        val (path, text) = when (sound) {
            is Sound.OfItem -> Audio.itemPath(course, sound.item) to sound.item.ar
            is Sound.OfText -> Audio.path(
                course,
                AudioNeed(ArabicScript.nfc(sound.text), sound.voiceKey ?: course.file.defaultVoice),
            ) to sound.text
        }
        val rate = settings.current.speechRate
        if (path.removePrefix("${Audio.DIR}/") in bundled && playAsset(path, rate, onDone)) return
        speech.speak(text, rate, onDone)
    }

    fun stop() {
        media?.let {
            it.setOnCompletionListener(null)
            runCatching { it.stop() }
            it.release()
        }
        media = null
        speech.stop()
    }

    private fun playAsset(path: String, rate: Float, onDone: (() -> Unit)?): Boolean = try {
        app.assets.openFd(path).use { fd ->
            val player = MediaPlayer()
            player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            player.setOnCompletionListener {
                if (media === it) {
                    it.release()
                    media = null
                }
                onDone?.let { done -> main.post { done() } }
            }
            player.prepare()
            // Vitesse réglée sans changer la hauteur de la voix.
            if (rate != 1f) runCatching { player.playbackParams = player.playbackParams.setSpeed(rate) }
            player.start()
            media = player
        }
        true
    } catch (_: Exception) {
        false
    }
}

/** Synthèse vocale arabe du téléphone. */
class PhoneSpeech(context: Context) : TextToSpeech.OnInitListener {
    enum class Status { STARTING, READY, NO_ARABIC_VOICE, UNAVAILABLE }

    var status by mutableStateOf(Status.STARTING)
        private set

    private val main = Handler(Looper.getMainLooper())
    private val callbacks = mutableMapOf<String, () -> Unit>()
    private var pending: Triple<String, Float, (() -> Unit)?>? = null
    private val tts = TextToSpeech(context, this)

    override fun onInit(result: Int) {
        main.post {
            if (result != TextToSpeech.SUCCESS) {
                status = Status.UNAVAILABLE
            } else {
                val lang = tts.setLanguage(Locale.forLanguageTag("ar"))
                status = if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Status.NO_ARABIC_VOICE
                } else {
                    Status.READY
                }
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) = Unit
                    override fun onDone(utteranceId: String) = finish(utteranceId)

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String) = finish(utteranceId)
                    override fun onStop(utteranceId: String, interrupted: Boolean) {
                        main.post { callbacks.remove(utteranceId) }
                    }
                })
            }
            pending?.let { (text, rate, done) ->
                pending = null
                speak(text, rate, done)
            }
        }
    }

    private fun finish(utteranceId: String) {
        main.post { callbacks.remove(utteranceId)?.invoke() }
    }

    fun speak(text: String, rate: Float, onDone: (() -> Unit)?) {
        when (status) {
            Status.STARTING -> pending = Triple(text, rate, onDone)
            Status.READY -> {
                tts.setSpeechRate(BASE_RATE * rate)
                val id = UUID.randomUUID().toString()
                if (onDone != null) callbacks[id] = onDone
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
            }
            // Pas de voix : on n'enchaîne pas en silence, mais on ne bloque pas non plus un dialogue.
            Status.NO_ARABIC_VOICE, Status.UNAVAILABLE -> onDone?.let { done -> main.post { done() } }
        }
    }

    fun stop() {
        pending = null
        callbacks.clear()
        if (status == Status.READY) tts.stop()
    }

    private companion object {
        /** La voix du téléphone est un peu ralentie : elle parle vite pour un débutant. */
        const val BASE_RATE = 0.85f
    }
}
