package com.example.habitz.feature.upserthabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.upserthabit.HabitFrequency

@Composable
fun FrequencySelector(
    selectedFrequency: HabitFrequency,
    onFrequencySelected: (HabitFrequency) -> Unit,
    specificDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    daysPerWeek: Int,
    onDaysPerWeekChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "FREQUENCY",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitFrequency.entries.forEach { freq ->
                val isSelected = freq == selectedFrequency
                FilterChip(
                    selected = isSelected,
                    onClick = { onFrequencySelected(freq) },
                    label = { Text(freq.label) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedFrequency) {
            HabitFrequency.SpecificDays -> {
                DayOfWeekSelector(
                    selectedDays = specificDays,
                    onDayToggle = onDayToggle
                )
            }
            HabitFrequency.XPerWeek -> {
                // Simplified version for now
                Text("Select $daysPerWeek days per week")
            }
            else -> {}
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
            specificDays = setOf(0, 2, 4),
            onDayToggle = {},
            daysPerWeek = 3,
            onDaysPerWeekChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
