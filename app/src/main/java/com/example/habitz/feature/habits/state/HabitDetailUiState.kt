package com.example.habitz.feature.habits.state

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.services.interfaces.HabitStats

data class HabitDetailUiState(
    val habit: Habit? = null,
    val stats: HabitStats? = null,
    val todayLogs: List<HabitActivity> = emptyList(),
    val historicalLogs: List<HabitActivity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
