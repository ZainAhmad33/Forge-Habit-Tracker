package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.CategoryToImage
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.interfaces.ICategoryRepository
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.database.interfaces.IUserRepository
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHomeService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.core.uiEntities.HomeHabit
import com.example.forge.core.uiEntities.HomeSummary
import com.example.forge.feature.home.viewmodel.HomeDashboardUIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class HomeService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val categoryRepository: ICategoryRepository,
    private val userRepository: IUserRepository,
    private val activityService: IHabitActivityService,
    private val timeService: ITimeService,
    private val habitStatsService: HabitStatsService
) : IHomeService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDashboardData(): Flow<HomeDashboardUIState> {
        return combine(
            habitRepository.getHabits(),
            activityService.getAllDailyQuantities(),
            userRepository.getUserDetails(),
            categoryRepository.getCategories(),
            timeService.getCurrentDateFlow()
        ) { habits, dailyQuantities, user, categories, todayDate ->
            val zoneId = ZoneId.systemDefault()

            // Pre-group aggregated quantities by (HabitId -> (LocalDate -> Sum Quantity))
            val activityMap: Map<UUID, Map<LocalDate, Int>> = dailyQuantities
                .groupBy { it.habitId }
                .mapValues { (_, quantities) ->
                    quantities.associate { it.day to it.totalQuantity }
                }

            val homeHabits = habits.map { habit ->
                val habitLogsMap = activityMap[habit.id] ?: emptyMap()
                val todayQuantity = habitLogsMap[todayDate] ?: 0

                val progress = if (habit.completionTargetPerDay > 0) {
                    (todayQuantity.toFloat() / habit.completionTargetPerDay * 100).toInt().coerceAtMost(100)
                } else 0

                val isScheduledForToday = isScheduledForDate(habit, todayDate)
                val streak = habitStatsService.calculateCurrentStreak(habit, habitLogsMap, habit.completionTargetPerDay, todayDate)

                convertToHomeHabit(
                    habit = habit,
                    progress = progress,
                    isCompletedToday = todayQuantity >= habit.completionTargetPerDay,
                    isScheduledForToday = isScheduledForToday,
                    streak = streak,
                    todayQuantity
                )
            }

            val totalStreak = calculatePerfectDayStreak(habits, activityMap, todayDate, zoneId)
            val habitSummary = createSummary(homeHabits, totalStreak)
            val categoryPills = categories.map {
                CategoryPill(it, CategoryToImage[it] ?: "❓")
            }

            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

            HomeDashboardUIState(
                getDynamicGreeting(),
                user?.firstName ?: "User",
                todayDate.format(formatter),
                habitSummary,
                categoryPills,
                homeHabits
            )
        }
    }

    override fun searchHabits(query: String): List<HomeHabit> = emptyList()

    private fun getDynamicGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    private fun createSummary(homeHabits: List<HomeHabit>, totalStreak: Int): HomeSummary {
        val scheduled = homeHabits.filter { it.isScheduledForToday }
        val habitsCompleted = scheduled.count { it.isCompletedToday }
        val totalHabits = scheduled.size
        val overallProgress = if (totalHabits > 0) (habitsCompleted.toFloat() / totalHabits * 100).toInt() else 0

        return HomeSummary(habitsCompleted, totalHabits, totalStreak, overallProgress)
    }

    private fun calculatePerfectDayStreak(
        habits: List<Habit>,
        activityMap: Map<UUID, Map<LocalDate, Int>>,
        today: LocalDate,
        zoneId: ZoneId
    ): Int {
        var streak = 0
        var date = today

        val limitDate = habits.minOfOrNull {
            timeService.toLocalDate(it.createdAt)
        } ?: today

        // Check today first
        val todayResult = checkPerfectDay(date, habits, activityMap)
        if (todayResult == true) streak++

        // Step back through days
        date = date.minusDays(1)
        while (!date.isBefore(limitDate)) {
            when (checkPerfectDay(date, habits, activityMap)) {
                true -> streak++
                false -> return streak
                null -> {} // Rest day: skip without breaking streak
            }
            date = date.minusDays(1)
        }

        return streak
    }

    private fun checkPerfectDay(
        date: LocalDate,
        habits: List<Habit>,
        activityMap: Map<UUID, Map<LocalDate, Int>>
    ): Boolean? {
        val scheduledHabits = habits.filter { isScheduledForDate(it, date) }
        if (scheduledHabits.isEmpty()) return null

        return scheduledHabits.all { habit ->
            val totalQuantity = activityMap[habit.id]?.get(date) ?: 0
            totalQuantity >= habit.completionTargetPerDay
        }
    }

    private fun isScheduledForDate(habit: Habit, date: LocalDate): Boolean {
        val habitStartDate = timeService.toLocalDate(habit.createdAt)
        if (date.isBefore(habitStartDate)) return false

        return when (habit.frequencyType) {
            HabitFrequency.EveryDay, HabitFrequency.DaysPerWeek -> true
            HabitFrequency.SpecificDays -> habit.trackedDays.contains(date.dayOfWeek.value - 1)
        }
    }

    private fun calculateHabitStreak(
        habit: Habit,
        habitLogsMap: Map<LocalDate, Int>,
        today: LocalDate,
        zoneId: ZoneId
    ): Int {
        var streak = 0
        var date = today
        val limitDate = timeService.toLocalDate(habit.createdAt)

        while (!date.isBefore(limitDate)) {
            val completed = (habitLogsMap[date] ?: 0) >= habit.completionTargetPerDay
            val isScheduled = isScheduledForDate(habit, date)

            if (isScheduled) {
                if (completed) {
                    streak++
                } else if (date != today) {
                    // Today not being finished yet doesn't break yesterday's streak
                    break
                }
            }
            else{
                streak++
            }
            date = date.minusDays(1)
        }

        return streak
    }

    private fun convertToHomeHabit(
        habit: Habit,
        progress: Int,
        isCompletedToday: Boolean,
        isScheduledForToday: Boolean,
        streak: Int,
        quantityLoggedToday: Int
    ): HomeHabit {
        return HomeHabit(
            habit.id.toString(),
            habit.title,
            habit.category,
            "${habit.completionTargetPerDay} ${habit.targetUnit}",
            streak,
            progress,
            isCompletedToday,
            habit.emoji,
            habit.progressShape,
            habit.habitType,
            isScheduledForToday,
            quantityLoggedToday
        )
    }
}