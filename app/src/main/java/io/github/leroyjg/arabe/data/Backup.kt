package io.github.leroyjg.arabe.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

/** Fichier de sauvegarde de la progression (export / import manuel). */
@Serializable
data class BackupFile(
    val format: String,
    val version: Int,
    val exportedAt: String,
    val results: List<BackupResult>,
    val cards: List<BackupCard>,
    val logs: List<BackupLog>,
    val saved: List<BackupSaved>,
)

@Serializable
data class BackupResult(
    val activityId: String,
    val finishedAt: Long,
    val localDate: String,
    val score: Double,
    val firstTryCorrect: Int,
    val exerciseCount: Int,
    val xp: Int,
    val passed: Boolean,
)

@Serializable
data class BackupCard(
    val key: String,
    val kind: Int,
    val stability: Double,
    val difficulty: Double,
    val lastReviewDay: Long,
    val dueDay: Long,
    val reps: Int,
    val lapses: Int,
    val lastGrade: Int,
)

@Serializable
data class BackupLog(val cardKey: String, val reviewedAt: Long, val day: Long, val grade: Int, val source: Int)

@Serializable
data class BackupSaved(val itemId: String, val savedAt: Long)

/** Export et import de toute la progression (leçons, révisions, favoris) dans un fichier JSON. */
class BackupManager(private val progress: ProgressDao, private val review: ReviewDao) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun export(out: OutputStream) {
        val file = BackupFile(
            format = FORMAT,
            version = VERSION,
            exportedAt = Instant.now().toString(),
            results = progress.all().map {
                BackupResult(it.activityId, it.finishedAt, it.localDate, it.score, it.firstTryCorrect, it.exerciseCount, it.xp, it.passed)
            },
            cards = review.allCards().map {
                BackupCard(it.key, it.kind, it.stability, it.difficulty, it.lastReviewDay, it.dueDay, it.reps, it.lapses, it.lastGrade)
            },
            logs = review.allLogs().map { BackupLog(it.cardKey, it.reviewedAt, it.day, it.grade, it.source) },
            saved = review.allSaved().map { BackupSaved(it.itemId, it.savedAt) },
        )
        out.bufferedWriter(Charsets.UTF_8).use { it.write(json.encodeToString(BackupFile.serializer(), file)) }
    }

    /** Lit et vérifie un fichier ; renvoie null s'il n'est pas une sauvegarde de l'app. */
    fun read(input: InputStream): BackupFile? = runCatching {
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        json.decodeFromString(BackupFile.serializer(), text)
    }.getOrNull()?.takeIf { it.format in ACCEPTED_FORMATS && it.version in 1..VERSION && it.isValid() }

    /** Remplace toute la progression par celle de la sauvegarde. */
    suspend fun restore(file: BackupFile) {
        progress.replaceAll(
            file.results.map {
                ActivityResultEntity(
                    activityId = it.activityId, finishedAt = it.finishedAt, localDate = it.localDate, score = it.score,
                    firstTryCorrect = it.firstTryCorrect, exerciseCount = it.exerciseCount, xp = it.xp, passed = it.passed,
                )
            },
        )
        review.replaceAll(
            cards = file.cards.map {
                ReviewCardEntity(it.key, it.kind, it.stability, it.difficulty, it.lastReviewDay, it.dueDay, it.reps, it.lapses, it.lastGrade)
            },
            logs = file.logs.map { ReviewLogEntity(cardKey = it.cardKey, reviewedAt = it.reviewedAt, day = it.day, grade = it.grade, source = it.source) },
            saved = file.saved.map { SavedItemEntity(it.itemId, it.savedAt) },
        )
    }

    /** Efface toute la progression. */
    suspend fun reset() {
        progress.replaceAll(emptyList())
        review.replaceAll(emptyList(), emptyList(), emptyList())
    }

    private fun BackupFile.isValid(): Boolean =
        results.all { it.activityId.isNotBlank() && it.score in 0.0..1.0 } &&
            cards.map { it.key }.toSet().size == cards.size &&
            cards.all { it.key.isNotBlank() && it.stability.isFinite() && it.stability > 0 && it.difficulty.isFinite() } &&
            saved.map { it.itemId }.toSet().size == saved.size

    companion object {
        const val FORMAT = "loura-progression"

        /** Les sauvegardes faites avant le changement de nom de l'app restent lisibles. */
        private val ACCEPTED_FORMATS = setOf(FORMAT, "lurata-progression")
        const val VERSION = 1
    }
}
