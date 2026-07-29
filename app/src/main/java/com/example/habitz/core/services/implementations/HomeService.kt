package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.CategoryToImage
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHomeService
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.core.uiEntities.HomeSummary
import com.example.habitz.feature.home.viewmodel.HomeDashboardUIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HomeService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val categoryRepository: ICategoryRepository,
    private val userRepository: IUserRepository,
    private val activityService: IHabitActivityService
) : IHomeService {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDashboardData(): Flow<HomeDashboardUIState> {
        return habitRepository.getHabits().flatMapLatest { habits ->
            val habitIds = habits.map { it.id }
            activityService.getActivitiesForToday(habitIds).map { logs ->
                val user = userRepository.getUserDetails()
                
                val homeHabits = habits.map { habit ->
                    val habitLogs = logs.filter { it.habitId == habit.id }
                    val totalQuantity = habitLogs.sumOf { it.quantity }
                    val progress = if (habit.completionTargetPerDay > 0) {
                        (totalQuantity.toFloat() / habit.completionTargetPerDay * 100).toInt().coerceAtMost(100)
                    } else 0
                    
                    convertToHomeHabit(habit, progress, totalQuantity >= habit.completionTargetPerDay)
                }

                val habitSummary = createSummary(homeHabits)
                val categories = categoryRepository.getCategories().distinct().map {
                    CategoryPill(it, CategoryToImage[it] ?: "❓")
                }

                val currentDate = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

                HomeDashboardUIState(
                    getDynamicGreeting(),
                    user.firstName,
                    currentDate.format(formatter),
                    habitSummary,
                    categories,
                    homeHabits
                )
            }
        }
    }

    override fun searchHabits(query: String): List<HomeHabit> {
        return emptyList()
    }

    private fun getDynamicGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    private fun createSummary(homeHabits: List<HomeHabit>): HomeSummary {
        val habitsCompleted = homeHabits.count { it.isCompletedToday }
        val totalHabits = homeHabits.size
        val totalStreak = 0
        val overallProgress = habitsCompleted.toFloat() / totalHabits * 100

        return HomeSummary(
            habitsCompleted,
            totalHabits,
            totalStreak,
            overallProgress.toInt()
        )
    }

    private fun convertToHomeHabit(habit: Habit, progress: Int, isCompletedToday: Boolean): HomeHabit {
        return HomeHabit(
            habit.id.toString(),
            habit.title,
            habit.category,
            "${habit.completionTargetPerDay} ${habit.targetUnit}",
            habit.dailyStreakCount,
            progress,
            isCompletedToday,
            habit.emoji,
            habit.progressShape,
            habit.habitType
        )
    }
}
