package com.example.forge.feature.habits.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.implementations.HabitsService
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.feature.habits.state.HabitDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitsService: HabitsService,
    private val statsService: IHabitStatsService,
    private val activityService: IHabitActivityService,
    private val timeService: ITimeService
) : ViewModel() {

    private val habitIdString: String = checkNotNull(savedStateHandle["habitId"])
    private val habitId = UUID.fromString(habitIdString)

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val _selectedCalendarMonth = MutableStateFlow(YearMonth.now())
    private val _selectedCompletionMonth = MutableStateFlow(YearMonth.now())

    private val habitHistory = habitsService.getHabitFlow(habitId)
        .distinctUntilChanged()
        .flatMapLatest { habit ->
            if (habit == null) return@flatMapLatest flowOf(emptyList())
            val startDate = timeService.toLocalDate(habit.createdAt)
            val today = timeService.getCurrentDate()
            val habitStartMonth = YearMonth.from(startDate)
            val todayMonth = YearMonth.from(today)
            val monthCount = (ChronoUnit.MONTHS.between(habitStartMonth, todayMonth).toInt() + 1).coerceAtLeast(1)
            
            statsService.getRangeActivityData(habitId, habitStartMonth, monthCount)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val monthlyCompletion = statsService.getAllMonthlyCompletion(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadHabitData()
        observeMonthSelections()
    }

    private fun loadHabitData() {
        val coreDataFlow = combine(
            habitsService.getHabitFlow(habitId).distinctUntilChanged(),
            statsService.getHabitStats(habitId).distinctUntilChanged(),
            activityService.getActivitiesForToday(listOf(habitId)).distinctUntilChanged(),
            timeService.getCurrentDateFlow().distinctUntilChanged()
        ) { habit, stats, todayLogs, today ->
            Quad(habit, stats, todayLogs, today)
        }

        combine(
            coreDataFlow,
            habitHistory,
            monthlyCompletion
        ) { core, history, completions ->
            val habit = core.first
            val stats = core.second
            val todayLogs = core.third
            val today = core.fourth

            if (habit == null) {
                _uiState.update { it.copy(error = "Habit not found", isLoading = false) }
            } else {
                val habitStartMonth = YearMonth.from(timeService.toLocalDate(habit.createdAt))
                val todayMonth = YearMonth.from(today)
                val months = mutableListOf<YearMonth>()
                var curr = habitStartMonth
                while (!curr.isAfter(todayMonth)) {
                    months.add(curr)
                    curr = curr.plusMonths(1)
                }

                _uiState.update { 
                    it.copy(
                        habit = habit,
                        stats = stats,
                        todayLogs = todayLogs,
                        today = today,
                        startDate = timeService.toLocalDate(habit.createdAt),
                        monthlyCalendarData = history,
                        allMonthlyCompletion = completions,
                        completionMonths = months,
                        isLoading = false
                    )
                }
            }
        }.debounce(100.milliseconds)
         .launchIn(viewModelScope)
    }

    private fun observeMonthSelections() {
        _selectedCalendarMonth.onEach { month ->
            _uiState.update { it.copy(selectedCalendarMonth = month) }
        }.launchIn(viewModelScope)

        _selectedCompletionMonth.onEach { month ->
            _uiState.update { it.copy(selectedCompletionMonth = month) }
        }.launchIn(viewModelScope)
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun onCalendarMonthChanged(month: YearMonth) {
        _selectedCalendarMonth.value = month
    }

    fun onCompletionMonthChanged(month: YearMonth) {
        _selectedCompletionMonth.value = month
    }

    fun deleteLog(activityId: UUID) {
        viewModelScope.launch {
            activityService.deleteHabitActivity(activityId)
        }
    }

    fun markCompleted() {
        val habit = _uiState.value.habit ?: return
        viewModelScope.launch {
            if (habit.habitType == HabitType.YesNo) {
                if (!habitsService.isHabitCompletedToday(habit.id, habit.completionTargetPerDay)) {
                    activityService.logHabitActivity(habit.id, 1)
                }
            }
        }
    }

    fun onLogProgress(habitId: String, quantity: Int) {
        viewModelScope.launch {
            activityService.logHabitActivity(UUID.fromString(habitId), quantity)
        }
    }

    fun getCompletedQuantity(): Int {
        return _uiState.value.todayLogs.sumOf { it.quantity }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            habitsService.deleteHabit(habitId)
        }
    }
}
