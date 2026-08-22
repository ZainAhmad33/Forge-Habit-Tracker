package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.BestWeekData
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.GapData
import com.example.forge.core.services.interfaces.HabitStats
import com.example.forge.core.services.interfaces.StreakInfo
import com.example.forge.core.services.interfaces.HabitTrends
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.services.interfaces.MonthlyRate
import com.example.forge.core.services.interfaces.TrendData
import com.example.forge.core.uiEntities.ActivityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

class HabitStatsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val activityService: IHabitActivityService,
    private val timeService: ITimeService
) : IHabitStatsService {

    override fun getHabitStats(habitId: UUID): Flow<HabitStats> {
        return combine(
            activityService.getDailyQuantitiesForHabit(habitId),
            habitRepository.getHabitFlow(habitId),
            timeService.getCurrentDateFlow()
        ) { dailyQuantitiesList, habit, today ->
            if (habit == null) return@combine HabitStats(0, 0, 0f, emptyList(), emptyList())

            val dailyQuantities = dailyQuantitiesList.associate { it.day to it.totalQuantity }

            val target = habit.completionTargetPerDay

            val currentStreakInfo = calculateCurrentStreak(habit, dailyQuantities, target, today)
            val currentStreak = currentStreakInfo.count
            val bestStreak = calculateBestStreak(habit, dailyQuantities, target, today)
            val overallRate = calculateOverallRate(habit, dailyQuantities, target, timeService.toLocalDate(habit.createdAt), today)

            val monthlyData = calculateMonthlyCompletion(dailyQuantities, habit, today)
            val quarterlyData = calculateQuarterlyRates(habit, dailyQuantities, today)
            val trends = calculateTrends(habit, dailyQuantities, target, today)

            HabitStats(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                overallCompletionRate = overallRate,
                monthlyCompletionData = monthlyData,
                quarterlyCompletionRates = quarterlyData,
                currentStreakStartDate = currentStreakInfo.startDate,
                trends = trends
            )
        }.flowOn(Dispatchers.IO) // 👈 Critical: Shift calculations off the main thread
    }

    override fun getMonthlyActivityData(habitId: UUID, yearMonth: YearMonth): Flow<List<ActivityData>> {
        return getRangeActivityData(habitId, yearMonth, 1)
    }

    override fun getRangeActivityData(habitId: UUID, startMonth: YearMonth, monthCount: Int): Flow<List<ActivityData>> {
        val startDate = startMonth.atDay(1)
        val endDate = startMonth.plusMonths(monthCount.toLong() - 1).atEndOfMonth()

        return combine(
            activityService.getDailyQuantitiesForHabit(habitId),
            habitRepository.getHabitFlow(habitId)
        ) { aggregatedData, habit ->
            if (habit == null) return@combine emptyList()
            val target = habit.completionTargetPerDay

            val dailyQuantities = aggregatedData
                .filter { !it.day.isBefore(startDate) && !it.day.isAfter(endDate) }
                .associate { it.day to it.totalQuantity }

            val allData = mutableListOf<ActivityData>()
            for (i in 0 until monthCount) {
                val currentMonth = startMonth.plusMonths(i.toLong())
                (1..currentMonth.lengthOfMonth()).forEach { day ->
                    val date = currentMonth.atDay(day)
                    val quantity = dailyQuantities[date] ?: 0
                    val percentage = if (target > 0) (quantity * 100) / target else 0
                    allData.add(ActivityData(date, percentage))
                }
            }
            allData
        }.flowOn(Dispatchers.IO)
    }

    internal fun calculateCurrentStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): StreakInfo {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            return calculateCurrentStreakForDaysPerWeek(habit, today, dailyTotals, target)
        }

        val firstDate = dailyTotals.keys.minOrNull() ?: return StreakInfo(0, null)
        val lastDate = today
        var streak = 0
        var current = lastDate
        var streakStartDate: LocalDate? = null

        if (isScheduledForDate(habit, current)) {
            if ((dailyTotals[current] ?: 0) >= target) {
                streak++
                streakStartDate = current
            }
        } else {
            streak++
            streakStartDate = current
        }
        current = current.minusDays(1)

        while (!current.isBefore(firstDate)) {
            if (isScheduledForDate(habit, current)) {
                if ((dailyTotals[current] ?: 0) >= target) {
                    streak++
                    streakStartDate = current
                } else {
                    return StreakInfo(streak, streakStartDate)
                }
            } else {
                streak++
                streakStartDate = current
            }
            current = current.minusDays(1)
        }
        return StreakInfo(streak, streakStartDate)
    }

    private fun calculateCurrentStreakForDaysPerWeek(
        habit: Habit,
        today: LocalDate,
        dailyTotals: Map<LocalDate, Int>,
        target: Int
    ): StreakInfo {
        var streak = 0
        var streakStartDate: LocalDate? = null
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        var weekStart = currentWeekStart
        var isCurrentWeek = true

        while (!weekStart.isBefore(startOfFirstWeek)) {
            val weekEnd = weekStart.plusDays(6)
            var completionsInWeek = 0
            var continueWeeksCompletion = true
            var completionsBeforeFirstMiss = 0
            var firstMissInWeek: LocalDate? = null

            for (i in 0..6) {
                val d = weekEnd.minusDays(i.toLong())
                if (d.isBefore(habitStart)) continue
                if (d.isAfter(today)) continue
                if ((dailyTotals[d] ?: 0) >= target) {
                    completionsInWeek++
                    if (continueWeeksCompletion) {
                        completionsBeforeFirstMiss += 1
                    }
                } else {
                    if (continueWeeksCompletion) {
                        firstMissInWeek = d
                    }
                    continueWeeksCompletion = false
                }
            }

            val goalMet = completionsInWeek >= habit.numberOfTrackedDays

            if (isCurrentWeek) {
                if (goalMet) {
                    val daysInStreak = ChronoUnit.DAYS.between(weekStart, today).toInt() + 1
                    streak += daysInStreak
                    streakStartDate = weekStart
                } else {
                    // Check if still possible
                    val daysRemaining = ChronoUnit.DAYS.between(today, weekEnd).toInt()
                    if (completionsInWeek + daysRemaining < habit.numberOfTrackedDays) {
                        return StreakInfo(0, null)
                    }
                    // Still possible, continue checking previous weeks without incrementing streak
                }
                isCurrentWeek = false
            } else {
                if (goalMet) {
                    streak += 7
                    streakStartDate = weekStart
                } else {
                    streak += completionsBeforeFirstMiss
                    if (completionsBeforeFirstMiss > 0) {
                        streakStartDate = firstMissInWeek?.plusDays(1) ?: weekStart
                    }
                    break
                }
            }
            weekStart = weekStart.minusWeeks(1)
        }
        return StreakInfo(streak, streakStartDate)
    }

    private fun calculateBestStreakForDaysPerWeek(
        habit: Habit,
        today: LocalDate,
        dailyTotals: Map<LocalDate, Int>,
        target: Int
    ): Int {
        var currentStreak = 0
        var maxStreak = 0
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        var weekStart = startOfFirstWeek

        while (!weekStart.isAfter(currentWeekStart)) {
            var completionsInWeek = 0
            var continueWeeksCompletion = true
            var completionsBeforeFirstMiss = 0

            for (i in 0..6) {
                val d = weekStart.plusDays(i.toLong())
                if (d.isBefore(habitStart)) continue
                if (d.isAfter(today)) continue
                if ((dailyTotals[d] ?: 0) >= target) {
                    completionsInWeek++
                    if (continueWeeksCompletion)
                        completionsBeforeFirstMiss += 1
                } else {
                    continueWeeksCompletion = false
                }
            }

            val goalMet = completionsInWeek >= habit.numberOfTrackedDays
            if (goalMet) {
                if (weekStart == currentWeekStart){
                    // streak continues but range is exhausted
                    currentStreak += ChronoUnit.DAYS.between(weekStart, today).toInt() + 1
                    maxStreak = maxOf(currentStreak, maxStreak)
                }
                else{
                    currentStreak += 7
                }
            } else {
                // streak broken
                currentStreak += completionsBeforeFirstMiss
                maxStreak = maxOf(currentStreak, maxStreak)
                currentStreak = 0
            }
            weekStart = weekStart.plusWeeks(1)
        }
        return maxStreak
    }

    internal fun calculateBestStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): Int {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            return calculateBestStreakForDaysPerWeek(habit, today, dailyTotals, target)
        }

        if (dailyTotals.isEmpty()) return 0
        val firstDate = dailyTotals.keys.minOrNull() ?: return 0
        val lastDate = today
        var maxStreak = 0
        var currentStreak = 0
        var current = lastDate

        while (!current.isBefore(firstDate)) {
            if (isScheduledForDate(habit, current)) {
                if ((dailyTotals[current] ?: 0) >= target) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
            } else {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            }
            current = current.minusDays(1)
        }
        return maxStreak
    }

    private fun isScheduledForDate(habit: Habit, date: LocalDate): Boolean {
        val habitStartDate = timeService.toLocalDate(habit.createdAt)
        if (date.isBefore(habitStartDate)) return false

        return when (habit.frequencyType) {
            HabitFrequency.EveryDay, HabitFrequency.DaysPerWeek -> true
            HabitFrequency.SpecificDays -> habit.trackedDays.contains(date.dayOfWeek.value - 1)
        }
    }

    internal fun calculateOverallRate(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, createdDate: LocalDate, today: LocalDate): Float {
        var date = createdDate
        var expectedDays = 0
        var successfulDays = 0

        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            // For DaysPerWeek, we calculate it by looking at weeks
            val startOfFirstWeek = createdDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            var weekStart = startOfFirstWeek
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            while (!weekStart.isAfter(currentWeekStart)) {
                val currentWeekEnd = weekStart.plusDays(6)
                val targetForWeek = habit.numberOfTrackedDays

                var completionsInWeek = 0
                var daysPassedInWeek = 0

                for (i in 0..6) {
                    val d = weekStart.plusDays(i.toLong())
                    if (d.isBefore(createdDate)) continue
                    if (d.isAfter(today)) break

                    daysPassedInWeek++
                    if ((dailyTotals[d] ?: 0) >= target) {
                        completionsInWeek++
                    }
                }

                // A week is "full" if its end is not after today
                val isFullWeek = !currentWeekEnd.isAfter(today)

                if (isFullWeek) {
                    expectedDays += targetForWeek
                    successfulDays += minOf(completionsInWeek, targetForWeek)
                } else {
                    // Current partial week: expected is at most the weekly target
                    expectedDays += minOf(daysPassedInWeek, targetForWeek)
                    successfulDays += minOf(completionsInWeek, targetForWeek)
                }

                weekStart = weekStart.plusWeeks(1)
            }
        } else {
            // EveryDay or SpecificDays
            while (!date.isAfter(today)) {
                if (isScheduledForDate(habit, date)) {
                    expectedDays++
                    if ((dailyTotals[date] ?: 0) >= target) {
                        successfulDays++
                    }
                }
                date = date.plusDays(1)
            }
        }

        if (expectedDays <= 0) return 0f
        return successfulDays.toFloat() / expectedDays
    }

    private fun calculateMonthlyCompletion(dailyTotals: Map<LocalDate, Int>, habit: Habit, today: LocalDate): List<DailyCompletion> {
        val daysInMonth = today.lengthOfMonth()
        val target = habit.completionTargetPerDay

        val successfulDates = dailyTotals.filter { it.value >= target }.keys

        return (1..daysInMonth).map { day ->
            val date = LocalDate.of(today.year, today.month, day)
            val sum = dailyTotals[date] ?: 0
            val isCompleted = sum >= target

            val isSkipDay = when (habit.frequencyType) {
                HabitFrequency.EveryDay -> false
                HabitFrequency.SpecificDays -> {
                    // Convert DayOfWeek (1=Mon..7=Sun) to app format (0=Mon..6=Sun)
                    val dayOfWeekIdx = date.dayOfWeek.value - 1
                    !habit.trackedDays.contains(dayOfWeekIdx)
                }
                HabitFrequency.DaysPerWeek -> {
                    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val endOfWeek = startOfWeek.plusDays(6)

                    var count = 0
                    var d = startOfWeek
                    while (!d.isAfter(endOfWeek)) {
                        if (successfulDates.contains(d)) count++
                        d = d.plusDays(1)
                    }

                    count == habit.numberOfTrackedDays && !isCompleted
                }
            }

            DailyCompletion(day, sum, isSkipDay)
        }
    }

    private fun calculateQuarterlyRates(habit: Habit, dailyTotals: Map<LocalDate, Int>, today: LocalDate): List<MonthlyRate> {
        val result = mutableListOf<MonthlyRate>()
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val target = habit.completionTargetPerDay
        var lastMonthDate = today.withDayOfMonth(1).minusMonths(1)

        repeat(3) {
            if (lastMonthDate.isAfter(habitStart.withDayOfMonth(1)) || lastMonthDate == habitStart.withDayOfMonth(1)) {
                val daysInMonth = lastMonthDate.lengthOfMonth()
                val monthName = lastMonthDate.month.name.take(3)

                val successfulDays = (1..daysInMonth).count { day ->
                    val date = lastMonthDate.withDayOfMonth(day)
                    (dailyTotals[date] ?: 0) >= target
                }

                result.add(MonthlyRate(monthName, if (daysInMonth > 0) successfulDays.toFloat() / daysInMonth else 0f))
            }
            lastMonthDate = lastMonthDate.minusMonths(1)
        }

        return result.reversed()
    }

    internal fun calculateTrends(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): HabitTrends {
        val habitStart = timeService.toLocalDate(habit.createdAt)
        
        val weeklyTrend = calculateTrend(habit, dailyTotals, target, 
            currentStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            currentEnd = today,
            previousStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1),
            previousEnd = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1),
            today = today
        )

        val monthlyTrend = calculateTrend(habit, dailyTotals, target,
            currentStart = today.withDayOfMonth(1),
            currentEnd = today,
            previousStart = today.withDayOfMonth(1).minusMonths(1),
            previousEnd = today.withDayOfMonth(1).minusDays(1),
            today = today
        )

        val longestGap = calculateLongestGap(habit, dailyTotals, target, habitStart, today)
        val allTimeAverage = calculateOverallRate(habit, dailyTotals, target, habitStart, today)
        val bestWeek = calculateBestWeek(habit, dailyTotals, target, habitStart, today, today)

        return HabitTrends(
            weeklyTrend = weeklyTrend,
            monthlyTrend = monthlyTrend,
            longestGap = longestGap,
            allTimeAverage = allTimeAverage,
            bestWeek = bestWeek
        )
    }

    private fun calculateTrend(
        habit: Habit, 
        dailyTotals: Map<LocalDate, Int>, 
        target: Int, 
        currentStart: LocalDate, 
        currentEnd: LocalDate, 
        previousStart: LocalDate, 
        previousEnd: LocalDate,
        today: LocalDate
    ): TrendData {
        val currentRate = calculateRangeRate(habit, dailyTotals, target, currentStart, currentEnd, today)
        val previousRate = calculateRangeRate(habit, dailyTotals, target, previousStart, previousEnd, today)
        
        val change = if (previousRate > 0) {
            ((currentRate - previousRate) / previousRate * 100).toInt()
        } else if (currentRate > 0) {
            100
        } else {
            0
        }

        return TrendData(currentRate, previousRate, change)
    }

    private fun calculateRangeRate(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, start: LocalDate, end: LocalDate, today: LocalDate): Float {
        val habitStart = timeService.toLocalDate(habit.createdAt)
        var date = if (start.isBefore(habitStart)) habitStart else start
        var expected = 0
        var completed = 0

        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            var weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            while (!weekStart.isAfter(end)) {
                var completionsInWeek = 0
                var daysInTargetRange = 0
                
                for (i in 0..6) {
                    val d = weekStart.plusDays(i.toLong())
                    if (d.isBefore(habitStart) || d.isBefore(start)) continue
                    if (d.isAfter(today) || d.isAfter(end)) break
                    
                    daysInTargetRange++
                    if ((dailyTotals[d] ?: 0) >= target) {
                        completionsInWeek++
                    }
                }
                
                val weekTarget = minOf(habit.numberOfTrackedDays, daysInTargetRange)
                expected += weekTarget
                completed += minOf(completionsInWeek, weekTarget)
                
                weekStart = weekStart.plusWeeks(1)
            }
        } else {
            while (!date.isAfter(end) && !date.isAfter(today)) {
                if (isScheduledForDate(habit, date)) {
                    expected++
                    if ((dailyTotals[date] ?: 0) >= target) {
                        completed++
                    }
                }
                date = date.plusDays(1)
            }
        }

        return if (expected > 0) completed.toFloat() / expected else 0f
    }

    private fun calculateLongestGap(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, start: LocalDate, end: LocalDate): GapData {
        var longestGap = 0
        var currentGap = 0
        var gapStart: LocalDate? = null
        var gapEnd: LocalDate? = null
        var currentGapStart: LocalDate? = null
        
        var date = start
        while (!date.isAfter(end)) {
            if (isScheduledForDate(habit, date)) {
                if ((dailyTotals[date] ?: 0) < target) {
                    if (currentGap == 0) currentGapStart = date
                    currentGap++
                    if (currentGap > longestGap) {
                        longestGap = currentGap
                        gapStart = currentGapStart
                        gapEnd = date
                    }
                } else {
                    currentGap = 0
                }
            }
            date = date.plusDays(1)
        }
        
        return GapData(longestGap, gapStart, gapEnd)
    }

    private fun calculateBestWeek(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, start: LocalDate, end: LocalDate, today: LocalDate): BestWeekData {
        var bestRate = -1f
        var bestStart = start
        
        // Use calendar weeks for "Best Week"
        var weekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        while (!weekStart.isAfter(end)) {
            val weekEnd = weekStart.plusDays(6)
            val rate = calculateRangeRate(habit, dailyTotals, target, weekStart, weekEnd, today)
            
            if (rate > bestRate) {
                bestRate = rate
                bestStart = weekStart
            }
            weekStart = weekStart.plusWeeks(1)
        }
        
        return BestWeekData(max(0f, bestRate), bestStart, bestStart.plusDays(6))
    }
}
