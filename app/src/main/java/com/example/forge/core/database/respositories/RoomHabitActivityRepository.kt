package com.example.forge.core.database.respositories

import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomHabitActivityRepository @Inject constructor(
    private val activityDao: HabitActivityDao
) : IHabitActivityRepository {

    override suspend fun logActivity(activity: HabitActivity) {
        activityDao.insertActivity(activity)
    }

    override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>> {
        return activityDao.getActivitiesForHabits(habitIds, from, to)
    }

    override fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>> {
        return activityDao.getActivitiesForHabit(habitId)
    }

    override fun getAllActivities(): Flow<List<HabitActivity>> {
        return activityDao.getAllActivities()
    }

    override suspend fun deleteActivity(activityId: UUID) {
        activityDao.deleteActivityById(activityId)
    }

    override suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int> {
        val activities = activityDao.getActivitiesByRange(habitId, from, to)
        return activities.groupBy { activity ->
            activity.createdAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.quantity } }
    }
}
