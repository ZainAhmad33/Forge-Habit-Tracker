package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.HabitActivity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

interface IHabitActivityService {
    suspend fun logHabitActivity(habitId: UUID, quantity: Int)
    fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>>
    fun getActivitiesForToday(habitIds: List<UUID>): Flow<List<HabitActivity>>
    fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>>
    fun getAllActivities(): Flow<List<HabitActivity>>
    fun deleteHabitActivity(activityId: UUID)
}
