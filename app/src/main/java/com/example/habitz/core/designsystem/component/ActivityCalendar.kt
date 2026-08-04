package com.example.habitz.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.uiEntities.ActivityData
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
    yearMonths: List<YearMonth>,
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

    val successColor = HabitzTheme.colors.success
    val emptyColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val activityMap = remember(activities) {
        activities.associateBy { it.date }
    }

    val startDate = remember(yearMonths) {
        val firstMonth = yearMonths.firstOrNull() ?: YearMonth.now()
        val firstDay = firstMonth.atDay(1)
        firstDay.minusDays(firstDay.dayOfWeek.value.toLong() % 7)
    }

    val endDate = remember(yearMonths, maxDate) {
        val lastMonth = yearMonths.lastOrNull() ?: YearMonth.now()
        val lastDayOfMonth = lastMonth.atEndOfMonth()
        val end = if (maxDate != null && maxDate.isBefore(lastDayOfMonth)) {
            maxDate
        } else {
            lastDayOfMonth
        }
        end.plusDays(6 - (end.dayOfWeek.value.toLong() % 7))
    }

    val totalWeeks = remember(startDate, endDate) {
        (ChronoUnit.DAYS.between(startDate, endDate) / 7).toInt() + 1
    }

    val locale = LocalConfiguration.current.locales[0]

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val dayLabelWidthDp = if (showLabels) 30.dp else 0.dp
        val spacingDp = 2.dp
        
        // Use a fixed number of weeks for size calculation to ensure consistent height across all pages.
        // 4 months usually span 18-20 weeks depending on start/end days.
        val referenceWeeks = 14
        val squareSizeDp = (availableWidth - dayLabelWidthDp - (spacingDp * (referenceWeeks - 1))) / referenceWeeks
        
        val monthLabelHeightDp = 20.dp
        // Fixed height based on the constant squareSize
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
                val daysOfWeek = listOf("Mon", "Wed", "Fri")
                daysOfWeek.forEachIndexed { index, day ->
                    val textLayoutResult = textMeasurer.measure(day, style = labelStyle)
                    val yOffset = monthLabelHeight + (index * 2 + 1) * (squareSize + spacing) + (squareSize / 2) - (textLayoutResult.size.height / 2)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(0f, yOffset)
                    )
                }
            }

            var currentX = dayLabelWidth

            for (w in 0 until totalWeeks) {
                val weekStartDate = startDate.plusWeeks(w.toLong())

                for (d in 0 until 7) {
                    val date = weekStartDate.plusDays(d.toLong())
                    if (date.dayOfMonth == 1 && yearMonths.contains(YearMonth.from(date))) {
                        val monthName = date.month.getDisplayName(TextStyle.SHORT, locale)
                        val monthTextLayoutResult = textMeasurer.measure(monthName, style = labelStyle)
                        drawText(
                            textLayoutResult = monthTextLayoutResult,
                            topLeft = Offset(currentX, 0f),
                        )
                        break
                    }
                }

                for (d in 0 until 7) {
                    val date = weekStartDate.plusDays(d.toLong())

                    if (yearMonths.none { YearMonth.from(date) == it }) continue
                    if (maxDate != null && date.isAfter(maxDate)) continue
                    if (minDate != null && date.isBefore(minDate)) continue

                    val activity = activityMap[date]
                    val color = if (activity != null && activity.percentage > 0) {
                        successColor.copy(alpha = activity.intensity.coerceAtLeast(0.15f))
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
 * A paginated monthly activity calendar component.
 */
@Composable
fun ActivityMonthlyPager(
    startDate: LocalDate,
    currentMonth: YearMonth,
    monthlyActivities: List<ActivityData>,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    monthsPerPage: Int = 3
) {
    val today = remember { LocalDate.now() }
    val todayMonth = remember { YearMonth.now() }
    
    val totalMonths = remember(startDate, todayMonth) {
        ChronoUnit.MONTHS.between(
            startDate.withDayOfMonth(1),
            todayMonth.atDay(1)
        ).toInt() + 1
    }
    
    val pageCount = remember(totalMonths, monthsPerPage) {
        (totalMonths + monthsPerPage - 1) / monthsPerPage
    }
    
    val initialPage = remember(pageCount) { (pageCount - 1).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }

    LaunchedEffect(pagerState.currentPage) {
        // We want the last page to end at todayMonth
        val offsetFromEnd = (pageCount - 1) - pagerState.currentPage
        val selectedMonth = todayMonth.minusMonths((offsetFromEnd * monthsPerPage).toLong())
        onMonthChanged(selectedMonth)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            pageSpacing = 0.dp, // No spacing for continuous look
            pageSize = PageSize.Fill // Fill width, we'll pass multiple months to one ActivityCalendar
        ) { page ->
            val offsetFromEnd = (pageCount - 1) - page
            val endMonth = todayMonth.minusMonths((offsetFromEnd * monthsPerPage).toLong())
            val yearMonthsToShow = (0 until monthsPerPage).map {
                endMonth.minusMonths((monthsPerPage - 1 - it).toLong())
            }

            // We highlight the 'currentMonth' in the grid.
            // Ideally, we'd fetch data for all visible months.
            // For now, we filter activities to match the requested range.
            val displayActivities = monthlyActivities.filter { activity ->
                yearMonthsToShow.any { YearMonth.from(activity.date) == it }
            }

            ActivityCalendar(
                yearMonths = yearMonthsToShow,
                activities = displayActivities,
                modifier = Modifier.fillMaxWidth(),
                showLabels = true,
                minDate = startDate,
                maxDate = today
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityMonthlyPagerPreview() {
    HabitzTheme {
        val currentMonth = YearMonth.now()
        val monthsToGenerate = 3
        val dummyData = (0 until monthsToGenerate).flatMap { m ->
            val month = currentMonth.minusMonths(m.toLong())
            (1..month.lengthOfMonth()).map { day ->
                ActivityData(
                    date = month.atDay(day),
                    percentage = (0..100).random()
                )
            }
        }
        ActivityMonthlyPager(
            startDate = currentMonth.minusMonths(6).atDay(1),
            currentMonth = currentMonth,
            monthlyActivities = dummyData,
            onMonthChanged = {},
            modifier = Modifier.padding(16.dp),
            monthsPerPage = monthsToGenerate
        )
    }
}
