package com.example.forge.feature.habits.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.DailyCompletion
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

@Composable
fun MonthlyCompletionPager(
    allData: Map<YearMonth, List<DailyCompletion>>,
    months: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    target: Int,
    today: LocalDate,
    modifier: Modifier = Modifier
){
    val pagerState = rememberPagerState(
        initialPage = months.indexOf(selectedMonth).coerceAtLeast(0),
        pageCount = { months.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        if (months.isNotEmpty()) {
            onMonthChanged(months[pagerState.currentPage])
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column{
                Text(
                    text = "Completion history",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight(700)
                )
                Text(
                    text = "Your daily completion vs daily goal",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Month Display
            if (months.size > 1) {
                Text(
                    text = selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            pageSpacing = 16.dp
        ) { page ->
            val month = months.getOrNull(page)
            val monthData = allData[month] ?: emptyList()
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val totalCompletion = monthData.filter { it.completedQuantity >= target }.size
                val effectiveDays = monthData.count { !it.isSkipDay }
                
                Text(
                    text = "$totalCompletion/$effectiveDays days on goal",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )

                MonthlyCompletionChart(
                    data = monthData,
                    target = target,
                    yearMonth = month ?: YearMonth.from(today),
                    today = today
                )
            }
        }
    }
}

@Composable
fun MonthlyCompletionChart(
    data: List<DailyCompletion>,
    target: Int,
    yearMonth: YearMonth,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legends
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = ForgeTheme.colors.success, label = "On Goal")
                    LegendItem(color = MaterialTheme.colorScheme.error, label = "Below Goal")
                    LegendItem(color = MaterialTheme.colorScheme.outlineVariant, label = "Skip Day")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxQuantity = data.maxOfOrNull { it.completedQuantity } ?: 0
            val yMax = maxOf(maxQuantity, target, 1)

            val successColor = ForgeTheme.colors.success
            val errorColor = MaterialTheme.colorScheme.error
            val skipColor = MaterialTheme.colorScheme.outlineVariant
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            val targetLineColor = MaterialTheme.colorScheme.outline
            
            val textMeasurer = rememberTextMeasurer()
            val labelStyle = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { contentDescription = "Monthly completion bar chart" }
            ) {
                val chartWidth = size.width
                val xAxisHeight = 24.dp.toPx()
                val chartHeight = size.height - xAxisHeight

                // Background Grid
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

                val totalBars = data.size
                if (totalBars == 0) return@Canvas

                val spacingPx = 4.dp.toPx()
                val totalSpacingPx = spacingPx * (totalBars - 1)
                val barWidth = ((chartWidth - totalSpacingPx) / totalBars).coerceAtLeast(1f)
                val cornerRadiusPx = 6.dp.toPx()

                val markerDays = listOf(1, 5, 10, 15, 20, 25, yearMonth.lengthOfMonth()).distinct()
                val targetBarTop = chartHeight - (chartHeight * (target.toFloat() / yMax).coerceIn(0f, 1f))

                data.forEachIndexed { index, item ->
                    val isFutureItem = yearMonth == YearMonth.from(today) && item.day > today.dayOfMonth
                    val barColor = when {
                        isFutureItem || item.isSkipDay -> skipColor
                        item.completedQuantity < target -> errorColor
                        else -> successColor
                    }
                    val xOffset = index * (barWidth + spacingPx)
                    val barHeight = (chartHeight * (item.completedQuantity.toFloat() / yMax).coerceIn(0f, 1f)).coerceAtLeast(4.dp.toPx())

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
                        topLeft = Offset(xOffset, chartHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                    
                    if (item.day in markerDays) {
                        val textLayoutResult = textMeasurer.measure(item.day.toString(), style = labelStyle)
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = xOffset + (barWidth / 2) - (textLayoutResult.size.width / 2),
                                y = chartHeight + 4.dp.toPx()
                            )
                        )
                    }
                }

                // Target Line
                drawLine(
                    color = targetLineColor,
                    start = Offset(0f, targetBarTop),
                    end = Offset(chartWidth, targetBarTop),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
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
fun MonthlyCompletionPagerPreview() {
    ForgeTheme {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        val data = (1..currentMonth.lengthOfMonth()).map { day ->
            DailyCompletion(day, Random.nextInt(1500, 3000), Random.nextInt(0, 10) > 8)
        }
        MonthlyCompletionPager(
            allData = mapOf(currentMonth to data),
            months = listOf(currentMonth),
            selectedMonth = currentMonth,
            onMonthChanged = {},
            target = 2500,
            today = today,
            modifier = Modifier.padding(16.dp)
        )
    }
}
