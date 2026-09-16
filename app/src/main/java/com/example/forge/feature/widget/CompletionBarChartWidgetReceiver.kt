package com.example.forge.feature.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.forge.feature.habits.widget.CompletionBarChartWidget

class CompletionBarChartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompletionBarChartWidget()
}
