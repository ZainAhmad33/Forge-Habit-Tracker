package com.example.forge.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "habit_activities",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitActivity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val habitId: UUID,
    val quantity: Int,
    val createdAt: Date = Date()
)
