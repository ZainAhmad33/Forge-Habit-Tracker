package com.example.forge.feature.upserthabit

import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import com.example.forge.feature.upserthabit.viewmodel.UpsertHabitViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpsertHabitViewModelTest {

    private lateinit var viewModel: UpsertHabitViewModel
    private val fakeHabitsService = object : IHabitsService {
        var createHabitCalled = false
        override fun getAllowedCategories(): List<CategoryPill> = emptyList()
        override suspend fun createHabit(habitForm: UpsertHabitUiState) {
            createHabitCalled = true
        }

        override suspend fun getHabitById(habitId: java.util.UUID): com.example.forge.core.database.entity.Habit? = null
        override fun getHabitFlow(habitId: java.util.UUID): kotlinx.coroutines.flow.Flow<com.example.forge.core.database.entity.Habit?> = kotlinx.coroutines.flow.emptyFlow()
    }

    @Before
    fun setup() {
        viewModel = UpsertHabitViewModel(fakeHabitsService)
    }

    @Test
    fun `onCreateHabitClick returns false when title is empty`() {
        viewModel.onTitleChange("")
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.titleError)
        assertFalse(fakeHabitsService.createHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns false when specific days are empty`() {
        viewModel.onTitleChange("Test Habit")
        viewModel.onFrequencyChange(HabitFrequency.SpecificDays)
        // No days selected
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.specificDaysError)
        assertFalse(fakeHabitsService.createHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns false when other unit is empty`() {
        viewModel.onTitleChange("Test Habit")
        viewModel.onTypeChange(HabitType.Quantity)
        viewModel.onUnitChange("Other")
        viewModel.onOtherUnitInputChange("")
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.otherUnitError)
        assertFalse(fakeHabitsService.createHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns false when reminders are enabled but none are added`() {
        viewModel.onTitleChange("Test Habit")
        viewModel.onRemindersEnabledChange(true)
        // No reminders added
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.remindersError)
        assertFalse(fakeHabitsService.createHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns true when all fields are valid`() {
        viewModel.onTitleChange("Test Habit")
        val result = viewModel.onCreateHabitClick()
        assertTrue(result)
        assertFalse(viewModel.uiState.value.titleError)
        assertTrue(fakeHabitsService.createHabitCalled)
    }
}
