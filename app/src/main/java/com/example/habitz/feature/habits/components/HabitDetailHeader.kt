package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.uiEntities.ProgressShape
import java.util.*

@Composable
fun HabitDetailHeader(habit: Habit, stats: HabitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = habit.emoji + " " + habit.title, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = habit.category.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${(stats.overallCompletionRate * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(label = "Current Streak", value = "${stats.currentStreak} 🔥")
                StatItem(label = "Best Streak", value = "${stats.bestStreak} ⭐")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun HabitDetailHeaderPreview() {
    HabitzTheme {
        val habit = Habit(
            id = UUID.randomUUID(),
            title = "Drink Water",
            category = HabitCategory.Health,
            emoji = "💧",
            habitType = HabitType.Quantity,
            reminders = emptyList(),
            frequencyType = HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 2000,
            targetUnit = "ml",
            progressShape = ProgressShape.Circle,
            createdAt = Date(),
            updatedAt = Date()
        )
        val stats = HabitStats(
            currentStreak = 5,
            bestStreak = 12,
            overallCompletionRate = 0.85f,
            monthlyCompletionData = emptyList(),
            quarterlyCompletionRates = emptyList(),
            rewards = emptyList()
        )
        HabitDetailHeader(habit = habit, stats = stats)
    }
}
