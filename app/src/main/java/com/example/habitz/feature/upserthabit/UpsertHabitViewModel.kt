package com.example.habitz.feature.upserthabit

import androidx.lifecycle.ViewModel
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.services.interfaces.IHabitsService
import com.example.habitz.core.uiEntities.CategoryPill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class UpsertHabitViewModel @Inject constructor(
    private val habitsService: IHabitsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UpsertHabitUiState(
            categories = habitsService.getAllowedCategories()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onEmojiChange(newEmoji: String) {
        _uiState.update { it.copy(selectedEmoji = newEmoji) }
    }

    fun onCategoryChange(newCategory: HabitCategory) {
        _uiState.update { it.copy(selectedCategory = newCategory) }
    }

    fun onTypeChange(newType: HabitType) {
        _uiState.update { it.copy(selectedType = newType) }
    }

    fun onGoalChange(newGoal: Int) {
        _uiState.update { it.copy(dailyGoal = newGoal) }
    }

    fun onUnitChange(newUnit: String) {
        _uiState.update { it.copy(selectedUnit = newUnit) }
    }

    fun onFrequencyChange(newFrequency: HabitFrequency) {
        _uiState.update { it.copy(selectedFrequency = newFrequency) }
    }

    fun toggleDay(dayIndex: Int) {
        _uiState.update { state ->
            val newDays = state.specificDays.toMutableSet()
            if (newDays.contains(dayIndex)) {
                newDays.remove(dayIndex)
            } else {
                newDays.add(dayIndex)
            }
            state.copy(specificDays = newDays)
        }
    }

    fun onDaysPerWeekChange(count: Int) {
        _uiState.update { it.copy(daysPerWeek = count) }
    }

    fun addReminder(time: LocalTime) {
        _uiState.update { state ->
            state.copy(reminders = state.reminders + time)
        }
    }

    fun removeReminder(time: LocalTime) {
        _uiState.update { state ->
            state.copy(reminders = state.reminders - time)
        }
    }

    fun onRemindersEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled) }
    }

    fun onOtherUnitInputChange(unitInput: String){
        _uiState.update { it.copy(otherUnitInput = unitInput) }
    }
}

data class UpsertHabitUiState(
    val title: String = "",
    val selectedEmoji: String = "💧",
    val selectedCategory: HabitCategory = HabitCategory.Health,
    val categories: List<CategoryPill> = emptyList(),
    val selectedType: HabitType = HabitType.YesNo,
    val dailyGoal: Int = 0,
    val selectedUnit: String = "Liters",
    val selectedFrequency: HabitFrequency = HabitFrequency.EveryDay,
    val specificDays: Set<Int> = emptySet(), // 0-6 for Mon-Sun
    val daysPerWeek: Int = 0,
    val remindersEnabled: Boolean = false,
    val reminders: List<LocalTime> = listOf(),
    val otherUnitInput: String = "",
    val popularEmojis: List<String> = listOf("💧", "🏃", "📖", "🧘", "🙏", "🍎", "😴"),
    val availableUnits: List<String> = listOf("Liters", "Minutes", "Hours", "Pages", "Glasses", "Kilometers", "Miles", "Other...")
)

enum class HabitType(val label: String, val subLabel: String) {
    YesNo("Yes / No", "Once a day"),
    Quantity("Quantity", "e.g. 3 L water"),
    Count("Count", "e.g. Pray 5x")
}

enum class HabitFrequency(val label: String) {
    EveryDay("Every day"),
    SpecificDays("Specific days"),
    XPerWeek("X per week")
}
