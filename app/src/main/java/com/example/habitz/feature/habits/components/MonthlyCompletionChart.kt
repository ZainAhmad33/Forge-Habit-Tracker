package com.example.habitz.feature.habits.components

// Foundation & Layout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

// Material 3 Components & Theme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Compose Geometry & Graphics (For Canvas drawing)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

// Compose Core & Accessibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.DailyCompletion
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

@Composable
fun CurrentMonthCompletion(
    data: List<DailyCompletion>,
    target: Int,
    unit: String,
    totalDays: Int,
    modifier: Modifier = Modifier
){
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column{
                val currentMonthFull = LocalDate.now()
                    .month
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                Text(
                    text = "${currentMonthFull}'s completion",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight(700)
                )
                Text(
                    text = "Your daily completion vs ${target} ${unit} goal.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            val totalCompletion = data.filter { it.completedQuantity >= target }.size
            Text(
                text = "${totalCompletion}/${totalDays} days on goal.",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        MonthlyCompletionChart(data, target)
    }
}

@Composable
fun MonthlyCompletionChart(
    data: List<DailyCompletion>,
    target: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp), // Increased height for legends and axis
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                
                // Legends
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = HabitzTheme.colors.success, label = "On Goal")
                    LegendItem(color = MaterialTheme.colorScheme.error, label = "Below Goal")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pre-calculate scale metrics
            val maxQuantity = data.maxOfOrNull { it.completedQuantity } ?: 0
            val yMax = maxOf(maxQuantity, target, 1)

            val successColor = HabitzTheme.colors.success
            val errorColor = MaterialTheme.colorScheme.error
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            val targetLineColor = MaterialTheme.colorScheme.outline
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            
            val textMeasurer = rememberTextMeasurer()
            val labelStyle = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = onSurfaceVariant
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { contentDescription = "Monthly completion bar chart with target line and date labels" }
            ) {
                val chartWidth = size.width
                val xAxisHeight = 24.dp.toPx()
                val chartHeight = size.height - xAxisHeight

                // 1. Draw Background Grid
                val segmentHeight = chartHeight / 10f
                for (i in 0..10) {
                    val y = chartHeight - (i * segmentHeight)
                    drawLine(
                        color = trackColor.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Bar Dimensions
                val totalBars = data.size
                if (totalBars == 0) return@Canvas

                val spacingPx = 4.dp.toPx()
                val totalSpacingPx = spacingPx * (totalBars - 1)
                val barWidth = ((chartWidth - totalSpacingPx) / totalBars).coerceAtLeast(1f)
                val cornerRadiusPx = 6.dp.toPx()

                // 3. Draw Bars and X-Axis Labels
                val lastDay: Int = YearMonth.now().lengthOfMonth()
                val markerDays = listOf(1, 5, 10, 15, 20, 25, lastDay).distinct()
                
                data.forEachIndexed { index, item ->
                    val isBelowGoal = item.completedQuantity < target
                    val barColor = if (isBelowGoal) errorColor else successColor
                    val xOffset = index * (barWidth + spacingPx)

                    val ratio = (item.completedQuantity.toFloat() / yMax).coerceIn(0f, 1f)
                    val barHeight = (chartHeight * ratio).coerceAtLeast(4.dp.toPx())
                    val barTop = chartHeight - barHeight

                    // Track
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(xOffset, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )

                    // Active Bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(xOffset, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                    
                    // X-Axis Marker
                    if (item.day in markerDays) {
                        val textLayoutResult = textMeasurer.measure(
                            text = item.day.toString(),
                            style = labelStyle
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = xOffset + (barWidth / 2) - (textLayoutResult.size.width / 2),
                                y = chartHeight + 4.dp.toPx()
                            )
                        )
                    }
                }

                // 4. Target Line and Goal Marker
                val targetRatio = (target.toFloat() / yMax).coerceIn(0f, 1f)
                val targetY = chartHeight - (chartHeight * targetRatio)

                drawLine(
                    color = targetLineColor,
                    start = Offset(0f, targetY),
                    end = Offset(chartWidth, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(12.dp.toPx(), 8.dp.toPx()),
                        phase = 0f
                    )
                )
                
                // Goal Label
                val goalText = "Goal: $target"
                val goalLayoutResult = textMeasurer.measure(
                    text = goalText,
                    style = labelStyle.copy(fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = goalLayoutResult,
                    topLeft = Offset(
                        x = chartWidth - goalLayoutResult.size.width - 4.dp.toPx(),
                        y = targetY - goalLayoutResult.size.height - 6.dp.toPx()
                    )
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(24.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentMonthCompletionPreview() {
    HabitzTheme {
        val lastDay: Int = YearMonth.now().lengthOfMonth()
        val target = 2500
        val data = (1..lastDay).map { day ->
            DailyCompletion(
                day = day,
                completedQuantity = Random.nextInt(from = 2400, until = 2701),
            )
        }
        CurrentMonthCompletion(data = data, target, "ML", 31)
    }
}
