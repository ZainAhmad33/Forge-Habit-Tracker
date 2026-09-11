package com.example.forge.feature.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.ProgressShape
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class HabitWidgetConfigurationActivity : ComponentActivity() {

    @Inject
    lateinit var habitRepository: IHabitRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            ForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val habits by habitRepository.getHabits().collectAsState(initial = emptyList())
                    HabitSelectionScreen(
                        habits = habits,
                        onHabitSelected = { habitId ->
                            saveWidgetConfig(habitId)
                        }
                    )
                }
            }
        }
    }

    private fun saveWidgetConfig(habitId: UUID) {
        val context = this
        val glanceManager = GlanceAppWidgetManager(context)
        
        lifecycleScope.launch {
            val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[HabitHeatmapWidget.habitIdKey] = habitId.toString()
                }
            }
            HabitHeatmapWidget().update(context, glanceId)
            
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}

@Composable
fun HabitSelectionScreen(
    habits: List<Habit>,
    onHabitSelected: (UUID) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding() // Consumes status bar and navigation bar insets
        .padding(16.dp)) {
        Text(
            text = "Select a habit for the widget",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(habits) { habit ->
                ListItem(
                    headlineContent = { Text(habit.title) },
                    leadingContent = { Text(habit.emoji, style = MaterialTheme.typography.headlineSmall) },
                    modifier = Modifier.clickable { onHabitSelected(habit.id) }
                )
                HorizontalDivider()
            }
        }
        
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No habits found. Create one first!")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitSelectionScreenPreview() {
    val habits = listOf(
        Habit(
            id = UUID.randomUUID(),
            title = "Drink Water",
            category = HabitCategory.Health,
            emoji = "💧",
            habitType = HabitType.YesNo,
            reminders = emptyList(),
            frequencyType = HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 1,
            targetUnit = "Times",
            progressShape = ProgressShape.Circle,
            createdAt = Date(),
            updatedAt = Date()
        ),
        Habit(
            id = UUID.randomUUID(),
            title = "Read",
            category = HabitCategory.Learning,
            emoji = "📚",
            habitType = HabitType.Quantity,
            reminders = emptyList(),
            frequencyType = HabitFrequency.EveryDay,
            numberOfTrackedDays = 7,
            completionTargetPerDay = 1,
            targetUnit = "Pages",
            progressShape = ProgressShape.Circle,
            createdAt = Date(),
            updatedAt = Date()
        )
    )
    ForgeTheme {
        Surface {
            HabitSelectionScreen(habits = habits, onHabitSelected = {})
        }
    }
}
