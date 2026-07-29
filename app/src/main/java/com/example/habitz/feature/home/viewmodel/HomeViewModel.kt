package com.example.habitz.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHomeService
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.core.uiEntities.HomeSummary
import com.example.habitz.feature.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeService: IHomeService,
    private val activityService: IHabitActivityService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(HabitCategory.All)
    private val _selectedHabitIdForLogging = MutableStateFlow<String?>(null)
    val selectedHabitIdForLogging = _selectedHabitIdForLogging.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        homeService.getDashboardData(),
        _searchQuery,
        _selectedCategory
    ) { dashboard, query, category ->
        val filteredHabits = if (query.isEmpty()) {
            dashboard.habits
        } else {
            dashboard.habits.filter { it.title.contains(query, ignoreCase = true) }
        }

        HomeUiState.from(
            dashboard.copy(habits = filteredHabits),
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.empty()
    )

    fun onCategorySelected(category: HabitCategory) {
        _selectedCategory.value = category
    }

    fun searchHabits(query: String){
        _searchQuery.value = query
    }

    fun onHabitClick(habit: HomeHabit) {
        if (habit.habitType == HabitType.YesNo) {
            if (!habit.isCompletedToday) {
                viewModelScope.launch {
                    activityService.logHabitActivity(UUID.fromString(habit.id), 1)
                }
            }
        } else {
            _selectedHabitIdForLogging.value = habit.id
        }
    }

    fun onLogProgress(habitId: String, quantity: Int) {
        viewModelScope.launch {
            activityService.logHabitActivity(UUID.fromString(habitId), quantity)
        }
        _selectedHabitIdForLogging.value = null
    }

    fun onDismissBottomSheet() {
        _selectedHabitIdForLogging.value = null
    }
}

data class HomeDashboardUIState(
    val greetingMessage: String,
    val greetingName: String,
    val dateLabel: String,
    val summary: HomeSummary,
    val categories: List<CategoryPill>,
    val habits: List<HomeHabit>
)
