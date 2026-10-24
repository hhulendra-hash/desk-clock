package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_routines")
data class SavedRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val unstructuredPrompt: String,
    val strictJsonPayload: String,
    val totalStudyMinutes: Int,
    val totalBreakMinutes: Int,
    val hydrationCount: Int,
    val startTime: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface SavedRoutineDao {
    @Query("SELECT * FROM saved_routines ORDER BY createdAtMillis DESC")
    fun getAllRoutines(): Flow<List<SavedRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: SavedRoutineEntity)

    @Query("DELETE FROM saved_routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Int)
}

@Database(entities = [SavedRoutineEntity::class], version = 1, exportSchema = false)
abstract class ChronoStudyDatabase : RoomDatabase() {
    abstract fun savedRoutineDao(): SavedRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: ChronoStudyDatabase? = null

        fun getInstance(context: Context): ChronoStudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronoStudyDatabase::class.java,
                    "chronostudy_routines.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class RoutineRepository(private val dao: SavedRoutineDao) {
    val allSavedRoutines: Flow<List<SavedRoutineEntity>> = dao.getAllRoutines()

    suspend fun saveRoutine(routine: SavedRoutineEntity) {
        dao.insertRoutine(routine)
    }

    suspend fun deleteRoutine(id: Int) {
        dao.deleteRoutineById(id)
    }
}
