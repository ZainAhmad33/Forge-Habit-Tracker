package com.example.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.feature.habits.screen.HabitDetailRoute
import com.example.forge.feature.home.screen.HomeRoute
import com.example.forge.feature.insights.screen.InsightsRoute
import com.example.forge.feature.onboarding.WelcomeRoute
import com.example.forge.feature.profile.screen.ProfileRoute
import com.example.forge.feature.upserthabit.screen.NewHabitRoute
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Ensure full screen dark surface
                ) {
                    ForgeApp()
                }
            }
        }
    }
}

@Composable
fun ForgeApp(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable("welcome") {
            WelcomeRoute(
                onGetStartedClick = { navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileRoute(
                onSaveSuccess = {
                    // If we came from onboarding, navigate to home and clear stack
                    // If we came from home (edit mode), just pop back
                    if (!navController.popBackStack()) {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("home") {
            HomeRoute(
                onAddHabitClick = { navController.navigate("new_habit") },
                onHabitDetailsClick = { habitId ->
                    navController.navigate("habit_detail/$habitId")
                },
                onNavigateToInsights = {
                    navController.navigate("insights") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }
        composable("insights") {
            InsightsRoute(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddHabitClick = { navController.navigate("new_habit") }
            )
        }
        composable(
            route = "habit_detail/{habitId}",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
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
            // Slide up from bottom when navigating in
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400)
                )
            },
            // Slide down to bottom when pressing back or popping stack
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
}
