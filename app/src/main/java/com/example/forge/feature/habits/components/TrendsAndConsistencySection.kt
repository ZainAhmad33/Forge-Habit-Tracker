package com.example.forge.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.BestWeekData
import com.example.forge.core.services.interfaces.GapData
import com.example.forge.core.services.interfaces.HabitTrends
import com.example.forge.core.services.interfaces.TrendData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TrendsAndConsistencySection(trends: HabitTrends?) {
    if (trends == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "Trend & consistency",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TrendItem(
                label = "This week",
                subLabel = "vs. last",
                value = "${(trends.weeklyTrend.currentRate * 100).toInt()}%",
                change = trends.weeklyTrend.changePercentage
            )
            TrendItem(
                label = "This month",
                subLabel = "vs. last",
                value = "${(trends.monthlyTrend.currentRate * 100).toInt()}%",
                change = trends.monthlyTrend.changePercentage
            )
            GapItem(
                label = "Longest gap",
                days = trends.longestGap.days,
                startDate = trends.longestGap.startDate?.format(DateTimeFormatter.ofPattern("MMM d")),
                endDate = trends.longestGap.endDate?.format(DateTimeFormatter.ofPattern("d"))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InsightCard(trends = trends)
    }
}

@Composable
private fun TrendItem(
    label: String,
    subLabel: String,
    value: String,
    change: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            TrendArrow(change = change)
            Text(
                text = "${Math.abs(change)}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (change >= 0) ForgeTheme.colors.success else MaterialTheme.colorScheme.error
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrendArrow(change: Int) {
    val icon = if (change >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val color = if (change >= 0) ForgeTheme.colors.success else MaterialTheme.colorScheme.error
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = color
    )
}

@Composable
private fun GapItem(
    label: String,
    days: Int,
    startDate: String?,
    endDate: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$days days",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$startDate–$endDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InsightCard(trends: HabitTrends) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            val bestWeekRange = "${trends.bestWeek.startDate.format(DateTimeFormatter.ofPattern("MMM d"))}–${trends.bestWeek.endDate.format(DateTimeFormatter.ofPattern("d"))}"
            val weeklyRate = (trends.weeklyTrend.currentRate * 100).toInt()
            val avgRate = (trends.allTimeAverage * 100).toInt()
            val bestRate = (trends.bestWeek.rate * 100).toInt()
            
            val comparison = if (weeklyRate >= avgRate) "above" else "below"
            
            Text(
                text = "This week's rate ($weeklyRate%) is $comparison your all-time average of $avgRate%, and your best week ever was $bestRate% ($bestWeekRange).",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrendsAndConsistencyPreview() {
    ForgeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TrendsAndConsistencySection(
                trends = HabitTrends(
                    weeklyTrend = TrendData(0.91f, 0.81f, 12),
                    monthlyTrend = TrendData(0.85f, 0.79f, 7),
                    longestGap = GapData(4, LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 5)),
                    allTimeAverage = 0.84f,
                    bestWeek = BestWeekData(1.0f, LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14))
                )
            )
        }
    }
}
