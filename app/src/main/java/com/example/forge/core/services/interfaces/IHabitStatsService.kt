package com.example.forge.core.services.interfaces

import com.example.forge.core.uiEntities.ActivityData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val overallCompletionRate: Float,
    val monthlyCompletionData: List<DailyCompletion>,
    val quarterlyCompletionRates: List<MonthlyRate>,
    val currentStreakStartDate: LocalDate? = null,
    val trends: HabitTrends? = null
)

data class StreakInfo(
    val count: Int,
    val startDate: LocalDate?
)

data class HabitTrends(
    val weeklyTrend: TrendData,
    val monthlyTrend: TrendData,
    val longestGap: GapData,
    val allTimeAverage: Float,
    val bestWeek: BestWeekData
)

data class TrendData(
    val currentRate: Float,
    val previousRate: Float,
    val changePercentage: Int
)

data class GapData(
    val days: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?
)

data class BestWeekData(
    val rate: Float,
    val startDate: LocalDate,
    val endDate: LocalDate
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
    fun getAllMonthlyCompletion(habitId: UUID): Flow<Map<YearMonth, List<DailyCompletion>>>
}
