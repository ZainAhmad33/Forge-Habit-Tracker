package com.example.forge.feature.upserthabit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
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
        _uiState.update { it.copy(title = newTitle, titleError = false) }
    }

    fun onEmojiChange(newEmoji: String) {
        _uiState.update { it.copy(selectedEmoji = newEmoji) }
    }

    fun onCategoryChange(newCategory: HabitCategory) {
        _uiState.update { it.copy(selectedCategory = newCategory) }
    }

    fun onTypeChange(newType: HabitType) {
        _uiState.update { it.copy(
            selectedType = newType
        ) }
    }

    fun onGoalChange(newGoal: Int) {
        _uiState.update { it.copy(dailyGoal = newGoal) }
    }

    fun onUnitChange(newUnit: String) {
        _uiState.update { it.copy(selectedUnit = newUnit) }
    }

    fun onFrequencyChange(newFrequency: HabitFrequency) {
        _uiState.update { it.copy(selectedFrequency = newFrequency, specificDaysError = false) }
    }

    fun toggleDay(dayIndex: Int) {
        _uiState.update { state ->
            var newDays = state.specificDays
            if (newDays.contains(dayIndex)) {
                newDays = newDays - dayIndex
            } else {
                newDays = newDays + dayIndex
            }
            state.copy(specificDays = newDays, specificDaysError = false)
        }
    }

    fun onDaysPerWeekChange(count: Int) {
        _uiState.update { it.copy(daysPerWeek = count) }
    }

    fun addReminder(time: LocalTime) {
        if (!_uiState.value.reminders.contains(time)){
            _uiState.update { state ->
                state.copy(reminders = state.reminders + time, remindersError = false)
            }
        }
    }

    fun removeReminder(time: LocalTime) {
        _uiState.update { state ->
            state.copy(reminders = state.reminders - time)
        }
    }

    fun onRemindersEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled, remindersError = false) }
    }

    fun onOtherUnitInputChange(unitInput: String){
        _uiState.update { it.copy(otherUnitInput = unitInput, otherUnitError = false) }
    }

    fun loadHabit(habitId: UUID) {
        viewModelScope.launch {
            habitsService.getHabitById(habitId)?.let { habit ->
                _uiState.update { state ->
                    val isOtherUnit = !state.availableUnits.contains(habit.targetUnit) && habit.habitType == HabitType.Quantity
                    state.copy(
                        habitId = habit.id,
                        title = habit.title,
                        selectedEmoji = habit.emoji,
                        selectedCategory = habit.category,
                        selectedType = habit.habitType,
                        dailyGoal = habit.completionTargetPerDay,
                        selectedUnit = if (isOtherUnit) "Other" else habit.targetUnit,
                        otherUnitInput = if (isOtherUnit) habit.targetUnit else "",
                        selectedFrequency = habit.frequencyType,
                        specificDays = habit.trackedDays,
                        daysPerWeek = habit.numberOfTrackedDays,
                        remindersEnabled = habit.reminders.isNotEmpty(),
                        reminders = habit.reminders
                    )
                }
            }
        }
    }

    fun onCreateHabitClick(): Boolean {
        if (validate()) {
            viewModelScope.launch {
                habitsService.upsertHabit(_uiState.value)
            }
            return true
        }
        return false
    }

    fun deleteHabit() {
        _uiState.value.habitId?.let { id ->
            viewModelScope.launch {
                habitsService.deleteHabit(id)
            }
        }
    }

    private fun validate(): Boolean {
        val titleError = _uiState.value.title.isBlank()
        val specificDaysError = _uiState.value.selectedFrequency == HabitFrequency.SpecificDays &&
                _uiState.value.specificDays.isEmpty()
        val otherUnitError = _uiState.value.selectedType == HabitType.Quantity &&
                _uiState.value.selectedUnit == "Other" &&
                _uiState.value.otherUnitInput.isBlank()
        val remindersError = _uiState.value.remindersEnabled && _uiState.value.reminders.isEmpty()

        _uiState.update {
            it.copy(
                titleError = titleError,
                specificDaysError = specificDaysError,
                otherUnitError = otherUnitError,
                remindersError = remindersError
            )
        }

        return !titleError && !specificDaysError && !otherUnitError && !remindersError
    }
}

