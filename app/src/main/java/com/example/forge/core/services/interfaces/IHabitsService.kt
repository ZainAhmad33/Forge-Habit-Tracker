package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IHabitsService {
    fun getAllowedCategories(): List<CategoryPill>
    suspend fun createHabit(habitForm: UpsertHabitUiState)
    suspend fun getHabitById(habitId: UUID): Habit?
    fun getHabitFlow(habitId: UUID): Flow<Habit?>
}
