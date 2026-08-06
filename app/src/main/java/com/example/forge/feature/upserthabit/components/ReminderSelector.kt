package com.example.forge.feature.upserthabit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReminderSelector(
    remindersEnabled: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    reminders: List<LocalTime>,
    onRemoveReminder: (LocalTime) -> Unit,
    onAddReminderClick: (time: LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var showTimePicker by remember{ mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Switch(
                checked = remindersEnabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemindersEnabledChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (remindersEnabled) {
            if (isError) {
                Text(
                    text = "Please add at least one reminder",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),// The gap between segmented items
            ) {
                val totalItems = reminders.size
                val formatter = DateTimeFormatter.ofPattern("h:mm a")

                reminders.forEachIndexed { index, time ->
                    val itemShapes = if (totalItems == 1) {
                        ListItemDefaults.shapes(shape = RoundedCornerShape(16.dp))
                    } else {
                        ListItemDefaults.segmentedShapes(index = index, count = totalItems)
                    }
                    SegmentedListItem(
                        onClick = {},
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shapes = itemShapes,
                        leadingContent = { Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) },
                        trailingContent = { IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRemoveReminder(time)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Remove reminder",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        } },
                        content = { Text(
                            text = time.format(formatter)
                        ) },
                    )

                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showTimePicker = true
                }
            ) {
                Text(
                    text = "+ Add reminder",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if(showTimePicker){
        TimeInputDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                hour, minute ->
                    val localTime = LocalTime.of(hour, minute)
                    onAddReminderClick(localTime)
                    showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    is24Hour: Boolean = false // Set false to show AM/PM selector
) {
    val haptic = LocalHapticFeedback.current
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Title
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // Time Input Fields (Automatically includes AM/PM toggle when is24Hour = false)
                TimeInput(state = timePickerState)

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderSelectorPreview() {
    ForgeTheme {
        ReminderSelector(
            remindersEnabled = true,
            onRemindersEnabledChange = {},
            reminders = listOf(LocalTime.of(7, 30), LocalTime.of(20, 0)),
            onRemoveReminder = {},
            onAddReminderClick = {} as (time: LocalTime) -> Unit,
            modifier = Modifier.padding(16.dp)
        )
    }
}
