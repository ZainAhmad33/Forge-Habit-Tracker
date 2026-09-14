package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.*
import com.example.forge.core.uiEntities.ProgressShape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.util.*

class HabitMaintenanceServiceTest {

    private lateinit var service: HabitMaintenanceService
    private lateinit var habitRepo: FakeHabitRepository
    private lateinit var activityService: FakeHabitActivityService
    private lateinit var statsService: FakeHabitStatsService
    private lateinit var realStatsService: HabitStatsService
    private lateinit var timeService: FakeTimeService
    private lateinit var widgetUpdater: FakeWidgetUpdater

    @Before
    fun setup() {
        habitRepo = FakeHabitRepository()
        activityService = FakeHabitActivityService()
        timeService = FakeTimeService()
        widgetUpdater = FakeWidgetUpdater()

        realStatsService = HabitStatsService(
            habitRepository = habitRepo,
            activityService = activityService,
            timeService = timeService
        )
        statsService = FakeHabitStatsService(realStatsService)

        service = HabitMaintenanceService(
            habitRepository = habitRepo,
            activityService = activityService,
            statsService = statsService,
            timeService = timeService,
            widgetUpdater = widgetUpdater
        )
    }

    private fun createHabit(
        id: UUID = UUID.randomUUID(),
        frequency: HabitFrequency = HabitFrequency.EveryDay,
        skipDays: Int = 0,
        createdAt: LocalDate = LocalDate.now().minusDays(10),
        lastMaintenance: LocalDate? = null,
        numDaysPerWeek: Int = 7,
        target: Int = 1
    ): Habit {
        return Habit(
            id = id,
            title = "Maintenance Test",
            category = HabitCategory.Health,
            emoji = "⚙️",
            habitType = HabitType.YesNo,
            frequencyType = frequency,
            numberOfTrackedDays = numDaysPerWeek,
            completionTargetPerDay = target,
            targetUnit = "Per Day",
            progressShape = ProgressShape.Circle,
            skipDaysAllowed = skipDays,
            createdAt = Date.from(createdAt.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            updatedAt = Date(),
            lastMaintenanceDate = lastMaintenance?.let { Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()) }
        )
    }

    @Test
    fun `EveryDay journey - earn skip, use skip, then lock`() = runBlocking {
        val habitId = UUID.randomUUID()
        val created = LocalDate.of(2026, 8, 1)
        val habit = createHabit(id = habitId, createdAt = created, lastMaintenance = created, skipDays = 0)
        habitRepo.habits[habitId] = habit

        // Journey 1: Maintain 30 day streak to earn 1 skip day
        // Aug 1 to Aug 31 is 31 days. 
        timeService.mockedCurrentDate = LocalDate.of(2026, 8, 31)
        for (i in 0..30) {
            activityService.addCompletion(habitId, created.plusDays(i.toLong()), 1)
        }

        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertEquals(1, updatedHabit.skipDaysAllowed)
        assertEquals(30, updatedHabit.lastMilestoneRewarded)

        // Journey 2: Miss Sept 1. Run on Sept 2.
        val missDay = LocalDate.of(2026, 9, 1)
        timeService.mockedCurrentDate = LocalDate.of(2026, 9, 2)
        
        service.performMaintenance(habitId)
        
        val skippedHabit = habitRepo.habits[habitId]!!
        assertEquals(0, skippedHabit.skipDaysAllowed)
        assertFalse(skippedHabit.isLocked)
        assertTrue(activityService.skipsLogged.any { it.habitId == habitId && it.date == missDay })

        // Journey 3: Miss Sept 2. Run on Sept 3.
        val lockDay = LocalDate.of(2026, 9, 2)
        timeService.mockedCurrentDate = LocalDate.of(2026, 9, 3)
        service.performMaintenance(habitId)
        
        val lockedHabit = habitRepo.habits[habitId]!!
        assertTrue(lockedHabit.isLocked)
        assertEquals(lockDay, lockedHabit.lockedAt?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate())
        assertEquals("Milestone rewarded should reset on lock", 0, lockedHabit.lastMilestoneRewarded)
    }

    @Test
    fun `SpecificDays journey - earn skip, use skip, then lock`() = runBlocking {
        val habitId = UUID.randomUUID()
        val created = LocalDate.of(2026, 9, 7) // Monday
        val habit = createHabit(
            id = habitId,
            frequency = HabitFrequency.SpecificDays,
            createdAt = created,
            lastMaintenance = created
        )
        habit.trackedDays = listOf(0, 2) // Monday and Wednesday
        habitRepo.habits[habitId] = habit

        // Journey 1: Maintain streak for 30 days
        timeService.mockedCurrentDate = created.plusDays(30)
        var d = created
        while (!d.isAfter(timeService.mockedCurrentDate)) {
            if (d.dayOfWeek.value == 1 || d.dayOfWeek.value == 3) {
                activityService.addCompletion(habitId, d, 1)
            }
            d = d.plusDays(1)
        }

        service.performMaintenance(habitId)
        
        var current = habitRepo.habits[habitId]!!
        assertEquals(1, current.skipDaysAllowed)

        // Journey 2: Miss Monday Oct 12. Run on Tuesday Oct 13.
        val missMonday = LocalDate.of(2026, 10, 12)
        timeService.mockedCurrentDate = LocalDate.of(2026, 10, 13)
        
        // Complete intermediate scheduled days
        d = created.plusDays(31)
        while (d.isBefore(missMonday)) {
            if (d.dayOfWeek.value == 1 || d.dayOfWeek.value == 3) {
                activityService.addCompletion(habitId, d, 1)
            }
            d = d.plusDays(1)
        }

        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertEquals(0, current.skipDaysAllowed)
        assertTrue(activityService.skipsLogged.any { it.habitId == habitId && it.date == missMonday })

        // Journey 3: Miss Wednesday Oct 14. Run on Thursday Oct 15.
        val missWednesday = LocalDate.of(2026, 10, 14)
        timeService.mockedCurrentDate = LocalDate.of(2026, 10, 15)
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertTrue(current.isLocked)
        assertEquals(missWednesday, current.lockedAt?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate())
    }

    @Test
    fun `DaysPerWeek journey - earn skip, use skip, then lock`() = runBlocking {
        val habitId = UUID.randomUUID()
        val monday = LocalDate.of(2026, 9, 7)
        val habit = createHabit(
            id = habitId,
            frequency = HabitFrequency.DaysPerWeek,
            numDaysPerWeek = 2,
            createdAt = monday,
            lastMaintenance = monday
        )
        habitRepo.habits[habitId] = habit

        // Journey 1: Maintain streak for 30 days (Oct 7)
        timeService.mockedCurrentDate = monday.plusDays(30)
        for (w in 0..4) {
            val weekStart = monday.plusWeeks(w.toLong())
            activityService.addCompletion(habitId, weekStart, 1)
            activityService.addCompletion(habitId, weekStart.plusDays(1), 1)
        }

        service.performMaintenance(habitId)
        
        var current = habitRepo.habits[habitId]!!
        assertEquals(1, current.skipDaysAllowed)

        // Journey 2: Miss week Oct 12-18.
        // On Monday Oct 19, Sunday Oct 18 is missed.
        // We run on Monday so that the Sunday miss is no longer "today" and can be triggered.
        timeService.mockedCurrentDate = LocalDate.of(2026, 10, 19)
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertEquals(0, current.skipDaysAllowed)
        assertFalse(current.isLocked)
        // Check that a skip was logged for the week (the specific date depends on maintenance loop start)
        assertTrue(activityService.skipsLogged.any { it.habitId == habitId })

        // Journey 3: Miss week Oct 19-25. Sunday Oct 25 -> Lock.
        val sundayOfLockWeek = LocalDate.of(2026, 10, 25)
        timeService.mockedCurrentDate = LocalDate.of(2026, 10, 26)
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertTrue(current.isLocked)
        assertEquals(sundayOfLockWeek, current.lockedAt?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate())
    }

    @Test
    fun `Unlock journey - Does not unlock automatically after 30 days`() = runBlocking {
        val habitId = UUID.randomUUID()
        val lockedAt = LocalDate.of(2026, 8, 1)
        val habit = createHabit(id = habitId, createdAt = lockedAt.minusDays(1), lastMaintenance = lockedAt)
        habit.isLocked = true
        habit.lockedAt = Date.from(lockedAt.atStartOfDay(ZoneId.systemDefault()).toInstant())
        habitRepo.habits[habitId] = habit

        // 30 days later - still locked (Passive unlock removed)
        timeService.mockedCurrentDate = lockedAt.plusDays(30)
        service.performMaintenance(habitId)
        assertTrue("Habit should remain locked without streak progress", habitRepo.habits[habitId]!!.isLocked)
    }

    @Test
    fun `New habit created yesterday - should not lock today if not completed yet`() = runBlocking {
        val habitId = UUID.randomUUID()
        val yesterday = LocalDate.now().minusDays(1)
        val habit = createHabit(id = habitId, createdAt = yesterday, lastMaintenance = null)
        habitRepo.habits[habitId] = habit

        // Today is now
        timeService.mockedCurrentDate = LocalDate.now()
        
        // Yesterday was completed
        activityService.addCompletion(habitId, yesterday, 1)

        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertFalse("Habit should not be locked for today's miss", updatedHabit.isLocked)
    }

    @Test
    fun `New habit created yesterday - should lock if yesterday was missed`() = runBlocking {
        val habitId = UUID.randomUUID()
        val yesterday = LocalDate.now().minusDays(1)
        val habit = createHabit(id = habitId, createdAt = yesterday, lastMaintenance = null)
        habitRepo.habits[habitId] = habit

        // Today is now
        timeService.mockedCurrentDate = LocalDate.now()
        
        // Yesterday was NOT completed
        
        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertTrue("Habit should be locked if yesterday was missed", updatedHabit.isLocked)
        assertEquals(yesterday, updatedHabit.lockedAt?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate())
    }

    @Test
    fun `DaysPerWeek habit - 6 days goal - created Monday night - should not lock at Tuesday midnight`() = runBlocking {
        val habitId = UUID.randomUUID()
        val created = LocalDate.of(2026, 9, 7) // Monday
        val habit = createHabit(
            id = habitId,
            frequency = HabitFrequency.DaysPerWeek,
            numDaysPerWeek = 6,
            createdAt = created,
            lastMaintenance = null,
            target = 5
        )
        habitRepo.habits[habitId] = habit

        // Monday activity logged (quantity 6, target 5)
        activityService.addCompletion(habitId, created, 6)

        // Maintenance runs at Tuesday 00:00 AM
        val today = LocalDate.of(2026, 9, 8)
        timeService.mockedCurrentDate = today
        
        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertFalse("Habit should not be locked at Tuesday midnight", updatedHabit.isLocked)
    }

    @Test
    fun `DaysPerWeek habit - First week target adjustment`() = runBlocking {
        val habitId = UUID.randomUUID()
        val friday = LocalDate.of(2026, 9, 11)
        timeService.mockedCurrentDate = friday
        
        // Created Friday, target 5 days per week.
        // Only Fri, Sat, Sun (3 days) are available in the first week.
        val habit = createHabit(
            id = habitId,
            frequency = HabitFrequency.DaysPerWeek,
            numDaysPerWeek = 5,
            createdAt = friday,
            lastMaintenance = null
        )
        habitRepo.habits[habitId] = habit

        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertFalse("Habit should not lock on first week if goal is impossible due to creation date", updatedHabit.isLocked)
    }

    // --- Fakes ---

    class FakeHabitRepository : IHabitRepository {
        val habits = mutableMapOf<UUID, Habit>()
        override fun getHabits(): Flow<List<Habit>> = emptyFlow()
        override fun getHabitFlow(habitId: UUID): Flow<Habit?> = emptyFlow()
        override suspend fun getHabitById(habitId: UUID): Habit? = habits[habitId]
        override suspend fun createHabit(habit: Habit) { habits[habit.id] = habit }
        override suspend fun deleteHabit(habit: Habit) { habits.remove(habit.id) }
        override suspend fun getAllHabitsSync(): List<Habit> = habits.values.toList()
    }

    class FakeHabitActivityService : IHabitActivityService {
        val dailyTotals = mutableMapOf<UUID, MutableMap<LocalDate, Int>>()
        val skipsLogged = mutableListOf<SkipLog>()
        data class SkipLog(val habitId: UUID, val date: LocalDate)

        fun addCompletion(habitId: UUID, date: LocalDate, quantity: Int) {
            dailyTotals.getOrPut(habitId) { mutableMapOf() }[date] = (dailyTotals[habitId]?.get(date) ?: 0) + quantity
        }

        override suspend fun logHabitActivity(habitId: UUID, quantity: Int) {}
        override suspend fun logSkipActivity(habitId: UUID, quantity: Int, date: Date) {
            val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            skipsLogged.add(SkipLog(habitId, localDate))
            addCompletion(habitId, localDate, quantity)
        }
        override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<com.example.forge.core.database.entity.HabitActivity>> = emptyFlow()
        override fun getActivitiesForToday(habitIds: List<UUID>): Flow<List<com.example.forge.core.database.entity.HabitActivity>> = emptyFlow()
        override fun getActivitiesForHabit(habitId: UUID): Flow<List<com.example.forge.core.database.entity.HabitActivity>> = emptyFlow()
        override fun getAllActivities(): Flow<List<com.example.forge.core.database.entity.HabitActivity>> = emptyFlow()
        override fun getAllDailyQuantities(): Flow<List<com.example.forge.core.database.pojo.DailyHabitQuantity>> = emptyFlow()
        override fun getDailyQuantitiesForHabit(habitId: UUID) = emptyFlow<List<com.example.forge.core.database.pojo.DailyHabitQuantity>>()
        override suspend fun getDailyQuantitiesForHabitSync(habitId: UUID): List<com.example.forge.core.database.pojo.DailyHabitQuantity> {
            return dailyTotals[habitId]?.map { (date, quantity) ->
                com.example.forge.core.database.pojo.DailyHabitQuantity(habitId, date, quantity)
            } ?: emptyList()
        }
        override suspend fun getCompletedQuantityByRange(habitId: UUID, from: Date, to: Date): Map<LocalDate, Int> {
            val fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            return dailyTotals[habitId]?.filter { (date, _) -> 
                !date.isBefore(fromDate) && !date.isAfter(toDate)
            } ?: emptyMap()
        }
        override suspend fun deleteHabitActivity(activityId: UUID) {}
    }

    class FakeHabitStatsService(private val realService: HabitStatsService) : IHabitStatsService {
        var streakOverride: StreakInfo? = null
        override fun getHabitStats(habitId: UUID): Flow<HabitStats> = emptyFlow()
        override fun getMonthlyActivityData(habitId: UUID, yearMonth: YearMonth): Flow<List<com.example.forge.core.uiEntities.ActivityData>> = emptyFlow()
        override fun getRangeActivityData(habitId: UUID, startMonth: YearMonth, monthCount: Int): Flow<List<com.example.forge.core.uiEntities.ActivityData>> = emptyFlow()
        override fun getAllMonthlyCompletion(habitId: UUID): Flow<Map<YearMonth, List<DailyCompletion>>> = emptyFlow()
        override suspend fun getStreakInfo(habitId: UUID): StreakInfo = streakOverride ?: realService.getStreakInfo(habitId)
    }

    class FakeTimeService : ITimeService {
        var mockedCurrentDate = LocalDate.now()
        override fun getCurrentDateFlow(): Flow<LocalDate> = emptyFlow()
        override fun getCurrentDate(): LocalDate = mockedCurrentDate
        override fun toLocalDate(date: Date): LocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        override fun toStartOfDayDate(localDate: LocalDate): Date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        override fun toEndOfDayDate(localDate: LocalDate): Date = Date.from(localDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
    }

    class FakeWidgetUpdater : IWidgetUpdater {
        var updateAllWidgetsCalledCount = 0
        override suspend fun updateAllWidgets() {
            updateAllWidgetsCalledCount++
        }
    }
}
