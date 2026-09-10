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
                val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
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

                    val heatmapData by statsService
                        .getRangeActivityData(
                            habitId,
                            YearMonth.from(today.minusMonths(6)),
                            14 // Request 14 weeks instead of 7
                        )
                        .collectAsState(initial = emptyList())

                    if (habit != null) {
                        HeatmapWidgetContent(
                            habit = habit!!,
                            heatmapData = heatmapData
                        )
                    } else {
                        EmptyWidgetContent()
                    }
                }
            }
        }
    }

    @Composable
    internal fun EmptyWidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tap to configure",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp)
            )
        }
    }

    @Composable
    internal fun HeatmapWidgetContent(
        habit: Habit,
        heatmapData: List<ActivityData>
    ) {
        val today = LocalDate.now()

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                // Uses standard Material 3 dynamic widget background color
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
            }

            Spacer(GlanceModifier.height(10.dp))

            if (habit.isLocked) {
                LockedHabitContent()
            } else {
                val size = LocalSize.current
                val dayLabelWidth = 30.dp
                val spacing = 2.dp

                // Select weeks visible based on widget width
                val calculatedWeeks = when {
                    size.width >= 320.dp -> 14
                    size.width >= 240.dp -> 10
                    else -> 7
                }

                // Calculate square size dynamically based on available width
                val horizontalPadding = 24.dp
                val availableWidth = size.width - horizontalPadding
                val squareSize = ((availableWidth - dayLabelWidth - (spacing * (calculatedWeeks - 1))) / calculatedWeeks)
                    .coerceAtLeast(10.dp)

                val firstVisibleMonday = today
                    .minusWeeks((calculatedWeeks - 1).toLong())
                    .minusDays((today.dayOfWeek.value - 1).toLong())

                val habitCreatedAt = habit.createdAt
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.Top
                ) {
                    GlanceActivityCalendar(
                        startDate = firstVisibleMonday,
                        weeksCount = calculatedWeeks,
                        activities = heatmapData,
                        today = today,
                        habitCreatedAt = habitCreatedAt,
                        squareSize = squareSize,
                        spacing = spacing,
                        modifier = GlanceModifier.fillMaxWidth()
                    )

                    Spacer(GlanceModifier.defaultWeight())

                    GlanceHeatmapLegend()
                }
            }
        }
    }

    @Composable
    private fun LockedHabitContent() {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_lock),
                    contentDescription = "Locked",
                    modifier = GlanceModifier.size(32.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = "Habit Locked",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private fun getMonthLabel(weekStart: LocalDate, weekIndex: Int): String? {
        for (dayIndex in 0..6) {
            val date = weekStart.plusDays(dayIndex.toLong())
            if (date.dayOfMonth == 1 || (weekIndex == 0 && dayIndex == 0)) {
                return date.month.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    java.util.Locale.getDefault()
                )
            }
        }
        return null
    }

    @Composable
    private fun GlanceActivityCalendar(
        startDate: LocalDate,
        weeksCount: Int,
        activities: List<ActivityData>,
        today: LocalDate,
        habitCreatedAt: LocalDate,
        squareSize: Dp,
        spacing: Dp,
        modifier: GlanceModifier = GlanceModifier
    ) {
        val activityMap = remember(activities) {
            activities.associateBy { it.date }
        }

        val labelStyle = TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            fontSize = 10.sp
        )

        val dayNumberStyle = TextStyle(
            fontSize = (squareSize.value * 0.45f).sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Day Labels
            Column(
                modifier = GlanceModifier
                    .width(30.dp)
                    .padding(top = 20.dp)
            ) {
                DAY_LABELS.forEach { day ->
                    Box(
                        modifier = GlanceModifier.height(squareSize + spacing),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = day,
                            style = labelStyle
                        )
                    }
                }
            }

            // Weeks Grid
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                for (weekIndex in 0 until weeksCount) {
                    val weekStart = startDate.plusWeeks(weekIndex.toLong())

                    Column(
                        modifier = GlanceModifier.width(squareSize + spacing)
                    ) {
                        val monthLabel = getMonthLabel(weekStart = weekStart, weekIndex = weekIndex)

                        Box(
                            modifier = GlanceModifier
                                .height(20.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (monthLabel != null) {
                                Text(
                                    text = monthLabel,
                                    style = labelStyle
                                )
                            }
                        }

                        for (dayIndex in 0..6) {
                            val date = weekStart.plusDays(dayIndex.toLong())

                            ActivityDayCell(
                                date = date,
                                activity = activityMap[date],
                                today = today,
                                habitCreatedAt = habitCreatedAt,
                                squareSize = squareSize,
                                spacing = spacing,
                                dayNumberStyle = dayNumberStyle
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ActivityDayCell(
        date: LocalDate,
        activity: ActivityData?,
        today: LocalDate,
        habitCreatedAt: LocalDate,
        squareSize: Dp,
        spacing: Dp,
        dayNumberStyle: TextStyle
    ) {
        val context = LocalContext.current
        val isFuture = date.isAfter(today)
        val isBeforeCreation = date.isBefore(habitCreatedAt)

        // Outer grid cell bounds (includes right and bottom spacing)
        if (isFuture || isBeforeCreation) {
            Spacer(
                modifier = GlanceModifier.size(
                    width = squareSize + spacing,
                    height = squareSize + spacing
                )
            )
            return
        }

        val hasActivity = activity != null && activity.percentage > 0

        val cellBackgroundColorProvider = if (hasActivity) {
            GlanceTheme.colors.primary
        } else {
            GlanceTheme.colors.surfaceVariant
        }

        val cellTextColorProvider = if (hasActivity) {
            GlanceTheme.colors.onPrimary
        } else {
            GlanceTheme.colors.onSurfaceVariant
        }

        val radius = (squareSize.value * 0.15f).coerceAtLeast(2f).dp

        // Outer container reserves total cell footprint + spacing
        Box(
            modifier = GlanceModifier
                .size(width = squareSize + spacing, height = squareSize + spacing)
                .padding(end = spacing, bottom = spacing),
            contentAlignment = Alignment.Center
        ) {
            // Inner actual square box
            Box(
                modifier = GlanceModifier
                    .size(squareSize)
                    .cornerRadius(radius)
                    .background(cellBackgroundColorProvider),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = dayNumberStyle.copy(
                        color = cellTextColorProvider
                    )
                )
            }
        }
    }

    @Composable
    private fun GlanceHeatmapLegend() {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
            horizontalAlignment = Alignment.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less ",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp)
            )
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(2.dp)
            ) {}
            Spacer(GlanceModifier.width(2.dp))
            repeat(4) {
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(2.dp)
                ) {}
                Spacer(GlanceModifier.width(2.dp))
            }
            Text(
                text = "More",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp)
            )
        }
    }
}

class NavigateToHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitHeatmapWidget.habitIdParam]
        if (habitId != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("habit_id", habitId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}