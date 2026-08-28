package com.example.forge.feature.insights.state

import com.example.forge.core.services.interfaces.*

data class InsightsUiState(
    val isLoading: Boolean = true,
    val globalStats: GlobalStats? = null,
    val heatmap: List<HeatmapCell> = emptyList(),
    val momentumTrend: List<MomentumPoint> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val weeklyPerformance: WeeklyPerformance? = null,
    val categoryBreakdown: List<CategoryShare> = emptyList(),
    val streakDistribution: List<StreakBucket> = emptyList(),
    val isEmpty: Boolean = false
)
