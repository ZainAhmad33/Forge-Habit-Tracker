package com.example.forge.feature.insights.state

import com.example.forge.core.services.interfaces.*
import java.time.YearMonth

data class InsightsUiState(
    val isLoading: Boolean = true,
    val globalStats: GlobalStats? = null,
    val heatmap: List<HeatmapCell> = emptyList(),
    val momentumTrend: List<MomentumPoint> = emptyList(),
    val currentMomentumMonth: YearMonth = YearMonth.now(),
    val momentumMonths: List<YearMonth> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val weeklyPerformance: WeeklyPerformance? = null,
    val categoryBreakdown: List<CategoryShare> = emptyList(),
    val streakDistribution: List<StreakBucket> = emptyList(),
    val isEmpty: Boolean = false
)
