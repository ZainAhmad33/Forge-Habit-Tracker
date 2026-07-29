package com.example.habitz.feature.home.state

import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.HomeSummary
import com.example.habitz.feature.home.viewmodel.HomeDashboardUIState

data class HomeUiState(
    val greetingMessage: String,
    val greetingName: String,
    val dateLabel: String,
    val summary: HomeSummary,
    val categories: List<CategoryPill>,
    val selectedCategory: HabitCategory,
    val habits: List<HomeHabit>,
) {
    val visibleHabits: List<HomeHabit>
        get() = if (selectedCategory == HabitCategory.All) {
            habits
        } else {
            habits.filter { it.category == selectedCategory }
        }

    companion object {
        fun from(
            dashboard: HomeDashboardUIState,
            selectedCategory: HabitCategory = HabitCategory.All,
        ) = HomeUiState(
            greetingMessage = dashboard.greetingMessage,
            greetingName = dashboard.greetingName,
            dateLabel = dashboard.dateLabel,
            summary = dashboard.summary,
            categories = dashboard.categories,
            selectedCategory = selectedCategory,
            habits = dashboard.habits,
        )

        fun empty() = HomeUiState(
            greetingMessage = "",
            greetingName = "",
            dateLabel = "",
            summary = HomeSummary(0, 0, 0, 0),
            categories = emptyList(),
            selectedCategory = HabitCategory.All,
            habits = emptyList()
        )
    }
}
