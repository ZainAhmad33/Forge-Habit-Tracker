package com.example.habitz.feature.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.DailyCompletion

@Composable
fun MonthlyCompletionChart(data: List<DailyCompletion>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Current Month", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { completion ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(completion.completionRatio.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(
                                if (completion.isBelowGoal) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthlyCompletionChartPreview() {
    HabitzTheme {
        val data = (1..30).map { day ->
            DailyCompletion(
                day = day,
                completionRatio = if (day % 3 == 0) 0.5f else 1.0f,
                isBelowGoal = day % 3 == 0
            )
        }
        MonthlyCompletionChart(data = data)
    }
}
