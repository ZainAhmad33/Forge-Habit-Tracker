package com.example.habitz.core.services.interfaces

import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.feature.upserthabit.state.UpsertHabitUiState

interface IHabitsService {
    fun getAllowedCategories(): List<CategoryPill>
    fun createHabit(habitForm: UpsertHabitUiState)
}
