package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.pojo.DailyHabitQuantity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

interface IHabitActivityService {
    suspend fun logHabitActivity(habitId: UUID, quantity: Int)
    fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>>
    fun getActivitiesForToday(habitIds: List<UUID>): Flow<List<HabitActivity>>
    fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>>
    fun getAllActivities(): Flow<List<HabitActivity>>
    fun getAllDailyQuantities(): Flow<List<DailyHabitQuantity>>
    fun getDailyQuantitiesForHabit(habitId: UUID): Flow<List<DailyHabitQuantity>>
    suspend fun deleteHabitActivity(activityId: UUID)
}
