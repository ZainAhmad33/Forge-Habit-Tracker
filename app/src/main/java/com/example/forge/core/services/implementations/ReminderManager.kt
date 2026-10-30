package com.example.forge.core.services.implementations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.notifications.HabitReminderReceiver
import com.example.forge.core.services.interfaces.IReminderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val habitRepository: IHabitRepository,
) : IReminderManager {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun updateReminders(habit: Habit) {
        cancelReminders(habit.id)

        if (habit.reminders.isEmpty()) return

        val nextReminderTime = getNextReminderTime(habit)
        val triggerAtMillis = nextReminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = HabitReminderReceiver.ACTION_HABIT_REMINDER
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habit.id.toString())
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TITLE, habit.title)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_EMOJI, habit.emoji)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancelReminders(habitId: UUID) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = HabitReminderReceiver.ACTION_HABIT_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun rescheduleAllReminders() {
        val habits = habitRepository.getAllHabitsSync()
        for (habit in habits) {
            if (habit.reminders.isNotEmpty()) {
                updateReminders(habit)
            } else {
                cancelReminders(habit.id)
            }
        }
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
