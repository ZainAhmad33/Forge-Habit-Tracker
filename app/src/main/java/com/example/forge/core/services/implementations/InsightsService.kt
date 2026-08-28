package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject

class InsightsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val activityService: IHabitActivityService,
    private val timeService: ITimeService,
    private val statsService: HabitStatsService // Injecting implementation to reuse internal methods
) : IInsightsService {

    override fun getGlobalStats(period: InsightPeriod): Flow<GlobalStats> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            if (habits.isEmpty()) return@combine GlobalStats(0f, 0, 0, 0, 0)

            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            
            // 1. Completion Rate
            val (currentRate, previousRate) = calculateGlobalRateForPeriod(habits, habitDailyTotals, period, today)
            val rateChange = if (previousRate > 0) ((currentRate - previousRate) / previousRate * 100).toInt() else 0

            // 2. Global Streak
            // Global streak: days where at least one habit was completed
            val globalCompletedDates = dailyQuantities.filter { it.totalQuantity > 0 }
                .map { it.day }.toSet()
            val (currentStreak, bestStreak) = calculateGlobalStreaks(globalCompletedDates, today)

            // 3. Perfect Days
            val perfectDays = calculatePerfectDays(habits, habitDailyTotals, today)

            GlobalStats(
                completionRate = currentRate,
                currentGlobalStreak = currentStreak,
                bestGlobalStreak = bestStreak,
                perfectDaysCount = perfectDays,
                rateChange = rateChange
            )
        }.flowOn(Dispatchers.Default)
    }

    override fun getActivityHeatmap(): Flow<List<HeatmapCell>> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitMap = habits.associateBy { it.id }
            val dailyActivity = dailyQuantities.associateBy { it.day }
            
            val startDate = today.minusDays(364)
            val cells = mutableListOf<HeatmapCell>()
            
            for (i in 0..364) {
                val date = startDate.plusDays(i.toLong())
                val count = dailyQuantities.count { it.day == date && it.totalQuantity > 0 } // Basic implementation: any habit done
                // Actually we should track which habits were done
                // We don't have per-habit quantity in getAllDailyQuantities easily mapped back to habit titles here without more info
                // But for now, intensity based on count of completed habits
                
                // We might need a better query to get (Date, List<HabitTitle>)
                cells.add(HeatmapCell(date, calculateIntensity(count), emptyList()))
            }
            cells
        }.flowOn(Dispatchers.Default)
    }

    override fun getMomentumTrend(): Flow<List<MomentumPoint>> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            val points = mutableListOf<MomentumPoint>()
            
            for (i in 0..89) { // Last 90 days
                val date = today.minusDays(i.toLong())
                val sevenDayAvg = calculateRollingAverage(habits, habitDailyTotals, date, 7)
                val thirtyDayAvg = calculateRollingAverage(habits, habitDailyTotals, date, 30)
                points.add(MomentumPoint(date, sevenDayAvg, thirtyDayAvg))
            }
            points.reversed()
        }.flowOn(Dispatchers.Default)
    }

    override fun getHabitLeaderboard(period: InsightPeriod): Flow<List<LeaderboardEntry>> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            
            val entries = habits.map { habit ->
                val rate = statsService.calculateOverallRate(
                    habit, 
                    habitDailyTotals[habit.id] ?: emptyMap(), 
                    habit.completionTargetPerDay, 
                    timeService.toLocalDate(habit.createdAt), 
                    today
                )
                LeaderboardEntry(habit.id, habit.title, habit.emoji, rate)
            }.sortedByDescending { it.completionRate }
            
            entries
        }.flowOn(Dispatchers.Default)
    }

    override fun getWeeklyPerformance(): Flow<WeeklyPerformance> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            val dayRates = mutableMapOf<Int, Float>()
            
            for (dayIdx in 0..6) {
                var totalExpected = 0
                var totalCompleted = 0
                
                val startDate = today.minusMonths(3)
                var date = startDate
                while (!date.isAfter(today)) {
                    if (date.dayOfWeek.value - 1 == dayIdx) {
                        habits.forEach { habit ->
                            val totals = habitDailyTotals[habit.id] ?: emptyMap()
                            if (statsService.calculateOverallRate(habit, totals, habit.completionTargetPerDay, date, date) > 0.99f) {
                                totalCompleted++
                            }
                            totalExpected++
                        }
                    }
                    date = date.plusDays(1)
                }
                dayRates[dayIdx] = if (totalExpected > 0) totalCompleted.toFloat() / totalExpected else 0f
            }
            
            val bestDay = dayRates.maxByOrNull { it.value }?.key ?: 0
            val worstDay = dayRates.minByOrNull { it.value }?.key ?: 0
            
            WeeklyPerformance(dayRates, bestDay, worstDay)
        }.flowOn(Dispatchers.Default)
    }

    override fun getCategoryBreakdown(): Flow<List<CategoryShare>> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            
            habits.groupBy { it.category }.map { (category, categoryHabits) ->
                val avgRate = categoryHabits.map { habit ->
                    statsService.calculateOverallRate(habit, habitDailyTotals[habit.id] ?: emptyMap(), habit.completionTargetPerDay, timeService.toLocalDate(habit.createdAt), today)
                }.average().toFloat()
                
                CategoryShare(category, avgRate, categoryHabits.size)
            }
        }.flowOn(Dispatchers.Default)
    }

    override fun getStreakDistribution(): Flow<List<StreakBucket>> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, today ->
            val habitDailyTotals = dailyQuantities.groupBy { it.habitId }
                .mapValues { (_, quantities) -> quantities.associate { it.day to it.totalQuantity } }
            val streaks = habits.map { habit ->
                statsService.calculateCurrentStreak(habit, habitDailyTotals[habit.id] ?: emptyMap(), habit.completionTargetPerDay, today).count
            }
            
            listOf(
                StreakBucket("0 days", streaks.count { it == 0 }),
                StreakBucket("1-6 days", streaks.count { it in 1..6 }),
                StreakBucket("7-29 days", streaks.count { it in 7..29 }),
                StreakBucket("30+ days", streaks.count { it >= 30 })
            )
        }.flowOn(Dispatchers.Default)
    }

    // Helper methods
    
    private fun calculateGlobalRateForPeriod(
        habits: List<Habit>,
        habitDailyTotals: Map<UUID, Map<LocalDate, Int>>,
        period: InsightPeriod,
        today: LocalDate
    ): Pair<Float, Float> {
        val (currentStart, currentEnd) = getPeriodRange(period, today)
        val (prevStart, prevEnd) = getPreviousPeriodRange(period, currentStart)
        
        val currentRate = calculateGlobalRateInRange(habits, habitDailyTotals, currentStart, currentEnd, today)
        val prevRate = calculateGlobalRateInRange(habits, habitDailyTotals, prevStart, prevEnd, today)
        
        return currentRate to prevRate
    }

    private fun calculateGlobalRateInRange(
        habits: List<Habit>,
        habitDailyTotals: Map<UUID, Map<LocalDate, Int>>,
        start: LocalDate,
        end: LocalDate,
        today: LocalDate
    ): Float {
        var totalExpected = 0
        var totalCompleted = 0
        
        habits.forEach { habit ->
            val habitStart = timeService.toLocalDate(habit.createdAt)
            var date = if (start.isBefore(habitStart)) habitStart else start
            val totals = habitDailyTotals[habit.id] ?: emptyMap()
            while (!date.isAfter(end) && !date.isAfter(today)) {
                if (isHabitScheduled(habit, date)) {
                    totalExpected++
                    if ((totals[date] ?: 0) >= habit.completionTargetPerDay) {
                        totalCompleted++
                    }
                }
                date = date.plusDays(1)
            }
        }
        return if (totalExpected > 0) totalCompleted.toFloat() / totalExpected else 0f
    }

    private fun isHabitScheduled(habit: Habit, date: LocalDate): Boolean {
        // Simple copy of HabitStatsService logic
        val habitStartDate = timeService.toLocalDate(habit.createdAt)
        if (date.isBefore(habitStartDate)) return false
        return when (habit.frequencyType) {
            HabitFrequency.EveryDay, HabitFrequency.DaysPerWeek -> true
            HabitFrequency.SpecificDays -> habit.trackedDays.contains(date.dayOfWeek.value - 1)
        }
    }

    private fun getPeriodRange(period: InsightPeriod, today: LocalDate): Pair<LocalDate, LocalDate> {
        return when (period) {
            InsightPeriod.Today -> today to today
            InsightPeriod.Week -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) to today
            InsightPeriod.Month -> today.withDayOfMonth(1) to today
            InsightPeriod.AllTime -> LocalDate.MIN to today
        }
    }

    private fun getPreviousPeriodRange(period: InsightPeriod, currentStart: LocalDate): Pair<LocalDate, LocalDate> {
        return when (period) {
            InsightPeriod.Today -> currentStart.minusDays(1) to currentStart.minusDays(1)
            InsightPeriod.Week -> currentStart.minusWeeks(1) to currentStart.minusDays(1)
            InsightPeriod.Month -> currentStart.minusMonths(1) to currentStart.minusDays(1)
            InsightPeriod.AllTime -> LocalDate.MIN to currentStart.minusDays(1)
        }
    }

    private fun calculateGlobalStreaks(completedDates: Set<LocalDate>, today: LocalDate): Pair<Int, Int> {
        if (completedDates.isEmpty()) return 0 to 0
        
        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0
        
        // Current streak
        var date = today
        // If today not done, check yesterday
        if (!completedDates.contains(date)) date = date.minusDays(1)
        
        while (completedDates.contains(date)) {
            currentStreak++
            date = date.minusDays(1)
        }
        
        // Best streak (global)
        val sortedDates = completedDates.sorted()
        if (sortedDates.isEmpty()) return 0 to 0
        
        var prevDate = sortedDates.first()
        tempStreak = 1
        bestStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val currDate = sortedDates[i]
            if (ChronoUnit.DAYS.between(prevDate, currDate) == 1L) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            bestStreak = maxOf(bestStreak, tempStreak)
            prevDate = currDate
        }
        
        return currentStreak to bestStreak
    }

    private fun calculatePerfectDays(habits: List<Habit>, habitDailyTotals: Map<UUID, Map<LocalDate, Int>>, today: LocalDate): Int {
        // A day is perfect if ALL habits scheduled for that day were completed
        // This is potentially slow. Let's limit to last year.
        var perfectDays = 0
        val startDate = today.minusYears(1)
        var date = startDate
        
        while (!date.isAfter(today)) {
            val scheduledHabits = habits.filter { isHabitScheduled(it, date) }
            if (scheduledHabits.isNotEmpty()) {
                val allCompleted = scheduledHabits.all { habit ->
                    val totals = habitDailyTotals[habit.id] ?: emptyMap()
                    (totals[date] ?: 0) >= habit.completionTargetPerDay
                }
                if (allCompleted) perfectDays++
            }
            date = date.plusDays(1)
        }
        return perfectDays
    }

    private fun calculateIntensity(count: Int): Int {
        return when {
            count == 0 -> 0
            count == 1 -> 1
            count in 2..3 -> 2
            count in 4..5 -> 3
            else -> 4
        }
    }

    private fun calculateRollingAverage(habits: List<Habit>, habitDailyTotals: Map<UUID, Map<LocalDate, Int>>, endDate: LocalDate, days: Int): Float {
        val startDate = endDate.minusDays(days.toLong() - 1)
        return calculateGlobalRateInRange(habits, habitDailyTotals, startDate, endDate, endDate)
    }
}
