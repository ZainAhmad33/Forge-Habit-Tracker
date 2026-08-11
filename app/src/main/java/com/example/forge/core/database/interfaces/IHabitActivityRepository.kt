package com.example.forge.core.database.interfaces

import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.pojo.DailyHabitQuantity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Date
import java.util.UUID

interface IHabitActivityRepository {
    suspend fun logActivity(activity: HabitActivity)
    fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>>
    fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>>
    fun getAllActivities(): Flow<List<HabitActivity>>
    fun getAllDailyQuantities(): Flow<List<DailyHabitQuantity>>
    fun getDailyQuantitiesForHabit(habitId: UUID): Flow<List<DailyHabitQuantity>>
    suspend fun deleteActivity(activityId: UUID)
    suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int>
}
