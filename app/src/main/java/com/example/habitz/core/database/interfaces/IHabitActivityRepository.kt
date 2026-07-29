package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.HabitActivity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

interface IHabitActivityRepository {
    fun logActivity(activity: HabitActivity)
    fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>>
    fun getAllActivities(): Flow<List<HabitActivity>>
    fun deleteActivity(activityId: UUID)
}
