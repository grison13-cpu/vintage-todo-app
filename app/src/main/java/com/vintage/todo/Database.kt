package com.vintage.todo

import android.content.Context
import androidx.room.*

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    var dueDate: Long, // Храним дату в миллисекундах (начало дня)
    val isAutoForward: Boolean,
    var isCompleted: Boolean = false
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 OR dueDate = :today ORDER BY isCompleted ASC, id DESC")
    fun getTasksForToday(today: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDate < :today AND isAutoForward = 1")
    fun getOverdueTasksToForward(today: Long): List<Task>

    @Insert
    fun insert(task: Task)

    @Update
    fun update(task: Task)
}

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "todo.db"
                ).allowMainThreadQueries().build().also { instance = it }
            }
    }
}
