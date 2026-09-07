package com.example.forge.core.uiEntities

import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitType

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
    val quantityLoggedToday: Int,
    val isLocked: Boolean = false
)