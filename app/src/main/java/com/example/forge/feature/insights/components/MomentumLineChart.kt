package com.example.forge.feature.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    
    val primary = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        Text(
            text = "Momentum",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)
            
            fun getX(index: Int) = index * stepX
            fun getY(value: Float) = height - (value * height)
            
            val path7 = Path().apply {
                moveTo(getX(0), getY(points[0].sevenDayRollingAvg))
                for (i in 1 until points.size) {
                    lineTo(getX(i), getY(points[i].sevenDayRollingAvg))
                }
            }
            
            drawPath(
                path = path7,
                color = primary,
                style = Stroke(width = 2.dp.toPx())
            )
            
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
                    colors = listOf(primary.copy(alpha = 0.3f), Color.Transparent)
                )
            )
            
            val lastPoint = points.last()
            drawCircle(
                color = primary,
                radius = 4.dp.toPx(),
                center = Offset(getX(points.size - 1), getY(lastPoint.sevenDayRollingAvg))
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = points.first().date.format(DateTimeFormatter.ofPattern("MMM d")), style = MaterialTheme.typography.labelSmall)
            Text(text = points.last().date.format(DateTimeFormatter.ofPattern("MMM d")), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MomentumLineChartPreview() {
    val samplePoints = (0..30).map { i ->
        MomentumPoint(LocalDate.now().minusDays(i.toLong()), (i % 10) / 10f, (i % 20) / 20f)
    }.reversed()
    ForgeTheme {
        MomentumLineChart(points = samplePoints, modifier = Modifier.padding(16.dp))
    }
}
