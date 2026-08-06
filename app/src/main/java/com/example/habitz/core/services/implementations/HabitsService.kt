package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.CategoryToImage
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.database.interfaces.IHabitActivityRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitsService
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.feature.upserthabit.state.UpsertHabitUiState
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitsService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val habitActivityRepository: IHabitActivityRepository
) : IHabitsService {
    override fun getAllowedCategories(): List<CategoryPill> {
        return HabitCategory.entries
            .filter { it != HabitCategory.All }
            .map { CategoryPill(it, CategoryToImage[it] ?: "❓") }
    }

    override fun getHabitById(habitId: UUID): Habit?{
        return habitRepository.getHabitById(habitId)
    }

    override fun createHabit(habitForm: UpsertHabitUiState) {
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

    fun isHabitCompletedToday(habitId: UUID, target: Int): Boolean{
        val todayQuantity = getTodaysCompletion(habitId)

        return todayQuantity >= target
    }

    fun getTodaysCompletion(habitId: UUID): Int{
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        val startOfDay = Date.from(today.atStartOfDay(zone).toInstant())
        val endOfDay = Date.from(today.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant())

        val result = habitActivityRepository.getCompletedQuantityByRange(habitId, startOfDay, endOfDay)

        val todayQuantity: Int = result[today] ?: 0

        return todayQuantity
    }
}
