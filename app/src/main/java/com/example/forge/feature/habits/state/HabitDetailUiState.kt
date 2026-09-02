package com.example.forge.feature.habits.state

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.HabitStats
import com.example.forge.core.uiEntities.ActivityData
import java.time.LocalDate
import java.time.YearMonth

data class HabitDetailUiState(
    val habit: Habit? = null,
    val stats: HabitStats? = null,
    val todayLogs: List<HabitActivity> = emptyList(),
    val monthlyCalendarData: List<ActivityData> = emptyList(),
    val allMonthlyCompletion: Map<YearMonth, List<DailyCompletion>> = emptyMap(),
    val completionMonths: List<YearMonth> = emptyList(),
    val selectedCalendarMonth: YearMonth = YearMonth.now(),
    val selectedCompletionMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val startDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
