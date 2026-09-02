package com.example.forge.feature.insights.state

import com.example.forge.core.services.interfaces.*
import com.example.forge.core.uiEntities.ActivityData
import java.time.LocalDate
import java.time.YearMonth

data class InsightsUiState(
    val isLoading: Boolean = true,
    val globalStats: GlobalStats? = null,
    val heatmap: List<ActivityData> = emptyList(),
    val earliestHabitDate: LocalDate? = null,
    val selectedHeatmapMonth: YearMonth = YearMonth.now(),
    val momentumData: Map<YearMonth, List<MomentumPoint>> = emptyMap(),
    val currentMomentumMonth: YearMonth = YearMonth.now(),
    val momentumMonths: List<YearMonth> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val weeklyPerformance: WeeklyPerformance? = null,
    val categoryBreakdown: List<CategoryShare> = emptyList(),
    val streakDistribution: List<StreakBucket> = emptyList(),
    val isEmpty: Boolean = false
)
