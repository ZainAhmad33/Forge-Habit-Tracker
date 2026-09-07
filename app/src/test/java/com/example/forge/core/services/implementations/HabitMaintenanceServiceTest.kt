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

    @Before
    fun setup() {
        habitRepo = FakeHabitRepository()
        activityService = FakeHabitActivityService()
        timeService = FakeTimeService()

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
            timeService = timeService
        )
    }

    private fun createHabit(
        id: UUID = UUID.randomUUID(),
        frequency: HabitFrequency = HabitFrequency.EveryDay,
        skipDays: Int = 0,
        createdAt: LocalDate = LocalDate.now().minusDays(10),
        lastMaintenance: LocalDate? = null,
        numDaysPerWeek: Int = 7
    ): Habit {
        return Habit(
            id = id,
            title = "Maintenance Test",
            category = HabitCategory.Health,
            emoji = "⚙️",
            habitType = HabitType.YesNo,
            frequencyType = frequency,
            numberOfTrackedDays = numDaysPerWeek,
            completionTargetPerDay = 1,
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
        val habit = createHabit(id = habitId, createdAt = created, lastMaintenance = created)
        habitRepo.habits[habitId] = habit

        // Journey 1: Maintain 30 day streak to earn 1 skip day
        timeService.mockedCurrentDate = created.plusDays(30)
        
        // Mark all days as completed (31 days total)
        for (i in 0..30) {
            activityService.addCompletion(habitId, created.plusDays(i.toLong()), 1)
        }

        // Verify initial streak
        assertEquals(31, realStatsService.getStreakInfo(habitId).count)

        service.performMaintenance(habitId)
        
        val updatedHabit = habitRepo.habits[habitId]!!
        assertEquals(1, updatedHabit.skipDaysAllowed)
        assertEquals(30, updatedHabit.lastMilestoneRewarded)

        // Journey 2: Miss a day, skip day should be consumed
        val missDay = created.plusDays(31)
        timeService.mockedCurrentDate = missDay
        
        service.performMaintenance(habitId)
        
        val skippedHabit = habitRepo.habits[habitId]!!
        assertEquals(0, skippedHabit.skipDaysAllowed)
        assertFalse(skippedHabit.isLocked)
        assertTrue(activityService.skipsLogged.any { it.habitId == habitId && it.date == missDay })
        
        // Verify streak is preserved (31 days + 1 skipped day = 32)
        assertEquals(32, realStatsService.getStreakInfo(habitId).count)

        // Journey 3: Miss another day, habit should lock
        val lockDay = created.plusDays(32)
        timeService.mockedCurrentDate = lockDay
        
        service.performMaintenance(habitId)
        
        val lockedHabit = habitRepo.habits[habitId]!!
        assertTrue(lockedHabit.isLocked)
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
        
        // Complete all scheduled days
        var d = created
        while (!d.isAfter(timeService.mockedCurrentDate)) {
            if (d.dayOfWeek.value == 1 || d.dayOfWeek.value == 3) {
                activityService.addCompletion(habitId, d, 1)
            }
            d = d.plusDays(1)
        }

        // Verify initial streak
        assertEquals(31, realStatsService.getStreakInfo(habitId).count)

        service.performMaintenance(habitId)
        
        var current = habitRepo.habits[habitId]!!
        assertEquals(1, current.skipDaysAllowed)

        // Journey 2: Miss a scheduled Monday
        val missMonday = created.plusWeeks(5) // Monday Oct 12
        timeService.mockedCurrentDate = missMonday
        
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
        
        // Streak preserved
        assertEquals(36, realStatsService.getStreakInfo(habitId).count)

        // Journey 3: Miss a scheduled Wednesday
        val missWednesday = missMonday.plusDays(2)
        timeService.mockedCurrentDate = missWednesday
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertTrue(current.isLocked)
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

        // Journey 1: Maintain streak for 30 days
        timeService.mockedCurrentDate = monday.plusDays(30)
        
        // Complete 2 days every week
        for (w in 0..4) {
            val weekStart = monday.plusWeeks(w.toLong())
            activityService.addCompletion(habitId, weekStart, 1)
            activityService.addCompletion(habitId, weekStart.plusDays(1), 1)
        }

        service.performMaintenance(habitId)
        
        var current = habitRepo.habits[habitId]!!
        assertEquals(1, current.skipDaysAllowed)

        // Journey 2: Make goal unachievable in a week
        val sundayFail = monday.plusWeeks(4).plusDays(12) // Sunday Oct 11
        timeService.mockedCurrentDate = sundayFail
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertEquals(0, current.skipDaysAllowed)
        assertFalse(current.isLocked)
        assertTrue(activityService.skipsLogged.any { it.habitId == habitId && it.date == sundayFail })

        // Journey 3: Make goal unachievable again in next week (Should lock)
        val nextSundayFail = sundayFail.plusWeeks(1)
        timeService.mockedCurrentDate = nextSundayFail
        service.performMaintenance(habitId)
        
        current = habitRepo.habits[habitId]!!
        assertTrue(current.isLocked)
    }

    @Test
    fun `Unlock journey - Serves 30 days sentence`() = runBlocking {
        val habitId = UUID.randomUUID()
        val lockedAt = LocalDate.of(2026, 8, 1)
        val habit = createHabit(id = habitId, createdAt = lockedAt.minusDays(1), lastMaintenance = lockedAt)
        habit.isLocked = true
        habit.lockedAt = Date.from(lockedAt.atStartOfDay(ZoneId.systemDefault()).toInstant())
        habitRepo.habits[habitId] = habit

        // 29 days later - still locked
        timeService.mockedCurrentDate = lockedAt.plusDays(29)
        service.performMaintenance(habitId)
        assertTrue(habitRepo.habits[habitId]!!.isLocked)

        // 30 days later - unlocked
        timeService.mockedCurrentDate = lockedAt.plusDays(30)
        service.performMaintenance(habitId)
        assertFalse(habitRepo.habits[habitId]!!.isLocked)
    }

    @Test
    fun `Unlock journey - Earns skip day to unlock early`() = runBlocking {
        val habitId = UUID.randomUUID()
        val lockDate = LocalDate.of(2026, 9, 1)
        val habit = createHabit(id = habitId, createdAt = lockDate.minusDays(5), lastMaintenance = lockDate)
        habit.isLocked = true
        habit.lockedAt = Date.from(lockDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        habitRepo.habits[habitId] = habit

        // Rebuild streak to 30
        statsService.streakOverride = StreakInfo(30, lockDate)
        
        service.performMaintenance(habitId)
        
        val updated = habitRepo.habits[habitId]!!
        assertFalse(updated.isLocked)
        assertEquals(0, updated.skipDaysAllowed)
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
            return dailyTotals[habitId] ?: emptyMap()
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
}
