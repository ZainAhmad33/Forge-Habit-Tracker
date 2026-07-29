package com.example.habitz.core.datastore

import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.core.database.entity.User
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.UUID

class dummyDatabase {
    companion object{
        var habits: List<Habit> = listOf(
        Habit(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            title = "Drink Water",
            category = HabitCategory.Health, // Replace with your actual enum constant
            emoji = "💧",
            habitType = HabitType.Quantity,
            reminders = listOf(
            LocalTime.of(9, 0),
            LocalTime.of(13, 0),
            LocalTime.of(18, 0)
            ),
            frequencyType = HabitFrequency.EveryDay,
            isTrackedEveryDay = true,
            trackedDays = emptyList(),
            numberOfTrackedDays = 7,
            completionTargetPerDay = 2500,
            targetUnit = "ml",
            dailyStreakCount = 0,
            progressShape = ProgressShape.Circle,
            createdAt = Date(),
            updatedAt = Date()
        ),
        Habit(
            id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "Morning Meditation",
            category = HabitCategory.Mindfulness,
            emoji = "🧘",
            habitType = HabitType.YesNo,
            reminders = listOf(
                LocalTime.of(7, 30)
            ),
            frequencyType = HabitFrequency.SpecificDays,
            isTrackedEveryDay = false,
            trackedDays = listOf(1, 3, 5), // Monday, Wednesday, Friday
            numberOfTrackedDays = 3,
            completionTargetPerDay = 1,
            targetUnit = "Per Day",
            dailyStreakCount = 0,
            progressShape = ProgressShape.Pill,
            createdAt = Date(),
            updatedAt = Date()
        ),
        Habit(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            title = "Daily Pushups",
            category = HabitCategory.Health,
            emoji = "💪",
            habitType = HabitType.Count,
            reminders = listOf(
                LocalTime.of(17, 0)
            ),
            frequencyType = HabitFrequency.EveryDay,
            isTrackedEveryDay = true,
            trackedDays = emptyList(),
            numberOfTrackedDays = 7,
            completionTargetPerDay = 50,
            targetUnit = "Per Day",
            dailyStreakCount = 0, // Reset / not completed today
            progressShape = ProgressShape.Gem,
            createdAt = Date(),
            updatedAt = Date()
        ),
        Habit(
            id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
            title = "Read Tech Blogs / Books",
            category = HabitCategory.Productivity,
            emoji = "📚",
            habitType = HabitType.Quantity,
            reminders = emptyList(), // No notifications scheduled
            frequencyType = HabitFrequency.DaysPerWeek, // Days per week
            isTrackedEveryDay = false,
            trackedDays = emptyList(),
            numberOfTrackedDays = 4, // 4 days out of 7
            completionTargetPerDay = 20,
            targetUnit = "pages",
            dailyStreakCount = 0,
            progressShape = ProgressShape.Arch,
            createdAt = Date(),
            updatedAt = Date()
        )
        )
        val userInformation = User("Zain", "Ahmad", Date(2001, 9, 1))

        private fun daysAgo(days: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            return calendar.time
        }

        private fun createLog(habitId: String, days: Int, quantity: Int): HabitActivity {
            val date = daysAgo(days)
            return HabitActivity(
                habitId = UUID.fromString(habitId),
                quantity = quantity,
                createdAt = date
            )
        }

        var activities: List<HabitActivity> = listOf(
            // --- Drink Water [Quantity] (Daily) ---
            // drank 2200ML today
            createLog("11111111-1111-1111-1111-111111111111", 0, 1000),
            createLog("11111111-1111-1111-1111-111111111111", 0, 600),
            createLog("11111111-1111-1111-1111-111111111111", 0, 600),
            // drank 2600 ML yesterday
            createLog("11111111-1111-1111-1111-111111111111", 1, 500),
            createLog("11111111-1111-1111-1111-111111111111", 1, 400),
            createLog("11111111-1111-1111-1111-111111111111", 1, 200),
            createLog("11111111-1111-1111-1111-111111111111", 1, 1000),
            createLog("11111111-1111-1111-1111-111111111111", 1, 500),
            // drank 2000ML 2 days ago
            createLog("11111111-1111-1111-1111-111111111111", 2, 2000),
            // Older Drink Water logs
            createLog("11111111-1111-1111-1111-111111111111", 3, 2500),
            createLog("11111111-1111-1111-1111-111111111111", 4, 2100),
            createLog("11111111-1111-1111-1111-111111111111", 5, 2500),
            createLog("11111111-1111-1111-1111-111111111111", 6, 2700),
            createLog("11111111-1111-1111-1111-111111111111", 7, 2500),
            createLog("11111111-1111-1111-1111-111111111111", 8, 2300),
            createLog("11111111-1111-1111-1111-111111111111", 9, 2500),
            createLog("11111111-1111-1111-1111-111111111111", 10, 2600),
            createLog("11111111-1111-1111-1111-111111111111", 11, 2400),
            createLog("11111111-1111-1111-1111-111111111111", 12, 2500),
            createLog("11111111-1111-1111-1111-111111111111", 13, 2200),

            // --- Morning Meditation ---
            createLog("22222222-2222-2222-2222-222222222222", 2, 1),
            createLog("22222222-2222-2222-2222-222222222222", 4, 1),
            createLog("22222222-2222-2222-2222-222222222222", 7, 1),
            createLog("22222222-2222-2222-2222-222222222222", 9, 1),
            createLog("22222222-2222-2222-2222-222222222222", 11, 1),

            // --- Daily Pushups ---
            createLog("33333333-3333-3333-3333-333333333333", 0, 10),
            createLog("33333333-3333-3333-3333-333333333333", 0, 5),
            createLog("33333333-3333-3333-3333-333333333333", 0, 15),
            createLog("33333333-3333-3333-3333-333333333333", 1, 10),
            createLog("33333333-3333-3333-3333-333333333333", 1, 15),
            createLog("33333333-3333-3333-3333-333333333333", 1, 20),
            createLog("33333333-3333-3333-3333-333333333333", 1, 6),
            createLog("33333333-3333-3333-3333-333333333333", 3, 60),
            createLog("33333333-3333-3333-3333-333333333333", 4, 50),
            createLog("33333333-3333-3333-3333-333333333333", 5, 45),
            createLog("33333333-3333-3333-3333-333333333333", 7, 50),
            createLog("33333333-3333-3333-3333-333333333333", 8, 50),
            createLog("33333333-3333-3333-3333-333333333333", 10, 55),
            createLog("33333333-3333-3333-3333-333333333333", 12, 50),

            // --- Read Tech Blogs ---
            createLog("44444444-4444-4444-4444-444444444444", 1, 25),
            createLog("44444444-4444-4444-4444-444444444444", 2, 15),
            createLog("44444444-4444-4444-4444-444444444444", 5, 20),
            createLog("44444444-4444-4444-4444-444444444444", 6, 30),
            createLog("44444444-4444-4444-4444-444444444444", 8, 20),
            createLog("44444444-4444-4444-4444-444444444444", 9, 22),
            createLog("44444444-4444-4444-4444-444444444444", 12, 20),
            createLog("44444444-4444-4444-4444-444444444444", 13, 25)
        )
    }
}
