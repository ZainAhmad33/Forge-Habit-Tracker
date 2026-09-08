package com.example.forge.feature.upserthabit.state

import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.uiEntities.CategoryPill
import java.time.LocalTime
import java.util.UUID

data class UpsertHabitUiState(
    val habitId: UUID? = null,
    val title: String = "",
    val titleError: Boolean = false,
    val selectedEmoji: String = "💧",
    val selectedCategory: HabitCategory = HabitCategory.Health,
    val categories: List<CategoryPill> = emptyList(),
    val selectedType: HabitType = HabitType.YesNo,
    val dailyGoal: Int = 1,
    val selectedUnit: String = "Liters",
    val selectedFrequency: HabitFrequency = HabitFrequency.EveryDay,
    val specificDays: List<Int> = listOf(), // 0-6 for Mon-Sun
    val specificDaysError: Boolean = false,
    val daysPerWeek: Int = 1,
    val remindersEnabled: Boolean = false,
    val reminders: List<LocalTime> = listOf(),
    val remindersError: Boolean = false,
    val otherUnitInput: String = "",
    val otherUnitError: Boolean = false,
    val isLockingEnabled: Boolean = true,
    val isLocked: Boolean = false,
    val popularEmojis: List<String> = listOf("💧", "🏃", "📖", "🧘", "🙏", "🍎", "😴"),
    val availableUnits: List<String> = listOf("Liters", "Minutes", "Hours", "Pages", "Glasses", "Kilometers", "Miles", "Other")
)