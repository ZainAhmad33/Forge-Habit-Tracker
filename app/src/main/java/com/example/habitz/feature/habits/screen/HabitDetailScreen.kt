package com.example.habitz.feature.habits.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.database.entity.Reward
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.DailyCompletion
import com.example.habitz.core.services.interfaces.HabitStats
import com.example.habitz.core.services.interfaces.MonthlyRate
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.feature.habits.components.AdditionalDetailsSection
import com.example.habitz.feature.habits.components.CurrentMonthCompletion
import com.example.habitz.feature.habits.components.HabitDetailHeader
import com.example.habitz.feature.habits.components.LogsSection
import com.example.habitz.feature.habits.components.QuarterlyProgressCards
import com.example.habitz.feature.habits.components.SkipDaysInfoSection
import com.example.habitz.feature.habits.state.HabitDetailUiState
import com.example.habitz.feature.habits.viewmodel.HabitDetailViewModel
import java.time.YearMonth
import java.util.Date
import java.util.UUID
import kotlin.random.Random

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
        onDeleteLog = { viewModel.deleteLog(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDeleteLog: (UUID) -> Unit
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                HabitDetailHeader(habit, stats)

                SkipDaysInfoSection(habit.skipDaysUnlocked, stats.currentStreak)

                CurrentMonthCompletion(stats.monthlyCompletionData, habit.completionTargetPerDay, habit.targetUnit)

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
        val lastDay: Int = YearMonth.now().lengthOfMonth()
        val target = 2500
        var data = (1..lastDay).map { day ->
            DailyCompletion(
                day = day,
                completedQuantity = Random.nextInt(from = 2000, until = 2601),
                isSkipDay = Random.nextInt(0, 20) > 15
            )
        }
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
            monthlyCompletionData = data,
            quarterlyCompletionRates = listOf(
                MonthlyRate("May", 0.96f),
                MonthlyRate("June", 0.90f),
                MonthlyRate("July", 0.60f)
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
