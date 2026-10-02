package com.example.forge.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class CompletionLineChartWidget : GlanceAppWidget() {
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
                val habitIdString = prefs[habitIdKey]
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
                    val currentYearMonth = YearMonth.now()

                    val monthlyCompletions by statsService
                        .getAllMonthlyCompletion(habitId)
                        .collectAsState(initial = emptyMap())

                    val sortedMonths = monthlyCompletions.entries
                        .filter { !it.key.isAfter(currentYearMonth) }
                        .sortedBy { it.key }

                    val totalDailyCompletions = if (sortedMonths.isEmpty()) {
                        emptyList()
                    } else {
                        sortedMonths.flatMap { (yearMonth, completions) ->
                            if (yearMonth == currentYearMonth) {
                                completions.filter { it.day <= today.dayOfMonth }
                            } else {
                                completions
                            }
                        }
                    }

                    if (habit != null) {
                        val streakCount by produceState(initialValue = 0, key1 = habitId) {
                            value = statsService.getStreakInfo(habitId).count
                        }
                        CompletionLineChartWidgetContent(
                            habit = habit!!,
                            streak = streakCount,
                            monthData = totalDailyCompletions,
                            target = habit!!.completionTargetPerDay,
                            today = today
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
        val currentMonth = YearMonth.now()
        val lastMonth = currentMonth.minusMonths(1)
        val twoMonthsAgo = currentMonth.minusMonths(2)
        val sixMonthsAgoDate = java.util.Date.from(
            today.minusMonths(6)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
        )

        val sampleHabit = Habit(
            id = UUID.randomUUID(),
            title = "Daily Reading",
            category = com.example.forge.core.database.entity.HabitCategory.Productivity,
            emoji = "📖",
            habitType = com.example.forge.core.database.entity.HabitType.YesNo,
            frequencyType = com.example.forge.core.database.entity.HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 2500,
            targetUnit = "Pages",
            progressShape = com.example.forge.core.uiEntities.ProgressShape.Circle,
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
            DailyCompletion(
                day = day,
                completedQuantity = if (isSkip) 0 else 3000,
                isSkipDay = isSkip
            )
        }

        provideContent {
            GlanceTheme {
                CompletionLineChartWidgetContent(
                    habit = sampleHabit,
                    streak = 15,
                    monthData = mockMonthData,
                    target = 2500,
                    today = today
                )
            }
        }
    }

    @Composable
    internal fun CompletionLineChartWidgetContent(
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
                        actionParametersOf(habitIdParam to habit.id.toString())
                    )
                )
        ) {
            // Unified Header (Emoji, Title, and Streak Pill) matching the bar graph widget
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
                        provider = ImageProvider(R.drawable.ic_local_fire_department),
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
            if (habit.isLocked) {
                LockedHabitContent()
            } else {
                // Chart Container Card
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(GlanceTheme.colors.widgetBackground)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        val size = LocalSize.current

                        if (size.width >= 250.dp) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                WidgetLegendItem(
                                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    label = "On Goal"
                                )
                                Spacer(GlanceModifier.width(12.dp))
                                WidgetLegendItem(
                                    color = GlanceTheme.colors.error.getColor(context),
                                    label = "Below Goal"
                                )
                                Spacer(GlanceModifier.width(12.dp))
                                WidgetLegendItem(
                                    color = GlanceTheme.colors.outline.getColor(context),
                                    label = "Skip Day"
                                )
                            }
                            Spacer(GlanceModifier.height(8.dp))
                        }

                        val density = context.resources.displayMetrics.density

                        val widthPx = ((size.width.value - 24f - 24f) * density).toInt().coerceAtLeast(100)
                        val heightPx = ((size.height.value - 32f - 24f - 20f) * density).toInt().coerceAtLeast(80)

                        val errorProvider = GlanceTheme.colors.error
                        val outlineProvider = GlanceTheme.colors.outline
                        val surfaceVariantProvider = GlanceTheme.colors.surfaceVariant
                        val onSurfaceVariantProvider = GlanceTheme.colors.onSurfaceVariant

                        val successColor = android.graphics.Color.parseColor("#4CAF50")

                        val errorColor = remember(errorProvider, context) {
                            runCatching { errorProvider.getColor(context).toArgb() }.getOrDefault(
                                android.graphics.Color.RED
                            )
                        }
                        val skipColor = remember(outlineProvider, context) {
                            runCatching { outlineProvider.getColor(context).toArgb() }.getOrDefault(
                                android.graphics.Color.GRAY
                            )
                        }
                        val trackColor = remember(surfaceVariantProvider, context) {
                            runCatching { surfaceVariantProvider.getColor(context).toArgb() }.getOrDefault(
                                android.graphics.Color.LTGRAY
                            )
                        }
                        val targetLineColor = remember(outlineProvider, context) {
                            runCatching { outlineProvider.getColor(context).toArgb() }.getOrDefault(
                                android.graphics.Color.DKGRAY
                            )
                        }
                        val labelTextColor = remember(onSurfaceVariantProvider, context) {
                            runCatching { onSurfaceVariantProvider.getColor(context).toArgb() }.getOrDefault(
                                android.graphics.Color.BLACK
                            )
                        }

                        val chartBitmap = remember(monthData, target, today, widthPx, heightPx) {
                            runCatching {
                                renderCompletionLineChartBitmap(
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
                                contentDescription = "Monthly completion line chart",
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                            )
                        }
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
            ) {}
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

    private fun renderCompletionLineChartBitmap(
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

        // Vertical padding so top and bottom nodes/lines are never clipped
        val topPaddingPx = 8f * density
        val bottomPaddingPx = 4f * density
        val availableChartHeight = (chartHeight - topPaddingPx - bottomPaddingPx).coerceAtLeast(1f)

        // Subtle background grid lines
        val segmentHeight = availableChartHeight / 10f
        paint.color = trackColor
        paint.alpha = 76
        paint.strokeWidth = 1f * density
        paint.style = Paint.Style.STROKE
        for (i in 0..10) {
            val y = topPaddingPx + availableChartHeight - (i * segmentHeight)
            canvas.drawLine(0f, y, widthPx.toFloat(), y, paint)
        }

        val minPointSpacingDp = 10f
        val minPointSpacingPx = minPointSpacingDp * density
        val maxFitPoints = ((chartWidth / minPointSpacingPx) + 1).toInt().coerceAtLeast(2)

        val visibleData = if (data.size >= maxFitPoints) {
            data.takeLast(maxFitPoints)
        } else {
            val paddingCount = maxFitPoints - data.size
            val padding = List(paddingCount) {
                DailyCompletion(
                    day = 0,
                    completedQuantity = 0,
                    isSkipDay = true
                )
            }
            padding + data
        }

        val numPoints = visibleData.size
        val stepX = if (numPoints > 1) chartWidth / (numPoints - 1) else 0f
        fun getX(index: Int): Float = horizontalPadding + (index * stepX)

        val maxQuantity = visibleData.maxOfOrNull { it.completedQuantity } ?: 0
        val yMax = maxOf(maxQuantity, target, 1)

        fun getY(quantity: Int): Float {
            val ratio = (quantity.toFloat() / yMax).coerceIn(0f, 1f)
            return topPaddingPx + availableChartHeight - (availableChartHeight * ratio)
        }

        val targetY = getY(target)

        // 1. Draw Fill and Line Stroke Segment by Segment (Green = On Goal, Red = Below Goal, Grey = Skip Day)
        for (i in 0 until numPoints - 1) {
            val item1 = visibleData[i]
            val item2 = visibleData[i + 1]

            val daysBeforeToday2 = (numPoints - 1 - (i + 1)).toLong()
            val itemDate2 = today.minusDays(daysBeforeToday2)
            val isFutureItem2 = itemDate2.isAfter(today)

            val segmentColor = when {
                item2.isSkipDay || isFutureItem2 -> skipColor
                item2.completedQuantity >= target -> successColor
                else -> errorColor
            }

            val x1 = getX(i)
            val y1 = getY(item1.completedQuantity)
            val x2 = getX(i + 1)
            val y2 = getY(item2.completedQuantity)

            // Fill Polygon under segment i -> i+1
            val segmentPath = Path().apply {
                moveTo(x1, chartHeight)
                lineTo(x1, y1)
                lineTo(x2, y2)
                lineTo(x2, chartHeight)
                close()
            }

            paint.style = Paint.Style.FILL
            val topAlpha = (0.35f * 255).toInt()
            val bottomAlpha = (0.05f * 255).toInt()
            val minY = minOf(y1, y2)
            val fillGradient = LinearGradient(
                0f, minY, 0f, chartHeight,
                intColorWithAlpha(segmentColor, topAlpha),
                intColorWithAlpha(segmentColor, bottomAlpha),
                Shader.TileMode.CLAMP
            )
            paint.shader = fillGradient
            canvas.drawPath(segmentPath, paint)
            paint.shader = null

            // Line Stroke for segment i -> i+1
            paint.style = Paint.Style.STROKE
            paint.color = segmentColor
            paint.strokeWidth = 2.5f * density
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // 2. Target / Goal Line (Dashed)
        paint.color = targetLineColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * density
        paint.pathEffect = DashPathEffect(floatArrayOf(4f * density, 4f * density), 0f)
        canvas.drawLine(0f, targetY, widthPx.toFloat(), targetY, paint)
        paint.pathEffect = null

        // 3. Highlight ONLY the single node circle at the very end of the line (far right)
        if (numPoints > 0) {
            val lastIndex = numPoints - 1
            val lastItem = visibleData[lastIndex]
            val lastX = getX(lastIndex)
            val lastY = getY(lastItem.completedQuantity)

            val lastNodeColor = when {
                lastItem.isSkipDay -> skipColor
                lastItem.completedQuantity >= target -> successColor
                else -> errorColor
            }

            val nodeRadiusPx = 5f * density
            val nodeStrokePx = 2f * density

            paint.style = Paint.Style.FILL
            paint.color = lastNodeColor
            canvas.drawCircle(lastX, lastY, nodeRadiusPx, paint)

            paint.style = Paint.Style.FILL
            paint.color = trackColor
            canvas.drawCircle(lastX, lastY, nodeRadiusPx - nodeStrokePx, paint)
        }

        // 4. X-Axis Date Labels
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8.5f * density
            color = labelTextColor
            textAlign = Paint.Align.CENTER
        }

        visibleData.forEachIndexed { index, item ->
            val daysBeforeToday = (numPoints - 1 - index).toLong()
            val itemDate = today.minusDays(daysBeforeToday)

            val x = getX(index)

            val isFirstDayOfMonth = itemDate.dayOfMonth == 1
            val isEvery5Days = itemDate.dayOfMonth % 5 == 0

            if (isFirstDayOfMonth || isEvery5Days) {
                val labelText = if (isFirstDayOfMonth) {
                    itemDate.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                } else {
                    if (itemDate.dayOfMonth != 30 && itemDate.dayOfMonth != 31) {
                        itemDate.dayOfMonth.toString()
                    } else ""
                }

                if (labelText.isNotEmpty()) {
                    val textY = chartHeight + 11f * density
                    canvas.drawText(labelText, x, textY, labelPaint)
                }
            }
        }

        return bitmap
    }

    private fun intColorWithAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }
}
