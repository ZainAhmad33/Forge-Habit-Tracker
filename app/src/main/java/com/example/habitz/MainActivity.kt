package com.example.habitz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.habitz.core.designsystem.theme.HabitzTheme
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
               // HomeRoute()
                NewHabitRoute({})
            }
        }
    }
}
