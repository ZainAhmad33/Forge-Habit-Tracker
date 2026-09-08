package com.example.forge.core.services.implementations

import android.content.Context
import androidx.work.*
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.notifications.HabitReminderWorker
import com.example.forge.core.services.interfaces.IReminderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IReminderManager {

    private val workManager = WorkManager.getInstance(context)

    override fun updateReminders(habit: Habit) {
        cancelReminders(habit.id)

        if (habit.reminders.isEmpty()) return

        val nextReminderTime = getNextReminderTime(habit.reminders)
        val delay = Duration.between(LocalDateTime.now(), nextReminderTime).toMillis()

        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("habit_${habit.id}")
            .setInputData(
                workDataOf(
                    "habit_id" to habit.id.toString(),
                    "title" to habit.title,
                    "emoji" to habit.emoji
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            "habit_reminder_${habit.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun cancelReminders(habitId: UUID) {
        workManager.cancelUniqueWork("habit_reminder_$habitId")
    }

    private fun getNextReminderTime(reminders: List<LocalTime>): LocalDateTime {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        
        val sortedReminders = reminders.sorted()
        
        // Find the first reminder today that is in the future
        for (time in sortedReminders) {
            val reminderDateTime = LocalDateTime.of(today, time)
            if (reminderDateTime.isAfter(now)) {
                return reminderDateTime
            }
        }
        
        // If no more reminders today, take the first one tomorrow
        return LocalDateTime.of(today.plusDays(1), sortedReminders.first())
    }
}
