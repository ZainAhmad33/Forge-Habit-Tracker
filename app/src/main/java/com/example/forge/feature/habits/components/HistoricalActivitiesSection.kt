package com.example.forge.feature.habits.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.component.ActivityMonthlyPager
import com.example.forge.core.uiEntities.ActivityData
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HistoricalActivitiesSection(
    startDate: LocalDate,
    currentMonth: YearMonth,
    monthlyActivities: List<ActivityData>,
    today: LocalDate,
    onMonthChanged: (YearMonth) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Historical activity",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight(700)
        )

        ActivityMonthlyPager(
            startDate = startDate,
            currentMonth = currentMonth,
            monthlyActivities = monthlyActivities,
            onMonthChanged = onMonthChanged,
            today = today
        )
    }
}
