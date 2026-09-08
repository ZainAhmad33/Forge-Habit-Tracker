package com.example.forge.core.services.interfaces

import java.util.UUID

interface INotificationService {
    fun createNotificationChannel()
    fun showHabitReminder(habitId: UUID, title: String, emoji: String)
}
