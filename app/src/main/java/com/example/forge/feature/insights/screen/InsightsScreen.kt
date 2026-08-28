package com.example.forge.feature.insights.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.designsystem.component.BottomNavBar
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.*
import com.example.forge.feature.insights.components.*
import com.example.forge.feature.insights.components.InsightsScreenSkeleton
import com.example.forge.feature.insights.state.InsightsUiState
import com.example.forge.feature.insights.viewmodel.InsightsViewModel
import java.time.LocalDate
import java.util.UUID

@Composable
fun InsightsRoute(
    onNavigateToHome: () -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    InsightsScreen(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onAddHabitClick = onAddHabitClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onNavigateToHome: () -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Insights", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoading) {
                InsightsScreenSkeleton()
            } else if (uiState.isEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.BarChart, null, Modifier.size(64.dp), MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("No insights yet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Keep tracking your habits to see your progress and trends here.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Hero Stats
                    uiState.globalStats?.let { stats ->
                        Column {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your overall performance since you started.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            GlobalHeroStatsCard(stats = stats)
                        }
                    }

                    ActivityHeatmap(
                        cells = uiState.heatmap,
                        description = "Your habit completion density over the past year."
                    )

                    MomentumLineChart(
                        points = uiState.momentumTrend,
                        description = "Direction of your consistency. A trend line above the 30-day baseline indicates you're improving."
                    )

                    LeaderboardBarChart(
                        entries = uiState.leaderboard,
                        description = "Top performing habits by completion rate."
                    )

                    uiState.weeklyPerformance?.let {
                        WeeklyPerformanceChart(
                            performance = it,
                            description = "Average completion rate by day of the week."
                        )
                    }

                    CategoryDonutChart(
                        shares = uiState.categoryBreakdown,
                        description = "Habit distribution across different life areas."
                    )

                    StreakDistributionChart(
                        buckets = uiState.streakDistribution,
                        description = "Current streak length buckets for all active habits."
                    )
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
                }
            }
            
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(
                    scrollBehavior = scrollBehavior,
                    onAddHabitClick = onAddHabitClick,
                    initialSelected = "Insights",
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToInsights = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsightsScreenPreview() {
    val today = LocalDate.now()
    val uiState = InsightsUiState(
        isLoading = false,
        globalStats = GlobalStats(
            completionRate = 0.85f,
            currentGlobalStreak = 12,
            bestGlobalStreak = 24,
            perfectDaysCount = 45,
            rateChange = 8
        ),
        heatmap = (0..30).map { i ->
            HeatmapCell(today.minusDays(i.toLong()), (0..4).random(), emptyList())
        },
        momentumTrend = (0..20).map { i ->
            MomentumPoint(today.minusDays(i.toLong()), (i % 5) / 5f, (i % 10) / 10f)
        }.reversed(),
        leaderboard = listOf(
            LeaderboardEntry(UUID.randomUUID(), "Water", "💧", 0.95f),
            LeaderboardEntry(UUID.randomUUID(), "Reading", "📚", 0.85f),
            LeaderboardEntry(UUID.randomUUID(), "Gym", "🏋️", 0.7f)
        ),
        weeklyPerformance = WeeklyPerformance(
            dayRates = mapOf(0 to 0.8f, 1 to 0.9f, 2 to 0.7f, 3 to 0.6f, 4 to 0.5f, 5 to 0.4f, 6 to 0.3f),
            bestDay = 1,
            worstDay = 6
        ),
        categoryBreakdown = listOf(
            CategoryShare(HabitCategory.Health, 0.85f, 3),
            CategoryShare(HabitCategory.Work, 0.7f, 2),
            CategoryShare(HabitCategory.Home, 0.5f, 1)
        ),
        streakDistribution = listOf(
            StreakBucket("0 days", 1),
            StreakBucket("1-6 days", 4),
            StreakBucket("7-29 days", 3),
            StreakBucket("30+ days", 2)
        )
    )

    ForgeTheme {
        InsightsScreen(
            uiState = uiState,
            onNavigateToHome = {},
            onAddHabitClick = {}
        )
    }
}
