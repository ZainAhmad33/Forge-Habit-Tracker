package com.example.forge.feature.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.forge.core.services.interfaces.IWidgetUpdater
import com.example.forge.feature.habits.widget.CompletionBarChartWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlanceWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) : IWidgetUpdater {
    override suspend fun updateAllWidgets() {
        CompletionBarChartWidget().updateAll(context)
        HabitHeatmapWidget().updateAll(context)
    }
}
