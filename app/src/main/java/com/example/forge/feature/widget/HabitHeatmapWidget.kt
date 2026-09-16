package com.example.forge.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.ColorFilter
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import com.example.forge.MainActivity
import com.example.forge.R
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.uiEntities.ActivityData
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.datastore.preferences.core.Preferences

// Workaround for restricted ColorProvider factory functions
private fun fixedColorProvider(color: Color): ColorProvider = object : ColorProvider {
    override fun getColor(context: Context): Color = color
}

class HabitHeatmapWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Exact

    companion object {
        val habitIdKey = stringPreferencesKey("habit_id")
        val habitIdParam = ActionParameters.Key<String>("habit_id")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun habitStatsService(): IHabitStatsService
        fun habitsService(): IHabitsService
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val statsService = entryPoint.habitStatsService()
        val habitsService = entryPoint.habitsService()

        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val HABIT_ID_KEY = stringPreferencesKey("habit_id")
                val habitIdString = prefs[HABIT_ID_KEY]
                val habitId = remember(habitIdString) {
                    habitIdString?.let { UUID.fromString(it) }
                }

                if (habitId == null) {
                    EmptyWidgetContent()
                } else {
                    val habit by habitsService
                        .getHabitFlow(habitId)
                        .collectAsState(initial = null)

                    val today = LocalDate.now()

                    val heatmapData by statsService
                        .getRangeActivityData(
                            habitId,
                            YearMonth.from(today.minusMonths(4)),
                            5 // Request 5 months instead of 7
                        )
                        .collectAsState(initial = emptyList())

                    if (habit != null) {
                        val streakCount by produceState(initialValue = 0, key1 = habitId) {
                            value = statsService.getStreakInfo(habitId).count
                        }
                        HeatmapWidgetContent(
                            habit = habit!!,
                            streak = streakCount,
                            heatmapData = heatmapData
                        )
                    } else {
                        EmptyWidgetContent()
                    }
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val today = LocalDate.now()
        val sixMonthsAgoDate = java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )

        val sampleHabit = Habit(
            id = UUID.randomUUID(),
            title = "Morning Workout",
            category = com.example.forge.core.database.entity.HabitCategory.Health,
            emoji = "🏋️",
            habitType = com.example.forge.core.database.entity.HabitType.YesNo,
            frequencyType = com.example.forge.core.database.entity.HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 1,
            targetUnit = "Session",
            progressShape = com.example.forge.core.uiEntities.ProgressShape.Circle,
            createdAt = sixMonthsAgoDate,
            updatedAt = java.util.Date()
        )

        val sampleHeatmapData = (0 until 98).map { dayOffset ->
            val date = today.minusDays(dayOffset.toLong())
            // Show varied intensities for a better preview
            val percentage = when {
                dayOffset % 7 == 0 -> 100
                dayOffset % 7 == 1 -> 80
                dayOffset % 7 == 2 -> 60
                dayOffset % 7 == 3 -> 40
                dayOffset % 7 == 4 -> 20
                else -> 0
            }
            ActivityData(date = date, percentage = percentage)
        }

        provideContent {
            GlanceTheme {
                HeatmapWidgetContent(
                    habit = sampleHabit,
                    streak = 10,
                    heatmapData = sampleHeatmapData
                )
            }
        }
    }

    private fun renderHeatmapBitmap(
        context: Context,
        startDate: LocalDate,
        weeksCount: Int,
        activities: List<ActivityData>,
        today: LocalDate,
        habitCreatedAt: LocalDate,
        squareSizeDp: Float,
        spacingDp: Float,
        dayLabelWidthDp: Float,
        dayLabelGapDp: Float, // New parameter
        primaryColor: Int,
        onPrimaryColor: Int,
        surfaceVariantColor: Int,
        onSurfaceVariantColor: Int
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val squareSizePx = squareSizeDp * density
        val spacingPx = spacingDp * density
        val dayLabelWidthPx = dayLabelWidthDp * density
        val dayLabelGapPx = dayLabelGapDp * density

        // In renderHeatmapBitmap:
        val labelTextSize = (squareSizePx * 0.55f).coerceIn(10f * density, 13f * density)
        val monthHeaderHeightPx = labelTextSize * 1.8f



        // Total width now includes the gap
        val totalWidthPx = (dayLabelWidthPx + dayLabelGapPx + (weeksCount * (squareSizePx + spacingPx)) - spacingPx).toInt().coerceAtLeast(1)
        val totalHeightPx = (monthHeaderHeightPx + (7 * (squareSizePx + spacingPx)) - spacingPx).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(totalWidthPx, totalHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val activityMap = activities.associateBy { it.date }
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceVariantColor
            textSize = labelTextSize
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = squareSizePx * 0.45f // Keeps day numbers clear and centered inside boxes
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val labelMetrics = labelPaint.fontMetrics
        val cornerRadiusPx = (squareSizePx * 0.2f).coerceAtLeast(2f * density)

        // Draw Day Labels
        for (i in dayLabels.indices) {
            val squareCenterY = monthHeaderHeightPx + (i * (squareSizePx + spacingPx)) + (squareSizePx / 2)
            val textBaselineY = squareCenterY - (labelMetrics.ascent + labelMetrics.descent) / 2
            canvas.drawText(dayLabels[i], 0f, textBaselineY, labelPaint)
        }

        var lastMonthLabelWeek = -3

        for (weekIndex in 0 until weeksCount) {
            val weekStart = startDate.plusWeeks(weekIndex.toLong())
            val xOffset = dayLabelWidthPx + dayLabelGapPx + (weekIndex * (squareSizePx + spacingPx))

            // Render month label only if at least 2 weeks have passed since the last label
            val monthLabel = getMonthLabel(weekStart)
            if (monthLabel != null && (weekIndex - lastMonthLabelWeek >= 2)) {
                val y = monthHeaderHeightPx / 2 - (labelMetrics.ascent + labelMetrics.descent) / 2
                canvas.drawText(monthLabel, xOffset, y, labelPaint)
                lastMonthLabelWeek = weekIndex
            }

            // Days
            for (dayIndex in 0..6) {
                val date = weekStart.plusDays(dayIndex.toLong())
                if (date.isAfter(today) || date.isBefore(habitCreatedAt)) continue

                val activity = activityMap[date]
                val percentage = activity?.percentage ?: 0
                val hasActivity = percentage > 0

                val cellBg = if (hasActivity) {
                    ColorUtils.blendARGB(surfaceVariantColor, primaryColor, (percentage / 100f).coerceIn(0f, 1f))
                } else {
                    surfaceVariantColor
                }
                
                // For text color, if intensity is very high, use onPrimary for contrast
                val cellFg = if (hasActivity && percentage > 70) onPrimaryColor else onSurfaceVariantColor

                val cellTop = monthHeaderHeightPx + (dayIndex * (squareSizePx + spacingPx))
                val rect = RectF(xOffset, cellTop, xOffset + squareSizePx, cellTop + squareSizePx)

                cellPaint.color = cellBg
                canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, cellPaint)

                textPaint.color = cellFg
                val textMetrics = textPaint.fontMetrics
                val textY = rect.centerY() - (textMetrics.ascent + textMetrics.descent) / 2
                canvas.drawText(date.dayOfMonth.toString(), rect.centerX(), textY, textPaint)
            }
        }

        return bitmap
    }

    @Composable
    internal fun HeatmapWidgetContent(
        habit: Habit,
        streak: Int,
        heatmapData: List<ActivityData>
    ) {
        val today = LocalDate.now()
        val context = LocalContext.current

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(
                    actionRunCallback<NavigateToHabitAction>(
                        actionParametersOf(habitIdParam to habit.id.toString())
                    )
                )
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji
                Text(
                    text = habit.emoji,
                    style = TextStyle(fontSize = 18.sp)
                )

                Spacer(GlanceModifier.width(8.dp))

                // Title (Consumes remaining middle space, pushing the streak pill to the right)
                Text(
                    text = habit.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(GlanceModifier.width(8.dp))

                // Streak Pill
                Row(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.tertiaryContainer)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_forge_flame),
                        contentDescription = null,
                        modifier = GlanceModifier.size(14.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer)
                    )

                    Spacer(GlanceModifier.width(4.dp))

                    Text(
                        text = streak.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onTertiaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            //Spacer(GlanceModifier.height(6.dp))

            if (habit.isLocked) {
                LockedHabitContent()
            } else {
                val habitCreatedAt = habit.createdAt
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                val size = LocalSize.current

                // Outer container padding: 12dp top + 12dp bottom = 24dp total
                val verticalPaddingDp = 24f

                // Outer container padding: 12dp left + 12dp right = 24dp total
                val horizontalPaddingDp = 24f

                // Header height: ~26dp (18sp emoji / 16sp bold text bounds)
                val headerHeightDp = 26f

                // Total available width for the canvas
                val availableWidthDp = (size.width.value - horizontalPaddingDp).coerceAtLeast(0f)

                // Total available height for the canvas
                val availableHeightDp = (size.height.value - verticalPaddingDp - headerHeightDp).coerceAtLeast(0f)

                val dayLabelWidthDp = 22f// 2. Define fixed layout constraints
                val monthHeaderHeightDp = 18f
                val spacingDp = 2f

                // 1. Calculate square size strictly from available HEIGHT to prevent image downscaling
                val squareSizeDp = ((availableHeightDp - monthHeaderHeightDp - (6 * spacingDp)) / 7f).coerceAtLeast(1f)

                // 2. Calculate how many full week columns fit across available width
                val gridAvailableWidthDp = availableWidthDp - dayLabelWidthDp
                val calculatedWeeks = (gridAvailableWidthDp/(squareSizeDp + spacingDp)).toInt()

                // 3. Absorbs leftover horizontal pixels into the gap so grid spans 100% of available width
                val totalGridWidthDp = (calculatedWeeks * (squareSizeDp + spacingDp)) - spacingDp
                val dayLabelGapDp = (gridAvailableWidthDp - totalGridWidthDp).coerceAtLeast(8f)

                val firstVisibleMonday = today
                    .minusWeeks((calculatedWeeks - 1).toLong())
                    .minusDays((today.dayOfWeek.value - 1).toLong())

                val primaryColor = GlanceTheme.colors.primary.getColor(context).toArgb()
                val onPrimaryColor = GlanceTheme.colors.onPrimary.getColor(context).toArgb()
                val surfaceVariantColor = GlanceTheme.colors.surfaceVariant.getColor(context).toArgb()
                val onSurfaceVariantColor = GlanceTheme.colors.onSurfaceVariant.getColor(context).toArgb()

                val heatmapBitmap = remember(heatmapData, calculatedWeeks, squareSizeDp, today, habitCreatedAt) {
                    renderHeatmapBitmap(
                        context = context,
                        startDate = firstVisibleMonday,
                        weeksCount = calculatedWeeks,
                        activities = heatmapData,
                        today = today,
                        habitCreatedAt = habitCreatedAt,
                        squareSizeDp = squareSizeDp,
                        spacingDp = spacingDp,
                        dayLabelWidthDp = dayLabelWidthDp,
                        dayLabelGapDp = dayLabelGapDp,
                        primaryColor = primaryColor,
                        onPrimaryColor = onPrimaryColor,
                        surfaceVariantColor = surfaceVariantColor,
                        onSurfaceVariantColor = onSurfaceVariantColor
                    )
                }

                Box(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Image(
                        provider = ImageProvider(heatmapBitmap),
                        contentDescription = "Habit Heatmap Grid",
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private fun getMonthLabel(weekStart: LocalDate): String? {
        for (dayIndex in 0..6) {
            val date = weekStart.plusDays(dayIndex.toLong())
            // Only draw the label if the month genuinely changes in this column
            if (date.dayOfMonth == 1) {
                return date.month.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    java.util.Locale.getDefault()
                )
            }
        }
        return null
    }

}

class NavigateToHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Check either parameter key to ensure compatibility across all sharing widgets
        val habitId = parameters[HabitHeatmapWidget.habitIdParam] ?: parameters[com.example.forge.feature.habits.widget.CompletionBarChartWidget.habitIdParam]
        if (habitId != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("habit_id", habitId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }
}