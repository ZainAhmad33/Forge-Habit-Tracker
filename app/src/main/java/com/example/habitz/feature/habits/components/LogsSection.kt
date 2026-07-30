package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.designsystem.theme.HabitzTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogsSection(
    todayLogs: List<HabitActivity>,
    onDeleteLog: (UUID) -> Unit,
    unit: String = ""
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val haptic = LocalHapticFeedback.current
        Text(
            text = "Today's activities",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight(700)
        )
        if (todayLogs.isEmpty()) {
            Text(
                text = "No logs for today yet",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),// The gap between segmented items
            ) {
                todayLogs.forEachIndexed { index, log ->
                    SegmentedListItem(
                        selected = false,
                        enabled = true,
                        onClick = {},
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = todayLogs.size),
                        leadingContent = { Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) },
                        trailingContent = { IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteLog(log.id)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Remove reminder",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        } },
                        content = { Text( text = "${log.quantity} ${unit}") },
                        supportingContent = { Text(text = "at ${timeFormat.format(log.createdAt)}")}
                    )
                }
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
        LogsSection(
            todayLogs = todayLogs,
            onDeleteLog = {}
        )
    }
}
