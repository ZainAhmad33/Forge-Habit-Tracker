package com.example.forge.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.CategoryShare

@Composable
fun CategoryDonutChart(
    shares: List<CategoryShare>,
    modifier: Modifier = Modifier
) {
    if (shares.size < 2) return
    
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer
    )
    
    Column(modifier = modifier) {
        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                val total = shares.sumOf { it.habitCount.toDouble() }.toFloat()
                
                shares.forEachIndexed { index, share ->
                    val sweepAngle = (share.habitCount / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 30.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                shares.forEachIndexed { index, share ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], CircleShape))
                        Text(
                            text = "${share.category.name}: ${share.habitCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryDonutChartPreview() {
    val sampleShares = listOf(
        CategoryShare(HabitCategory.Health, 0.8f, 3),
        CategoryShare(HabitCategory.Work, 0.6f, 2),
        CategoryShare(HabitCategory.Home, 0.4f, 1)
    )
    ForgeTheme {
        CategoryDonutChart(shares = sampleShares, modifier = Modifier.padding(16.dp))
    }
}
