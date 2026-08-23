package com.example.forge.feature.home.state

import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.uiEntities.HomeHabit
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.core.uiEntities.HomeSummary
import com.example.forge.feature.home.viewmodel.HomeDashboardUIState

data class HomeUiState(
    val greetingMessage: String,
    val greetingName: String,
    val dateLabel: String,
    val summary: HomeSummary,
    val categories: List<CategoryPill>,
    val selectedCategory: HabitCategory,
    val habits: List<HomeHabit>,
    val isLoading: Boolean = false,
    val searchQuery: String = ""
) {
    val visibleHabits: List<HomeHabit>
        get() = if (selectedCategory == HabitCategory.All) {
            habits
        } else {
            habits.filter { it.category == selectedCategory }
        }

    val todaysHabits: List<HomeHabit>
        get() = visibleHabits.filter { it.isScheduledForToday }

    val otherHabits: List<HomeHabit>
        get() = visibleHabits.filter { !it.isScheduledForToday }

    companion object {
        fun from(
            dashboard: HomeDashboardUIState,
            selectedCategory: HabitCategory = HabitCategory.All,
            isLoading: Boolean = false,
            searchQuery: String = ""
        ) = HomeUiState(
            greetingMessage = dashboard.greetingMessage,
            greetingName = dashboard.greetingName,
            dateLabel = dashboard.dateLabel,
            summary = dashboard.summary,
            categories = dashboard.categories,
            selectedCategory = selectedCategory,
            habits = dashboard.habits,
            isLoading = isLoading,
            searchQuery = searchQuery
        )

        fun empty() = HomeUiState(
            greetingMessage = "",
            greetingName = "",
            dateLabel = "",
            summary = HomeSummary(0, 0, 0, 0),
            categories = emptyList(),
            selectedCategory = HabitCategory.All,
            habits = emptyList(),
            isLoading = true
        )
    }
}
