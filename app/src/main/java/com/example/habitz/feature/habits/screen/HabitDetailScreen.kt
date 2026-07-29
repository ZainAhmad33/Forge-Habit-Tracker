package com.example.habitz.feature.habits.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.database.entity.Reward
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.feature.habits.components.AdditionalDetailsSection
import com.example.habitz.feature.habits.components.CurrentMonthCompletion
import com.example.habitz.feature.habits.components.HabitDetailHeader
import com.example.habitz.feature.habits.components.LogsSection
import com.example.habitz.feature.habits.components.MonthlyCompletionChart
import com.example.habitz.feature.habits.components.QuarterlyProgressCards
import com.example.habitz.feature.habits.components.RewardsSection
import com.example.habitz.feature.habits.viewmodel.HabitDetailViewModel
import com.example.habitz.feature.habits.state.HabitDetailUiState
import java.util.Date
import java.util.UUID

@Composable
fun HabitDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HabitDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = { uiState.habit?.id?.let { onEditClick(it.toString()) } },
        onMarkCompleted = { viewModel.markCompleted() },
        onDeleteLog = { viewModel.deleteLog(it) },
        activeDays = viewModel.totalActiveDays()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDeleteLog: (UUID) -> Unit,
    activeDays: Int = 0
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Habit Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = onEditClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                FloatingActionButton(
                    onClick = onMarkCompleted,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark Completed")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val habit = uiState.habit!!
            val stats = uiState.stats!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                HabitDetailHeader(habit, stats)

                RewardsSection(stats.rewards)

                CurrentMonthCompletion(stats.monthlyCompletionData, habit.completionTargetPerDay, habit.targetUnit, activeDays)

                QuarterlyProgressCards(stats.quarterlyCompletionRates)
                
                HorizontalDivider()

                AdditionalDetailsSection(habit)
                
                HorizontalDivider()

                LogsSection(
                    todayLogs = uiState.todayLogs,
                    historicalLogs = uiState.historicalLogs,
                    onDeleteLog = onDeleteLog
                )
                
                Spacer(modifier = Modifier.height(100.dp)) // Padding for FABs
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitDetailScreenPreview() {
    HabitzTheme {
        val habit = com.example.habitz.core.database.entity.Habit(
            id = UUID.randomUUID(),
            title = "Morning Meditation",
            category = HabitCategory.Mindfulness,
            emoji = "🧘",
            habitType = HabitType.YesNo,
            reminders = emptyList(),
            frequencyType = HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 1,
            targetUnit = "Per Day",
            progressShape = ProgressShape.Pill,
            createdAt = Date(),
            updatedAt = Date()
        )
        val stats = HabitStats(
            currentStreak = 10,
            bestStreak = 25,
            overallCompletionRate = 0.95f,
            monthlyCompletionData = emptyList(),
            quarterlyCompletionRates = emptyList(),
            rewards = listOf(
                Reward(title = "Starter", description = "3 day streak", emoji = "🥉", requiredStreak = 3, isUnlocked = true),
                Reward(title = "Consistent", description = "7 day streak", emoji = "🥈", requiredStreak = 7, isUnlocked = true),
                Reward(title = "Consistent", description = "14 day streak", emoji = "🥈", requiredStreak = 7, isUnlocked = false)
            )
        )
        val uiState = HabitDetailUiState(
            habit = habit,
            stats = stats,
            isLoading = false
        )
        HabitDetailScreen(
            uiState = uiState,
            onBackClick = {},
            onEditClick = {},
            onMarkCompleted = {},
            onDeleteLog = {}
        )
    }
}
