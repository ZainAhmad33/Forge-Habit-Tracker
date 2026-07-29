package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.datastore.dummyDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary local data source for the home feature.
 *
 * Keep this implementation as the composition root's dependency until Room is
 * introduced; then replace it with a database-backed [com.example.habitz.core.database.interfaces.IHabitRepository].
 */
@Singleton
class InMemoryIHabitRepository @Inject constructor() : IHabitRepository {

    private val _habits = MutableStateFlow(dummyDatabase.habits)

    override fun getHabits(): Flow<List<Habit>> {
        return _habits
    }

    override fun getHabitById(habitId: UUID): Habit? {
        return dummyDatabase.habits.find { it.id == habitId }
    }

    override fun createHabit(habit: Habit){
        dummyDatabase.habits = dummyDatabase.habits + habit
        _habits.value = dummyDatabase.habits
    }

    override fun incrementStreak(habitId: UUID) {
        val index = dummyDatabase.habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = dummyDatabase.habits[index]
            habit.dailyStreakCount += 1
            _habits.value = dummyDatabase.habits.toList() // Trigger flow update
        }
    }
}
