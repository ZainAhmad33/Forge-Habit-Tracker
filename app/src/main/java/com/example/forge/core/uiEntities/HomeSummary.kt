package com.example.forge.core.uiEntities

data class HomeSummary(
    val completedCount: Int,
    val totalCount: Int,
    val currentStreakDays: Int,
    val weeklyCompletionPercent: Int,
)