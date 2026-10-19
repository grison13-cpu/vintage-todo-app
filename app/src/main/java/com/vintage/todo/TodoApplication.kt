package com.vintage.todo

import android.app.Application
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Настраиваем ежесуточный запуск проверки задач в полночь
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val diff = dueDate.timeInMillis - currentDate.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<DailyWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyForwardWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
