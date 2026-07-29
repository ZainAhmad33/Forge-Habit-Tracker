package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.MonthlyRate

@Composable
fun QuarterlyProgressCards(rates: List<MonthlyRate>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rates.forEach { rate ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = rate.monthName, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { rate.rate },
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "${(rate.rate * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuarterlyProgressCardsPreview() {
    HabitzTheme {
        val rates = listOf(
            MonthlyRate("May", 0.75f),
            MonthlyRate("Jun", 0.90f),
            MonthlyRate("Jul", 0.60f)
        )
        QuarterlyProgressCards(rates = rates)
    }
}
