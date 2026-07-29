package com.example.habitz.feature.upserthabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.component.CounterInput
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.database.entity.HabitFrequency

@Composable
fun FrequencySelector(
    selectedFrequency: HabitFrequency,
    onFrequencySelected: (HabitFrequency) -> Unit,
    specificDays: List<Int>,
    onDayToggle: (Int) -> Unit,
    daysPerWeek: Int,
    onDaysPerWeekChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Frequency",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        )  {
            HabitFrequency.entries.forEach { freq ->
                val isSelected = freq == selectedFrequency
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFrequencySelected(freq)
                    },
                    label = { Text(freq.label, textAlign = TextAlign.Center) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = FilterChipDefaults.shape
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedFrequency) {
            HabitFrequency.SpecificDays -> {
                DayOfWeekSelector(
                    selectedDays = specificDays,
                    onDayToggle = onDayToggle,
                    isError = isError
                )
            }
            HabitFrequency.DaysPerWeek -> {
                CounterInput(
                    value = daysPerWeek,
                    valueMin = 1,
                    valueMax = 7,
                    onValueChange = onDaysPerWeekChange
                )
            }
            else -> {}
        }

        if (isError) {
            Text(
                text = "Please select at least one day",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            val backgroundColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDayToggle(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FrequencySelectorPreview() {
    HabitzTheme {
        FrequencySelector(
            selectedFrequency = HabitFrequency.SpecificDays,
            onFrequencySelected = {},
            specificDays = listOf(0, 2, 4),
            onDayToggle = {},
            daysPerWeek = 3,
            onDaysPerWeekChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FrequencySelectorErrorPreview() {
    HabitzTheme {
        FrequencySelector(
            selectedFrequency = HabitFrequency.SpecificDays,
            onFrequencySelected = {},
            specificDays = emptyList(),
            onDayToggle = {},
            daysPerWeek = 3,
            onDaysPerWeekChange = {},
            isError = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
