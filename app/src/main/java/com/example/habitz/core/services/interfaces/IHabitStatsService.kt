package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.Reward
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val overallCompletionRate: Float,
    val monthlyCompletionData: List<DailyCompletion>,
    val quarterlyCompletionRates: List<MonthlyRate>,
    val rewards: List<Reward>
)

data class DailyCompletion(
    val day: Int,
    val completionRatio: Float, // 0.0 to 1.0
    val isBelowGoal: Boolean
)

data class MonthlyRate(
    val monthName: String,
    val rate: Float
)

interface IHabitStatsService {
    fun getHabitStats(habitId: UUID): Flow<HabitStats>
}
