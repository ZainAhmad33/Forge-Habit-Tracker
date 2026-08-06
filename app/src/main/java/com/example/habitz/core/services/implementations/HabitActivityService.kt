package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.interfaces.IHabitActivityRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitActivityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.toList
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitActivityService @Inject constructor(
    private val activityRepository: IHabitActivityRepository
) : IHabitActivityService {

    override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {
        val activity = HabitActivity(
            habitId = habitId,
            quantity = quantity
        )
        activityRepository.logActivity(activity)
    }

    override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>> {
        return activityRepository.getActivitiesForHabits(habitIds, from, to)
    }

    override fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>> {
        return activityRepository.getActivitiesForHabit(habitId)
    }

    override fun getActivitiesForToday(habitIds: List<UUID>): Flow<List<HabitActivity>> {
        val calendar = Calendar.getInstance()
        
        // Start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val from = calendar.time

        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val to = calendar.time

        return activityRepository.getActivitiesForHabits(habitIds, from, to)
    }

    override fun getAllActivities(): Flow<List<HabitActivity>> {
        return activityRepository.getAllActivities()
    }

    override suspend fun deleteHabitActivity(activityId: UUID) {
        activityRepository.deleteActivity(activityId)
    }
}
