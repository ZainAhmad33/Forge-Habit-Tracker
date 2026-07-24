package com.example.habitz.feature.home.presentation.state

import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HomeDashboard
import com.example.habitz.core.database.entity.HomeHabit
import com.example.habitz.core.database.entity.HomeSummary
import com.example.habitz.core.uiEntities.CategoryPill

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
            dashboard: HomeDashboard,
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
    }
}
