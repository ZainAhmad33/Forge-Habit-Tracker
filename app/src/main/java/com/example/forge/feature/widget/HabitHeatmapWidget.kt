package com.example.forge.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.uiEntities.ActivityData
import com.example.forge.core.uiEntities.ProgressShape
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Date
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
                val habitId = remember(habitIdString) { habitIdString?.let { UUID.fromString(it) } }

                if (habitId == null) {
                    EmptyWidgetContent()
                } else {
                    val habit by habitsService.getHabitFlow(habitId).collectAsState(initial = null)
                    val today = LocalDate.now()
                    val sixMonthsAgo = today.minusMonths(6)
                    val heatmapData by statsService.getRangeActivityData(
                        habitId,
                        YearMonth.from(sixMonthsAgo),
                        7
                    ).collectAsState(initial = emptyList())

                    if (habit != null) {
                        HeatmapWidgetContent(habit!!, heatmapData, today)
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
        heatmapData: List<ActivityData>,
        today: LocalDate
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(
                    actionRunCallback<NavigateToHabitAction>(
                        actionParametersOf(habitIdParam to habit.id.toString())
                    )
                )
        ) {
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
                    maxLines = 1
                )
            }
            
            Spacer(GlanceModifier.height(8.dp))

            if (habit.isLocked) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            } else {
                val firstMonday = remember(today) {
                    val sixMonthsAgo = today.minusMonths(6)
                    sixMonthsAgo.minusDays((sixMonthsAgo.dayOfWeek.value - 1).toLong())
                }
                
                val thisMonday = remember(today) {
                    today.minusDays((today.dayOfWeek.value - 1).toLong())
                }

                val totalWeeks = remember(firstMonday, thisMonday) {
                    ChronoUnit.WEEKS.between(firstMonday, thisMonday).toInt() + 1
                }

                val activityMap = remember(heatmapData) {
                    heatmapData.associateBy { it.date }
                }

                val weeksList = (0 until totalWeeks).toList().reversed()

                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(weeksList) { w ->
                        val weekStartDate = firstMonday.plusWeeks(w.toLong())
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (d in 0 until 7) {
                                val date = weekStartDate.plusDays(d.toLong())
                                val activity = activityMap[date]
                                
                                val provider = if (activity != null && activity.percentage > 0) {
                                    // Use primary color with alpha for intensity
                                    // Glance doesn't have a direct way to apply alpha to a ColorProvider easily in background()
                                    // so we use the intensity to pick an alpha level on the primary color.
                                    val alpha = activity.intensity.coerceAtLeast(0.3f)
                                    fixedColorProvider(
                                        GlanceTheme.colors.primary.getColor(LocalContext.current).copy(alpha = alpha)
                                    )
                                } else {
                                    GlanceTheme.colors.surfaceVariant
                                }
                                
                                Box(
                                    modifier = GlanceModifier
                                        .size(16.dp)
                                        .padding(1.dp)
                                        .background(provider)
                                ) {}
                            }
                        }
                    }
                }
            }
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

