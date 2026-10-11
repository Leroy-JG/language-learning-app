package io.github.leroyjg.arabe

import android.app.Application
import android.content.Context
import io.github.leroyjg.arabe.audio.AudioPlayer
import io.github.leroyjg.arabe.data.AssetContentSource
import io.github.leroyjg.arabe.data.ContentRepository

class ArabeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Dépendances de l'app, créées une seule fois (injection manuelle). */
class AppContainer(context: Context) {
    val content = ContentRepository(AssetContentSource(context.assets))
    val audio: AudioPlayer by lazy { AudioPlayer(context) }
}
