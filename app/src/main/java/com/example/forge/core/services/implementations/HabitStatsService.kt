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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Date
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
            val startDate = timeService.toLocalDate(habit.createdAt)

            coroutineScope {
                val currentStreakInfoDeferred = async { calculateCurrentStreak(habit, dailyQuantities, target, today) }
                val bestStreakDeferred = async { calculateBestStreak(habit, dailyQuantities, target, today) }
                val overallRateDeferred = async { calculateOverallRate(habit, dailyQuantities, target, startDate, today) }
                val monthlyDataDeferred = async { calculateMonthlyCompletion(dailyQuantities, habit, YearMonth.from(today)) }
                val quarterlyDataDeferred = async { calculateQuarterlyRates(habit, dailyQuantities, today) }
                val trendsDeferred = async { calculateTrends(habit, dailyQuantities, target, today) }

                val currentStreakInfo = currentStreakInfoDeferred.await()
                
                HabitStats(
                    currentStreak = currentStreakInfo.count,
                    bestStreak = bestStreakDeferred.await(),
                    overallCompletionRate = overallRateDeferred.await(),
                    monthlyCompletionData = monthlyDataDeferred.await(),
                    quarterlyCompletionRates = quarterlyDataDeferred.await(),
                    currentStreakStartDate = currentStreakInfo.startDate,
                    trends = trendsDeferred.await()
                )
            }
        }.flowOn(Dispatchers.Default)
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
                    val percentage = if (target > 0) {
                        (quantity.toFloat() / target * 100).toInt()
                    } else 0
                    allData.add(ActivityData(date, percentage))
                }
            }
            allData
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllMonthlyCompletion(habitId: UUID): Flow<Map<YearMonth, List<DailyCompletion>>> {
        return combine(
            activityService.getDailyQuantitiesForHabit(habitId),
            habitRepository.getHabitFlow(habitId),
            timeService.getCurrentDateFlow()
        ) { dailyQuantitiesList, habit, today ->
            if (habit == null) return@combine emptyMap()
            
            val dailyTotals = dailyQuantitiesList.associate { it.day to it.totalQuantity }
            val habitStartMonth = YearMonth.from(timeService.toLocalDate(habit.createdAt))
            val todayMonth = YearMonth.from(today)
            
            val result = mutableMapOf<YearMonth, List<DailyCompletion>>()
            var current = habitStartMonth
            while (!current.isAfter(todayMonth)) {
                result[current] = calculateMonthlyCompletion(dailyTotals, habit, current)
                current = current.plusMonths(1)
            }
            result
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun getStreakInfo(habitId: UUID): StreakInfo {
        val habit = habitRepository.getHabitById(habitId) ?: return StreakInfo(0, null)
        val activityList = activityService.getDailyQuantitiesForHabitSync(habitId)
        val dailyTotals = activityList.associate { it.day to it.totalQuantity }
        val today = timeService.getCurrentDate()
        return calculateCurrentStreak(habit, dailyTotals, habit.completionTargetPerDay, today)
    }

    internal fun calculateCurrentStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): StreakInfo {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            return calculateCurrentStreakForDaysPerWeek(habit, today, dailyTotals, target)
        }

        val firstDate = timeService.toLocalDate(habit.createdAt)
        val lastDate = today
        var streak = 0
        var current = lastDate
        var streakStartDate: LocalDate? = null
        var completionsCount = 0

        // Handle today
        if (isScheduledForDate(habit, current)) {
            if ((dailyTotals[current] ?: 0) >= target) {
                streak++
                streakStartDate = current
                completionsCount++
            } else {
                // Today is scheduled but not done yet. 
                // We don't increment streak for today, but we continue backwards
                // to see if previous days maintain the streak.
            }
        } else {
            // Not scheduled for today (rest day), streak continues
            streak++
            streakStartDate = current
        }

        // Check previous days
        current = current.minusDays(1)
        while (!current.isBefore(firstDate)) {
            if (isScheduledForDate(habit, current)) {
                if ((dailyTotals[current] ?: 0) >= target) {
                    streak++
                    streakStartDate = current
                    completionsCount++
                } else {
                    return if (completionsCount > 0) StreakInfo(streak, streakStartDate) else StreakInfo(0, null)
                }
            } else {
                streak++
                streakStartDate = current
            }
            current = current.minusDays(1)
        }

        return if (completionsCount > 0) StreakInfo(streak, streakStartDate) else StreakInfo(0, null)
    }

    private fun calculateCurrentStreakForDaysPerWeek(
        habit: Habit,
        today: LocalDate,
        dailyTotals: Map<LocalDate, Int>,
        target: Int
    ): StreakInfo {
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        var weekStart = currentWeekStart
        var streakStartDate: LocalDate? = null
        var foundAnyCompletion = false
        var isCurrentWeek = true
        var overallStreakDays = 0

        // Streak count only includes today if it's completed, consistent with EveryDay habits
        val isTodayCompleted = (dailyTotals[today] ?: 0) >= target
        val lastDayOfStreak = if (isTodayCompleted) today else today.minusDays(1)

        while (!weekStart.isBefore(startOfFirstWeek)) {
            val weekEnd = weekStart.plusDays(6)
            
            var completionsInWeek = 0
            for (i in 0..6) {
                val d = weekStart.plusDays(i.toLong())
                if (d.isBefore(habitStart)) continue
                if (d.isAfter(today)) break
                if ((dailyTotals[d] ?: 0) >= target) {
                    completionsInWeek++
                    foundAnyCompletion = true
                }
            }

            val availableDaysInWeek = if (weekStart == startOfFirstWeek) {
                ChronoUnit.DAYS.between(habitStart, weekEnd).toInt() + 1
            } else {
                7
            }
            val targetForThisWeek = minOf(habit.numberOfTrackedDays, availableDaysInWeek)

            val goalMet = completionsInWeek >= targetForThisWeek
            
            if (isCurrentWeek) {
                val daysRemaining = ChronoUnit.DAYS.between(today, weekEnd).toInt()
                val isStillPossible = completionsInWeek + daysRemaining >= targetForThisWeek

                if (goalMet || isStillPossible) {
                    val effectiveStart = if (weekStart.isBefore(habitStart)) habitStart else weekStart
                    if (!lastDayOfStreak.isBefore(effectiveStart)) {
                        overallStreakDays += ChronoUnit.DAYS.between(effectiveStart, lastDayOfStreak).toInt() + 1
                        streakStartDate = effectiveStart
                    }
                } else {
                    // Goal not met and no longer possible
                    return if (foundAnyCompletion) StreakInfo(0, null) else StreakInfo(0, null)
                }
                isCurrentWeek = false
            } else {
                if (goalMet) {
                    val effectiveStart = if (weekStart.isBefore(habitStart)) habitStart else weekStart
                    val daysInWeek = ChronoUnit.DAYS.between(effectiveStart, weekEnd).toInt() + 1
                    overallStreakDays += daysInWeek
                    streakStartDate = effectiveStart
                } else {
                    break
                }
            }
            weekStart = weekStart.minusWeeks(1)
        }

        return if (foundAnyCompletion && overallStreakDays > 0) {
            StreakInfo(overallStreakDays, streakStartDate)
        } else {
            StreakInfo(0, null)
        }
    }

    private fun calculateBestStreakForDaysPerWeek(
        habit: Habit,
        today: LocalDate,
        dailyTotals: Map<LocalDate, Int>,
        target: Int
    ): Int {
        var currentStreakCount = 0
        var maxStreakCount = 0
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val isTodayCompleted = (dailyTotals[today] ?: 0) >= target
        val lastDayOfTodayWeek = if (isTodayCompleted) today else today.minusDays(1)

        var weekStart = startOfFirstWeek
        while (!weekStart.isAfter(currentWeekStart)) {
            val weekEnd = weekStart.plusDays(6)
            
            var completionsInWeek = 0
            for (i in 0..6) {
                val d = weekStart.plusDays(i.toLong())
                if (d.isBefore(habitStart)) continue
                if (d.isAfter(today)) break
                if ((dailyTotals[d] ?: 0) >= target) {
                    completionsInWeek++
                }
            }

            val availableDaysInWeek = if (weekStart == startOfFirstWeek) {
                ChronoUnit.DAYS.between(habitStart, weekEnd).toInt() + 1
            } else {
                7
            }
            val targetForThisWeek = minOf(habit.numberOfTrackedDays, availableDaysInWeek)

            val goalMet = completionsInWeek >= targetForThisWeek
            
            if (weekStart == currentWeekStart) {
                val daysRemaining = ChronoUnit.DAYS.between(today, weekEnd).toInt()
                val isStillPossible = completionsInWeek + daysRemaining >= targetForThisWeek

                if (goalMet || isStillPossible) {
                    val effectiveStart = if (weekStart.isBefore(habitStart)) habitStart else weekStart
                    if (!lastDayOfTodayWeek.isBefore(effectiveStart)) {
                        currentStreakCount += ChronoUnit.DAYS.between(effectiveStart, lastDayOfTodayWeek).toInt() + 1
                    }
                    maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
                } else {
                    maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
                    currentStreakCount = 0
                }
            } else {
                if (goalMet) {
                    val effectiveStart = if (weekStart.isBefore(habitStart)) habitStart else weekStart
                    val daysInWeek = ChronoUnit.DAYS.between(effectiveStart, weekEnd).toInt() + 1
                    currentStreakCount += daysInWeek
                    maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
                } else {
                    maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
                    currentStreakCount = 0
                }
            }
            weekStart = weekStart.plusWeeks(1)
        }
        return maxStreakCount
    }


    internal fun calculateBestStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): Int {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            return calculateBestStreakForDaysPerWeek(habit, today, dailyTotals, target)
        }

        if (dailyTotals.isEmpty()) return 0
        val firstDate = timeService.toLocalDate(habit.createdAt)
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

                if (weekStart == currentWeekStart && today == currentWeekEnd){
                    completionsInWeek++
                }

                expectedDays += minOf(daysPassedInWeek, targetForWeek)
                successfulDays += minOf(completionsInWeek, targetForWeek)

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

    private fun calculateMonthlyCompletion(dailyTotals: Map<LocalDate, Int>, habit: Habit, yearMonth: YearMonth): List<DailyCompletion> {
        val daysInMonth = yearMonth.lengthOfMonth()
        val target = habit.completionTargetPerDay

        val successfulDates = dailyTotals.filter { it.value >= target }.keys

        return (1..daysInMonth).map { day ->
            val date = LocalDate.of(yearMonth.year, yearMonth.month, day)
            val sum = dailyTotals[date] ?: 0
            val isCompleted = sum >= target

            var isSkipDay = false
            if (date.isBefore(timeService.toLocalDate(habit.createdAt))){
                isSkipDay = true
            }
            else {
                isSkipDay = when (habit.frequencyType) {
                    HabitFrequency.EveryDay -> false
                    HabitFrequency.SpecificDays -> {
                        // Convert DayOfWeek (1=Mon..7=Sun) to app format (0=Mon..6=Sun)
                        val dayOfWeekIdx = date.dayOfWeek.value - 1
                        !habit.trackedDays.contains(dayOfWeekIdx)
                    }

                    HabitFrequency.DaysPerWeek -> {
                        val startOfWeek =
                            date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
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
            }

            DailyCompletion(day, sum, isSkipDay)
        }
    }

    internal fun calculateQuarterlyRates(habit: Habit, dailyTotals: Map<LocalDate, Int>, today: LocalDate): List<MonthlyRate> {
        val result = mutableListOf<MonthlyRate>()
        val habitStart = timeService.toLocalDate(habit.createdAt)
        val target = habit.completionTargetPerDay
        
        // Go back 3 months from today (excluding current month as it's shown in main chart)
        var monthDate = today.withDayOfMonth(1).minusMonths(1)

        repeat(3) {
            val monthStart = monthDate.withDayOfMonth(1)
            val monthEnd = monthDate.with(TemporalAdjusters.lastDayOfMonth())
            
            // Only add the month if the habit existed during some part of it
            if (!monthEnd.isBefore(habitStart)) {
                val monthName = monthDate.month.name.take(3).lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                val rate = calculateRangeRate(habit, dailyTotals, target, monthStart, monthEnd, today)
                result.add(MonthlyRate(monthName, rate))
            }
            monthDate = monthDate.minusMonths(1)
        }

        return result.reversed()
    }

    internal suspend fun calculateTrends(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): HabitTrends = coroutineScope {
        val habitStart = timeService.toLocalDate(habit.createdAt)
        
        val weeklyTrendDeferred = async {
            calculateTrend(habit, dailyTotals, target,
                currentStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                currentEnd = today,
                previousStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1),
                previousEnd = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1),
                today = today
            )
        }

        val monthlyTrendDeferred = async {
            calculateTrend(habit, dailyTotals, target,
                currentStart = today.withDayOfMonth(1),
                currentEnd = today,
                previousStart = today.withDayOfMonth(1).minusMonths(1),
                previousEnd = today.withDayOfMonth(1).minusDays(1),
                today = today
            )
        }

        val longestGapDeferred = async { calculateLongestGap(habit, dailyTotals, target, habitStart, today) }
        val allTimeAverageDeferred = async { calculateOverallRate(habit, dailyTotals, target, habitStart, today) }
        val bestWeekDeferred = async { calculateBestWeek(habit, dailyTotals, target, habitStart, today, today) }

        HabitTrends(
            weeklyTrend = weeklyTrendDeferred.await(),
            monthlyTrend = monthlyTrendDeferred.await(),
            longestGap = longestGapDeferred.await(),
            allTimeAverage = allTimeAverageDeferred.await(),
            bestWeek = bestWeekDeferred.await()
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
