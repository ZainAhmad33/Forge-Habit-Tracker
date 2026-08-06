package com.example.forge.core.services.interfaces

import com.example.forge.core.uiEntities.ActivityData
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.util.UUID

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val overallCompletionRate: Float,
    val monthlyCompletionData: List<DailyCompletion>,
    val quarterlyCompletionRates: List<MonthlyRate>
)

data class DailyCompletion(
    val day: Int,
    val completedQuantity: Int,
    val isSkipDay: Boolean = false
)

data class MonthlyRate(
    val monthName: String,
    val rate: Float
)

interface IHabitStatsService {
    fun getHabitStats(habitId: UUID): Flow<HabitStats>
    fun getMonthlyActivityData(habitId: UUID, yearMonth: YearMonth): Flow<List<ActivityData>>
    fun getRangeActivityData(habitId: UUID, startMonth: YearMonth, monthCount: Int): Flow<List<ActivityData>>
}
