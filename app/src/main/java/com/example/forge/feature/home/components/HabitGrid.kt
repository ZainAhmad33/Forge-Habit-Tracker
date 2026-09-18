package com.example.forge.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.uiEntities.HomeHabit

@Composable
fun HabitGrid(
    habits: List<HomeHabit>,
    modifier: Modifier = Modifier,
    onHabitCardClick: (habit: HomeHabit) -> Unit,
    onHabitDetailsClick: (habitId: String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        habits.chunked(2).forEachIndexed { rowIndex, rowHabits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowHabits.forEachIndexed { colIndex, habit ->
                    val isFirstItem = rowIndex == 0 && colIndex == 0
                    HabitCard(
                        habit = habit,
                        modifier = Modifier.weight(1f),
                        enableOnboardingHints = isFirstItem,
                        onHabitCardClick = onHabitCardClick,
                        onDetailsClick = { onHabitDetailsClick(habit.id) }
                    )
                }
                if (rowHabits.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
