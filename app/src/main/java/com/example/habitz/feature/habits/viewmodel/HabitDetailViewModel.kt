package com.example.habitz.feature.habits.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHabitStatsService
import com.example.habitz.feature.habits.state.HabitDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: IHabitRepository,
    private val statsService: IHabitStatsService,
    private val activityService: IHabitActivityService
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

    private fun loadHabitData() {
        val habit = habitRepository.getHabitById(habitId)
        if (habit == null) {
            _uiState.value = _uiState.value.copy(error = "Habit not found", isLoading = false)
            return
        }

        combine(
            statsService.getHabitStats(habitId),
            activityService.getActivitiesForToday(listOf(habitId))
        ) { stats, todayLogs ->
            _uiState.value = _uiState.value.copy(
                habit = habit,
                stats = stats,
                todayLogs = todayLogs,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }

    private fun observeMonthlyData() {
        _selectedMonth
            .flatMapLatest { month ->
                // Fetch 4 months ending at the selected month
                val startMonth = month.minusMonths(3)
                statsService.getRangeActivityData(habitId, startMonth, 4)
                    .map { data -> month to data }
            }
            .onEach { (month, data) ->
                _uiState.value = _uiState.value.copy(
                    selectedCalendarMonth = month,
                    monthlyCalendarData = data
                )
            }
            .launchIn(viewModelScope)
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
            activityService.logHabitActivity(habitId, habit.completionTargetPerDay)
        }
    }
}
