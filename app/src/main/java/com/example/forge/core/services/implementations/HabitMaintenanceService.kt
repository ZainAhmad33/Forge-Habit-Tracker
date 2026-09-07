package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitMaintenanceService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.ITimeService
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitMaintenanceService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val activityService: IHabitActivityService,
    private val statsService: IHabitStatsService,
    private val timeService: ITimeService
) : IHabitMaintenanceService {

    override suspend fun performMaintenanceForAll() {
        val habits = habitRepository.getAllHabitsSync()
        for (habit in habits) {
            performMaintenance(habit.id)
        }
    }

    override suspend fun performMaintenance(habitId: UUID) {
        val habit = habitRepository.getHabitById(habitId) ?: return
        val today = timeService.getCurrentDate()
        
        // 1. Handle Unlock Logic if Locked
        if (habit.isLocked) {
            val lockedAtLocalDate = habit.lockedAt?.let { timeService.toLocalDate(it) }
            if (lockedAtLocalDate != null) {
                val daysServed = ChronoUnit.DAYS.between(lockedAtLocalDate, today).toInt()
                if (daysServed >= 30) {
                    habit.isLocked = false
                    habit.lockedAt = null
                    // Reset last maintenance to today so we don't penalize for days served
                    habit.lastMaintenanceDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
            }
        }

        val lastMaintenance = habit.lastMaintenanceDate ?: habit.createdAt
        val lastMaintenanceLocalDate = timeService.toLocalDate(lastMaintenance)

        // 2. Handle Skip Days Logic if Not Locked
        if (!habit.isLocked) {
            // Start from the day of creation if never maintained, otherwise the day after last maintenance
            var currentDate = if (habit.lastMaintenanceDate == null) lastMaintenanceLocalDate else lastMaintenanceLocalDate.plusDays(1)
            
            val from = timeService.toStartOfDayDate(currentDate)
            val to = timeService.toEndOfDayDate(today)
            var currentCompletedQuantityMap = activityService.getCompletedQuantityByRange(habit.id, from, to)

            while (!currentDate.isAfter(today)) {
                // If it was already locked during this loop, stop
                if (habit.isLocked) break

                // For EveryDay and SpecificDays, we only lock after the day has passed.
                // For DaysPerWeek, we can lock as soon as the goal becomes unachievable.
                if (currentDate == today && habit.frequencyType != com.example.forge.core.database.entity.HabitFrequency.DaysPerWeek) {
                    break
                }

                val isMissed = isDayMissedInternal(habit, currentDate, currentCompletedQuantityMap)
                if (isMissed) {
                    if (habit.skipDaysAllowed > 0) {
                        // Consume skip day
                        habit.skipDaysAllowed--
                        activityService.logSkipActivity(
                            habitId = habit.id,
                            quantity = habit.completionTargetPerDay,
                            date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        )
                        // Update the map locally so subsequent checks (e.g. for DaysPerWeek) see the skip
                        currentCompletedQuantityMap = currentCompletedQuantityMap.toMutableMap().apply {
                            this[currentDate] = habit.completionTargetPerDay
                        }
                    } else {
                        // Lock habit
                        habit.isLocked = true
                        habit.lockedAt = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    }
                }
                currentDate = currentDate.plusDays(1)
            }
        }

        // 3. Reward Milestones (even if locked, maybe? requirement said "Once user gains a skip day (maintains a 30 day streak) that skip day will be used to unlock")
        // But if logging is disabled, streak won't increase. 
        // However, if skip days were used, the streak continues.
        val streakInfo = statsService.getStreakInfo(habit.id)
        val currentStreak = streakInfo.count
        
        val nextMilestone = ((habit.lastMilestoneRewarded / 30) + 1) * 30
        if (currentStreak >= nextMilestone) {
            val rewardsToGive = (currentStreak / 30) - (habit.lastMilestoneRewarded / 30)
            if (rewardsToGive > 0) {
                habit.skipDaysAllowed += rewardsToGive
                habit.lastMilestoneRewarded = (currentStreak / 30) * 30
                
                // If this reward happens while locked, use it to unlock immediately
                if (habit.isLocked) {
                    habit.isLocked = false
                    habit.lockedAt = null
                    habit.skipDaysAllowed-- // Use the new skip day to unlock
                }
            }
        }

        habit.lastMaintenanceDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
        habit.updatedAt = Date()
        habit.skipDaysAllowed = habit.skipDaysAllowed.coerceAtLeast(0)
        
        habitRepository.createHabit(habit) // Room @Insert(onConflict = REPLACE)
    }

    private fun isDayMissedInternal(habit: Habit, date: LocalDate, completedQuantityMap: Map<LocalDate, Int>): Boolean {
        if (habit.frequencyType == com.example.forge.core.database.entity.HabitFrequency.DaysPerWeek) {
            val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = weekStart.plusDays(6)
            
            var completionsSoFar = 0
            var curr = weekStart
            while (!curr.isAfter(date)) {
                if ((completedQuantityMap[curr] ?: 0) >= habit.completionTargetPerDay) {
                    completionsSoFar++
                }
                curr = curr.plusDays(1)
            }
            
            val daysRemaining = ChronoUnit.DAYS.between(date, weekEnd).toInt()
            
            return (completionsSoFar + daysRemaining) < habit.numberOfTrackedDays
        }

        // Only check if it's a scheduled day and after creation
        if (!isScheduledForDate(habit, date)) return false
        
        val quantity = completedQuantityMap[date] ?: 0
        return quantity < habit.completionTargetPerDay
    }

    private fun isScheduledForDate(habit: Habit, date: LocalDate): Boolean {
        val habitStartDate = timeService.toLocalDate(habit.createdAt)
        if (date.isBefore(habitStartDate)) return false

        return when (habit.frequencyType) {
            com.example.forge.core.database.entity.HabitFrequency.EveryDay -> true
            com.example.forge.core.database.entity.HabitFrequency.DaysPerWeek -> true
            com.example.forge.core.database.entity.HabitFrequency.SpecificDays -> {
                val dayOfWeekIdx = date.dayOfWeek.value - 1
                habit.trackedDays.contains(dayOfWeekIdx)
            }
        }
    }
}
