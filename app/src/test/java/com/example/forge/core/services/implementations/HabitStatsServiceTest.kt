package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.uiEntities.ProgressShape
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class HabitStatsServiceTest {

    private lateinit var service: HabitStatsService
    private lateinit var fakeTimeService: ITimeService

    @Before
    fun setup() {
        // We only need a working TimeService for conversion, others can be stubbed
        fakeTimeService = object : ITimeService {
            override fun getCurrentDateFlow(): kotlinx.coroutines.flow.Flow<LocalDate> = kotlinx.coroutines.flow.emptyFlow()
            override fun getCurrentDate(): LocalDate = LocalDate.now()
            override fun toLocalDate(date: Date): LocalDate {
                return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            }
            override fun toStartOfDayDate(localDate: LocalDate): Date = Date()
            override fun toEndOfDayDate(localDate: LocalDate): Date = Date()
        }

        service = HabitStatsService(
            habitRepository = object : IHabitRepository {
                override fun getHabits() = kotlinx.coroutines.flow.emptyFlow<List<Habit>>()
                override fun getHabitFlow(habitId: UUID) = kotlinx.coroutines.flow.emptyFlow<Habit?>()
                override suspend fun getHabitById(habitId: UUID): Habit? = null
                override suspend fun createHabit(habit: Habit) {}
            },
            activityService = object : IHabitActivityService {
                override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {}
                override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date) = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.entity.HabitActivity>>()
                override fun getActivitiesForToday(habitIds: List<UUID>) = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.entity.HabitActivity>>()
                override fun getActivitiesForHabit(habitId: UUID) = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.entity.HabitActivity>>()
                override fun getAllActivities() = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.entity.HabitActivity>>()
                override fun getAllDailyQuantities() = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.pojo.DailyHabitQuantity>>()
                override fun getDailyQuantitiesForHabit(habitId: UUID) = kotlinx.coroutines.flow.emptyFlow<List<com.example.forge.core.database.pojo.DailyHabitQuantity>>()
                override suspend fun deleteHabitActivity(activityId: UUID) {}
            },
            timeService = fakeTimeService
        )
    }

    private fun createHabit(
        frequency: HabitFrequency = HabitFrequency.EveryDay,
        trackedDays: List<Int> = emptyList(),
        numberOfTrackedDays: Int = 7,
        createdAt: LocalDate = LocalDate.now().minusDays(10),
        target: Int = 1
    ): Habit {
        return Habit(
            id = UUID.randomUUID(),
            title = "Test Habit",
            category = HabitCategory.Health,
            emoji = "💪",
            habitType = HabitType.YesNo,
            frequencyType = frequency,
            trackedDays = trackedDays,
            numberOfTrackedDays = numberOfTrackedDays,
            completionTargetPerDay = target,
            targetUnit = "Per Day",
            progressShape = ProgressShape.Circle,
            createdAt = Date.from(createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
            updatedAt = Date()
        )
    }

    @Test
    fun `EveryDay streak - simple consecutive days`() {
        val today = LocalDate.of(2026, 8, 22)
        val habit = createHabit(createdAt = today.minusDays(5))
        val dailyTotals = mapOf(
            today to 1,
            today.minusDays(1) to 1,
            today.minusDays(2) to 1
        )

        val streak = service.calculateCurrentStreak(habit, dailyTotals, 1, today)
        assertEquals(3, streak)
    }

    @Test
    fun `EveryDay streak - broken streak`() {
        val today = LocalDate.of(2026, 8, 22)
        val habit = createHabit(createdAt = today.minusDays(5))
        val dailyTotals = mapOf(
            today to 1,
            today.minusDays(1) to 0,
            today.minusDays(2) to 1
        )

        val streak = service.calculateCurrentStreak(habit, dailyTotals, 1, today)
        assertEquals(1, streak)
    }

    @Test
    fun `SpecificDays streak - skips non-tracked days`() {
        // Monday(0) and Wednesday(2)
        val today = LocalDate.of(2026, 8, 24) // Monday
        val lastWednesday = today.minusDays(5) // Wednesday Aug 19
        
        val habit = createHabit(
            frequency = HabitFrequency.SpecificDays,
            trackedDays = listOf(0, 2),
            createdAt = today.minusDays(10)
        )
        val dailyTotals = mapOf(
            today to 1,
            lastWednesday to 1
        )

        // Streak should be 6 because it increments for non-scheduled days (Tue, Sun, Sat, Fri, Thu)
        // Aug 24 (Done), 23, 22, 21, 20, 19 (Done)
        val streak = service.calculateCurrentStreak(habit, dailyTotals, 1, today)
        assertEquals(6, streak)
    }

    @Test
    fun `DaysPerWeek streak - counts full weeks met`() {
        val today = LocalDate.of(2026, 8, 22) // Saturday
        val habit = createHabit(
            frequency = HabitFrequency.DaysPerWeek,
            numberOfTrackedDays = 3,
            createdAt = today.minusWeeks(3)
        )
        
        // This week (starts Aug 17): 3 completions (met)
        // Last week (starts Aug 10): 3 completions (met)
        // Week before (starts Aug 3): 2 completions (failed)
        val dailyTotals = mapOf(
            LocalDate.of(2026, 8, 17) to 1,
            LocalDate.of(2026, 8, 18) to 1,
            LocalDate.of(2026, 8, 19) to 1, // Current week
            LocalDate.of(2026, 8, 10) to 1,
            LocalDate.of(2026, 8, 11) to 1,
            LocalDate.of(2026, 8, 12) to 1,
            LocalDate.of(2026, 8, 13) to 1,
            LocalDate.of(2026, 8, 8) to 1,
            LocalDate.of(2026, 8, 9) to 1
        )

        val streak = service.calculateCurrentStreak(habit, dailyTotals, 1, today)
        assertEquals(15, streak)
    }

    @Test
    fun `DaysPerWeek best streak - counts full weeks met`() {
        val today = LocalDate.of(2026, 8, 22) // Saturday
        val habit = createHabit(
            frequency = HabitFrequency.DaysPerWeek,
            numberOfTrackedDays = 3,
            createdAt = today.minusWeeks(3)
        )

        // This week (starts Aug 17): 3 completions (met)
        // Last week (starts Aug 10): 3 completions (met)
        // Week before (starts Aug 3): 2 completions (failed)
        val dailyTotals = mapOf(
            LocalDate.of(2026, 8, 3) to 1, // week 1
            LocalDate.of(2026, 8, 4) to 1,
            LocalDate.of(2026, 8, 5) to 1,


            LocalDate.of(2026, 8, 10) to 1, // week 2
            LocalDate.of(2026, 8, 12) to 1,
            LocalDate.of(2026, 8, 13) to 1,

            LocalDate.of(2026, 8, 17) to 1, // week 3
            LocalDate.of(2026, 8, 18) to 1,
            LocalDate.of(2026, 8, 19) to 1,

            // longest streak maintained from 3rd of August to 18th August -> 17 days

        )

        val streak = service.calculateBestStreak(habit, dailyTotals, 1, today)
        assertEquals(20, streak)
    }

    @Test
    fun `calculateBestStreak returns maximum streak`() {
        val today = LocalDate.of(2026, 8, 22)
        val habit = createHabit(createdAt = today.minusDays(10))
        val dailyTotals = mapOf(
            today to 1,
            today.minusDays(1) to 1, // Streak 2
            today.minusDays(3) to 1,
            today.minusDays(4) to 1,
            today.minusDays(5) to 1 // Streak 3
        )

        val bestStreak = service.calculateBestStreak(habit, dailyTotals, 1, today)
        assertEquals(3, bestStreak)
    }

    @Test
    fun `calculateOverallRate - daily habit`() {
        val today = LocalDate.of(2026, 8, 22)
        val habit = createHabit(createdAt = today.minusDays(3)) // 4 days total: T, T-1, T-2, T-3
        val dailyTotals = mapOf(
            today to 1,
            today.minusDays(2) to 1
        )

        val rate = service.calculateOverallRate(habit, dailyTotals, 1, today.minusDays(3), today)
        // 2 successful / 4 expected = 0.5
        assertEquals(0.5f, rate, 0.01f)
    }

    @Test
    fun `DaysPerWeek streak - current week still possible`() {
        val today = LocalDate.of(2026, 8, 17) // Monday (start of week)
        val habit = createHabit(
            frequency = HabitFrequency.DaysPerWeek,
            numberOfTrackedDays = 3,
            createdAt = today.minusWeeks(2)
        )
        
        // This week: 0 completions (but it's only Monday, 3 days still possible)
        // Last week (starts Aug 10): 3 completions (met)
        val dailyTotals = mapOf(
            LocalDate.of(2026, 8, 10) to 1, 
            LocalDate.of(2026, 8, 11) to 1, 
            LocalDate.of(2026, 8, 12) to 1
        )

        val streak = service.calculateCurrentStreak(habit, dailyTotals, 1, today)
        // Streak should be 1 (from last week) because current week is still possible
        assertEquals(1, streak)
    }

    @Test
    fun `calculateOverallRate - weekly habit`() {
        val today = LocalDate.of(2026, 8, 23) // Sunday (end of week)
        val habit = createHabit(
            frequency = HabitFrequency.DaysPerWeek,
            numberOfTrackedDays = 3,
            createdAt = today.minusWeeks(1).minusDays(6) // Two full weeks
        )
        
        // Week 1: 3 completions
        // Week 2: 2 completions
        val dailyTotals = mapOf(
            today.minusWeeks(1) to 1, today.minusWeeks(1).minusDays(1) to 1, today.minusWeeks(1).minusDays(2) to 1,
            today.minusDays(1) to 1, today.minusDays(2) to 1
        )

        val rate = service.calculateOverallRate(habit, dailyTotals, 1, today.minusWeeks(1).minusDays(6), today)
        // Expected: 3 (Week 1) + 3 (Week 2) = 6
        // Successful: 3 (Week 1) + 2 (Week 2) = 5
        // 5 / 6 = 0.833
        assertEquals(0.833f, rate, 0.01f)
    }
}
