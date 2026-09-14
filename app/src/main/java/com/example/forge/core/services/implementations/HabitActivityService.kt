package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.pojo.DailyHabitQuantity
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.services.interfaces.IWidgetUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitActivityService @Inject constructor(
    private val activityRepository: IHabitActivityRepository,
    private val timeService: ITimeService,
    private val widgetUpdater: IWidgetUpdater
) : IHabitActivityService {

    override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {
        val activity = HabitActivity(
            habitId = habitId,
            quantity = quantity
        )
        activityRepository.logActivity(activity)
        widgetUpdater.updateAllWidgets()
    }

    override suspend fun logSkipActivity(habitId: UUID, quantity: Int, date: Date) {
        val activity = HabitActivity(
            habitId = habitId,
            quantity = quantity,
            isSkip = true,
            createdAt = date
        )
        activityRepository.logActivity(activity)
        widgetUpdater.updateAllWidgets()
    }

    override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>> {
        return activityRepository.getActivitiesForHabits(habitIds, from, to)
    }

    override fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>> {
        return activityRepository.getActivitiesForHabit(habitId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActivitiesForToday(habitIds: List<UUID>): Flow<List<HabitActivity>> {
        return timeService.getCurrentDateFlow().flatMapLatest { today ->
            val from = timeService.toStartOfDayDate(today)
            val to = timeService.toEndOfDayDate(today)

            activityRepository.getActivitiesForHabits(habitIds, from, to)
        }
    }

    override fun getAllActivities(): Flow<List<HabitActivity>> {
        return activityRepository.getAllActivities()
    }

    override fun getAllDailyQuantities(): Flow<List<DailyHabitQuantity>> {
        return activityRepository.getAllDailyQuantities()
    }

    override fun getDailyQuantitiesForHabit(habitId: UUID): Flow<List<DailyHabitQuantity>> {
        return activityRepository.getDailyQuantitiesForHabit(habitId)
    }

    override suspend fun getDailyQuantitiesForHabitSync(habitId: UUID): List<DailyHabitQuantity> {
        return activityRepository.getDailyQuantitiesForHabitSync(habitId)
    }

    override suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int> {
        return activityRepository.getCompletedQuantityByRange(habitId, from, to)
    }

    override suspend fun deleteHabitActivity(activityId: UUID) {
        activityRepository.deleteActivity(activityId)
        widgetUpdater.updateAllWidgets()
    }
}
