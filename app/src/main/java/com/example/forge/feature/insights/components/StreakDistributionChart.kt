package com.example.forge.feature.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.StreakBucket

@Composable
fun StreakDistributionChart(
    buckets: List<StreakBucket>,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = "Streak distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            buckets.forEach { bucket ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = bucket.count.toString(), style = MaterialTheme.typography.labelMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((bucket.count * 20).dp.coerceAtLeast(4.dp))
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = bucket.label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StreakDistributionChartPreview() {
    val sampleBuckets = listOf(
        StreakBucket("0 days", 2),
        StreakBucket("1-6 days", 5),
        StreakBucket("7-29 days", 3),
        StreakBucket("30+ days", 1)
    )
    ForgeTheme {
        StreakDistributionChart(
            buckets = sampleBuckets,
            modifier = Modifier.padding(16.dp),
            description = "Current streak length buckets for all active habits."
        )
    }
}
