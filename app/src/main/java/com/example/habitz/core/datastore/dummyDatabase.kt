package com.example.habitz.core.datastore

import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HomeHabit
import com.example.habitz.core.database.entity.ProgressShape
import com.example.habitz.core.database.entity.UserModel
import java.util.Date

class dummyDatabase {
    companion object{
        val habitsById = linkedMapOf(
            "morning-walk" to HomeHabit(
                id = "morning-walk",
                title = "Morning walk",
                category = HabitCategory.Health,
                scheduleLabel = "Daily",
                streakDays = 12,
                progressPercent = 100,
                isCompletedToday = true,
                image = "🚶‍♂️",
                progressShape = ProgressShape.Cookie12Sided,
            ),
            "read" to HomeHabit(
                id = "read",
                title = "Read 10 pages",
                category = HabitCategory.Mindfulness,
                scheduleLabel = "Daily",
                streakDays = 6,
                progressPercent = 85,
                isCompletedToday = false,
                image = "📖",
                progressShape = ProgressShape.Cookie4Sided,
            ),
            "deep-work" to HomeHabit(
                id = "deep-work",
                title = "Deep work block",
                category = HabitCategory.Work,
                scheduleLabel = "Weekdays",
                streakDays = 4,
                progressPercent = 65,
                isCompletedToday = false,
                image = "💻",
                progressShape = ProgressShape.VerySunny,
            ),
            "water" to HomeHabit(
                id = "water",
                title = "Drink water",
                category = HabitCategory.Health,
                scheduleLabel = "4 of 6 glasses",
                streakDays = 9,
                progressPercent = 67,
                isCompletedToday = false,
                image = "🥤",
                progressShape = ProgressShape.Arch,
            ),
            "tidy-room" to HomeHabit(
                id = "tidy-room",
                title = "Tidy room",
                category = HabitCategory.Home,
                scheduleLabel = "Evening",
                streakDays = 2,
                progressPercent = 20,
                isCompletedToday = false,
                image = "🧹",
                progressShape = ProgressShape.Gem,
            ),
        )

        val userInformation = UserModel("Zain", "Ahmad", Date(2001, 9, 1))
    }

}