package io.github.leroyjg.arabe.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.review.CardKind
import io.github.leroyjg.arabe.review.Completion
import io.github.leroyjg.arabe.review.Grade
import io.github.leroyjg.arabe.review.GradedKey
import io.github.leroyjg.arabe.review.Memory
import io.github.leroyjg.arabe.review.ReviewCard
import io.github.leroyjg.arabe.review.ReviewRules
import io.github.leroyjg.arabe.review.ReviewScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/** Mémoire d'un mot ou d'un point de grammaire (une ligne par carte, mise à jour à chaque révision). */
@Entity(tableName = "review_card")
data class ReviewCardEntity(
    @PrimaryKey @ColumnInfo(name = "card_key") val key: String,
    val kind: Int,
    val stability: Double,
    val difficulty: Double,
    @ColumnInfo(name = "last_review_day") val lastReviewDay: Long,
    @ColumnInfo(name = "due_day") val dueDay: Long,
    val reps: Int,
    val lapses: Int,
    @ColumnInfo(name = "last_grade") val lastGrade: Int,
)

/** Historique des révisions (jamais modifié) : servira aux statistiques. */
@Entity(tableName = "review_log", indices = [Index("card_key")])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "card_key") val cardKey: String,
    @ColumnInfo(name = "reviewed_at") val reviewedAt: Long,
    val day: Long,
    val grade: Int,
    /** 0 : leçon, 1 : séance de révision, 2 : reprise des leçons faites avant les révisions. */
    val source: Int,
)

/** Mot mis en favori. */
@Entity(tableName = "saved_item")
data class SavedItemEntity(
    @PrimaryKey @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long,
)

@Dao
abstract class ReviewDao {
    @Query("SELECT * FROM review_card")
    abstract fun cards(): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM review_card")
    abstract suspend fun allCards(): List<ReviewCardEntity>

    @Query("SELECT * FROM review_log ORDER BY id")
    abstract suspend fun allLogs(): List<ReviewLogEntity>

    @Query("SELECT item_id FROM saved_item")
    abstract fun savedIds(): Flow<List<String>>

    @Query("SELECT * FROM saved_item")
    abstract suspend fun allSaved(): List<SavedItemEntity>

    @Upsert
    abstract suspend fun upsertCards(cards: List<ReviewCardEntity>)

    @Insert
    abstract suspend fun insertLogs(logs: List<ReviewLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSaved(items: List<SavedItemEntity>)

    @Query("DELETE FROM saved_item WHERE item_id = :itemId")
    abstract suspend fun deleteSaved(itemId: String)

    @Query("DELETE FROM review_card")
    abstract suspend fun deleteCards()

    @Query("DELETE FROM review_log")
    abstract suspend fun deleteLogs()

    @Query("DELETE FROM saved_item")
    abstract suspend fun deleteAllSaved()

    @Transaction
    open suspend fun record(cards: List<ReviewCardEntity>, logs: List<ReviewLogEntity>) {
        upsertCards(cards)
        insertLogs(logs)
    }

    /** Remplace tout (import d'une sauvegarde, réinitialisation). */
    @Transaction
    open suspend fun replaceAll(cards: List<ReviewCardEntity>, logs: List<ReviewLogEntity>, saved: List<SavedItemEntity>) {
        deleteCards()
        deleteLogs()
        deleteAllSaved()
        upsertCards(cards)
        insertLogs(logs)
        insertSaved(saved)
    }
}

/** Base des révisions, séparée de celle de la progression. */
@Database(entities = [ReviewCardEntity::class, ReviewLogEntity::class, SavedItemEntity::class], version = 1, exportSchema = true)
abstract class ReviewDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao

    companion object {
        const val NAME = "revision.db"

        fun create(context: Context): ReviewDatabase =
            Room.databaseBuilder(context.applicationContext, ReviewDatabase::class.java, NAME).build()
    }
}

fun ReviewCardEntity.toCard(): ReviewCard? {
    val kind = CardKind.of(kind) ?: return null
    val grade = Grade.of(lastGrade) ?: return null
    if (!stability.isFinite() || stability <= 0.0 || !difficulty.isFinite()) return null
    return ReviewCard(key, kind, Memory(stability, difficulty), lastReviewDay, dueDay, reps, lapses, grade)
}

fun ReviewCard.toEntity() = ReviewCardEntity(
    key = key,
    kind = kind.code,
    stability = memory.stability,
    difficulty = memory.difficulty,
    lastReviewDay = lastReviewDay,
    dueDay = dueDay,
    reps = reps,
    lapses = lapses,
    lastGrade = lastGrade.value,
)

/** Mémoire des mots et de la grammaire ; les écritures continuent même si l'écran se ferme. */
class ReviewRepository(val dao: ReviewDao, val scheduler: ReviewScheduler = ReviewScheduler()) {
    private val writes = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()

    val cards: Flow<List<ReviewCard>> = dao.cards().map { list -> list.mapNotNull { it.toCard() } }
    val saved: Flow<Set<String>> = dao.savedIds().map { it.toSet() }

    fun today(): Long = LocalDate.now().toEpochDay()

    /** Fin d'une leçon : les mots vus entrent en mémoire, ceux interrogés sont révisés. */
    fun afterActivity(course: Course, activity: Activity, itemResults: Map<String, Boolean>, score: Double) {
        writes.launch {
            lock.withLock {
                val known = dao.allCards().map { it.key }.toSet()
                val graded = ReviewRules.afterActivity(course, activity, itemResults, score, known)
                apply(graded.map { it to today() }, SOURCE_LESSON)
            }
        }
    }

    /** Fin d'une séance de révision. */
    fun afterReview(graded: List<GradedKey>) {
        writes.launch { lock.withLock { apply(graded.map { it to today() }, SOURCE_REVIEW) } }
    }

    /** Crée les cartes des leçons terminées avant l'arrivée des révisions (sans toucher aux autres). */
    fun backfill(course: Course, progress: ProgressDao) {
        writes.launch {
            lock.withLock {
                val completions = progress.completions().mapNotNull { row ->
                    val day = runCatching { LocalDate.parse(row.firstDate).toEpochDay() }.getOrNull() ?: return@mapNotNull null
                    Completion(row.activityId, day, row.bestScore)
                }
                val known = dao.allCards().map { it.key }.toSet()
                val graded = ReviewRules.backfill(course, completions, known)
                if (graded.isNotEmpty()) apply(graded, SOURCE_BACKFILL)
            }
        }
    }

    fun setSaved(itemId: String, saved: Boolean) {
        writes.launch {
            if (saved) dao.insertSaved(listOf(SavedItemEntity(itemId, System.currentTimeMillis())))
            else dao.deleteSaved(itemId)
        }
    }

    private suspend fun apply(graded: List<Pair<GradedKey, Long>>, source: Int) {
        if (graded.isEmpty()) return
        val current = dao.allCards().mapNotNull { it.toCard() }.associateBy { it.key }.toMutableMap()
        val now = System.currentTimeMillis()
        val logs = mutableListOf<ReviewLogEntity>()
        val changed = linkedMapOf<String, ReviewCard>()
        for ((g, day) in graded) {
            val before = current[g.key]
            val after = if (before == null) scheduler.create(g.key, g.kind, g.grade, day) else scheduler.review(before, g.grade, day)
            current[g.key] = after
            changed[g.key] = after
            logs += ReviewLogEntity(cardKey = g.key, reviewedAt = now, day = day, grade = g.grade.value, source = source)
        }
        dao.record(changed.values.map { it.toEntity() }, logs)
    }

    companion object {
        const val SOURCE_LESSON = 0
        const val SOURCE_REVIEW = 1
        const val SOURCE_BACKFILL = 2
    }
}
