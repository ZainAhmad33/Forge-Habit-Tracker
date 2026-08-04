package com.example.habitz.feature.habits.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
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
import java.util.Calendar
import java.util.Date
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
            activityService.getActivitiesForToday(listOf(habitId)),
            activityService.getActivitiesForHabits(listOf(habitId), habit.createdAt, Date())
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

    private fun getStartOfToday(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun totalActiveDays(): Int{
        val habit = _uiState.value.habit
        if (habit?.frequencyType == HabitFrequency.EveryDay){
            return YearMonth.now().lengthOfMonth()
        }
        else if(habit?.frequencyType == HabitFrequency.DaysPerWeek){
            return (YearMonth.now().lengthOfMonth().toFloat()/7 * habit.numberOfTrackedDays.toFloat()).toInt()
        }
        else{
            val daysTracked = habit?.trackedDays ?: listOf()
            val currentYearMonth = YearMonth.now() // e.g., current month & year
            val daysInMonth = currentYearMonth.lengthOfMonth() // e.g., 30, 31, 28, 29

            // Convert list to Set for O(1) fast lookup
            val trackedSet = daysTracked.toSet()

            // Count how many days in the month fall on a tracked day
            return (1..daysInMonth).count { day ->
                val date = currentYearMonth.atDay(day)
                // date.dayOfWeek.value returns 1 (Mon) through 7 (Sun)
                date.dayOfWeek.value in trackedSet
            }
        }

        return 0
    }
}
