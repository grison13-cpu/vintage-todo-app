package com.vintage.todo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class DailyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.taskDao()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Берем просроченные задачи с включенным флагом и "продлеваем" их на сегодня
        val oldTasks = dao.getOverdueTasksToForward(today)
        for (task in oldTasks) {
            task.dueDate = today
            dao.update(task)
        }
        return Result.success()
    }
}
