package com.example.habitz.feature.habits.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.Reward
import com.example.habitz.core.services.interfaces.DailyCompletion
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.services.interfaces.MonthlyRate
import java.text.SimpleDateFormat
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

@Composable
fun RewardsSection(rewards: List<Reward>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Rewards",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(rewards) { reward ->
                RewardCard(reward)
            }
        }
    }
}

@Composable
fun RewardCard(reward: Reward) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isUnlocked) MaterialTheme.colorScheme.secondaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = reward.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = reward.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${reward.requiredStreak}d",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthlyCompletionChart(data: List<DailyCompletion>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Current Month", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { completion ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(completion.completionRatio.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(
                                if (completion.isBelowGoal) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun QuarterlyProgressCards(rates: List<MonthlyRate>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rates.forEach { rate ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = rate.monthName, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { rate.rate },
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "${(rate.rate * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdditionalDetailsSection(habit: Habit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(modifier = Modifier.fillMaxWidth()) {
        DetailRow("Frequency", habit.frequencyType.label)
        if (habit.reminders.isNotEmpty()) {
            DetailRow("Reminders", habit.reminders.joinToString(", "))
        }
        DetailRow("Started On", dateFormat.format(habit.createdAt))
        DetailRow("Daily Goal", "${habit.completionTargetPerDay} ${habit.targetUnit}")
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LogsSection(
    todayLogs: List<HabitActivity>,
    historicalLogs: List<HabitActivity>,
    onDeleteLog: (UUID) -> Unit
) {
    var historyExpanded by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    Column {
        Text(text = "Today's Logs", style = MaterialTheme.typography.titleMedium)
        if (todayLogs.isEmpty()) {
            Text(
                text = "No logs for today yet",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            todayLogs.forEach { log ->
                LogItem(
                    title = "${log.quantity} units",
                    subtitle = "at ${timeFormat.format(log.createdAt)}",
                    isDeletable = true,
                    onDelete = { onDeleteLog(log.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { historyExpanded = !historyExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Historical Activity", style = MaterialTheme.typography.titleMedium)
            Icon(
                if (historyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (historyExpanded) {
            if (historicalLogs.isEmpty()) {
                Text(
                    text = "No historical logs found",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                historicalLogs.forEach { log ->
                    LogItem(
                        title = "${log.quantity} units",
                        subtitle = "on ${dateFormat.format(log.createdAt)} at ${timeFormat.format(log.createdAt)}",
                        isDeletable = false
                    )
                }
            }
        }
    }
}

@Composable
fun LogItem(title: String, subtitle: String, isDeletable: Boolean, onDelete: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isDeletable) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
