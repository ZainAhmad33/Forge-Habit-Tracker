package com.example.habitz.feature.habits.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHabitStatsService
import com.example.habitz.feature.habits.presentation.state.HabitDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

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

    init {
        loadHabitData()
    }

    private fun loadHabitData() {
        val habit = habitRepository.getHabitById(habitId)
        if (habit == null) {
            _uiState.value = _uiState.value.copy(error = "Habit not found", isLoading = false)
            return
        }

        combine(
            statsService.getHabitStats(habitId),
            activityService.getActivitiesForToday(listOf(habitId)),
            activityService.getActivitiesForHabits(listOf(habitId), habit.createdAt, java.util.Date())
        ) { stats, todayLogs, allLogs ->
            val historicalLogs = allLogs.filter { it.createdAt.time < getStartOfToday().time }
                .sortedByDescending { it.createdAt }

            _uiState.value = _uiState.value.copy(
                habit = habit,
                stats = stats,
                todayLogs = todayLogs,
                historicalLogs = historicalLogs,
                isLoading = false
            )
        }.launchIn(viewModelScope)
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

    private fun getStartOfToday(): java.util.Date {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.time
    }
}
