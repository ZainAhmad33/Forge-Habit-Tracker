package com.example.habitz.feature.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habitz.R
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.designsystem.component.BottomNavBar
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.uiEntities.CategoryPill
import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.core.uiEntities.HomeSummary
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.feature.home.components.HabitCategoryChips
import com.example.habitz.feature.home.components.HabitGrid
import com.example.habitz.feature.home.components.HabitLogBottomSheet
import com.example.habitz.feature.home.components.HomeAppBar
import com.example.habitz.feature.home.components.HomeHeader
import com.example.habitz.feature.home.components.HomeSummaryCard
import com.example.habitz.feature.home.components.SectionHeader
import com.example.habitz.feature.home.state.HomeUiState
import com.example.habitz.feature.home.viewmodel.HomeDashboardUIState
import com.example.habitz.feature.home.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeRoute(
    onAddHabitClick: () -> Unit,
    onHabitDetailsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedHabitId by viewModel.selectedHabitIdForLogging.collectAsState()
    val selectedHabit = uiState.habits.find { it.id == selectedHabitId }

    HomeScreen(
        uiState = uiState,
        selectedHabitForLogging = selectedHabit,
        onCategorySelected = viewModel::onCategorySelected,
        onAddHabitClick = onAddHabitClick,
        onHabitDetailsClick = onHabitDetailsClick,
        modifier = modifier,
        searchHabits = viewModel::searchHabits,
        onHabitCardClick = viewModel::onHabitClick,
        onLogProgress = viewModel::onLogProgress,
        onDismissBottomSheet = viewModel::onDismissBottomSheet
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedHabitForLogging: HomeHabit?,
    onCategorySelected: (HabitCategory) -> Unit,
    searchHabits: (String) -> Unit,
    onAddHabitClick: () -> Unit,
    onHabitDetailsClick: (String) -> Unit,
    onHabitCardClick: (habit: HomeHabit) -> Unit,
    onLogProgress: (String, Int) -> Unit,
    onDismissBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {

    var query by remember { mutableStateOf("") }
    var isToolbarVisible by remember { mutableStateOf(true) }
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f) isToolbarVisible = false
                if (available.y > 10f) isToolbarVisible = true
                return Offset.Zero
            }
        }
    }
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            HomeAppBar(
                appName = "HabitTracker",
                searchQuery = query,
                onSearchQueryChange = {
                    query = it
                    searchHabits(query)
                },
                onSearchSubmitted = {
                    searchHabits(query)
                },
                onProfileClick = {
                    // Navigate to Profile Screen
                },
                backdropColor = MaterialTheme.colorScheme.surface.copy(),

            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(PaddingValues(horizontal = 20.dp, vertical = 18.dp)),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                HomeHeader(
                    greetingMessage = uiState.greetingMessage,
                    greetingName = uiState.greetingName,
                    dateLabel = uiState.dateLabel,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.habits.size > 0){
                    HomeSummaryCard(
                        summary = uiState.summary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HabitCategoryChips(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = onCategorySelected,
                        showAllCategoryChip = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SectionHeader(
                        title = "Today's habits",
                        trailingText = "${uiState.todaysHabits.size} shown",
                    )
                    HabitGrid(
                        habits = uiState.todaysHabits,
                        onHabitCardClick = onHabitCardClick,
                        onHabitDetailsClick = onHabitDetailsClick
                    )

                    if (uiState.otherHabits.isNotEmpty()) {
                        SectionHeader(
                            title = "Other habits",
                            trailingText = "${uiState.otherHabits.size} shown",
                        )
                        HabitGrid(
                            habits = uiState.otherHabits,
                            onHabitCardClick = onHabitCardClick,
                            onHabitDetailsClick = onHabitDetailsClick
                        )
                    }
                }
                else{
                    // no habits currently
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .weight(1f)
                            .offset(0.dp, -80.dp),
                        verticalArrangement = Arrangement.Center, // Centers everything vertically on screen
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_seedling),
                            contentDescription = "Seedling icon",
                            modifier = Modifier.size(200.dp) // ✅ Fixed: lowercase modifier replaced with Modifier
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
                            style = MaterialTheme.typography.bodyMedium, // Better typography token for multi-line body text
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp) // Prevents text from hitting edge of screen
                        )
                    }
                }

            }
            BottomNavBar(
                scrollBehavior = scrollBehavior,
                onAddHabitClick = onAddHabitClick
            )
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
            List<CategoryPill>(2) { CategoryPill(HabitCategory.Home, "🏡") },
            listOf(
                HomeHabit(
                    "1", "Water", HabitCategory.Health, "2L", 5, 50, false, "💧",
                    ProgressShape.Circle, HabitType.Quantity, true, 1000
                ),
                HomeHabit(
                    "2", "Read", HabitCategory.Productivity, "20p", 3, 0, false, "📚",
                    ProgressShape.Arch, HabitType.Quantity, false, 10
                )
            )
        ),
        selectedCategory = selectedCategory,
    )
    HabitzTheme {
        HomeScreen(
            uiState = uiState,
            selectedHabitForLogging = null,
            onCategorySelected = {},
            searchHabits = {},
            onAddHabitClick = {},
            onHabitDetailsClick = {},
            onHabitCardClick = {},
            onLogProgress = { _, _ -> },
            onDismissBottomSheet = {}
        )
    }
}
