package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.CategoryToImage
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.services.interfaces.IHabitsService
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.feature.upserthabit.state.UpsertHabitUiState
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HabitsService @Inject constructor(
    private val habitRepository: IHabitRepository
) : IHabitsService {
    override fun getAllowedCategories(): List<CategoryPill> {
        return HabitCategory.entries
            .filter { it != HabitCategory.All }
            .map { CategoryPill(it, CategoryToImage[it] ?: "❓") }
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
}
