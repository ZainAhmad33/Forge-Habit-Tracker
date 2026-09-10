package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.CategoryToImage
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.services.interfaces.IReminderManager
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
    private val timeService: ITimeService,
    private val reminderManager: IReminderManager
) : IHabitsService {
    override fun getAllowedCategories(): List<CategoryPill> {
        return HabitCategory.entries
            .filter { it != HabitCategory.All }
            .map { CategoryPill(it, CategoryToImage[it] ?: "❓") }
    }

    override suspend fun getHabitById(habitId: UUID): Habit?{
        return habitRepository.getHabitById(habitId)
    }

    override suspend fun deleteHabit(habitId: UUID) {
        val habit = habitRepository.getHabitById(habitId)
        if (habit != null) {
            reminderManager.cancelReminders(habitId)
            habitActivityRepository.deleteActivitiesForHabit(habitId)
            habitRepository.deleteHabit(habit)
        }
    }

    override fun getHabitFlow(habitId: UUID): Flow<Habit?> {
        return habitRepository.getHabitFlow(habitId)
    }

    override suspend fun upsertHabit(habitForm: UpsertHabitUiState) {
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

        val existingHabit = habitForm.habitId?.let { habitRepository.getHabitById(it) }

        val habit = Habit(
            id = habitForm.habitId ?: UUID.randomUUID(),
            title = habitForm.title,
            category = habitForm.selectedCategory,
            emoji = habitForm.selectedEmoji,
            habitType = habitForm.selectedType,
            reminders = reminders,
            frequencyType = habitForm.selectedFrequency,
            trackedDays = specificDays,
            numberOfTrackedDays = numOfTrackedDays,
            completionTargetPerDay = habitForm.dailyGoal,
            targetUnit = unit,
            progressShape = existingHabit?.progressShape ?: ProgressShape.getRandom(),
            skipDaysAllowed = existingHabit?.skipDaysAllowed ?: 0,
            lastMilestoneRewarded = existingHabit?.lastMilestoneRewarded ?: 0,
            isLocked = existingHabit?.isLocked ?: false,
            isLockingEnabled = habitForm.isLockingEnabled,
            lockedAt = existingHabit?.lockedAt,
            lastMaintenanceDate = existingHabit?.lastMaintenanceDate,
            createdAt = existingHabit?.createdAt ?: Date(),
            updatedAt = Date(),
        )

        habitRepository.createHabit(habit)
        
        if (habit.reminders.isNotEmpty()) {
            reminderManager.updateReminders(habit)
        } else {
            reminderManager.cancelReminders(habit.id)
        }
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
