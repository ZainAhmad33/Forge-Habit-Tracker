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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.forge.core.designsystem.component.HeroStatCard
import com.example.forge.core.uiEntities.HeroStatItem
import androidx.compose.material.icons.rounded.Lock

@Composable
fun HabitDetailHeader(
    emoji: String,
    title: String,
    category: HabitCategory,
    completionTargetPerDay: Int,
    targetUnit: String,
    currentStreak: Int,
    bestStreak: Int,
    overallCompletionRate: Float,
    currentStreakStartDate: LocalDate?,
    isLocked: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = if (isLocked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 48.sp)
                    }
                }
                if (isLocked) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = 8.dp, y = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight(800))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                val icon = CategoryToImage[category] ?: ""
                FilterChip(
                    label = {Text(text = category.label)},
                    selected = true,
                    onClick = {},
                    leadingIcon = { Text(icon) }
                )
                FilterChip(
                    label = {Text(text = "$completionTargetPerDay $targetUnit")},
                    selected = false,
                    onClick = {}
                )
            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        val streakLabel = remember(currentStreak, currentStreakStartDate) {
            if (currentStreak > 0 && currentStreakStartDate != null) {
                val formatter = DateTimeFormatter.ofPattern("MMM, d", Locale.getDefault())
                "${currentStreakStartDate.format(formatter)}"
            } else {
                "-"
            }
        }
        val statItems = listOf(
            HeroStatItem(
                label = "Current streak",
                value = "$currentStreak days",
                icon = Icons.Rounded.LocalFireDepartment
            ),
            HeroStatItem(
                label = "Best streak",
                value = "$bestStreak days",
                icon = Icons.Rounded.EmojiEvents
            ),
            HeroStatItem(
                label = if (streakLabel == "-") "No active streak" else "Streak started",
                value = streakLabel,
                icon = Icons.Rounded.CalendarMonth
            ),
            HeroStatItem(
                label = "Overall completion",
                value = "${(overallCompletionRate * 100).toInt()}%",
                icon = Icons.Rounded.CheckCircle
            )
        )

        HeroStatCard(items = statItems)
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
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
        HabitDetailHeader(
            emoji = habit.emoji,
            title = habit.title,
            category = habit.category,
            completionTargetPerDay = habit.completionTargetPerDay,
            targetUnit = habit.targetUnit,
            currentStreak = stats.currentStreak,
            bestStreak = stats.bestStreak,
            overallCompletionRate = stats.overallCompletionRate,
            currentStreakStartDate = stats.currentStreakStartDate
        )
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
        HabitDetailHeader(
            emoji = habit.emoji,
            title = habit.title,
            category = habit.category,
            completionTargetPerDay = habit.completionTargetPerDay,
            targetUnit = habit.targetUnit,
            currentStreak = stats.currentStreak,
            bestStreak = stats.bestStreak,
            overallCompletionRate = stats.overallCompletionRate,
            currentStreakStartDate = stats.currentStreakStartDate
        )
    }
}
