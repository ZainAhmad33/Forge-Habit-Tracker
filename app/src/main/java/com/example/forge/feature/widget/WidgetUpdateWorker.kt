package com.example.forge.feature.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.forge.core.services.interfaces.IHabitMaintenanceService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val maintenanceService: IHabitMaintenanceService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            maintenanceService.performMaintenanceForAll()
            scheduleNextMidnightUpdate(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "DailyWidgetUpdateWork"

        fun scheduleNextMidnightUpdate(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delay = Duration.between(now, nextMidnight).toMillis()

            val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
