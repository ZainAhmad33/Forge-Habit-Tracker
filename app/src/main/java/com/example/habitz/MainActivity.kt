package com.example.habitz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.habits.screen.HabitDetailRoute
import com.example.habitz.feature.home.screen.HomeRoute
import com.example.habitz.feature.upserthabit.screen.NewHabitRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitzTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Ensure full screen dark surface
                ) {
                    HabitzApp()
                }
            }
        }
    }
}

@Composable
fun HabitzApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeRoute(
                onAddHabitClick = { navController.navigate("new_habit") },
                onHabitDetailsClick = { habitId ->
                    navController.navigate("habit_detail/$habitId")
                }
            )
        }
        composable("habit_detail/{habitId}") {
            HabitDetailRoute(
                onBackClick = { navController.popBackStack() },
                onEditClick = { habitId ->
                    // Navigate to edit screen if available
                    // navController.navigate("edit_habit/$habitId")
                }
            )
        }
        composable(
                route = "new_habit",
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
        }) {
            NewHabitRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
