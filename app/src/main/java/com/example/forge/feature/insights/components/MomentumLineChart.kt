package com.example.forge.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.MomentumPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MomentumLineChart(
    points: List<MomentumPoint>,
    totalDaysInMonth: Int,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    
    val primary = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            
            // Horizontal padding to prevent circle clipping at edges
            val horizontalPadding = 6.dp.toPx()
            val availableWidth = width - (horizontalPadding * 2)
            
            // Use totalDaysInMonth to ensure the x-axis scale is consistent for the whole month
            val stepX = availableWidth / (totalDaysInMonth - 1)
            
            fun getX(index: Int) = horizontalPadding + (index * stepX)
            fun getY(value: Float) = height - (value * height)
            
            // 1. Draw 30-day rolling avg (Baseline)
            val path30 = Path().apply {
                moveTo(getX(0), getY(points[0].thirtyDayRollingAvg))
                for (i in 1 until points.size) {
                    lineTo(getX(i), getY(points[i].thirtyDayRollingAvg))
                }
            }
            
            drawPath(
                path = path30,
                color = primary.copy(alpha = 0.5f), // Increased alpha from 0.3
                style = Stroke(
                    width = 2.dp.toPx(), // Increased width from 1.5
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            )
            
            // 2. Draw 7-day rolling avg (Current Trend)
            val path7 = Path().apply {
                moveTo(getX(0), getY(points[0].sevenDayRollingAvg))
                for (i in 1 until points.size) {
                    lineTo(getX(i), getY(points[i].sevenDayRollingAvg))
                }
            }
            
            drawPath(
                path = path7,
                color = primary,
                style = Stroke(width = 3.dp.toPx()) // Increased width from 2.5
            )
            
            // 3. Gradient fill for the 7-day trend
            val fillPath = Path().apply {
                moveTo(getX(0), height)
                lineTo(getX(0), getY(points[0].sevenDayRollingAvg))
                for (i in 1 until points.size) {
                    lineTo(getX(i), getY(points[i].sevenDayRollingAvg))
                }
                lineTo(getX(points.size - 1), height)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primary.copy(alpha = 0.2f), Color.Transparent)
                )
            )
            
            // 4. Highlight current point if it's the current month (we'll just show it always on the last point of the set)
            val lastPoint = points.last()
            drawCircle(
                color = primary,
                radius = 5.dp.toPx(),
                center = Offset(getX(points.size - 1), getY(lastPoint.sevenDayRollingAvg))
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = points.first().date.format(DateTimeFormatter.ofPattern("MMM d")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(color = primary, label = "7-day (Trend)", isDashed = false)
                LegendItem(color = primary.copy(alpha = 0.6f), label = "30-day (Baseline)", isDashed = true)
            }

            Text(
                text = points.last().date.format(DateTimeFormatter.ofPattern("MMM d")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, isDashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.size(width = 16.dp, height = 2.dp)) {
            drawLine(
                color = color,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                strokeWidth = size.height,
                pathEffect = if (isDashed) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f) else null
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun MomentumLineChartPreview() {
    val samplePoints = (0..30).map { i ->
        MomentumPoint(LocalDate.now().minusDays(i.toLong()), (i % 10) / 10f, (i % 20) / 20f)
    }.reversed()
    ForgeTheme {
        MomentumLineChart(
            points = samplePoints,
            totalDaysInMonth = 30,
            modifier = Modifier.padding(16.dp)
        )
    }
}
