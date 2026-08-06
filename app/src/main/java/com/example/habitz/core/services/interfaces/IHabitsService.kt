package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.feature.upserthabit.state.UpsertHabitUiState
import java.util.UUID

interface IHabitsService {
    fun getAllowedCategories(): List<CategoryPill>
    fun createHabit(habitForm: UpsertHabitUiState)
    fun getHabitById(habitId: UUID): Habit?
}
