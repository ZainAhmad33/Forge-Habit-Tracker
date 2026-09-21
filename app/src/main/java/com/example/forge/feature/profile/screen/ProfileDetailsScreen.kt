package com.example.forge.feature.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forge.R
import com.example.forge.core.database.entity.User
import com.example.forge.core.designsystem.component.HeroStatCard
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.HeroStatItem
import com.example.forge.core.services.interfaces.ProfileStats
import com.example.forge.feature.profile.viewmodel.ProfileDetailsUiState
import com.example.forge.feature.profile.viewmodel.ProfileDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileDetailsRoute(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileDetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    uiState: ProfileDetailsUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    val monthYearFormatter = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Background Decoration
        Icon(
            painter = painterResource(id = R.drawable.ic_forge_flame),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = 100.dp)
                .rotate(-15f)
                .alpha(0.03f),
            tint = Color.Unspecified
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBackClick()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Edit Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${uiState.user?.firstName ?: ""} ${uiState.user?.lastName ?: ""}".trim(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    uiState.stats?.let { stats ->
                        Text(
                            text = "Member since ${dateFormatter.format(stats.joinedDate)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Performance Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.stats != null) {
                        val statItems = listOf(
                            HeroStatItem(
                                label = "Total Habits",
                                value = uiState.stats.totalHabits.toString(),
                                icon = Icons.Rounded.FactCheck
                            ),
                            HeroStatItem(
                                label = "Joining Date",
                                value = monthYearFormatter.format(uiState.stats.joinedDate),
                                icon = Icons.Rounded.History
                            ),
                            HeroStatItem(
                                label = "Total Logs",
                                value = uiState.stats.totalCompletions.toString(),
                                icon = Icons.Rounded.Insights
                            ),
                            HeroStatItem(
                                label = "Avg. Rate",
                                value = "${uiState.stats.averageCompletionRate}%",
                                icon = Icons.Rounded.Insights
                            )
                        )
                        HeroStatCard(items = statItems)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileDetailsScreenPreview() {
    ForgeTheme {
        ProfileDetailsScreen(
            uiState = ProfileDetailsUiState(
                user = User(firstName = "John", lastName = "Doe", dob = java.util.Date()),
                stats = ProfileStats(
                    totalHabits = 5,
                    joinedDate = java.util.Date(),
                    totalCompletions = 120,
                    averageCompletionRate = 85
                ),
                isLoading = false
            ),
            onBackClick = {},
            onEditClick = {}
        )
    }
}
