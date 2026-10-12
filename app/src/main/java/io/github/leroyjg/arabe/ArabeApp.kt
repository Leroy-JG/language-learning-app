package io.github.leroyjg.arabe

import android.app.Application
import android.content.Context
import io.github.leroyjg.arabe.audio.AudioPlayer
import io.github.leroyjg.arabe.data.AssetContentSource
import io.github.leroyjg.arabe.data.ContentRepository
import io.github.leroyjg.arabe.data.ProgressDatabase
import io.github.leroyjg.arabe.data.ProgressRepository

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
    val progress: ProgressRepository by lazy { ProgressRepository(ProgressDatabase.create(context).progressDao()) }
    val audio: AudioPlayer by lazy { AudioPlayer(context) }
}
