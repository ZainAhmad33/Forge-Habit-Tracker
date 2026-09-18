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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.DailyCompletion
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
    if (months.isEmpty()) return

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
        val currentMonthData = allData[selectedMonth] ?: emptyList()
        val totalCompletion = currentMonthData.filter { it.completedQuantity >= target }.size
        val effectiveDays = currentMonthData.count { !it.isSkipDay }
        val locale = LocalConfiguration.current.locales[0]
        val monthName = selectedMonth.month.getDisplayName(TextStyle.FULL, locale)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$monthName's completion",
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

            Text(
                text = "$totalCompletion/$effectiveDays days on goal",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Legends (Static)
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

                com.example.forge.core.designsystem.component.ShowcaseHintOverlay(
                    hintKey = "hint_details_bar_chart_scroll",
                    message = "Swipe on the chart to navigate between different months!"
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.Top,
                        pageSpacing = 16.dp
                    ) { page ->
                        val month = months.getOrNull(page)
                        val monthData = allData[month] ?: emptyList()

                        MonthlyCompletionChart(
                            data = monthData,
                            target = target,
                            yearMonth = month ?: YearMonth.from(today),
                            today = today,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
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
        modifier = modifier
            .semantics { contentDescription = "Monthly completion bar chart" }
    ) {
        val canvasWidth = size.width
        val xAxisHeight = 24.dp.toPx()
        val chartHeight = size.height - xAxisHeight

        // Horizontal padding to prevent clipping of first/last bars and labels
        val horizontalPadding = 8.dp.toPx()
        val chartWidth = canvasWidth - (horizontalPadding * 2)

        // Background Grid
        val segmentHeight = chartHeight / 10f
        for (i in 0..10) {
            val y = chartHeight - (i * segmentHeight)
            drawLine(
                color = trackColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
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
            val xOffset = horizontalPadding + (index * (barWidth + spacingPx))
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
            end = Offset(canvasWidth, targetBarTop),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
        )
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
        val today = LocalDate.of(2026, 9, 2)
        val aug2026 = YearMonth.of(2026, 8)
        val sept2026 = YearMonth.of(2026, 9)
        
        var selectedMonth by remember { mutableStateOf(sept2026) }
        
        val months = listOf(aug2026, sept2026)
        val allData = months.associateWith { month ->
            (1..month.lengthOfMonth()).map { day ->
                DailyCompletion(day, Random.nextInt(1500, 3000), Random.nextInt(0, 10) > 8)
            }
        }
        
        MonthlyCompletionPager(
            allData = allData,
            months = months,
            selectedMonth = selectedMonth,
            onMonthChanged = { selectedMonth = it },
            target = 2500,
            today = today,
            modifier = Modifier.padding(16.dp)
        )
    }
}
