package com.example.forge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.forge.core.uiEntities.ProgressShape
import java.time.LocalTime
import java.util.Date
import java.util.UUID

@Entity(tableName = "habits")
class Habit (
    @PrimaryKey var id: UUID,
    var title: String,
    var category: HabitCategory,
    var emoji: String,
    var habitType: HabitType, // Yes/No, Quantity, Count
    var reminders: List<LocalTime> = listOf(),
    var frequencyType: HabitFrequency, // EveryDay, SpecificDays, NumberOfDays
    var isTrackedEveryDay: Boolean = true,
    var trackedDays: List<Int> = listOf(), // to be populated only if frequencyType == SpecificDays
    var numberOfTrackedDays: Int, // to be populated only if frequencyType == DaysPerWeek,
    var completionTargetPerDay: Int, // 1 in case of HabitType==YesOrNo otherwise >= 1
    var targetUnit: String, // "Per Day" in case of HabitType==Count/YesOrNo, otherwise string input for quantity
    var progressShape: ProgressShape,
    var skipDaysUnlocked: Int = 0,
    var createdAt: Date,
    var updatedAt: Date
)