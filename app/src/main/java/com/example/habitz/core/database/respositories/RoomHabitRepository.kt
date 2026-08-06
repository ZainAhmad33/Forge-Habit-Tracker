package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.dao.HabitDao
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.interfaces.IHabitRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomHabitRepository @Inject constructor(
    private val habitDao: HabitDao
) : IHabitRepository {

    override fun getHabits(): Flow<List<Habit>> = habitDao.getHabits()

    override fun getHabitFlow(habitId: UUID): Flow<Habit?> = habitDao.getHabitFlow(habitId)

    override suspend fun getHabitById(habitId: UUID): Habit? = habitDao.getHabitById(habitId)

    override suspend fun createHabit(habit: Habit) = habitDao.insertHabit(habit)
}
