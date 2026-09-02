package com.example.forge.feature.habits.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.implementations.HabitsService
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.feature.habits.state.HabitDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    init {
        loadHabitData()
        observeMonthlyData()
    }

    private val habitHistory = habitsService.getHabitFlow(habitId)
        .distinctUntilChanged()
        .flatMapLatest { habit ->
            if (habit == null) return@flatMapLatest kotlinx.coroutines.flow.flowOf(emptyList())
            val startDate = timeService.toLocalDate(habit.createdAt)
            val today = timeService.getCurrentDate()
            val habitStartMonth = YearMonth.from(startDate)
            val todayMonth = YearMonth.from(today)
            val monthCount = (ChronoUnit.MONTHS.between(habitStartMonth, todayMonth).toInt() + 1).coerceAtLeast(1)
            
            statsService.getRangeActivityData(habitId, habitStartMonth, monthCount)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun loadHabitData() {
        combine(
            habitsService.getHabitFlow(habitId).distinctUntilChanged(),
            statsService.getHabitStats(habitId).distinctUntilChanged(),
            activityService.getActivitiesForToday(listOf(habitId)).distinctUntilChanged(),
            timeService.getCurrentDateFlow().distinctUntilChanged(),
            habitHistory
        ) { habit, stats, todayLogs, today, history ->
            if (habit == null) {
                _uiState.value = _uiState.value.copy(error = "Habit not found", isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    habit = habit,
                    stats = stats,
                    todayLogs = todayLogs,
                    today = today,
                    startDate = timeService.toLocalDate(habit.createdAt),
                    monthlyCalendarData = history,
                    isLoading = false
                )
            }
        }.debounce(100.milliseconds) // Avoid rapid UI updates during batch operations
         .launchIn(viewModelScope)
    }

    private fun observeMonthlyData() {
        _selectedMonth.onEach { month ->
            _uiState.value = _uiState.value.copy(selectedCalendarMonth = month)
        }.launchIn(viewModelScope)
    }

    fun onMonthChanged(month: YearMonth) {
        _selectedMonth.value = month
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
