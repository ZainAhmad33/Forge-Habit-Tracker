package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.entity.HomeHabit
import com.example.habitz.core.database.entity.HomeSummary
import com.example.habitz.core.datastore.dummyDatabase

/**
 * Temporary local data source for the home feature.
 *
 * Keep this implementation as the composition root's dependency until Room is
 * introduced; then replace it with a database-backed [com.example.habitz.core.database.interfaces.IHabitRepository].
 */
class InMemoryIHabitRepository : IHabitRepository {


    override fun getHabitSummary(): HomeSummary{
        var completedCount = dummyDatabase.habitsById.values.filter { it.isCompletedToday }.size
        var totalCount = dummyDatabase.habitsById.values.size
        var summary = HomeSummary(
            completedCount = completedCount,
            totalCount = totalCount,
            currentStreakDays = 12,
            weeklyCompletionPercent = 78,
        )

        return summary
    }

    override fun getHabits(): List<HomeHabit> {
        var habits = dummyDatabase.habitsById.values.toList()
        return habits
    }


}