package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.interfaces.IHabitActivityRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitActivityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitActivityService @Inject constructor(
    private val activityRepository: IHabitActivityRepository,
    private val habitRepository: IHabitRepository
) : IHabitActivityService {

    override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {
        val habit = habitRepository.getHabitById(habitId) ?: return

        // 1. Get current logs for today
        val todayLogs = getActivitiesForToday(listOf(habitId)).first()
        val currentTotal = todayLogs.sumOf { it.quantity }

        // 2. Log new activity
        val activity = HabitActivity(
            habitId = habitId,
            quantity = quantity,
            completedAt = Date()
        )
        activityRepository.logActivity(activity)

        // 3. Check if goal was met for the FIRST time today
        val newTotal = currentTotal + quantity
        if (currentTotal < habit.completionTargetPerDay && newTotal >= habit.completionTargetPerDay) {
            habitRepository.incrementStreak(habitId)
        }
    }

    override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>> {
        return activityRepository.getActivitiesForHabits(habitIds, from, to)
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

    override fun deleteHabitActivity(activityId: UUID) {
        activityRepository.deleteActivity(activityId)
    }
}
