package com.example.forge.core.database.interfaces

import com.example.forge.core.database.entity.Habit
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Access point for the habits shown on the home dashboard.
 *
 * A database-backed implementation can replace the in-memory implementation
 * without changing presentation code.
 */
interface IHabitRepository {

    fun getHabits(): Flow<List<Habit>>
    fun getHabitFlow(habitId: UUID): Flow<Habit?>
    suspend fun getHabitById(habitId: UUID): Habit?
    suspend fun createHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun getAllHabitsSync(): List<Habit>
}
