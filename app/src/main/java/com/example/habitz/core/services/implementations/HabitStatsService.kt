package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.Reward
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.DailyCompletion
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHabitStatsService
import com.example.habitz.core.services.interfaces.MonthlyRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitStatsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val activityService: IHabitActivityService
) : IHabitStatsService {

    override fun getHabitStats(habitId: UUID): Flow<HabitStats> {
        return activityService.getAllActivities().map { allActivities ->
            val habit = habitRepository.getHabitById(habitId) ?: return@map HabitStats(0, 0, 0f, emptyList(), emptyList(), emptyList())
            val habitActivities = allActivities.filter { it.habitId == habitId }.sortedByDescending { it.createdAt }

            val currentStreak = calculateCurrentStreak(habitActivities, habit.completionTargetPerDay)
            val bestStreak = calculateBestStreak(habitActivities, habit.completionTargetPerDay)
            val overallRate = calculateOverallRate(habitActivities, habit.completionTargetPerDay, habit.createdAt)
            
            val monthlyData = calculateMonthlyCompletion(habitActivities, habit.completionTargetPerDay)
            val quarterlyData = calculateQuarterlyRates(habitActivities, habit.completionTargetPerDay)
            val rewards = generateRewards(currentStreak, bestStreak)

            HabitStats(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                overallCompletionRate = overallRate,
                monthlyCompletionData = monthlyData,
                quarterlyCompletionRates = quarterlyData,
                rewards = rewards
            )
        }
    }

    private fun calculateCurrentStreak(activities: List<HabitActivity>, target: Int): Int {
        val activitiesByDay = activities.groupBy { truncateDate(it.createdAt) }
        var streak = 0
        val cal = Calendar.getInstance()
        
        // Start from today or yesterday
        val today = truncateDate(Date())
        val yesterday = Calendar.getInstance().apply { time = today; add(Calendar.DATE, -1) }.time
        
        var current = if (activitiesByDay[today]?.sumOf { it.quantity } ?: 0 >= target) today else yesterday
        
        while (true) {
            val dailySum = activitiesByDay[current]?.sumOf { it.quantity } ?: 0
            if (dailySum >= target) {
                streak++
                val nextCal = Calendar.getInstance().apply { time = current; add(Calendar.DATE, -1) }
                current = nextCal.time
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateBestStreak(activities: List<HabitActivity>, target: Int): Int {
        val activitiesByDay = activities.groupBy { truncateDate(it.createdAt) }.toSortedMap()
        if (activitiesByDay.isEmpty()) return 0
        
        var maxStreak = 0
        var currentStreak = 0
        
        val firstDate = activitiesByDay.firstKey()
        val lastDate = truncateDate(Date())
        
        val cal = Calendar.getInstance()
        cal.time = firstDate
        
        while (!cal.time.after(lastDate)) {
            val dailySum = activitiesByDay[cal.time]?.sumOf { it.quantity } ?: 0
            if (dailySum >= target) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 0
            }
            cal.add(Calendar.DATE, 1)
        }
        
        return maxStreak
    }

    private fun calculateOverallRate(activities: List<HabitActivity>, target: Int, createdAt: Date): Float {
        val daysSinceCreation = ((Date().time - createdAt.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        val successfulDays = activities.groupBy { truncateDate(it.createdAt) }
            .count { it.value.sumOf { activity -> activity.quantity } >= target }
        
        return if (daysSinceCreation > 0) successfulDays.toFloat() / daysSinceCreation else 0f
    }

    private fun calculateMonthlyCompletion(activities: List<HabitActivity>, target: Int): List<DailyCompletion> {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val activitiesByDay = activities.groupBy { 
            val c = Calendar.getInstance().apply { time = it.createdAt }
            if (c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear) {
                c.get(Calendar.DAY_OF_MONTH)
            } else -1
        }

        return (1..daysInMonth).map { day ->
            val sum = activitiesByDay[day]?.sumOf { it.quantity } ?: 0
            val ratio = (sum.toFloat() / target).coerceAtMost(1f)
            DailyCompletion(day, ratio, sum < target)
        }
    }

    private fun calculateQuarterlyRates(activities: List<HabitActivity>, target: Int): List<MonthlyRate> {
        val result = mutableListOf<MonthlyRate>()
        val cal = Calendar.getInstance()
        
        repeat(3) {
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault()) ?: ""
            
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthActivities = activities.filter {
                val c = Calendar.getInstance().apply { time = it.createdAt }
                c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
            }
            
            val successfulDays = monthActivities.groupBy { truncateDate(it.createdAt) }
                .count { it.value.sumOf { a -> a.quantity } >= target }
            
            result.add(MonthlyRate(monthName, if (daysInMonth > 0) successfulDays.toFloat() / daysInMonth else 0f))
            cal.add(Calendar.MONTH, -1)
        }
        
        return result.reversed()
    }

    private fun generateRewards(currentStreak: Int, bestStreak: Int): List<Reward> {
        val milestones = listOf(3, 7, 15, 30, 50, 100)
        val titles = listOf("Starter", "Consistent", "Dedicated", "Master", "Legend", "Immortal")
        val emojis = listOf("🥉", "🥈", "🥇", "💎", "👑", "🔥")
        
        return milestones.mapIndexed { index, milestone ->
            Reward(
                title = titles[index],
                description = "$milestone day streak",
                emoji = emojis[index],
                requiredStreak = milestone,
                isUnlocked = bestStreak >= milestone
            )
        }
    }

    private fun truncateDate(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}
