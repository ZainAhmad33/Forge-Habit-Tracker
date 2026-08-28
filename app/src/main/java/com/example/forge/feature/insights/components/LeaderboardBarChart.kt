package com.example.forge.feature.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.LeaderboardEntry
import java.util.UUID

@Composable
fun LeaderboardBarChart(
    entries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = "Habit leaderboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        entries.take(5).forEach { entry ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = entry.emoji, fontSize = 16.sp)
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${(entry.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = { entry.completionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardBarChartPreview() {
    val sampleEntries = listOf(
        LeaderboardEntry(UUID.randomUUID(), "Water", "💧", 0.9f),
        LeaderboardEntry(UUID.randomUUID(), "Gym", "🏋️", 0.75f),
        LeaderboardEntry(UUID.randomUUID(), "Read", "📚", 0.6f),
        LeaderboardEntry(UUID.randomUUID(), "Meditation", "🧘", 0.5f)
    )
    ForgeTheme {
        LeaderboardBarChart(
            entries = sampleEntries,
            modifier = Modifier.padding(16.dp),
            description = "Top performing habits by completion rate."
        )
    }
}
