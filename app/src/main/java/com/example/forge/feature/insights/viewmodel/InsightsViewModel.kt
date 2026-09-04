package com.example.forge.feature.insights.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.services.interfaces.*
import com.example.forge.core.uiEntities.ActivityData
import com.example.forge.feature.insights.state.InsightsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsService: IInsightsService
) : ViewModel() {

    private val _selectedMomentumMonth = MutableStateFlow(YearMonth.now())
    val selectedMomentumMonth = _selectedMomentumMonth.asStateFlow()

    private val _selectedHeatmapMonth = MutableStateFlow(YearMonth.now())
    val selectedHeatmapMonth = _selectedHeatmapMonth.asStateFlow()

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

    private val heatmapHistory = insightsService.getActivityHeatmap()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InsightsUiState> = combine(
        insightsService.getGlobalStats(InsightPeriod.AllTime),
        heatmapHistory,
        momentumMonths.flatMapLatest { months ->
            insightsService.getAllMomentumTrends(months)
        },
        insightsService.getHabitLeaderboard(InsightPeriod.AllTime),
        insightsService.getWeeklyPerformance(),
        insightsService.getCategoryBreakdown(),
        insightsService.getStreakDistribution(),
        momentumMonths,
        _selectedMomentumMonth,
        _selectedHeatmapMonth,
        insightsService.getEarliestHabitDate()
    ) { flows ->
        val stats = flows[0] as GlobalStats
        val heatmap = flows[1] as List<ActivityData>
        val momentumData = flows[2] as Map<YearMonth, List<MomentumPoint>>
        val leaderboard = flows[3] as List<LeaderboardEntry>
        val weekly = flows[4] as WeeklyPerformance
        val categories = flows[5] as List<CategoryShare>
        val streaks = flows[6] as List<StreakBucket>
        val months = flows[7] as List<YearMonth>
        val currentMomentumMonth = flows[8] as YearMonth
        val currentHeatmapMonth = flows[9] as YearMonth
        val earliestDate = flows[10] as LocalDate?

        InsightsUiState(
            isLoading = false,
            globalStats = stats,
            heatmap = heatmap,
            earliestHabitDate = earliestDate,
            selectedHeatmapMonth = currentHeatmapMonth,
            momentumData = momentumData,
            currentMomentumMonth = currentMomentumMonth,
            momentumMonths = months,
            leaderboard = leaderboard,
            weeklyPerformance = weekly,
            categoryBreakdown = categories,
            streakDistribution = streaks,
            isEmpty = leaderboard.isEmpty() && (stats.completionRate == 0f)
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun onMomentumMonthSelected(month: YearMonth) {
        _selectedMomentumMonth.value = month
    }

    fun onHeatmapMonthSelected(month: YearMonth) {
        _selectedHeatmapMonth.value = month
    }
}
