package com.example.forge.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.forge.core.services.interfaces.IInsightsService
import com.example.forge.core.services.interfaces.InsightPeriod
import com.example.forge.core.services.interfaces.LeaderboardEntry
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.UUID

class HabitLeaderboardWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun insightsService(): IInsightsService
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val insightsService = entryPoint.insightsService()

        provideContent {
            GlanceTheme {
                val leaderboard by insightsService.getHabitLeaderboard(InsightPeriod.AllTime)
                    .collectAsState(initial = emptyList())

                if (leaderboard.isEmpty()) {
                    EmptyWidgetContent()
                } else {
                    LeaderboardWidgetContent(leaderboard)
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val sampleEntries = listOf(
            LeaderboardEntry(UUID.randomUUID(), "Water", "💧", 0.9f),
            LeaderboardEntry(UUID.randomUUID(), "Gym", "🏋️", 0.75f),
            LeaderboardEntry(UUID.randomUUID(), "Read", "📚", 0.6f),
            LeaderboardEntry(UUID.randomUUID(), "Meditation", "🧘", 0.5f),
            LeaderboardEntry(UUID.randomUUID(), "Sleep", "😴", 0.4f)
        )

        provideContent {
            GlanceTheme {
                LeaderboardWidgetContent(sampleEntries)
            }
        }
    }

    @Composable
    internal fun LeaderboardWidgetContent(entries: List<LeaderboardEntry>) {
        val size = LocalSize.current
        
        // Calculate how many entries can fit
        // Title: ~24dp
        // Padding: 24dp (top + bottom)
        // Entry: ~44dp each
        val availableHeight = size.height.value - 24 - 24
        val maxEntries = (availableHeight / 44).toInt().coerceAtLeast(1)
        val displayedEntries = entries.take(maxEntries)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.widgetBackground)
        ) {
            Text(
                text = "Habit Leaderboard",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(8.dp))

            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                displayedEntries.forEachIndexed { index, entry ->
                    LeaderboardItem(entry)
                    if (index < displayedEntries.size - 1) {
                        Spacer(GlanceModifier.height(12.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun LeaderboardItem(entry: LeaderboardEntry) {
        val size = LocalSize.current
        val horizontalPadding = 24.dp // 12dp * 2
        val availableWidth = size.width - horizontalPadding
        
        val isSmallWidth = size.width < 200.dp
        val titleFontSize = if (isSmallWidth) 12.sp else 14.sp
        val percentageFontSize = if (isSmallWidth) 10.sp else 12.sp

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.emoji, 
                    style = TextStyle(fontSize = if (isSmallWidth) 12.sp else 14.sp)
                )
                Spacer(GlanceModifier.width(if (isSmallWidth) 4.dp else 8.dp))
                Text(
                    text = entry.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = "${(entry.completionRate * 100).toInt()}%",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = percentageFontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(GlanceModifier.height(4.dp))
            
            // Progress bar
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(3.dp)
            ) {
                if (entry.completionRate > 0f) {
                    Box(
                        modifier = GlanceModifier
                            .width(availableWidth * entry.completionRate.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(GlanceTheme.colors.primary)
                            .cornerRadius(3.dp)
                    ) {}
                }
            }
        }
    }
}
