package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.Habit
import java.util.UUID

interface IReminderManager {
    fun updateReminders(habit: Habit)
    fun cancelReminders(habitId: UUID)
}
