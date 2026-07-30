package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.DailyCompletion
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHabitStatsService
import com.example.habitz.core.services.interfaces.MonthlyRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitStatsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val activityService: IHabitActivityService
) : IHabitStatsService {

    override fun getHabitStats(habitId: UUID): Flow<HabitStats> {
        // Recommend changing activityService to fetch ONLY this habit's activities:
        // activityService.getActivitiesForHabit(habitId)
        return activityService.getAllActivities().map { allActivities ->
            val habit = habitRepository.getHabitById(habitId)
                ?: return@map HabitStats(0, 0, 0f, emptyList(), emptyList())

            val habitActivities = allActivities
                .filter { it.habitId == habitId }
                .sortedByDescending { it.createdAt }

            // Pre-process quantities per date using modern LocalDate
            val dailyQuantities: Map<LocalDate, Int> = habitActivities
                .groupBy { it.createdAt.toLocalDate() }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }

            val target = habit.completionTargetPerDay

            val currentStreak = calculateCurrentStreak(dailyQuantities, target)
            val bestStreak = calculateBestStreak(dailyQuantities, target)
            val overallRate = calculateOverallRate(dailyQuantities, target, habit.createdAt.toLocalDate())

            val monthlyData = calculateMonthlyCompletion(dailyQuantities, habit)
            val quarterlyData = calculateQuarterlyRates(habit, dailyQuantities)

            HabitStats(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                overallCompletionRate = overallRate,
                monthlyCompletionData = monthlyData,
                quarterlyCompletionRates = quarterlyData
            )
        }.flowOn(Dispatchers.IO) // 👈 Critical: Shift calculations off the main thread
    }

    private fun calculateCurrentStreak(dailyTotals: Map<LocalDate, Int>, target: Int): Int {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var current = if ((dailyTotals[today] ?: 0) >= target) today else yesterday
        var streak = 0

        while ((dailyTotals[current] ?: 0) >= target) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    private fun calculateBestStreak(dailyTotals: Map<LocalDate, Int>, target: Int): Int {
        if (dailyTotals.isEmpty()) return 0

        val firstDate = dailyTotals.keys.minOrNull() ?: return 0
        val lastDate = LocalDate.now()

        var maxStreak = 0
        var currentStreak = 0
        var current = firstDate

        while (!current.isAfter(lastDate)) {
            if ((dailyTotals[current] ?: 0) >= target) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 0
            }
            current = current.plusDays(1)
        }

        return maxStreak
    }

    private fun calculateOverallRate(dailyTotals: Map<LocalDate, Int>, target: Int, createdDate: LocalDate): Float {
        val today = LocalDate.now()
        val totalDays = (today.toEpochDay() - createdDate.toEpochDay() + 1).toInt()
        if (totalDays <= 0) return 0f

        val successfulDays = dailyTotals.count { it.value >= target }
        return successfulDays.toFloat() / totalDays
    }

    private fun calculateMonthlyCompletion(dailyTotals: Map<LocalDate, Int>, habit: Habit): List<DailyCompletion> {
        val today = LocalDate.now()
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

    private fun calculateQuarterlyRates(habit: Habit, dailyTotals: Map<LocalDate, Int>): List<MonthlyRate> {
        val result = mutableListOf<MonthlyRate>()
        val habitStart = habit.createdAt.toLocalDate()
        val target = habit.completionTargetPerDay
        var currentMonthDate = LocalDate.now().withDayOfMonth(1)

        repeat(3) {
            if (currentMonthDate.isAfter(habitStart.withDayOfMonth(1)) || currentMonthDate == habitStart.withDayOfMonth(1)) {
                val daysInMonth = currentMonthDate.lengthOfMonth()
                val monthName = currentMonthDate.month.name.take(3)

                val successfulDays = (1..daysInMonth).count { day ->
                    val date = currentMonthDate.withDayOfMonth(day)
                    (dailyTotals[date] ?: 0) >= target
                }

                result.add(MonthlyRate(monthName, if (daysInMonth > 0) successfulDays.toFloat() / daysInMonth else 0f))
            }
            currentMonthDate = currentMonthDate.minusMonths(1)
        }

        return result.reversed()
    }

    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}