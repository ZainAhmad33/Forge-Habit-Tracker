package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.feature.upserthabit.state.UpsertHabitUiState
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IHabitsService {
    fun getAllowedCategories(): List<CategoryPill>
    suspend fun createHabit(habitForm: UpsertHabitUiState)
    suspend fun getHabitById(habitId: UUID): Habit?
    fun getHabitFlow(habitId: UUID): Flow<Habit?>
}
