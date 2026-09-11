package com.example.forge.feature.habits.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.forge.R
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.services.interfaces.DailyCompletion
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.feature.widget.EmptyWidgetContent
import com.example.forge.feature.widget.HabitHeatmapWidget
import com.example.forge.feature.widget.NavigateToHabitAction
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID


class MonthlyCompletionWidget: GlanceAppWidget(){
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
                val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
                val habitIdString = prefs[HabitHeatmapWidget.habitIdKey]
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

                    val monthlyCompletions by statsService
                        .getAllMonthlyCompletion(
                            habitId
                        )
                        .collectAsState(initial = emptyMap())
                    val currentYearMonth = YearMonth.now()
                    val currentData = monthlyCompletions.get(currentYearMonth)
                    val lastMonth = currentYearMonth.minusMonths(1)
                    val lastMonthData = monthlyCompletions.get(lastMonth)

                    var totalDailyCompletions: List<DailyCompletion> = emptyList()
                    if (currentData?.size != 0 && lastMonthData?.size != 0){
                        totalDailyCompletions = lastMonthData!! + currentData!!
                    }

                    if (habit != null) {
                        val streakCount by produceState(initialValue = 0, key1 = habitId) {
                            value = statsService.getStreakInfo(habitId).count
                        }
                        CompletionBarChartWidgetContent(
                            habit = habit!!,
                            streak = streakCount,
                            monthData = totalDailyCompletions,
                            target = habit!!.completionTargetPerDay,
                            today = LocalDate.now()
                        )
                    } else {
                        EmptyWidgetContent()
                    }
                }
            }
        }
    }

    @Composable
    internal fun CompletionBarChartWidgetContent(
        habit: Habit,
        streak: Int,
        monthData: List<DailyCompletion>,
        target: Int,
        today: LocalDate
    ) {
        val context = LocalContext.current

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(
                    actionRunCallback<NavigateToHabitAction>(
                        actionParametersOf(HabitHeatmapWidget.habitIdParam to habit.id.toString())
                    )
                )
        ) {
            // 1. Unified Header (Emoji, Title, and Streak Pill) matching the heatmap widget
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.emoji,
                    style = TextStyle(fontSize = 18.sp)
                )

                Spacer(GlanceModifier.width(8.dp))

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
                        colorFilter = androidx.glance.ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer)
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

            Spacer(GlanceModifier.height(8.dp))

            // 2. Chart Container Card
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(24.dp)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    val size = LocalSize.current
                    // Static Legends

                    if (size.width >= 250.dp){
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WidgetLegendItem(color = androidx.compose.ui.graphics.Color(0xFF4CAF50), label = "On Goal")
                            // Use Spacer instead of horizontalArrangement
                            Spacer(GlanceModifier.width(12.dp))
                            WidgetLegendItem(color = GlanceTheme.colors.error.getColor(context), label = "Below Goal")
                            // Use Spacer instead of horizontalArrangement
                            Spacer(GlanceModifier.width(12.dp))
                            WidgetLegendItem(color = GlanceTheme.colors.outline.getColor(context), label = "Skip Day")
                        }
                        Spacer(GlanceModifier.height(8.dp))
                    }


                    val density = context.resources.displayMetrics.density

                    // Approximate dynamic pixel sizing based on Glance available dimensions
                    val widthPx = ((size.width.value - 24f - 24f) * density).toInt().coerceAtLeast(100)
                    val heightPx = ((size.height.value - 24f - 32f - 24f - 20f) * density).toInt().coerceAtLeast(80)

                    // 1. Extract the ColorProviders first (These are @Composable calls)
                    val errorProvider = GlanceTheme.colors.error
                    val outlineProvider = GlanceTheme.colors.outline
                    val surfaceVariantProvider = GlanceTheme.colors.surfaceVariant
                    val onSurfaceVariantProvider = GlanceTheme.colors.onSurfaceVariant

// 2. Resolve the exact colors safely inside runCatching (Standard function calls)
                    val successColor = android.graphics.Color.parseColor("#4CAF50")

                    val errorColor = remember(errorProvider, context) {
                        runCatching { errorProvider.getColor(context).toArgb() }.getOrDefault(android.graphics.Color.RED)
                    }
                    val skipColor = remember(outlineProvider, context) {
                        runCatching { outlineProvider.getColor(context).toArgb() }.getOrDefault(android.graphics.Color.GRAY)
                    }
                    val trackColor = remember(surfaceVariantProvider, context) {
                        runCatching { surfaceVariantProvider.getColor(context).toArgb() }.getOrDefault(android.graphics.Color.LTGRAY)
                    }
                    val targetLineColor = remember(outlineProvider, context) {
                        runCatching { outlineProvider.getColor(context).toArgb() }.getOrDefault(android.graphics.Color.DKGRAY)
                    }
                    val labelTextColor = remember(onSurfaceVariantProvider, context) {
                        runCatching { onSurfaceVariantProvider.getColor(context).toArgb() }.getOrDefault(android.graphics.Color.BLACK)
                    }

                    val chartBitmap = remember(monthData, target, today, widthPx, heightPx) {
                        runCatching {
                            renderCompletionBarChartBitmap(
                                context = context,
                                data = monthData,
                                target = target,
                                today = today,
                                widthPx = widthPx,
                                heightPx = heightPx,
                                successColor = successColor,
                                errorColor = errorColor,
                                skipColor = skipColor,
                                trackColor = trackColor,
                                targetLineColor = targetLineColor,
                                labelTextColor = labelTextColor
                            )
                        }.getOrNull()
                    }

                    if (chartBitmap != null) {
                        Image(
                            provider = ImageProvider(chartBitmap),
                            contentDescription = "Monthly completion bar chart",
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetLegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(10.dp)
                    .background(color)
                    .cornerRadius(5.dp)
            ){}
            // Use Spacer instead of horizontalArrangement
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }

    private fun renderCompletionBarChartBitmap(
        context: Context,
        data: List<DailyCompletion>,
        target: Int,
        today: LocalDate,
        widthPx: Int,
        heightPx: Int,
        successColor: Int,
        errorColor: Int,
        skipColor: Int,
        trackColor: Int,
        targetLineColor: Int,
        labelTextColor: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val density = context.resources.displayMetrics.density
        val xAxisHeightPx = 14f * density
        val chartHeight = heightPx - xAxisHeightPx
        val horizontalPadding = 8f * density
        val chartWidth = widthPx - (horizontalPadding * 2)

        // Background Grid lines
        val segmentHeight = chartHeight / 10f
        paint.color = trackColor
        paint.alpha = 76
        paint.strokeWidth = 1f * density
        for (i in 0..10) {
            val y = chartHeight - (i * segmentHeight)
            canvas.drawLine(0f, y, widthPx.toFloat(), y, paint)
        }

        val standardBarWidthDp = 7f
        val spacingDp = 3f

        val barWidthPx = standardBarWidthDp * density
        val spacingPx = spacingDp * density

        // 1. Calculate how many bars physically fit inside chartWidth
        val maxFitBars = ((chartWidth + spacingPx) / (barWidthPx + spacingPx)).toInt()
            .coerceIn(1, data.size)

        // 2. Slice data to show the most recent visible days
        val visibleData = data.takeLast(maxFitBars)

        // 3. Recalculate exact barWidth in PX to fit width without fractional pixel gaps
        val totalSpacingPx = spacingPx * (visibleData.size - 1)
        //val adjustedBarWidthPx = ((chartWidth - totalSpacingPx) / visibleData.size).coerceAtLeast(1f)

        val cornerRadiusPx = 4f * density
        val maxQuantity = visibleData.maxOfOrNull { it.completedQuantity } ?: 0
        val yMax = maxOf(maxQuantity, target, 1)

        val targetBarTop = chartHeight - (chartHeight * (target.toFloat() / yMax).coerceIn(0f, 1f))

        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8.5f * density
            color = labelTextColor
            textAlign = Paint.Align.CENTER
        }

        val totalVisible = visibleData.size
        visibleData.forEachIndexed { index, item ->
            // Calculate exact LocalDate for this bar assuming visibleData ends on today
            val daysBeforeToday = (totalVisible - 1 - index).toLong()
            val itemDate = today.minusDays(daysBeforeToday)

            val isFutureItem = itemDate.isAfter(today)
            val barColorInt = when {
                isFutureItem || item.isSkipDay -> skipColor
                item.completedQuantity < target -> errorColor
                else -> successColor
            }

            val xOffset = horizontalPadding + (index * (barWidthPx + spacingPx))
            val barHeight = (chartHeight * (item.completedQuantity.toFloat() / yMax).coerceIn(0f, 1f)).coerceAtLeast(4f * density)

            // Track Background
            paint.color = trackColor
            paint.alpha = 255
            paint.style = Paint.Style.FILL
            val trackRect = RectF(xOffset, 0f, xOffset + barWidthPx, chartHeight)
            canvas.drawRoundRect(trackRect, cornerRadiusPx, cornerRadiusPx, paint)

            // Active Bar
            paint.color = barColorInt
            val barRect = RectF(xOffset, chartHeight - barHeight, xOffset + barWidthPx, chartHeight)
            canvas.drawRoundRect(barRect, cornerRadiusPx, cornerRadiusPx, paint)

            // X-Axis Date Labels Logic
            val isFirstDayOfMonth = itemDate.dayOfMonth == 1
            val isEvery5Days = itemDate.dayOfMonth % 5 == 0

            if (isFirstDayOfMonth || isEvery5Days) {
                val labelText = if (isFirstDayOfMonth) {
                    // Displays short month name (e.g., "Sep", "Oct") on month turnover
                    itemDate.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                } else {
                    itemDate.dayOfMonth.toString()
                }

                val textX = xOffset + (barWidthPx / 2f)
                val textY = chartHeight + 11f * density
                canvas.drawText(labelText, textX, textY, labelPaint)
            }
        }

        // Target Line (Dashed)
        paint.color = targetLineColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * density
        paint.pathEffect = DashPathEffect(floatArrayOf(4f * density, 4f * density), 0f)
        canvas.drawLine(0f, targetBarTop, widthPx.toFloat(), targetBarTop, paint)

        return bitmap
    }
}

