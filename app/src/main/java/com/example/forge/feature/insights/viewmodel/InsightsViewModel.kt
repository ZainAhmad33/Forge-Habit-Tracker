package com.example.forge.feature.insights.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.services.interfaces.*
import com.example.forge.feature.insights.state.InsightsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsService: IInsightsService
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(InsightPeriod.Week)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<InsightsUiState> = combine(
        _selectedPeriod.flatMapLatest { insightsService.getGlobalStats(it) },
        insightsService.getActivityHeatmap(),
        insightsService.getMomentumTrend(),
        _selectedPeriod.flatMapLatest { insightsService.getHabitLeaderboard(it) },
        insightsService.getWeeklyPerformance(),
        insightsService.getCategoryBreakdown(),
        insightsService.getStreakDistribution(),
        _selectedPeriod
    ) { flows ->
        val stats = flows[0] as GlobalStats
        val heatmap = flows[1] as List<HeatmapCell>
        val momentum = flows[2] as List<MomentumPoint>
        val leaderboard = flows[3] as List<LeaderboardEntry>
        val weekly = flows[4] as WeeklyPerformance
        val categories = flows[5] as List<CategoryShare>
        val streaks = flows[6] as List<StreakBucket>
        val period = flows[7] as InsightPeriod

        InsightsUiState(
            isLoading = false,
            selectedPeriod = period,
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

    fun onPeriodSelected(period: InsightPeriod) {
        _selectedPeriod.value = period
    }
}
