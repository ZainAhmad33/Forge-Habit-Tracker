package com.example.forge.feature.upserthabit

import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import com.example.forge.feature.upserthabit.viewmodel.UpsertHabitViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class UpsertHabitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: UpsertHabitViewModel
    private val fakeHabitsService = object : IHabitsService {
        var upsertHabitCalled = false
        override fun getAllowedCategories(): List<CategoryPill> = emptyList()
        override suspend fun upsertHabit(habitForm: UpsertHabitUiState) {
            upsertHabitCalled = true
        }

        override suspend fun getHabitById(habitId: java.util.UUID): com.example.forge.core.database.entity.Habit? = null
        override suspend fun deleteHabit(habitId: java.util.UUID) {}
        override fun getHabitFlow(habitId: java.util.UUID): kotlinx.coroutines.flow.Flow<com.example.forge.core.database.entity.Habit?> = kotlinx.coroutines.flow.emptyFlow()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UpsertHabitViewModel(fakeHabitsService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreateHabitClick returns false when title is empty`() {
        viewModel.onTitleChange("")
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.titleError)
        assertFalse(fakeHabitsService.upsertHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns false when specific days are empty`() {
        viewModel.onTitleChange("Test Habit")
        viewModel.onFrequencyChange(HabitFrequency.SpecificDays)
        // No days selected
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.specificDaysError)
        assertFalse(fakeHabitsService.upsertHabitCalled)
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
        assertFalse(fakeHabitsService.upsertHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns false when reminders are enabled but none are added`() {
        viewModel.onTitleChange("Test Habit")
        viewModel.onRemindersEnabledChange(true)
        // No reminders added
        val result = viewModel.onCreateHabitClick()
        assertFalse(result)
        assertTrue(viewModel.uiState.value.remindersError)
        assertFalse(fakeHabitsService.upsertHabitCalled)
    }

    @Test
    fun `onCreateHabitClick returns true when all fields are valid`() = runTest {
        viewModel.onTitleChange("Test Habit")
        val result = viewModel.onCreateHabitClick()
        advanceUntilIdle()
        assertTrue(result)
        assertFalse(viewModel.uiState.value.titleError)
        assertTrue(fakeHabitsService.upsertHabitCalled)
    }
}
