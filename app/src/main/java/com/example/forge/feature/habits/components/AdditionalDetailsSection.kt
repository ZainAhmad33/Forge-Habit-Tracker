package com.example.forge.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.ProgressShape
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdditionalDetailsSection(habit: Habit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Additional details",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight(700)
        )
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),// The gap between segmented items
        ) {
            val dayNames = listOf(
                "Mon",    // 0
                "Tue",   // 1
                "Wed", // 2
                "Thu",  // 3
                "Fri",    // 4
                "Sat",  // 5
                "Sun"     // 6
            )
            val frequency = when (habit.frequencyType) {
                HabitFrequency.EveryDay -> "Every day"
                HabitFrequency.DaysPerWeek -> "${habit.numberOfTrackedDays} days per week"
                HabitFrequency.SpecificDays -> habit.trackedDays.joinToString(", ") { dayNames[it] }
            }
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            
            val totalRows = 4 + (if (habit.reminders.isNotEmpty()) 1 else 0)
            var rowIndex = 0
            
            DetailRow(Icons.Rounded.Flag, "Target", "${habit.completionTargetPerDay} ${habit.targetUnit}", rowIndex++, totalRows)
            
            DetailRow(
                icon = Icons.Rounded.EventRepeat,
                label = "Frequency",
                index = rowIndex++,
                totalItems = totalRows
            ) {
                if (habit.frequencyType == HabitFrequency.SpecificDays) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        habit.trackedDays.forEach { dayIndex ->
                            ValueChip(text = dayNames[dayIndex])
                        }
                    }
                } else {
                    ValueChip(text = frequency)
                }
            }
            
            if (habit.reminders.isNotEmpty()){
                DetailRow(
                    icon = Icons.Rounded.Notifications,
                    label = "Reminders",
                    index = rowIndex++,
                    totalItems = totalRows
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        habit.reminders.sorted().forEach { time ->
                            ValueChip(text = time.format(formatter))
                        }
                    }
                }
            }
            
            DetailRow(
                icon = if (habit.isLockingEnabled) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                label = "Locking",
                value = if (habit.isLockingEnabled) "Enabled" else "Disabled",
                index = rowIndex++,
                totalItems = totalRows,
            )

            val formatterDT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
            DetailRow(
                icon = Icons.Rounded.CalendarMonth,
                label = "Created On",
                value = habit.createdAt.toInstant().atZone(ZoneId.systemDefault()).format(formatterDT),
                index = rowIndex,
                totalItems = totalRows,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailRow(icon: ImageVector, label: String, value: String, index: Int, totalItems: Int) {
    DetailRow(
        icon = icon,
        label = label,
        index = index,
        totalItems = totalItems
    ) {
        ValueChip(text = value)
    }
}

@Composable
private fun ValueChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    index: Int,
    totalItems: Int,
    trailingContent: @Composable () -> Unit
) {
    SegmentedListItem(
        selected = false,
        enabled = true,
        onClick = {},
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = totalItems),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    trailingContent()
                }
            }
        },
    )
}


@Preview
@Composable
fun AdditionalDetailsPreview() {
    val habit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Meditation",
        category = HabitCategory.Mindfulness,
        emoji = "🧘",
        habitType = HabitType.YesNo,
        reminders = listOf(
            LocalTime.of(17, 0),
            LocalTime.of(22, 0),
            LocalTime.of(19, 0),
            LocalTime.of(23, 0)
        ),
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Per Day",
        progressShape = ProgressShape.Pill,
        createdAt = Date(),
        updatedAt = Date()
    )
    ForgeTheme{
        AdditionalDetailsSection(habit)
    }
}
