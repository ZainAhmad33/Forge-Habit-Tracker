package com.example.habitz.feature.upserthabit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ReminderSelector(
    remindersEnabled: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    reminders: List<LocalTime>,
    onRemoveReminder: (LocalTime) -> Unit,
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REMINDERS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Switch(
                checked = remindersEnabled,
                onCheckedChange = onRemindersEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (remindersEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            reminders.forEachIndexed { index, time ->
                ReminderItem(
                    time = time,
                    onRemove = { onRemoveReminder(time) }
                )
                if (index < reminders.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderItem(
    time: LocalTime,
    onRemove: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = time.format(formatter),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove reminder",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderSelectorPreview() {
    HabitzTheme {
        ReminderSelector(
            remindersEnabled = true,
            onRemindersEnabledChange = {},
            reminders = listOf(LocalTime.of(7, 30), LocalTime.of(20, 0)),
            onRemoveReminder = {},
            onAddReminderClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
