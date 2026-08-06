package com.example.forge.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.uiEntities.HomeSummary

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeSummaryCard(
    summary: HomeSummary,
    modifier: Modifier = Modifier,
) {
    val progress = summary.completedCount.toFloat() / summary.totalCount.toFloat()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                Text(
                    text = "${summary.weeklyCompletionPercent}% Complete Today",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight(700),
                    modifier = Modifier.padding(0.dp, 8.dp)
                )
                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    stroke = Stroke(17f),
                    amplitude = { 1f }
                )
            }
            Column (verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${summary.completedCount} of ${summary.totalCount} habits completed",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "🔥 ${summary.currentStreakDays} Perfect Day Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
@Preview
fun previewHomeSummaryCard(){
    HomeSummaryCard(HomeSummary(4, 7, 34, 57))
}
