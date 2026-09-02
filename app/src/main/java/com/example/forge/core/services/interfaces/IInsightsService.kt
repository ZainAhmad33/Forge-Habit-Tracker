package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.uiEntities.ActivityData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

enum class InsightPeriod {
    Today, Week, Month, AllTime
}

data class GlobalStats(
    val completionRate: Float,
    val currentGlobalStreak: Int,
    val bestGlobalStreak: Int,
    val perfectDaysCount: Int,
    val rateChange: Int // Percentage change vs previous period
)

data class MomentumPoint(
    val date: LocalDate,
    val sevenDayRollingAvg: Float,
    val thirtyDayRollingAvg: Float
)

data class LeaderboardEntry(
    val habitId: UUID,
    val title: String,
    val emoji: String,
    val completionRate: Float
)

data class WeeklyPerformance(
    val dayRates: Map<Int, Float>, // 0 (Mon) to 6 (Sun)
    val bestDay: Int,
    val worstDay: Int
)

data class CategoryShare(
    val category: HabitCategory,
    val completionRate: Float,
    val habitCount: Int
)

data class StreakBucket(
    val label: String,
    val count: Int
)

interface IInsightsService {
    fun getGlobalStats(period: InsightPeriod): Flow<GlobalStats>
    fun getActivityHeatmap(): Flow<List<ActivityData>>
    fun getMomentumTrend(): Flow<List<MomentumPoint>>
    fun getMomentumTrend(startDate: LocalDate, endDate: LocalDate): Flow<List<MomentumPoint>>
    fun getAllMomentumTrends(months: List<java.time.YearMonth>): Flow<Map<java.time.YearMonth, List<MomentumPoint>>>
    fun getEarliestHabitDate(): Flow<LocalDate?>
    fun getHabitLeaderboard(period: InsightPeriod): Flow<List<LeaderboardEntry>>
    fun getWeeklyPerformance(): Flow<WeeklyPerformance>
    fun getCategoryBreakdown(): Flow<List<CategoryShare>>
    fun getStreakDistribution(): Flow<List<StreakBucket>>
}
