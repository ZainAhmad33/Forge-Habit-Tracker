package com.example.forge.core.database.pojo

import java.time.LocalDate
import java.util.UUID

data class DailyHabitQuantity(
    val habitId: UUID,
    val day: LocalDate,
    val totalQuantity: Int
)
