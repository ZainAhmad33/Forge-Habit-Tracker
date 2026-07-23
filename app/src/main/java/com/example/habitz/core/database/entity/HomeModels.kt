package com.example.habitz.core.database.entity

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

data class HomeDashboard(
    val greetingName: String,
    val dateLabel: String,
    val summary: HomeSummary,
    val categories: List<HabitCategory>,
    val habits: List<HomeHabit>
)

data class HomeSummary(
    val completedCount: Int,
    val totalCount: Int,
    val currentStreakDays: Int,
    val weeklyCompletionPercent: Int,
)

data class HomeHabit(
    val id: String,
    val title: String,
    val category: HabitCategory,
    val scheduleLabel: String,
    val streakDays: Int,
    val progressPercent: Int,
    val isCompletedToday: Boolean,
    val image: String,
    val progressShape: ProgressShape
)

enum class HabitCategory(val label: String) {
    All("All"),
    Health("Health"),
    Mind("Mind"),
    Work("Work"),
    Home("Home"),
}

enum class ProgressShape{
    Circle,
    Square,
    Pill,
    Arch,
    Slanted,
    Pentagon,
    Gem,
    VerySunny,
    Sunny,
    Cookie4Sided,
    Cookie6Sided,
    Cookie7Sided,
    Cookie9Sided,
    Cookie12Sided,
    Clover4Leaf,
    Clover8Leaf,
    Puffy
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ProgressShapeEnumResolver: Map<ProgressShape, Shape>
    @Composable
    get() = mapOf(
        ProgressShape.Circle            to MaterialShapes.Circle.toShape(),
        ProgressShape.Square            to MaterialShapes.Square.toShape(),
        ProgressShape.Pill              to MaterialShapes.Pill.toShape(),
        ProgressShape.Arch              to MaterialShapes.Arch.toShape(),
        ProgressShape.Slanted           to MaterialShapes.Slanted.toShape(),
        ProgressShape.Pentagon          to MaterialShapes.Pentagon.toShape(),
        ProgressShape.Gem               to MaterialShapes.Gem.toShape(),
        ProgressShape.Sunny             to MaterialShapes.Sunny.toShape(),
        ProgressShape.VerySunny         to MaterialShapes.VerySunny.toShape(),
        ProgressShape.Cookie4Sided      to MaterialShapes.Cookie4Sided.toShape(),
        ProgressShape.Cookie7Sided      to MaterialShapes.Cookie7Sided.toShape(),
        ProgressShape.Cookie6Sided      to MaterialShapes.Cookie6Sided.toShape(),
        ProgressShape.Cookie9Sided      to MaterialShapes.Cookie9Sided.toShape(),
        ProgressShape.Cookie12Sided     to MaterialShapes.Cookie12Sided.toShape(),
        ProgressShape.Clover4Leaf       to MaterialShapes.Clover4Leaf.toShape(),
        ProgressShape.Clover8Leaf       to MaterialShapes.Clover8Leaf.toShape(),
        ProgressShape.Puffy             to MaterialShapes.Puffy.toShape(),

    )
