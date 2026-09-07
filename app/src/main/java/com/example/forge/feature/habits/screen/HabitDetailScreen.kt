package com.example.forge.feature.habits.screen

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.BestWeekData
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.GapData
import com.example.forge.core.services.interfaces.HabitStats
import com.example.forge.core.services.interfaces.HabitTrends
import com.example.forge.core.services.interfaces.MonthlyRate
import com.example.forge.core.services.interfaces.TrendData
import com.example.forge.core.uiEntities.ProgressShape
import com.example.forge.feature.habits.components.AdditionalDetailsSection
import com.example.forge.feature.habits.components.HabitDetailHeader
import com.example.forge.feature.habits.components.HabitLockedWidget
import com.example.forge.feature.habits.components.HistoricalActivitiesSection
import com.example.forge.feature.habits.components.LogsSection
import com.example.forge.feature.habits.components.MonthlyCompletionPager
import com.example.forge.feature.habits.components.QuarterlyProgressCards
import com.example.forge.feature.habits.components.SkipDaysInfoSection
import com.example.forge.feature.habits.components.TrendsAndConsistencySection
import com.example.forge.feature.habits.state.HabitDetailUiState
import com.example.forge.feature.habits.viewmodel.HabitDetailViewModel
import com.example.forge.core.services.interfaces.ITimeService
import com.example.forge.feature.home.components.HabitLogBottomSheet
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.UUID
import kotlin.random.Random

@Composable
fun HabitDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onHabitDeleted: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HabitDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = { uiState.habit?.id?.let { onEditClick(it.toString()) } },
        onDeleteHabit = {
            viewModel.deleteHabit()
            onHabitDeleted()
        },
        onMarkCompleted = { viewModel.markCompleted() },
        onDeleteLog = { viewModel.deleteLog(it) },
        onCalendarMonthChanged = { viewModel.onCalendarMonthChanged(it) },
        onCompletionMonthChanged = { viewModel.onCompletionMonthChanged(it) },
        getCompletionQuantity = viewModel::getCompletedQuantity,
        onLogProgress = viewModel::onLogProgress
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteHabit: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDeleteLog: (UUID) -> Unit,
    onCalendarMonthChanged: (YearMonth) -> Unit,
    onCompletionMonthChanged: (YearMonth) -> Unit,
    getCompletionQuantity: () -> Int,
    onLogProgress: (String, Int) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Habit Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEditClick()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (uiState.habit!!.habitType != HabitType.YesNo) {
                            showBottomSheet = true
                        } else {
                            onMarkCompleted()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark Completed")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
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

                HabitDetailHeader(
                    emoji = habit.emoji,
                    title = habit.title,
                    category = habit.category,
                    completionTargetPerDay = habit.completionTargetPerDay,
                    targetUnit = habit.targetUnit,
                    currentStreak = stats.currentStreak,
                    bestStreak = stats.bestStreak,
                    overallCompletionRate = stats.overallCompletionRate,
                    currentStreakStartDate = stats.currentStreakStartDate,
                    isLocked = habit.isLocked
                )

                if (habit.isLocked) {
                    val lockedAt = habit.lockedAt?.let {
                        val instant = it.toInstant()
                        val zone = java.time.ZoneId.systemDefault()
                        instant.atZone(zone).toLocalDate()
                    } ?: uiState.today
                    
                    HabitLockedWidget(
                        lockedAt = lockedAt,
                        today = uiState.today
                    )
                } else {
                    TrendsAndConsistencySection(
                        weeklyTrend = stats.trends?.weeklyTrend,
                        monthlyTrend = stats.trends?.monthlyTrend,
                        longestGapDays = stats.trends?.longestGap?.days ?: 0,
                        longestGapStartDate = stats.trends?.longestGap?.startDate,
                        longestGapEndDate = stats.trends?.longestGap?.endDate,
                        allTimeAverage = stats.trends?.allTimeAverage ?: 0f,
                        bestWeekRate = stats.trends?.bestWeek?.rate ?: 0f,
                        bestWeekStartDate = stats.trends?.bestWeek?.startDate,
                        bestWeekEndDate = stats.trends?.bestWeek?.endDate
                    )

                    SkipDaysInfoSection(habit.skipDaysAllowed, stats.currentStreak)

                    MonthlyCompletionPager(
                        allData = uiState.allMonthlyCompletion,
                        months = uiState.completionMonths,
                        selectedMonth = uiState.selectedCompletionMonth,
                        onMonthChanged = onCompletionMonthChanged,
                        target = habit.completionTargetPerDay,
                        today = uiState.today
                    )

                    QuarterlyProgressCards(stats.quarterlyCompletionRates)

                    AdditionalDetailsSection(habit)

                    LogsSection(
                        todayLogs = uiState.todayLogs,
                        onDeleteLog = onDeleteLog,
                        unit = if (habit.habitType == HabitType.Quantity) habit.targetUnit else ""
                    )

                    HistoricalActivitiesSection(
                        startDate = uiState.startDate ?: LocalDate.now(),
                        currentMonth = uiState.selectedCalendarMonth,
                        monthlyActivities = uiState.monthlyCalendarData,
                        onMonthChanged = onCalendarMonthChanged,
                        today = uiState.today
                    )
                }

                Spacer(modifier = Modifier.height(100.dp)) // Padding for FABs
            }
        }
    }
    if(showBottomSheet){
        val habit = uiState.habit!!
        HabitLogBottomSheet(
            habit.emoji,
            habit.title,
            habit.category,
            getCompletionQuantity(),
            habit.completionTargetPerDay,
            habit.targetUnit,
            onDismiss = { showBottomSheet = false },
            onLogProgress = { quantity ->
                onLogProgress(habit.id.toString(), quantity)
                showBottomSheet = false
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete '${uiState.habit?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteHabit()
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
fun HabitDetailScreenPreview() {
    ForgeTheme {
        val lastDay: Int = YearMonth.now().lengthOfMonth()
        val target = 2500
        var data = (1..lastDay).map { day ->
            DailyCompletion(
                day = day,
                completedQuantity = Random.nextInt(from = 2000, until = 2601),
                isSkipDay = Random.nextInt(0, 20) > 15
            )
        }
        val habit = com.example.forge.core.database.entity.Habit(
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
            ),
            trends = HabitTrends(
                weeklyTrend = TrendData(0.91f, 0.81f, 12),
                monthlyTrend = TrendData(0.85f, 0.79f, 7),
                longestGap = GapData(4, LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 5)),
                allTimeAverage = 0.84f,
                bestWeek = BestWeekData(1.0f, LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14))
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
            onDeleteHabit = {},
            onMarkCompleted = {},
            onDeleteLog = {},
            onCalendarMonthChanged = {},
            onCompletionMonthChanged = {},
            getCompletionQuantity = {100},
            onLogProgress = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitDetailScreenLockedPreview() {
    ForgeTheme {
        val habit = com.example.forge.core.database.entity.Habit(
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
            isLocked = true,
            lockedAt = Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 12), // 12 days ago
            createdAt = Date(),
            updatedAt = Date()
        )
        val stats = HabitStats(
            currentStreak = 0,
            bestStreak = 25,
            overallCompletionRate = 0.85f,
            monthlyCompletionData = emptyList(),
            quarterlyCompletionRates = emptyList(),
            currentStreakStartDate = null,
            trends = null
        )
        val uiState = HabitDetailUiState(
            habit = habit,
            stats = stats,
            isLoading = false,
            today = LocalDate.now()
        )
        HabitDetailScreen(
            uiState = uiState,
            onBackClick = {},
            onEditClick = {},
            onDeleteHabit = {},
            onMarkCompleted = {},
            onDeleteLog = {},
            onCalendarMonthChanged = {},
            onCompletionMonthChanged = {},
            getCompletionQuantity = {0},
            onLogProgress = { _, _ -> }
        )
    }
}
