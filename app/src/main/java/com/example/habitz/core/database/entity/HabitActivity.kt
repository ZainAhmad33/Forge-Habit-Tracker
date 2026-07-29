package com.example.habitz.core.database.entity

import java.util.Date
import java.util.UUID

data class HabitActivity(
    val id: UUID = UUID.randomUUID(),
    val habitId: UUID,
    val completedAt: Date,
    val quantity: Int,
    val createdAt: Date = Date()
)
