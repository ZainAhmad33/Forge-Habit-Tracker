package com.example.forge.core.database.respositories

import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.pojo.DailyHabitQuantity
import com.example.forge.core.services.interfaces.ITimeService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomHabitActivityRepository @Inject constructor(
    private val activityDao: HabitActivityDao,
    private val timeService: ITimeService
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

    override fun getAllDailyQuantities(): Flow<List<DailyHabitQuantity>> {
        return activityDao.getAllDailyQuantities()
    }

    override fun getDailyQuantitiesForHabit(habitId: UUID): Flow<List<DailyHabitQuantity>> {
        return activityDao.getDailyQuantitiesForHabit(habitId)
    }

    override suspend fun deleteActivity(activityId: UUID) {
        activityDao.deleteActivityById(activityId)
    }

    override suspend fun deleteActivitiesForHabit(habitId: UUID) {
        activityDao.deleteActivitiesForHabit(habitId)
    }

    override suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int> {
        return activityDao.getDailyQuantitiesByRange(habitId, from.time, to.time)
            .associate { it.day to it.totalQuantity }
    }
}
