package com.example.forge.feature.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.WeeklyPerformance

@Composable
fun WeeklyPerformanceChart(
    performance: WeeklyPerformance,
    modifier: Modifier = Modifier
) {
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
    val primary = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        Text(
            text = "Weekly Performance",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dayNames.forEachIndexed { index, name ->
                val rate = performance.dayRates[index] ?: 0f
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .fillMaxHeight(fraction = rate.coerceAtLeast(0.05f))
                            .background(
                                color = if (rate == (performance.dayRates[performance.bestDay] ?: -1f)) primary else primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Text(text = name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        val bestDayName = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[performance.bestDay]
        val worstDayName = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[performance.worstDay]
        
        Card(
            modifier = Modifier.padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", fontSize = 16.sp)
                Text(
                    text = "You're most consistent on $bestDayName and tend to skip $worstDayName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyPerformanceChartPreview() {
    val samplePerformance = WeeklyPerformance(
        dayRates = mapOf(0 to 0.8f, 1 to 0.9f, 2 to 0.7f, 3 to 0.6f, 4 to 0.4f, 5 to 0.5f, 6 to 0.3f),
        bestDay = 1,
        worstDay = 6
    )
    ForgeTheme {
        WeeklyPerformanceChart(performance = samplePerformance, modifier = Modifier.padding(16.dp))
    }
}
