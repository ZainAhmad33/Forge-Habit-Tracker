package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.Habit
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
    fun getHabitById(habitId: UUID): Habit?
    fun createHabit(habit: Habit)
    fun incrementStreak(habitId: UUID)
}
