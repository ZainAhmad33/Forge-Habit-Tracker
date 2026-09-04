package com.example.forge.feature.upserthabit.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.feature.home.components.HabitCategoryChips
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.feature.upserthabit.state.UpsertHabitUiState
import com.example.forge.feature.upserthabit.viewmodel.UpsertHabitViewModel
import com.example.forge.feature.upserthabit.components.CreateHabitButton
import com.example.forge.feature.upserthabit.components.EmojiPreviewCard
import com.example.forge.feature.upserthabit.components.EmojiSelectorBar
import com.example.forge.feature.upserthabit.components.FrequencySelector
import com.example.forge.feature.upserthabit.components.GoalSelector
import com.example.forge.feature.upserthabit.components.HabitTypeSelector
import com.example.forge.feature.upserthabit.components.ReminderSelector
import java.time.LocalTime
import java.util.UUID

@Composable
fun NewHabitRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    habitId: UUID? = null,
    viewModel: UpsertHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(habitId) {
        habitId?.let { viewModel.loadHabit(it) }
    }

    NewHabitScreen(
        uiState = uiState,
        pageTitle = if (uiState.habitId != null) "Edit Habit" else "New Habit",
        onBackClick = onBackClick,
        onDeleteClick = {
            viewModel.deleteHabit()
            onBackClick()
        },
        onTitleChange = viewModel::onTitleChange,
        onEmojiSelected = viewModel::onEmojiChange,
        onCategorySelected = viewModel::onCategoryChange,
        onTypeSelected = viewModel::onTypeChange,
        onGoalChange = viewModel::onGoalChange,
        onUnitSelected = viewModel::onUnitChange,
        onFrequencySelected = viewModel::onFrequencyChange,
        onDayToggle = viewModel::toggleDay,
        onDaysPerWeekChange = viewModel::onDaysPerWeekChange,
        onRemindersEnabledChange = viewModel::onRemindersEnabledChange,
        onRemoveReminder = viewModel::removeReminder,
        onAddReminderClick = viewModel::addReminder,
        onCreateHabitClick = {
            if (viewModel.onCreateHabitClick()) {
                onBackClick()
            }
        },
        onOtherUnitInputChange = viewModel::onOtherUnitInputChange,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitScreen(
    uiState: UpsertHabitUiState,
    pageTitle: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onCategorySelected: (HabitCategory) -> Unit,
    onTypeSelected: (HabitType) -> Unit,
    onGoalChange: (Int) -> Unit,
    onUnitSelected: (String) -> Unit,
    onFrequencySelected: (HabitFrequency) -> Unit,
    onDayToggle: (Int) -> Unit,
    onDaysPerWeekChange: (Int) -> Unit,
    onRemindersEnabledChange: (Boolean) -> Unit,
    onRemoveReminder: (LocalTime) -> Unit,
    onAddReminderClick: (LocalTime) -> Unit,
    onCreateHabitClick: () -> Unit,
    onOtherUnitInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClick()
                    }) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (uiState.habitId != null) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteConfirmation = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Habit",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                CreateHabitButton(
                    text = if (uiState.habitId != null) "Save Changes" else "Create Habit",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCreateHabitClick()
                    },
                    icon = if (uiState.habitId != null) Icons.Rounded.Save else Icons.Rounded.Add
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Emoji Preview
            EmojiPreviewCard(emoji = uiState.selectedEmoji)

            // 2. Emoji Selector Bar
            EmojiSelectorBar(
                selectedEmoji = uiState.selectedEmoji,
                popularEmojis = uiState.popularEmojis,
                onEmojiSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEmojiSelected(it)
                },
                onMoreClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    /* Open emoji picker */
                }
            )

            // 3. Habit Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Name") },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.titleError,
                supportingText = if (uiState.titleError) {
                    { Text("Habit name cannot be empty") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // 4. Category Picker
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                HabitCategoryChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCategorySelected(it)
                    }
                )
            }

            // 5. Habit Type Selection
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How do you want to track it?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                HabitTypeSelector(
                    selectedType = uiState.selectedType,
                    onTypeSelected = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTypeSelected(it)
                    }
                )
            }

            // 6. Goal Selector
            GoalSelector(
                goal = uiState.dailyGoal,
                onGoalChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGoalChange(it)
                },
                showUnit = uiState.selectedType == HabitType.Quantity,
                unit = uiState.selectedUnit,
                availableUnits = uiState.availableUnits,
                otherUnitInput = uiState.otherUnitInput,
                onOtherUnitInputChange = onOtherUnitInputChange,
                onUnitSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnitSelected(it)
                },
                showCounter = uiState.selectedType != HabitType.YesNo,
                isOtherUnitError = uiState.otherUnitError,
                habitTypeSelected = uiState.selectedType
            )

            // 7 & 8. Frequency Selector
            FrequencySelector(
                selectedFrequency = uiState.selectedFrequency,
                onFrequencySelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFrequencySelected(it)
                },
                specificDays = uiState.specificDays,
                onDayToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDayToggle(it)
                },
                daysPerWeek = uiState.daysPerWeek,
                onDaysPerWeekChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDaysPerWeekChange(it)
                },
                isError = uiState.specificDaysError
            )

            // 9. Reminders
            ReminderSelector(
                remindersEnabled = uiState.remindersEnabled,
                onRemindersEnabledChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemindersEnabledChange(it)
                },
                reminders = uiState.reminders,
                onRemoveReminder = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemoveReminder(it)
                },
                onAddReminderClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddReminderClick(it)
                },
                isError = uiState.remindersError
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete '${uiState.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewHabitScreenPreview() {
    ForgeTheme {
        NewHabitScreen(
            uiState = UpsertHabitUiState(
                categories = listOf(
                    CategoryPill(HabitCategory.Health, "🏃"),
                    CategoryPill(HabitCategory.Mindfulness, "🧘")
                )
            ),
            onBackClick = {},
            onDeleteClick = {},
            onTitleChange = {},
            onEmojiSelected = {},
            onCategorySelected = {},
            onTypeSelected = {},
            onGoalChange = {},
            onUnitSelected = {},
            onFrequencySelected = {},
            onDayToggle = {},
            onDaysPerWeekChange = {},
            onRemindersEnabledChange = {},
            onRemoveReminder = {},
            onAddReminderClick = {},
            onCreateHabitClick = {},
            onOtherUnitInputChange = {},
            pageTitle = "New Habit"
        )
    }
}
