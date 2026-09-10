package com.example.forge.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.INotificationService
import com.example.forge.core.services.interfaces.IReminderManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: INotificationService,
    private val habitRepository: IHabitRepository,
    private val reminderManager: IReminderManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitIdString = inputData.getString("habit_id") ?: return Result.failure()
        val habitId = UUID.fromString(habitIdString)
        val title = inputData.getString("title") ?: ""
        val emoji = inputData.getString("emoji") ?: ""

        // Show the notification
        notificationService.showHabitReminder(habitId, title, emoji)

        // Reschedule the next reminder
        val habit = habitRepository.getHabitById(habitId)
        if ((habit != null) && habit.reminders.isNotEmpty()) {
            reminderManager.updateReminders(habit)
        }

        return Result.success()
    }
}
