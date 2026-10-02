package com.example.forge.feature.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.GlanceTheme
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.uiEntities.ProgressShape
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 401, heightDp = 218)
@Composable
fun CompletionLineChartWidgetPreview() {
    val widget = CompletionLineChartWidget()
    val today = LocalDate.of(2026, 9, 11)
    val currentMonth = YearMonth.of(2026, 9)
    val lastMonth = currentMonth.minusMonths(1)
    val twoMonthsAgo = currentMonth.minusMonths(2)
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

    val mockMonthData = (1..twoMonthsAgo.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..lastMonth.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..today.dayOfMonth).map { day ->
        val isSkip = day % 7 == 0
        val completedQuantity = when {
            isSkip -> 0
            day % 3 == 0 -> 1500
            else -> 3000
        }
        DailyCompletion(
            day = day,
            completedQuantity = completedQuantity,
            isSkipDay = isSkip
        )
    }

    GlanceTheme {
        widget.CompletionLineChartWidgetContent(
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
fun CompletionLineChartMidWidgetPreview() {
    val widget = CompletionLineChartWidget()
    val today = LocalDate.of(2026, 9, 11)
    val currentMonth = YearMonth.of(2026, 9)
    val lastMonth = currentMonth.minusMonths(1)
    val twoMonthsAgo = currentMonth.minusMonths(2)
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

    val mockMonthData = (1..twoMonthsAgo.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..lastMonth.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..today.dayOfMonth).map { day ->
        val isSkip = day % 7 == 0
        val completedQuantity = when {
            isSkip -> 0
            day % 3 == 0 -> 1500
            else -> 3000
        }
        DailyCompletion(
            day = day,
            completedQuantity = completedQuantity,
            isSkipDay = isSkip
        )
    }

    GlanceTheme {
        widget.CompletionLineChartWidgetContent(
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
fun CompletionLineChartSmallWidgetPreview() {
    val widget = CompletionLineChartWidget()
    val today = LocalDate.of(2026, 9, 14)
    val currentMonth = YearMonth.of(2026, 9)
    val lastMonth = currentMonth.minusMonths(1)
    val twoMonthsAgo = currentMonth.minusMonths(2)
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

    val mockMonthData = (1..twoMonthsAgo.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..lastMonth.lengthOfMonth()).map { day ->
        val isSkip = day % 7 == 0
        DailyCompletion(
            day = day,
            completedQuantity = if (isSkip) 0 else 3000,
            isSkipDay = isSkip
        )
    } + (1..today.dayOfMonth).map { day ->
        val isSkip = day % 7 == 0
        val completedQuantity = when {
            isSkip -> 0
            day % 3 == 0 -> 1500
            else -> 3000
        }
        DailyCompletion(
            day = day,
            completedQuantity = completedQuantity,
            isSkipDay = isSkip
        )
    }

    GlanceTheme {
        widget.CompletionLineChartWidgetContent(
            habit = sampleHabit,
            streak = 15,
            monthData = mockMonthData,
            target = 2500,
            today = today
        )
    }
}
