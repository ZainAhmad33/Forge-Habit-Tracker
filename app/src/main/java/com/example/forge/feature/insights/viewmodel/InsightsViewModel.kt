package com.example.forge.feature.insights.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.services.interfaces.*
import com.example.forge.feature.insights.state.InsightsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsService: IInsightsService
) : ViewModel() {

    private val _selectedMomentumMonth = MutableStateFlow(YearMonth.now())
    val selectedMomentumMonth = _selectedMomentumMonth.asStateFlow()

    private val momentumMonths = insightsService.getEarliestHabitDate().map { earliestDate ->
        val start = earliestDate?.let { YearMonth.from(it) } ?: YearMonth.now()
        val end = YearMonth.now()
        val months = mutableListOf<YearMonth>()
        var curr = start
        while (!curr.isAfter(end)) {
            months.add(curr)
            curr = curr.plusMonths(1)
        }
        months // Chronological order: Earliest -> Latest
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(YearMonth.now()))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InsightsUiState> = combine(
        insightsService.getGlobalStats(InsightPeriod.AllTime),
        insightsService.getActivityHeatmap(),
        _selectedMomentumMonth.flatMapLatest { ym ->
            insightsService.getMomentumTrend(ym.atDay(1), ym.atEndOfMonth())
        },
        insightsService.getHabitLeaderboard(InsightPeriod.AllTime),
        insightsService.getWeeklyPerformance(),
        insightsService.getCategoryBreakdown(),
        insightsService.getStreakDistribution(),
        momentumMonths,
        _selectedMomentumMonth
    ) { flows ->
        val stats = flows[0] as GlobalStats
        val heatmap = flows[1] as List<HeatmapCell>
        val momentum = flows[2] as List<MomentumPoint>
        val leaderboard = flows[3] as List<LeaderboardEntry>
        val weekly = flows[4] as WeeklyPerformance
        val categories = flows[5] as List<CategoryShare>
        val streaks = flows[6] as List<StreakBucket>
        val months = flows[7] as List<YearMonth>
        val currentMonth = flows[8] as YearMonth

        InsightsUiState(
            isLoading = false,
            globalStats = stats,
            heatmap = heatmap,
            momentumTrend = momentum,
            currentMomentumMonth = currentMonth,
            momentumMonths = months,
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

    fun onMomentumMonthSelected(month: YearMonth) {
        _selectedMomentumMonth.value = month
    }
}
