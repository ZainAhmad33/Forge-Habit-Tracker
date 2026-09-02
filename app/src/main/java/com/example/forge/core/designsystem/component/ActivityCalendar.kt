package com.example.forge.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.ActivityData
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * A GitHub-style activity calendar component.
 * Displays multiple months continuously with days of the week as rows.
 */
@Composable
fun ActivityCalendar(
    startDate: LocalDate, // Should be a Monday for best alignment
    weeksCount: Int,
    activities: List<ActivityData>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val activityMap = remember(activities) {
        activities.associateBy { it.date }
    }

    val locale = LocalConfiguration.current.locales[0]

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val dayLabelWidthDp = if (showLabels) 30.dp else 0.dp
        val spacingDp = 2.dp
        
        // Ensure squareSize is calculated to fit exactly weeksCount columns
        val squareSizeDp = (availableWidth - dayLabelWidthDp - (spacingDp * (weeksCount - 1))) / weeksCount
        
        val monthLabelHeightDp = 20.dp
        val totalHeightDp = monthLabelHeightDp + (squareSizeDp * 7) + (spacingDp * 6) + 8.dp

        val dayNumberStyle = MaterialTheme.typography.labelSmall.copy(
            fontSize = (squareSizeDp.value * 0.5f).sp,
            fontWeight = FontWeight.Bold
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp)
        ) {
            val squareSize = squareSizeDp.toPx()
            val spacing = spacingDp.toPx()
            val cornerRadius = (squareSize * 0.15f).coerceAtLeast(2.dp.toPx())
            val dayLabelWidth = dayLabelWidthDp.toPx()
            val monthLabelHeight = monthLabelHeightDp.toPx()

            if (showLabels) {
                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                daysOfWeek.forEachIndexed { index, day ->
                    val textLayoutResult = textMeasurer.measure(day, style = labelStyle)
                    val yOffset = monthLabelHeight + index * (squareSize + spacing) + (squareSize / 2) - (textLayoutResult.size.height / 2)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(0f, yOffset)
                    )
                }
            }

            var currentX = dayLabelWidth

            for (w in 0 until weeksCount) {
                val weekStartDate = startDate.plusWeeks(w.toLong())

                // Draw Month Label
                for (d in 0 until 7) {
                    val date = weekStartDate.plusDays(d.toLong())
                    // Draw month label if it's the 1st of the month, or if it's the very first column of the pager
                    if (date.dayOfMonth == 1 || (w == 0 && d == 0)) {
                        val monthName = date.month.getDisplayName(TextStyle.SHORT, locale)
                        val monthTextLayoutResult = textMeasurer.measure(monthName, style = labelStyle)
                        drawText(
                            textLayoutResult = monthTextLayoutResult,
                            topLeft = Offset(currentX, 0f),
                        )
                        break
                    }
                }

                // Draw Day Squares
                for (d in 0 until 7) {
                    val date = weekStartDate.plusDays(d.toLong())

                    if (maxDate != null && date.isAfter(maxDate)) continue
                    if (minDate != null && date.isBefore(minDate)) continue

                    val activity = activityMap[date]
                    val color = if (activity != null && activity.percentage > 0) {
                        activeColor.copy(alpha = activity.intensity.coerceAtLeast(0.15f))
                    } else {
                        emptyColor
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(currentX, monthLabelHeight + d * (squareSize + spacing)),
                        size = Size(squareSize, squareSize),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )

                    val dayNumber = date.dayOfMonth.toString()
                    val textColor = if (activity != null && activity.percentage > 0) {
                        if (activity.intensity > 0.5f) onPrimary else onSurface
                    } else {
                        onSurface.copy(alpha = 0.5f)
                    }
                    val dayTextLayoutResult = textMeasurer.measure(
                        text = dayNumber,
                        style = dayNumberStyle.copy(color = textColor)
                    )
                    drawText(
                        textLayoutResult = dayTextLayoutResult,
                        topLeft = Offset(
                            x = currentX + (squareSize / 2) - (dayTextLayoutResult.size.width / 2),
                            y = monthLabelHeight + d * (squareSize + spacing) + (squareSize / 2) - (dayTextLayoutResult.size.height / 2)
                        )
                    )
                }
                currentX += squareSize + spacing
            }
        }
    }
}

/**
 * A paginated weekly activity calendar component.
 */
@Composable
fun ActivityWeeklyPager(
    startDate: LocalDate,
    currentMonth: YearMonth,
    monthlyActivities: List<ActivityData>,
    onMonthChanged: (YearMonth) -> Unit,
    today: LocalDate,
    modifier: Modifier = Modifier,
    weeksPerPage: Int = 14,
    showLegend: Boolean = true
) {
    // Start of the very first week (Monday)
    val firstMonday = remember(startDate) {
        startDate.minusDays((startDate.dayOfWeek.value - 1).toLong())
    }
    
    // Start of the current week (Monday)
    val thisMonday = remember(today) {
        today.minusDays((today.dayOfWeek.value - 1).toLong())
    }

    val totalWeeks = remember(firstMonday, thisMonday) {
        ChronoUnit.WEEKS.between(firstMonday, thisMonday).toInt() + 1
    }
    
    val pageCount = remember(totalWeeks, weeksPerPage) {
        (totalWeeks + weeksPerPage - 1) / weeksPerPage
    }
    
    if (pageCount <= 0) return
    
    val initialPage = remember(pageCount) { (pageCount - 1).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }

    LaunchedEffect(pagerState.currentPage) {
        // The start of the current page's window
        val offsetFromEnd = (pageCount - 1) - pagerState.currentPage
        // Last page should end at 'thisMonday', so it starts at 'thisMonday - (weeksPerPage - 1)'
        val lastPageStart = thisMonday.minusWeeks((weeksPerPage - 1).toLong())
        val pageStartMonday = lastPageStart.minusWeeks((offsetFromEnd * weeksPerPage).toLong())
        
        // Find the month centered in this 14-week window
        val middleDate = pageStartMonday.plusWeeks((weeksPerPage / 2).toLong())
        onMonthChanged(YearMonth.from(middleDate))
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                pageSpacing = 0.dp,
                pageSize = PageSize.Fill
            ) { page ->
                val offsetFromEnd = (pageCount - 1) - page
                val lastPageStart = thisMonday.minusWeeks((weeksPerPage - 1).toLong())
                val pageStartMonday = lastPageStart.minusWeeks((offsetFromEnd * weeksPerPage).toLong())

                val pageActivities = remember(monthlyActivities, pageStartMonday) {
                    val pageEndSunday = pageStartMonday.plusWeeks(weeksPerPage.toLong()).minusDays(1)
                    monthlyActivities.filter { !it.date.isBefore(pageStartMonday) && !it.date.isAfter(pageEndSunday) }
                }

                ActivityCalendar(
                    startDate = pageStartMonday,
                    weeksCount = weeksPerPage,
                    activities = pageActivities,
                    modifier = Modifier.fillMaxWidth(),
                    showLabels = true,
                    minDate = startDate,
                    maxDate = today
                )
            }

            if (showLegend) {
                ActivityHeatmapLegend(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * A legend for the activity heatmap showing intensity levels.
 */
@Composable
fun ActivityHeatmapLegend(
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(emptyColor, shape = RoundedCornerShape(2.dp))
            )
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = activeColor.copy(alpha = (i + 1) * 0.25f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        
        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityWeeklyPagerPreview() {
    ForgeTheme {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        val weeksToGenerate = 14
        val dummyData = (0 until weeksToGenerate).flatMap { w ->
            val weekDate = today.minusWeeks(w.toLong())
            (0..6).map { day ->
                ActivityData(
                    date = weekDate.minusDays(day.toLong()),
                    percentage = (0..100).random()
                )
            }
        }
        ActivityWeeklyPager(
            startDate = today.minusMonths(6),
            currentMonth = currentMonth,
            monthlyActivities = dummyData,
            onMonthChanged = {},
            today = today,
            modifier = Modifier.padding(16.dp),
            weeksPerPage = weeksToGenerate
        )
    }
}
