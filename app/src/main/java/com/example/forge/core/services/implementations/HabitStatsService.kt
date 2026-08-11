package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.HabitStats
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.services.interfaces.MonthlyRate
import com.example.forge.core.uiEntities.ActivityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.UUID
import javax.inject.Inject

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

            val currentStreak = calculateCurrentStreak(habit, dailyQuantities, target, today)
            val bestStreak = calculateBestStreak(habit, dailyQuantities, target, today)
            val overallRate = calculateOverallRate(habit, dailyQuantities, target, timeService.toLocalDate(habit.createdAt), today)

            val monthlyData = calculateMonthlyCompletion(dailyQuantities, habit, today)
            val quarterlyData = calculateQuarterlyRates(habit, dailyQuantities, today)

            HabitStats(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                overallCompletionRate = overallRate,
                monthlyCompletionData = monthlyData,
                quarterlyCompletionRates = quarterlyData
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

    private fun calculateCurrentStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): Int {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            var streak = 0
            val habitStart = timeService.toLocalDate(habit.createdAt)
            val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            var weekStart = currentWeekStart
            var isCurrentWeek = true

            while (!weekStart.isBefore(startOfFirstWeek)) {
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

                val goalMet = completionsInWeek >= habit.numberOfTrackedDays

                if (isCurrentWeek) {
                    if (goalMet) {
                        streak++
                    } else {
                        // Check if still possible
                        val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, weekEnd).toInt()
                        if (completionsInWeek + daysRemaining < habit.numberOfTrackedDays) {
                            return 0
                        }
                        // Still possible, continue checking previous weeks without incrementing streak
                    }
                    isCurrentWeek = false
                } else {
                    if (goalMet) {
                        streak++
                    } else {
                        break
                    }
                }
                weekStart = weekStart.minusWeeks(1)
            }
            return streak
        }

        val firstDate = dailyTotals.keys.minOrNull() ?: return 0
        val lastDate = today
        var streak = 0
        var current = lastDate

        if (isScheduledForDate(habit, current)) {
            if ((dailyTotals[current] ?: 0) >= target) {
                streak++
            }
        } else {
            streak++
        }
        current = current.minusDays(1)

        while (!current.isBefore(firstDate)) {
            if (isScheduledForDate(habit, current)) {
                if ((dailyTotals[current] ?: 0) >= target) {
                    streak++
                } else {
                    return streak
                }
            } else {
                streak++
            }
            current = current.minusDays(1)
        }
        return streak
    }

    private fun calculateBestStreak(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, today: LocalDate): Int {
        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            val habitStart = timeService.toLocalDate(habit.createdAt)
            val startOfFirstWeek = habitStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            var maxStreak = 0
            var currentStreak = 0
            var weekStart = currentWeekStart

            while (!weekStart.isBefore(startOfFirstWeek)) {
                var completionsInWeek = 0
                for (i in 0..6) {
                    val d = weekStart.plusDays(i.toLong())
                    if (d.isBefore(habitStart)) continue
                    if (d.isAfter(today)) break
                    if ((dailyTotals[d] ?: 0) >= target) {
                        completionsInWeek++
                    }
                }

                if (completionsInWeek >= habit.numberOfTrackedDays) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
                weekStart = weekStart.minusWeeks(1)
            }
            return maxStreak
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

    private fun calculateOverallRate(habit: Habit, dailyTotals: Map<LocalDate, Int>, target: Int, createdDate: LocalDate, today: LocalDate): Float {
        var date = createdDate
        var expectedDays = 0
        var successfulDays = 0

        if (habit.frequencyType == HabitFrequency.DaysPerWeek) {
            // For DaysPerWeek, we calculate it by looking at weeks
            val startOfFirstWeek = createdDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            var currentWeekStart = startOfFirstWeek

            while (!currentWeekStart.isAfter(today)) {
                val currentWeekEnd = currentWeekStart.plusDays(6)
                val targetForWeek = habit.numberOfTrackedDays

                var completionsInWeek = 0
                var daysPassedInWeek = 0

                for (i in 0..6) {
                    val d = currentWeekStart.plusDays(i.toLong())
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

                currentWeekStart = currentWeekStart.plusWeeks(1)
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
}
