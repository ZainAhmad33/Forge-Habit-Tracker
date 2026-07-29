package com.example.habitz.feature.habits.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.designsystem.theme.HabitzTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsSection(
    todayLogs: List<HabitActivity>,
    historicalLogs: List<HabitActivity>,
    onDeleteLog: (UUID) -> Unit
) {
    var historyExpanded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

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

@Preview(showBackground = true)
@Composable
fun LogsSectionPreview() {
    HabitzTheme {
        val todayLogs = listOf(
            HabitActivity(habitId = UUID.randomUUID(), quantity = 500, createdAt = Date()),
            HabitActivity(habitId = UUID.randomUUID(), quantity = 250, createdAt = Date())
        )
        val historicalLogs = listOf(
            HabitActivity(habitId = UUID.randomUUID(), quantity = 1000, createdAt = Date(System.currentTimeMillis() - 86400000)),
            HabitActivity(habitId = UUID.randomUUID(), quantity = 750, createdAt = Date(System.currentTimeMillis() - 172800000))
        )
        LogsSection(
            todayLogs = todayLogs,
            historicalLogs = historicalLogs,
            onDeleteLog = {}
        )
    }
}
