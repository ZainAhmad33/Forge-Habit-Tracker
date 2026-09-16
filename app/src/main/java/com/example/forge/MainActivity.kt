package com.example.forge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.forge.core.designsystem.component.BottomNavBar
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.feature.habits.screen.HabitDetailRoute
import com.example.forge.feature.home.screen.HomeRoute
import com.example.forge.feature.insights.screen.InsightsRoute
import com.example.forge.feature.onboarding.WelcomeRoute
import com.example.forge.feature.profile.screen.ProfileDetailsRoute
import com.example.forge.feature.profile.screen.ProfileRoute
import com.example.forge.feature.upserthabit.screen.NewHabitRoute
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationHabitId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            ForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background, // Ensure full screen dark surface
                ) {
                    ForgeApp(
                        notificationHabitId = notificationHabitId,
                        onNotificationHandled = { notificationHabitId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("habit_id")?.let {
            notificationHabitId = it
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForgeApp(
    notificationHabitId: String?,
    onNotificationHandled: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    if (startDestination == null) return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(notificationHabitId, currentRoute) {
        if (notificationHabitId != null && currentRoute != null) {
            navController.navigate("habit_detail/$notificationHabitId") {
                // Clear backstack duplicates if clicking multiple times from outside the app
                popUpTo("home") { saveState = false }
                launchSingleTop = true
            }
            onNotificationHandled()
        }
    }

    val horizontalEnter = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(400))

    val horizontalExit = fadeOut(animationSpec = tween(400))

    val horizontalPopEnter = fadeIn(animationSpec = tween(400))

    val horizontalPopExit = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
    ) + fadeOut(animationSpec = tween(400))

    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
    )

    val shouldShowBottomBar = currentRoute in listOf("home", "insights")

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior),
        contentWindowInsets = WindowInsets.navigationBars,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                startDestination = startDestination!!,
                enterTransition = { horizontalEnter },
                exitTransition = { horizontalExit },
                popEnterTransition = { horizontalPopEnter },
                popExitTransition = { horizontalPopExit }
            ) {
                composable("welcome") {
                    WelcomeRoute {
                        navController.navigate("profile_setup")
                    }
                }
                composable("profile_setup") {
                    ProfileRoute(
                        onSaveSuccess = {
                            if (!navController.popBackStack()) {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable("profile_details") {
                    ProfileDetailsRoute(
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate("profile_setup") }
                    )
                }
                composable("home") {
                    HomeRoute(
                        onHabitDetailsClick = { habitId ->
                            navController.navigate("habit_detail/$habitId")
                        },
                        onProfileClick = {
                            navController.navigate("profile_details")
                        }
                    )
                }
                composable("insights") {
                    InsightsRoute()
                }
                composable(
                    route = "habit_detail/{habitId}"
                ) {
                    HabitDetailRoute(
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { habitId ->
                            navController.navigate("new_habit?habitId=$habitId")
                        },
                        onHabitDeleted = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "new_habit?habitId={habitId}",
                    arguments = listOf(
                        navArgument("habitId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(400)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(400)
                        )
                    }
                ) { backStackEntry ->
                    val habitIdString = backStackEntry.arguments?.getString("habitId")
                    val habitId = habitIdString?.let { UUID.fromString(it) }

                    NewHabitRoute(
                        onBackClick = { navController.popBackStack() },
                        habitId = habitId
                    )
                }
            }

            if (shouldShowBottomBar) {
                BottomNavBar(
                    scrollBehavior = scrollBehavior,
                    onAddHabitClick = { navController.navigate("new_habit") },
                    selectedTab = if (currentRoute == "home") "Home" else "Insights",
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInsights = {
                        navController.navigate("insights") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
