package com.example.habitz.feature.habits.presentation.screen

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habitz.feature.habits.presentation.components.*
import com.example.habitz.feature.habits.presentation.viewmodel.HabitDetailViewModel

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
    uiState: com.example.habitz.feature.habits.presentation.state.HabitDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDeleteLog: (java.util.UUID) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Details") },
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                HabitDetailHeader(habit, stats)
                
                RewardsSection(stats.rewards)
                
                MonthlyCompletionChart(stats.monthlyCompletionData)
                
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
