package com.example.habitz.feature.upserthabit.screen

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.home.components.HabitCategoryChips
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.feature.upserthabit.UpsertHabitUiState
import com.example.habitz.feature.upserthabit.UpsertHabitViewModel
import com.example.habitz.feature.upserthabit.components.CreateHabitButton
import com.example.habitz.feature.upserthabit.components.EmojiPreviewCard
import com.example.habitz.feature.upserthabit.components.EmojiSelectorBar
import com.example.habitz.feature.upserthabit.components.FrequencySelector
import com.example.habitz.feature.upserthabit.components.GoalSelector
import com.example.habitz.feature.upserthabit.components.HabitTypeSelector
import com.example.habitz.feature.upserthabit.components.ReminderSelector
import java.time.LocalTime

@Composable
fun NewHabitRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpsertHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NewHabitScreen(
        uiState = uiState,
        pageTitle = "New Habit",
        onBackClick = onBackClick,
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
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
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
                CreateHabitButton(onClick = onCreateHabitClick)
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Emoji Preview
            EmojiPreviewCard(emoji = uiState.selectedEmoji)

            // 2. Emoji Selector Bar
            EmojiSelectorBar(
                selectedEmoji = uiState.selectedEmoji,
                popularEmojis = uiState.popularEmojis,
                onEmojiSelected = onEmojiSelected,
                onMoreClick = { /* Open emoji picker */ }
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                HabitCategoryChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            // 5. Habit Type Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How do you want to track it?",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                HabitTypeSelector(
                    selectedType = uiState.selectedType,
                    onTypeSelected = onTypeSelected
                )
            }

            // 6. Goal Selector
            GoalSelector(
                goal = uiState.dailyGoal,
                onGoalChange = onGoalChange,
                showUnit = uiState.selectedType == HabitType.Quantity,
                unit = uiState.selectedUnit,
                availableUnits = uiState.availableUnits,
                otherUnitInput = uiState.otherUnitInput,
                onOtherUnitInputChange = onOtherUnitInputChange,
                onUnitSelected = onUnitSelected,
                showCounter = uiState.selectedType != HabitType.YesNo,
                isOtherUnitError = uiState.otherUnitError,
                habitTypeSelected = uiState.selectedType
            )

            // 7 & 8. Frequency Selector
            FrequencySelector(
                selectedFrequency = uiState.selectedFrequency,
                onFrequencySelected = onFrequencySelected,
                specificDays = uiState.specificDays,
                onDayToggle = onDayToggle,
                daysPerWeek = uiState.daysPerWeek,
                onDaysPerWeekChange = onDaysPerWeekChange,
                isError = uiState.specificDaysError
            )

            // 9. Reminders
            ReminderSelector(
                remindersEnabled = uiState.remindersEnabled,
                onRemindersEnabledChange = onRemindersEnabledChange,
                reminders = uiState.reminders,
                onRemoveReminder = onRemoveReminder,
                onAddReminderClick = onAddReminderClick,
                isError = uiState.remindersError
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewHabitScreenPreview() {
    HabitzTheme {
        NewHabitScreen(
            uiState = UpsertHabitUiState(
                categories = listOf(
                    CategoryPill(HabitCategory.Health, "🏃"),
                    CategoryPill(HabitCategory.Mindfulness, "🧘")
                )
            ),
            onBackClick = {},
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
