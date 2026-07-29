package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.Habit
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdditionalDetailsSection(habit: Habit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
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
