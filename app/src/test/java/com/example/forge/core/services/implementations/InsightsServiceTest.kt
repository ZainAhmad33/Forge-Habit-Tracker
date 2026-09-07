package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.database.pojo.DailyHabitQuantity
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.ITimeService
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class InsightsServiceTest {

    private lateinit var service: InsightsService
    private lateinit var statsService: HabitStatsService
    private lateinit var fakeTimeService: ITimeService

    @Before
    fun setup() {
        fakeTimeService = object : ITimeService {
            override fun getCurrentDateFlow() = flowOf(LocalDate.of(2026, 8, 28))
            override fun getCurrentDate() = LocalDate.of(2026, 8, 28)
            override fun toLocalDate(date: Date): LocalDate {
                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            }
            override fun toStartOfDayDate(localDate: LocalDate): Date = Date.from(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
            override fun toEndOfDayDate(localDate: LocalDate): Date = Date()
        }

        statsService = HabitStatsService(
            habitRepository = stubHabitRepo(),
            activityService = stubActivityService(),
            timeService = fakeTimeService
        )

        service = InsightsService(
            habitRepository = stubHabitRepo(),
            activityService = stubActivityService(),
            timeService = fakeTimeService,
            statsService = statsService
        )
    }

    private fun stubHabitRepo() = object : IHabitRepository {
        override fun getHabits() = flowOf(emptyList<Habit>())
        override fun getHabitFlow(habitId: UUID) = flowOf(null)
        override suspend fun getHabitById(habitId: UUID) = null
        override suspend fun createHabit(habit: Habit) {}
        override suspend fun deleteHabit(habit: Habit) {}
        override suspend fun getAllHabitsSync(): List<Habit> = emptyList()
    }

    private fun stubActivityService() = object : IHabitActivityService {
        override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {}
        override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date) = flowOf(emptyList<HabitActivity>())
        override fun getActivitiesForToday(habitIds: List<UUID>) = flowOf(emptyList<HabitActivity>())
        override fun getActivitiesForHabit(habitId: UUID) = flowOf(emptyList<HabitActivity>())
        override fun getAllActivities() = flowOf(emptyList<HabitActivity>())
        override fun getAllDailyQuantities() = flowOf(emptyList<DailyHabitQuantity>())
        override fun getDailyQuantitiesForHabit(habitId: UUID) = flowOf(emptyList<DailyHabitQuantity>())
        override suspend fun deleteHabitActivity(activityId: UUID) {}
        override suspend fun logSkipActivity(habitId: UUID, quantity: Int, date: Date) {}
        override suspend fun getDailyQuantitiesForHabitSync(habitId: UUID): List<DailyHabitQuantity> = emptyList()
        override suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int> = emptyMap()
    }

    @Test
    fun `Test setup works`() {
        // Just verify setup
    }
}
