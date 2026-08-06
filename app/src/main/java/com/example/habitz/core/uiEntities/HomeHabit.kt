package com.example.habitz.core.uiEntities

import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitType

data class HomeHabit(
    val id: String,
    val title: String,
    val category: HabitCategory,
    val targetLabel: String, // target label
    val streakDays: Int,
    val progressPercent: Int,
    val isCompletedToday: Boolean,
    val image: String,
    val progressShape: ProgressShape,
    val habitType: HabitType,
    val isScheduledForToday: Boolean,
    val quantityLoggedToday: Int
)