package com.example.habitz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.home.presentation.screen.HomeRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitzTheme {
                HomeRoute()
            }
        }
    }
}
