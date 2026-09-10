package com.example.forge.feature.insights.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.designsystem.component.ActivityWeeklyPager
import com.example.forge.core.designsystem.component.HeroStatCard
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.*
import com.example.forge.core.uiEntities.ActivityData
import com.example.forge.core.uiEntities.HeroStatItem
import com.example.forge.feature.insights.components.InsightsScreenSkeleton
import com.example.forge.feature.insights.components.LeaderboardBarChart
import com.example.forge.feature.insights.components.MomentumLineChart
import com.example.forge.feature.insights.components.StreakDistributionChart
import com.example.forge.feature.insights.components.WeeklyPerformanceChart
import com.example.forge.feature.insights.components.CategoryDonutChart
import com.example.forge.feature.insights.state.InsightsUiState
import com.example.forge.feature.insights.viewmodel.InsightsViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.util.UUID

@Composable
fun InsightsRoute(
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    InsightsScreen(
        uiState = uiState,
        onMomentumMonthSelected = viewModel::onMomentumMonthSelected,
        onHeatmapMonthSelected = viewModel::onHeatmapMonthSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onMomentumMonthSelected: (YearMonth) -> Unit,
    onHeatmapMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    InsightsScreenSkeleton()
                }
            } else if (uiState.isEmpty) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                        .padding(innerPadding)
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
                            val statItems = listOf(
                                HeroStatItem(
                                    label = "Completion",
                                    value = "${(stats.completionRate * 100).toInt()}%",
                                    icon = Icons.Rounded.CheckCircle,
                                    delta = stats.rateChange
                                ),
                                HeroStatItem(
                                    label = "Current streak",
                                    value = "${stats.currentGlobalStreak} days",
                                    icon = Icons.Rounded.LocalFireDepartment
                                ),
                                HeroStatItem(
                                    label = "Perfect days",
                                    value = stats.perfectDaysCount.toString(),
                                    icon = Icons.Rounded.Star
                                ),
                                HeroStatItem(
                                    label = "Best streak",
                                    value = "${stats.bestGlobalStreak} days",
                                    icon = Icons.Rounded.EmojiEvents
                                )
                            )
                            
                            HeroStatCard(items = statItems)
                        }
                    }

                    Column {
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Your habit completion density across all habits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ActivityWeeklyPager(
                            startDate = uiState.earliestHabitDate ?: LocalDate.now(),
                            currentMonth = uiState.selectedHeatmapMonth,
                            monthlyActivities = uiState.heatmap,
                            onMonthChanged = onHeatmapMonthSelected,
                            today = LocalDate.now(),
                            weeksPerPage = 14,
                            showLegend = true
                        )
                    }

                    // Momentum Section
                    if (uiState.momentumMonths.isNotEmpty()) {
                        val pagerState = rememberPagerState(
                            initialPage = uiState.momentumMonths.indexOf(uiState.currentMomentumMonth).coerceAtLeast(0),
                        ) { uiState.momentumMonths.size }

                        LaunchedEffect(pagerState.currentPage) {
                            onMomentumMonthSelected(uiState.momentumMonths[pagerState.currentPage])
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Momentum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Month Display (Swiping handles navigation)
                                if (uiState.momentumMonths.size > 1) {
                                    Text(
                                        text = uiState.currentMomentumMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Direction of your consistency. A trend line above the 30-day baseline indicates you're improving.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                pageSpacing = 16.dp
                            ) { page ->
                                val month = uiState.momentumMonths.getOrNull(page)
                                val monthlyPoints = uiState.momentumData[month] ?: emptyList()

                                MomentumLineChart(
                                    points = monthlyPoints,
                                    totalDaysInMonth = month?.lengthOfMonth() ?: 30,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

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

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsightsScreenPreview() {
    val today = LocalDate.now()
    val aug2026 = YearMonth.of(2026, 8)
    val sept2026 = YearMonth.of(2026, 9)
    
    val momentumMonths = listOf(aug2026, sept2026)
    
    var currentMonth by remember { mutableStateOf(sept2026) }
    
    val momentumData = remember {
        val augPoints = (1..aug2026.lengthOfMonth()).map { day ->
            MomentumPoint(aug2026.atDay(day), 0.8f, 0.75f)
        }
        val septPoints = (1..7).map { day ->
            MomentumPoint(sept2026.atDay(day), 0.7f, 0.6f)
        }
        mapOf(aug2026 to augPoints, sept2026 to septPoints)
    }

    val uiState = InsightsUiState(
        isLoading = false,
        globalStats = GlobalStats(
            completionRate = 0.85f,
            currentGlobalStreak = 12,
            bestGlobalStreak = 24,
            perfectDaysCount = 45,
            rateChange = 8
        ),
        heatmap = (0..90).map { i ->
            ActivityData(today.minusDays(i.toLong()), (0..100).random())
        },
        momentumData = momentumData,
        currentMomentumMonth = currentMonth,
        momentumMonths = momentumMonths,
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
            onMomentumMonthSelected = { currentMonth = it },
            onHeatmapMonthSelected = {}
        )
    }
}
