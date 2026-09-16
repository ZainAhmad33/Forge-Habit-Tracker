package com.example.forge.feature.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.LeaderboardEntry
import com.example.forge.core.uiEntities.ActivityData
import com.example.forge.core.uiEntities.ProgressShape
import com.example.forge.feature.habits.widget.CompletionBarChartWidget
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 218)
@Composable
fun HabitHeatmapWidgetPreview() {
    val widget = HabitHeatmapWidget()
    val today = LocalDate.now()

    // Convert LocalDate to java.util.Date cleanly
    val sixMonthsAgoDate = remember(today) {
        Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }

    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Workout",
        category = HabitCategory.Health,
        emoji = "🏋️",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Session",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    val sampleHeatmapData = remember {
        (0 until 98).map { dayOffset ->
            val date = today.minusDays(dayOffset.toLong())
            // Show varied intensities (0, 20, 40, 60, 80, 100)
            val percentage = (dayOffset % 6) * 20
            ActivityData(
                date = date,
                percentage = percentage
            )
        }
    }

    GlanceTheme {
        widget.HeatmapWidgetContent(
            habit = sampleHabit,
            streak = 10,
            heatmapData = sampleHeatmapData
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 294, heightDp = 218)
@Composable
fun HabitHeatmapWidgetMidWidePreview() {
    val widget = HabitHeatmapWidget()
    val today = LocalDate.now()

    // Creation date set 6 months in the past so early dates aren't filtered out as pre-creation spacers
    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }

    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Reading",
        category = HabitCategory.Productivity,
        emoji = "📖",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Pages",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    // Generated daily activity data spanning 180 days (~25 weeks) for wide previews
    val sampleHeatmapData = remember {
        (0..180).map { dayOffset ->
            val date = today.minusDays(dayOffset.toLong())
            // Simulate realistic habit completion: ~75% active days with varied intensity percentages
            val percentage = if ((0..100).random() > 25) (25..100).random() else 0
            ActivityData(
                date = date,
                percentage = percentage
            )
        }
    }

    GlanceTheme {
        widget.HeatmapWidgetContent(
            habit = sampleHabit,
            streak = 5,
            heatmapData = sampleHeatmapData
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 401, heightDp = 218)
@Composable
fun HabitHeatmapWidgetWidePreview() {
    val widget = HabitHeatmapWidget()
    val today = LocalDate.now()

    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }

    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Reading",
        category = HabitCategory.Productivity,
        emoji = "📖",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Pages",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    val sampleHeatmapData = remember {
        (0..100).map { dayOffset ->
            val date = today.minusDays(dayOffset.toLong())
            val percentage = if ((0..100).random() > 25) (25..100).random() else 0
            ActivityData(
                date = date,
                percentage = percentage
            )
        }
    }

    GlanceTheme {
        CompositionLocalProvider(LocalSize provides DpSize(400.dp, 200.dp)) {
            widget.HeatmapWidgetContent(
                habit = sampleHabit,
                streak = 99,
                heatmapData = sampleHeatmapData
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 218)
@Composable
fun HabitHeatmapWidgetEmptyPreview() {
    val widget = HabitHeatmapWidget()
    GlanceTheme {
        EmptyWidgetContent()
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 218)
@Composable
fun HabitHeatmapWidgetLockedPreview() {
    val widget = HabitHeatmapWidget()
    val lockedHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Run",
        category = HabitCategory.Health,
        emoji = "🏃",
        habitType = HabitType.Quantity,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 5,
        targetUnit = "km",
        progressShape = ProgressShape.Circle,
        isLocked = true,
        createdAt = Date(),
        updatedAt = Date()
    )

    GlanceTheme {
        widget.HeatmapWidgetContent(
            habit = lockedHabit,
            streak = 2,
            heatmapData = emptyList()
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 401, heightDp = 218)
@Composable
fun MonthlyCompletionWidgetPreview() {
    val widget = CompletionBarChartWidget()
    val today = LocalDate.of(2026, 9, 11)
    val selectedMonth = YearMonth.of(2026, 8)
    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }
    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Reading",
        category = HabitCategory.Productivity,
        emoji = "📖",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Pages",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    // Generate mock completion data for the month
    val mockMonthData = (1..selectedMonth.lengthOfMonth()).map { day ->
        val isFuture = day > today.dayOfMonth
        val isSkip = day % 7 == 0
        val completedQuantity = when {
            isFuture || isSkip -> 0
            day % 3 == 0 -> 1500 // Below goal
            else -> 3000          // On goal
        }

        DailyCompletion(
            day = day,
            completedQuantity = completedQuantity,
            isSkipDay = isSkip
        )
    }

    GlanceTheme {
        widget.CompletionBarChartWidgetContent(
            habit = sampleHabit,
            streak = 15,
            monthData = mockMonthData,
            target = 2500,
            today = today
        )
    }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 294, heightDp = 218)
@Composable
fun MonthlyCompletionMidWidgetPreview() {
    val widget = CompletionBarChartWidget()
    val today = LocalDate.of(2026, 9, 11)
    val selectedMonth = YearMonth.of(2026, 9)
    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }
    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Reading",
        category = HabitCategory.Productivity,
        emoji = "📖",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Pages",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    // Generate mock completion data for the month
    val mockMonthData = (1..selectedMonth.lengthOfMonth()).map { day ->
        val isFuture = day > today.dayOfMonth
        val isSkip = day % 7 == 0
        val completedQuantity = when {
            isFuture || isSkip -> 0
            day % 3 == 0 -> 1500 // Below goal
            else -> 3000          // On goal
        }

        DailyCompletion(
            day = day,
            completedQuantity = completedQuantity,
            isSkipDay = isSkip
        )
    }

    GlanceTheme {
        widget.CompletionBarChartWidgetContent(
            habit = sampleHabit,
            streak = 15,
            monthData = mockMonthData,
            target = 2500,
            today = today
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 218)
@Composable
fun MonthlyCompletionSmallWidgetPreview() {

    val widget = CompletionBarChartWidget()
    val today = LocalDate.of(2026, 9, 14)
    val selectedMonth = YearMonth.of(2026, 9)
    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )
    }
    val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Reading",
        category = HabitCategory.Productivity,
        emoji = "📖",
        habitType = HabitType.YesNo,
        frequencyType = HabitFrequency.EveryDay,
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Pages",
        progressShape = ProgressShape.Circle,
        createdAt = sixMonthsAgoDate,
        updatedAt = java.util.Date()
    )

    // Generate mock completion data for the month
    val mockMonthData = listOf(
        DailyCompletion(day = 1,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 2,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 3,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 4,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 5,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 6,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 7,  completedQuantity = 0,    isSkipDay = true),
        DailyCompletion(day = 8,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 9,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 10, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 11, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 12, completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 13, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 14, completedQuantity = 0,    isSkipDay = true),

        DailyCompletion(day = 15, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 16, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 17, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 18, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 19, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 20, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 21, completedQuantity = 0, isSkipDay = true),
        DailyCompletion(day = 22, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 23, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 24, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 25, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 26, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 27, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 28, completedQuantity = 0, isSkipDay = true),
        DailyCompletion(day = 29, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 30, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 1,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 2,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 3,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 4,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 5,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 6,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 7,  completedQuantity = 0,    isSkipDay = true),
        DailyCompletion(day = 8,  completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 9,  completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 10, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 11, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 12, completedQuantity = 1500, isSkipDay = false),
        DailyCompletion(day = 13, completedQuantity = 3000, isSkipDay = false),
        DailyCompletion(day = 14, completedQuantity = 0,    isSkipDay = true),

        DailyCompletion(day = 15, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 16, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 17, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 18, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 19, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 20, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 21, completedQuantity = 0, isSkipDay = true),
        DailyCompletion(day = 22, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 23, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 24, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 25, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 26, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 27, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 28, completedQuantity = 0, isSkipDay = true),
        DailyCompletion(day = 29, completedQuantity = 0, isSkipDay = false),
        DailyCompletion(day = 30, completedQuantity = 0, isSkipDay = false)
    )

    GlanceTheme {
        widget.CompletionBarChartWidgetContent(
            habit = sampleHabit,
            streak = 15,
            monthData = mockMonthData,
            target = 2500,
            today = today
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 218)
@Composable
fun HabitLeaderboardWidgetPreview() {
    val widget = HabitLeaderboardWidget()
    val sampleEntries = listOf(
        LeaderboardEntry(UUID.randomUUID(), "Hydration", "💧", 0.9f),
        LeaderboardEntry(UUID.randomUUID(), "Gym Session", "🏋️", 0.75f),
        LeaderboardEntry(UUID.randomUUID(), "Daily Reading", "📚", 0.6f),
        LeaderboardEntry(UUID.randomUUID(), "Morning Meditation", "🧘", 0.5f),
        LeaderboardEntry(UUID.randomUUID(), "Sleep Hygiene", "😴", 0.4f)
    )

    GlanceTheme {
        widget.LeaderboardWidgetContent(sampleEntries)
    }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 294, heightDp = 218)
@Composable
fun HabitLeaderboardMedWidgetPreview() {
    val widget = HabitLeaderboardWidget()
    val sampleEntries = listOf(
        LeaderboardEntry(UUID.randomUUID(), "Hydration", "💧", 0.9f),
        LeaderboardEntry(UUID.randomUUID(), "Gym Session", "🏋️", 0.75f),
        LeaderboardEntry(UUID.randomUUID(), "Daily Reading", "📚", 0.6f),
        LeaderboardEntry(UUID.randomUUID(), "Morning Meditation", "🧘", 0.5f),
        LeaderboardEntry(UUID.randomUUID(), "Sleep Hygiene", "😴", 0.4f)
    )

    GlanceTheme {
        widget.LeaderboardWidgetContent(sampleEntries)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 187, heightDp = 327)
@Composable
fun HabitLeaderboardLongWidgetPreview() {
    val widget = HabitLeaderboardWidget()
    val sampleEntries = listOf(
        LeaderboardEntry(UUID.randomUUID(), "Hydration", "💧", 0.9f),
        LeaderboardEntry(UUID.randomUUID(), "Gym Session", "🏋️", 0.75f),
        LeaderboardEntry(UUID.randomUUID(), "Daily Reading", "📚", 0.6f),
        LeaderboardEntry(UUID.randomUUID(), "Morning Meditation", "🧘", 0.5f),
        LeaderboardEntry(UUID.randomUUID(), "Sleep Hygiene", "😴", 0.4f)
    )

    GlanceTheme {
        widget.LeaderboardWidgetContent(sampleEntries)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 294, heightDp = 327)
@Composable
fun HabitLeaderboardMedLongWidgetPreview() {
    val widget = HabitLeaderboardWidget()
    val sampleEntries = listOf(
        LeaderboardEntry(UUID.randomUUID(), "Hydration", "💧", 0.9f),
        LeaderboardEntry(UUID.randomUUID(), "Gym Session", "🏋️", 0.75f),
        LeaderboardEntry(UUID.randomUUID(), "Daily Reading", "📚", 0.6f),
        LeaderboardEntry(UUID.randomUUID(), "Morning Meditation", "🧘", 0.5f),
        LeaderboardEntry(UUID.randomUUID(), "Sleep Hygiene", "😴", 0.4f)
    )

    GlanceTheme {
        widget.LeaderboardWidgetContent(sampleEntries)
    }
}


