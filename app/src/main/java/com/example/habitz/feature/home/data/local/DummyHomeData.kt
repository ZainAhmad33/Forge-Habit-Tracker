package com.example.habitz.feature.home.data.local

import com.example.habitz.feature.home.domain.model.HabitCategory
import com.example.habitz.feature.home.domain.model.HomeDashboard
import com.example.habitz.feature.home.domain.model.HomeHabit
import com.example.habitz.feature.home.domain.model.HomeSummary
import com.example.habitz.feature.home.domain.model.ProgressShape

object DummyHomeData {
    val dashboard = HomeDashboard(
        greetingName = "Zain",
        dateLabel = "Today, July 22",
        summary = HomeSummary(
            completedCount = 4,
            totalCount = 7,
            currentStreakDays = 12,
            weeklyCompletionPercent = 78,
        ),
        categories = HabitCategory.entries,
        habits = listOf(
            HomeHabit(
                id = "morning-walk",
                title = "Morning walk",
                category = HabitCategory.Health,
                scheduleLabel = "Daily",
                streakDays = 12,
                progressPercent = 100,
                isCompletedToday = true,
                image = "🚶‍♂️",
                progressShape = ProgressShape.Cookie12Sided
            ),
            HomeHabit(
                id = "read",
                title = "Read 10 pages",
                category = HabitCategory.Mind,
                scheduleLabel = "Daily",
                streakDays = 6,
                progressPercent = 85,
                isCompletedToday = false,
                image = "📖️",
                progressShape = ProgressShape.Cookie4Sided
            ),
            HomeHabit(
                id = "deep-work",
                title = "Deep work block",
                category = HabitCategory.Work,
                scheduleLabel = "Weekdays",
                streakDays = 4,
                progressPercent = 65,
                isCompletedToday = false,
                image = "💻",
                progressShape = ProgressShape.VerySunny
            ),
            HomeHabit(
                id = "water",
                title = "Drink water",
                category = HabitCategory.Health,
                scheduleLabel = "4 of 6 glasses",
                streakDays = 9,
                progressPercent = 67,
                isCompletedToday = false,
                image = "🥤️",
                progressShape = ProgressShape.Arch
            ),
            HomeHabit(
                id = "tidy-room",
                title = "Tidy room",
                category = HabitCategory.Home,
                scheduleLabel = "Evening",
                streakDays = 2,
                progressPercent = 20,
                isCompletedToday = false,
                image = "🧹",
                progressShape = ProgressShape.Gem
            ),
        ),
    )
}
