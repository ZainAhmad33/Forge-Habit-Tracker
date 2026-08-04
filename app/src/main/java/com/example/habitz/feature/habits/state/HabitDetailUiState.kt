package com.example.habitz.feature.habits.state

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.uiEntities.ActivityData
import java.time.YearMonth

data class HabitDetailUiState(
    val habit: Habit? = null,
    val stats: HabitStats? = null,
    val todayLogs: List<HabitActivity> = emptyList(),
    val historicalLogs: List<HabitActivity> = emptyList(),
    val monthlyCalendarData: List<ActivityData> = emptyList(),
    val selectedCalendarMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val error: String? = null
)
