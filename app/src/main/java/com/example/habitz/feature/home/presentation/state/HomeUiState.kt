package com.example.habitz.feature.home.presentation.state

import com.example.habitz.feature.home.domain.model.HabitCategory
import com.example.habitz.feature.home.domain.model.HomeDashboard
import com.example.habitz.feature.home.domain.model.HomeHabit
import com.example.habitz.feature.home.domain.model.HomeSummary

data class HomeUiState(
    val greetingName: String,
    val dateLabel: String,
    val summary: HomeSummary,
    val categories: List<HabitCategory>,
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
            greetingName = dashboard.greetingName,
            dateLabel = dashboard.dateLabel,
            summary = dashboard.summary,
            categories = dashboard.categories,
            selectedCategory = selectedCategory,
            habits = dashboard.habits,
        )
    }
}
