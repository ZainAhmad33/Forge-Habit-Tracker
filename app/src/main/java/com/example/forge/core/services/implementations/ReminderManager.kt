package com.example.forge.core.services.implementations

import android.content.Context
import androidx.work.*
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.notifications.HabitReminderWorker
import com.example.forge.core.services.interfaces.IReminderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : IReminderManager {

    private val workManager = WorkManager.getInstance(context)

    override fun updateReminders(habit: Habit) {
        cancelReminders(habit.id)

        if (habit.reminders.isEmpty()) return

        val nextReminderTime = getNextReminderTime(habit)
        val delay = Duration.between(LocalDateTime.now(), nextReminderTime).toMillis()

        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("habit_${habit.id}")
            .setInputData(
                workDataOf(
                    "habit_id" to habit.id.toString(),
                    "title" to habit.title,
                    "emoji" to habit.emoji,
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

    private fun getNextReminderTime(habit: Habit): LocalDateTime {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val sortedReminders = habit.reminders.sorted()

        // 1. Check if there are more reminders today
        if (isScheduledOn(habit, today)) {
            for (time in sortedReminders) {
                val reminderDateTime = LocalDateTime.of(today, time)
                if (reminderDateTime.isAfter(now)) {
                    return reminderDateTime
                }
            }
        }

        // 2. Find the next scheduled day
        // Search up to 7 days ahead
        for (i in 1..7) {
            val nextDate = today.plusDays(i.toLong())
            if (isScheduledOn(habit, nextDate)) {
                return LocalDateTime.of(nextDate, sortedReminders.first())
            }
        }

        // Fallback: tomorrow first reminder
        return LocalDateTime.of(today.plusDays(1), sortedReminders.first())
    }

    private fun isScheduledOn(habit: Habit, date: LocalDate): Boolean {
        return when (habit.frequencyType) {
            HabitFrequency.EveryDay -> true
            HabitFrequency.SpecificDays -> {
                // UI uses 0-indexed days (0=Mon, 6=Sun)
                // DayOfWeek uses 1-7 (1=Mon, 7=Sun)
                val dayOfWeek = date.dayOfWeek.value
                habit.trackedDays.contains(dayOfWeek - 1)
            }
            HabitFrequency.DaysPerWeek -> true
        }
    }
}
