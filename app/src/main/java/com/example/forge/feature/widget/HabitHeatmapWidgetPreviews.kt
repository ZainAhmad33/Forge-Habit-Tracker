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
import com.example.forge.core.uiEntities.ActivityData
import com.example.forge.core.uiEntities.ProgressShape
import java.time.LocalDate
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Composable
fun HabitHeatmapWidgetPreview() {
    val widget = HabitHeatmapWidget()
    val today = LocalDate.now()

    // Convert LocalDate to java.util.Date cleanly
    val sixMonthsAgoDate = remember(today) {
        java.util.Date.from(
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
            val percentage = if (dayOffset % 2 == 0) 100 else 0 // Alternate days for clear visual testing
            ActivityData(
                date = date,
                percentage = percentage
            )
        }
    }

    GlanceTheme {
        widget.HeatmapWidgetContent(
            habit = sampleHabit,
            heatmapData = sampleHeatmapData
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 100)
@Composable
fun HabitHeatmapWidgetEmptyPreview() {
    val widget = HabitHeatmapWidget()
    GlanceTheme {
        widget.EmptyWidgetContent()
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 300)
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
            heatmapData = emptyList()
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 300, heightDp = 250)
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
            heatmapData = sampleHeatmapData
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 400, heightDp = 250)
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
                heatmapData = sampleHeatmapData
            )
        }
    }
}