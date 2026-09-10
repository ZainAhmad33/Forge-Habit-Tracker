package com.example.forge.feature.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.R
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.CategoryPill
import com.example.forge.core.uiEntities.HomeHabit
import com.example.forge.core.uiEntities.HomeSummary
import com.example.forge.core.uiEntities.ProgressShape
import com.example.forge.feature.home.components.HabitCard
import com.example.forge.feature.home.components.HabitCategoryChips
import com.example.forge.feature.home.components.HabitGrid
import com.example.forge.feature.home.components.HabitLogBottomSheet
import com.example.forge.feature.home.components.HomeAppBar
import com.example.forge.feature.home.components.HomeHeader
import com.example.forge.feature.home.components.HomeScreenSkeleton
import com.example.forge.feature.home.components.HomeSummaryCard
import com.example.forge.feature.home.components.SectionHeader
import com.example.forge.feature.home.state.HomeUiState
import com.example.forge.feature.home.viewmodel.HomeDashboardUIState
import com.example.forge.feature.home.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeRoute(
    onHabitDetailsClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedHabitId by viewModel.selectedHabitIdForLogging.collectAsState()
    val selectedHabit = uiState.habits.find { it.id == selectedHabitId }

    HomeScreen(
        uiState = uiState,
        selectedHabitForLogging = selectedHabit,
        onCategorySelected = viewModel::onCategorySelected,
        onHabitDetailsClick = onHabitDetailsClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
        searchHabits = viewModel::searchHabits,
        onHabitCardClick = viewModel::onHabitClick,
        onLogProgress = viewModel::onLogProgress,
        onDismissBottomSheet = viewModel::onDismissBottomSheet,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedHabitForLogging: HomeHabit?,
    onCategorySelected: (HabitCategory) -> Unit,
    searchHabits: (String) -> Unit,
    onHabitDetailsClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onHabitCardClick: (habit: HomeHabit) -> Unit,
    onLogProgress: (String, Int) -> Unit,
    onDismissBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var query by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            HomeAppBar(
                appName = "Forge",
                searchQuery = query,
                onSearchQueryChange = {
                    query = it
                    searchHabits(query)
                },
                onSearchSubmitted = {
                    searchHabits(query)
                },
                onProfileClick = onProfileClick,
                backdropColor = MaterialTheme.colorScheme.surface.copy(),

            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        )
        {
            if (uiState.isLoading) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreenSkeleton()
                }
            } 
            else 
            {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    item {
                        HomeHeader(
                            greetingMessage = uiState.greetingMessage,
                            greetingName = uiState.greetingName,
                            dateLabel = uiState.dateLabel,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (uiState.totalHabitsCount > 0) {
                        item {
                            HomeSummaryCard(
                                summary = uiState.summary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        item {
                            HabitCategoryChips(
                                categories = uiState.categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCategorySelected(it)
                                },
                                showAllCategoryChip = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (uiState.todaysHabits.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Today's habits",
                                    trailingText = "${uiState.todaysHabits.size} shown",
                                )
                            }
                            items(uiState.todaysHabits.chunked(2)) { rowHabits ->
                                HabitRow(
                                    habits = rowHabits,
                                    onHabitCardClick = onHabitCardClick,
                                    onHabitDetailsClick = onHabitDetailsClick
                                )
                            }
                        }

                        if (uiState.otherHabits.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Other habits",
                                    trailingText = "${uiState.otherHabits.size} shown",
                                )
                            }
                            items(uiState.otherHabits.chunked(2)) { rowHabits ->
                                HabitRow(
                                    habits = rowHabits,
                                    onHabitCardClick = onHabitCardClick,
                                    onHabitDetailsClick = onHabitDetailsClick
                                )
                            }
                        }

                        if (uiState.todaysHabits.isEmpty() && uiState.otherHabits.isEmpty()) {
                            item {
                                // No results for current search or category filter
                                EmptySearchState(query = uiState.searchQuery)
                            }
                        }
                    } else {
                        item {
                            // no habits currently in DB
                            EmptyHabitState()
                        }
                    }
                }
            }
        }

        selectedHabitForLogging?.let { habit ->
            val splitLabel = habit.targetLabel.split(" ")
            HabitLogBottomSheet(
                habit.image,
                habit.title,
                habit.category,
                habit.quantityLoggedToday,
                splitLabel.first().toInt(),
                splitLabel.subList(1, splitLabel.size).joinToString(" "),
                onDismiss = onDismissBottomSheet,
                onLogProgress = { quantity ->
                    onLogProgress(habit.id, quantity)
                }
            )
        }
    }
}

@Composable
private fun HabitRow(
    habits: List<HomeHabit>,
    onHabitCardClick: (HomeHabit) -> Unit,
    onHabitDetailsClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        habits.forEach { habit ->
            HabitCard(
                habit = habit,
                modifier = Modifier.weight(1f),
                onHabitCardClick = onHabitCardClick,
                onDetailsClick = { onHabitDetailsClick(habit.id) }
            )
        }
        if (habits.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyHabitState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_forge_flame),
            contentDescription = "Forge flame icon",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create habits",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.W600
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first habit and start building a streak — even one small habit a day adds up.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "No results",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.W600
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        val message = if (query.isNotEmpty()) {
            "We couldn't find any habits matching \"$query\". Try a different search term."
        } else {
            "No habits found in this category. Try selecting a different category or create a new habit."
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    var selectedCategory by remember { mutableStateOf(HabitCategory.All) }
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

        val uiState = HomeUiState.from(
            dashboard = HomeDashboardUIState(
                "Good morning",
                "Zain",
                currentDate.format(formatter),
                HomeSummary(5, 10, 10, 50),
                List(2) { CategoryPill(HabitCategory.Home, "🏡") },
                listOf(
                HomeHabit(
                    "1", "Water", HabitCategory.Health, "2L", 5, 50, isCompletedToday = false, "💧",
                    ProgressShape.Circle, HabitType.Quantity, isScheduledForToday = true, quantityLoggedToday = 1000
                ),
                HomeHabit(
                    "2", "Read", HabitCategory.Productivity, "20p", 3, 0, isCompletedToday = false, "📚",
                    ProgressShape.Arch, HabitType.Quantity, isScheduledForToday = false, quantityLoggedToday = 10
                )
            )
        ),
        selectedCategory = selectedCategory,
        totalHabitsCount = 2
    )
    ForgeTheme {
        HomeScreen(
            uiState = uiState,
            selectedHabitForLogging = null,
            onCategorySelected = {},
            searchHabits = {},
            onHabitDetailsClick = {},
            onHabitCardClick = {},
            onLogProgress = { _, _ -> },
            onDismissBottomSheet = {},
            onProfileClick = {}
        )
    }
}
