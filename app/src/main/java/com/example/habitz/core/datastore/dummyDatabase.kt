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

        var activities: List<HabitActivity> = generateDummyActivities(habits)

        private fun generateDummyActivities(habits: List<Habit>): List<HabitActivity> {
            val list = mutableListOf<HabitActivity>()
            val calendar = Calendar.getInstance()
            val now = Date()

            for (i in 1..14) { // Last 14 days
                calendar.time = now
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val date = calendar.time
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                habits.forEach { habit ->
                    val shouldLog = when (habit.frequencyType) {
                        HabitFrequency.EveryDay -> true
                        HabitFrequency.SpecificDays -> habit.trackedDays.contains(
                            when (dayOfWeek) {
                                Calendar.MONDAY -> 1
                                Calendar.TUESDAY -> 2
                                Calendar.WEDNESDAY -> 3
                                Calendar.THURSDAY -> 4
                                Calendar.FRIDAY -> 5
                                Calendar.SATURDAY -> 6
                                Calendar.SUNDAY -> 7
                                else -> 0
                            }
                        )
                        HabitFrequency.DaysPerWeek -> (i % 7) < habit.numberOfTrackedDays
                        else -> true
                    }

                    // Add some randomness (80% chance of completion)
                    if (shouldLog && (0..10).random() > 2) {
                        val quantity = when (habit.habitType) {
                            HabitType.Quantity -> {
                                // For water (2500ml target), logs around 1500-3000
                                if (habit.title.contains("Water")) (1500..3000).random()
                                // For reading (20 pages target), logs 10-30
                                else (10..30).random()
                            }
                            HabitType.Count -> (40..60).random() // For pushups (50 target)
                            HabitType.YesNo -> 1
                        }

                        list.add(
                            HabitActivity(
                                habitId = habit.id,
                                completedAt = date,
                                quantity = quantity,
                                createdAt = date
                            )
                        )
                    }
                }
            }
            return list
        }
    }
}
