package com.example.forge.core.database.respositories

import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.interfaces.IHabitRepository
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

    override suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
}
