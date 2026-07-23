package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.HomeHabit
import com.example.habitz.core.database.entity.HomeSummary

/**
 * Access point for the habits shown on the home dashboard.
 *
 * A database-backed implementation can replace the in-memory implementation
 * without changing presentation code.
 */
interface IHabitRepository {
    fun getHabitSummary(): HomeSummary

    fun getHabits(): List<HomeHabit>
}