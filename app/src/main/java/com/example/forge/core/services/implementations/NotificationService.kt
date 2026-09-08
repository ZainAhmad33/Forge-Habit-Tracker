package com.example.forge.core.services.implementations

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.forge.MainActivity
import com.example.forge.R
import com.example.forge.core.services.interfaces.INotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) : INotificationService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationMessages = listOf(
        "Time to stay on track!",
        "Keep your momentum going.",
        "Time to keep the streak alive!",
        "One small step for today.",
        "Ready when you are.",
        "Make today count!",
        "Keep building your consistency.",
        "A little progress goes a long way!",
        "Don't let today slip by!",
        "Your future self will thank you.",
        "Another day, another step forward!",
        "Keep the momentum alive.",
        "You've got this. Time to get it done.",
        "Stay on track with your goals!",
        "Show up for yourself today.",
        "It's a good time to make it happen!"
    )
    companion object {
        private const val CHANNEL_ID = "habit_reminders"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for habit reminders"
    }

    override fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun showHabitReminder(habitId: UUID, title: String, emoji: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habit_id", habitId.toString())
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val message = notificationMessages.random()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_forge_flame)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }
}
