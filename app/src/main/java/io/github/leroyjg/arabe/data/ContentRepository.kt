package io.github.leroyjg.arabe.data

import android.content.res.AssetManager
import io.github.leroyjg.arabe.content.ContentIssue
import io.github.leroyjg.arabe.content.ContentLoader
import io.github.leroyjg.arabe.content.ContentSource
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.CourseIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

/** Contenu des cours lu dans les assets de l'APK (dossier `content/`). */
class AssetContentSource(private val assets: AssetManager, private val root: String = "content") : ContentSource {
    override fun read(path: String): String? = try {
        assets.open("$root/$path").bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (_: FileNotFoundException) {
        null
    }

    override fun list(dir: String): List<String>? = assets.list("$root/$dir")?.toList()
}

sealed interface ContentState {
    data object Loading : ContentState
    class Ready(val course: Course, val index: CourseIndex) : ContentState
    class Failed(val issues: List<ContentIssue>) : ContentState
}

/** Charge le cours une fois, en arrière-plan, au démarrage de l'app. */
class ContentRepository(private val source: ContentSource, private val courseDir: String = "ar") {
    private val _state = MutableStateFlow<ContentState>(ContentState.Loading)
    val state: StateFlow<ContentState> = _state.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { _state.value = load() }
    }

    private fun load(): ContentState = try {
        val result = ContentLoader(source).load(courseDir)
        val course = result.course
        if (course != null && result.issues.isEmpty()) ContentState.Ready(course, CourseIndex(course))
        else ContentState.Failed(result.issues)
    } catch (e: Exception) {
        ContentState.Failed(listOf(ContentIssue(courseDir, e.toString())))
    }
}
