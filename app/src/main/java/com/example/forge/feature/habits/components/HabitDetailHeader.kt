package com.example.forge.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.database.entity.CategoryToImage
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.HabitStats
import com.example.forge.core.uiEntities.ProgressShape
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.remember

@Composable
fun HabitDetailHeader(habit: Habit, stats: HabitStats) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = habit.emoji, fontSize = 48.sp)
                }
            }
            Spacer(Modifier.size(24.dp))
            Text(habit.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight(800))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                val icon = CategoryToImage[habit.category] ?: ""
                FilterChip(
                    label = {Text(text = habit.category.label)},
                    selected = true,
                    onClick = {},
                    leadingIcon = { Text(icon) }
                )
                FilterChip(
                    label = {Text(text = habit.completionTargetPerDay.toString() + " " + habit.targetUnit)},
                    selected = false,
                    onClick = {}
                )
            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ){
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(56.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                val streakLabel = remember(stats.currentStreak, stats.currentStreakStartDate) {
                    if (stats.currentStreak > 0 && stats.currentStreakStartDate != null) {
                        val formatter = DateTimeFormatter.ofPattern("MMM, d", Locale.getDefault())
                        "Started ${stats.currentStreakStartDate.format(formatter)}"
                    } else {
                        "No active streak"
                    }
                }
                StatItem(label = streakLabel, value = "🔥  ${stats.currentStreak}")
                VerticalDivider()
                StatItem(label = "Best streak", value = "🏆  ${stats.bestStreak}")
                VerticalDivider()
                StatItem(label = "Avg. completion", value = "${(stats.overallCompletionRate * 100).toInt()}%")
            }
        }

    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun HabitDetailHeaderPreview() {
    ForgeTheme {
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
            currentStreakStartDate = LocalDate.now().minusDays(4),
            trends = null
        )
        HabitDetailHeader(habit = habit, stats = stats)
    }
}

@Preview(showBackground = true)
@Composable
fun HabitDetailHeaderDaysPerWeekPreview() {
    ForgeTheme {
        val calendar = Calendar.getInstance()
        // Set to Wednesday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val createdDate = calendar.time

        val habit = Habit(
            id = UUID.randomUUID(),
            title = "Gym",
            category = HabitCategory.Health,
            emoji = "🏋️",
            habitType = HabitType.YesNo,
            reminders = emptyList(),
            frequencyType = HabitFrequency.DaysPerWeek,
            numberOfTrackedDays = 3,
            completionTargetPerDay = 1,
            targetUnit = "times per day",
            progressShape = ProgressShape.Pill,
            createdAt = createdDate,
            updatedAt = createdDate
        )
        val stats = HabitStats(
            currentStreak = 10,
            bestStreak = 15,
            overallCompletionRate = 0.9f,
            monthlyCompletionData = emptyList(),
            quarterlyCompletionRates = emptyList(),
            currentStreakStartDate = LocalDate.now().minusDays(9),
            trends = null
        )
        HabitDetailHeader(habit = habit, stats = stats)
    }
}
