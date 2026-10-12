package io.github.leroyjg.arabe.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Une activité terminée (une ligne par fin d'activité, jamais modifiée ni effacée).
 * La date locale sert à la série de jours ; l'heure exacte à l'historique.
 */
@Entity(tableName = "activity_result", indices = [Index("activity_id")])
data class ActivityResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "activity_id") val activityId: String,
    @ColumnInfo(name = "finished_at") val finishedAt: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
    val score: Double,
    @ColumnInfo(name = "first_try_correct") val firstTryCorrect: Int,
    @ColumnInfo(name = "exercise_count") val exerciseCount: Int,
    val xp: Int,
    val passed: Boolean,
)

@Dao
interface ProgressDao {
    @Insert
    suspend fun insert(result: ActivityResultEntity)

    @Query("SELECT DISTINCT activity_id FROM activity_result WHERE passed = 1")
    fun completedActivityIds(): Flow<List<String>>
}

/**
 * Base de la progression. Chaque changement de schéma passe par une migration écrite et testée :
 * la base n'est jamais effacée lors d'une mise à jour de l'app.
 */
@Database(entities = [ActivityResultEntity::class], version = 1, exportSchema = true)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        const val NAME = "progression.db"

        fun create(context: Context): ProgressDatabase =
            Room.databaseBuilder(context.applicationContext, ProgressDatabase::class.java, NAME).build()
    }
}

/** Accès à la progression ; les écritures continuent même si l'écran qui les demande se ferme. */
class ProgressRepository(private val dao: ProgressDao) {
    private val writes = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Ids des activités terminées (réussies au moins une fois). */
    val completed: Flow<Set<String>> = dao.completedActivityIds().map { it.toSet() }

    fun record(
        activityId: String,
        score: Double,
        firstTryCorrect: Int,
        exerciseCount: Int,
        xp: Int,
        passed: Boolean,
    ) {
        val entity = ActivityResultEntity(
            activityId = activityId,
            finishedAt = System.currentTimeMillis(),
            localDate = LocalDate.now().toString(),
            score = score,
            firstTryCorrect = firstTryCorrect,
            exerciseCount = exerciseCount,
            xp = xp,
            passed = passed,
        )
        writes.launch { dao.insert(entity) }
    }
}
