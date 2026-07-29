package com.example.habitz.core.database.entity

import java.util.UUID

data class Reward(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val emoji: String,
    val requiredStreak: Int,
    val isUnlocked: Boolean = false
)
