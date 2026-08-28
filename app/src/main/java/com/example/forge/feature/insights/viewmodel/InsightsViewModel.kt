package com.example.forge.feature.insights.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.services.interfaces.*
import com.example.forge.feature.insights.state.InsightsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsService: IInsightsService
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        insightsService.getGlobalStats(InsightPeriod.AllTime),
        insightsService.getActivityHeatmap(),
        insightsService.getMomentumTrend(),
        insightsService.getHabitLeaderboard(InsightPeriod.AllTime),
        insightsService.getWeeklyPerformance(),
        insightsService.getCategoryBreakdown(),
        insightsService.getStreakDistribution()
    ) { flows ->
        val stats = flows[0] as GlobalStats
        val heatmap = flows[1] as List<HeatmapCell>
        val momentum = flows[2] as List<MomentumPoint>
        val leaderboard = flows[3] as List<LeaderboardEntry>
        val weekly = flows[4] as WeeklyPerformance
        val categories = flows[5] as List<CategoryShare>
        val streaks = flows[6] as List<StreakBucket>

        InsightsUiState(
            isLoading = false,
            globalStats = stats,
            heatmap = heatmap,
            momentumTrend = momentum,
            leaderboard = leaderboard,
            weeklyPerformance = weekly,
            categoryBreakdown = categories,
            streakDistribution = streaks,
            isEmpty = leaderboard.isEmpty() && (stats.completionRate == 0f)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )
}
