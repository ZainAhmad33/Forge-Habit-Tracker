package com.example.habitz.feature.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.component.BottomNavBar
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.home.data.local.DummyHomeData
import com.example.habitz.feature.home.domain.model.HabitCategory
import com.example.habitz.feature.home.presentation.components.HomeAppBar
import com.example.habitz.feature.home.presentation.components.HabitCategoryChips
import com.example.habitz.feature.home.presentation.components.HabitGrid
import com.example.habitz.feature.home.presentation.components.HomeHeader
import com.example.habitz.feature.home.presentation.components.HomeSummaryCard
import com.example.habitz.feature.home.presentation.components.SectionHeader
import com.example.habitz.feature.home.presentation.state.HomeUiState

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf(HabitCategory.All) }
    val uiState = HomeUiState.from(
        dashboard = DummyHomeData.dashboard,
        selectedCategory = selectedCategory,
    )

    HomeScreen(
        uiState = uiState,
        onCategorySelected = { selectedCategory = it },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCategorySelected: (HabitCategory) -> Unit,
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
                onSearchQueryChange = { query = it },
                onSearchSubmitted = { submittedQuery ->
                    // Handle search submit (e.g. filter list or trigger API call)
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
                    greetingName = uiState.greetingName,
                    dateLabel = uiState.dateLabel,
                    modifier = Modifier.fillMaxWidth(),
                )
                HomeSummaryCard(
                    summary = uiState.summary,
                    modifier = Modifier.fillMaxWidth(),
                )
                HabitCategoryChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.fillMaxWidth(),
                )
                SectionHeader(
                    title = "Today's habits",
                    trailingText = "${uiState.visibleHabits.size} shown",
                )
                HabitGrid(habits = uiState.visibleHabits)
            }
            BottomNavBar(
                scrollBehavior
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HabitzTheme {
        HomeScreen(
            uiState = HomeUiState.from(DummyHomeData.dashboard),
            onCategorySelected = {},
        )
    }
}
