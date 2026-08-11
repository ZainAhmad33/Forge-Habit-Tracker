package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.CategoryToImage
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.core.uiEntities.ProgressShape
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val habitActivityRepository: IHabitActivityRepository,
    private val timeService: ITimeService
) : IHabitsService {
    override fun getAllowedCategories(): List<CategoryPill> {
        return HabitCategory.entries
            .filter { it != HabitCategory.All }
            .map { CategoryPill(it, CategoryToImage[it] ?: "❓") }
    }

    override suspend fun getHabitById(habitId: UUID): Habit?{
        return habitRepository.getHabitById(habitId)
    }

    override fun getHabitFlow(habitId: UUID): Flow<Habit?> {
        return habitRepository.getHabitFlow(habitId)
    }

    override suspend fun createHabit(habitForm: UpsertHabitUiState) {
        val reminders = if (habitForm.remindersEnabled) habitForm.reminders else listOf()
        var numOfTrackedDays: Int
        if (habitForm.selectedFrequency == HabitFrequency.EveryDay)
            numOfTrackedDays = 7
        else if (habitForm.selectedFrequency == HabitFrequency.DaysPerWeek)
            numOfTrackedDays = habitForm.daysPerWeek
        else
            numOfTrackedDays = -1
        val specificDays = if (habitForm.selectedFrequency == HabitFrequency.SpecificDays) habitForm.specificDays else listOf()

        var unit: String
        if (habitForm.selectedType == HabitType.Quantity){
            if (habitForm.selectedUnit == "Other"){
                unit = habitForm.otherUnitInput
            }
            else{
                unit = habitForm.selectedUnit
            }
        }
        else{
            unit = "times per day"
        }

        val habit = Habit(
            UUID.randomUUID(),
            habitForm.title,
            habitForm.selectedCategory,
            habitForm.selectedEmoji,
            habitForm.selectedType,
            reminders,
            habitForm.selectedFrequency,
            habitForm.selectedFrequency == HabitFrequency.EveryDay,
            specificDays,
            numOfTrackedDays,
            habitForm.dailyGoal,
            unit,
            ProgressShape.getRandom(),
            0,
            Date(),
            Date(),
        )

        habitRepository.createHabit(habit)
    }

    suspend fun isHabitCompletedToday(habitId: UUID, target: Int): Boolean{
        val todayQuantity = getTodaysCompletion(habitId)

        return todayQuantity >= target
    }

    suspend fun getTodaysCompletion(habitId: UUID): Int{
        val today = timeService.getCurrentDate()

        val startOfDay = timeService.toStartOfDayDate(today)
        val endOfDay = timeService.toEndOfDayDate(today)

        val result = habitActivityRepository.getCompletedQuantityByRange(habitId, startOfDay, endOfDay)

        val todayQuantity: Int = result[today] ?: 0

        return todayQuantity
    }
}
