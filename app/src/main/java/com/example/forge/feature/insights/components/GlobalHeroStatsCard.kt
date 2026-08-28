package com.example.forge.feature.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.GlobalStats

@Composable
fun GlobalHeroStatsCard(
    stats: GlobalStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // First Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Completion",
                    value = "${(stats.completionRate * 100).toInt()}%",
                    icon = Icons.Rounded.CheckCircle,
                    delta = stats.rateChange,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Current Streak",
                    value = "${stats.currentGlobalStreak}d",
                    icon = Icons.Rounded.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            // Second Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Perfect Days",
                    value = stats.perfectDaysCount.toString(),
                    icon = Icons.Rounded.Star,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Best Streak",
                    value = "${stats.bestGlobalStreak}d",
                    icon = Icons.Rounded.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    delta: Int? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Column {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (delta != null && delta != 0) {
                    val color = if (delta > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val sign = if (delta > 0) "▲" else "▼"
                    Text(
                        text = "$sign${kotlin.math.abs(delta)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GlobalHeroStatsCardPreview() {
    ForgeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlobalHeroStatsCard(
                stats = GlobalStats(
                    completionRate = 0.85f,
                    currentGlobalStreak = 12,
                    bestGlobalStreak = 24,
                    perfectDaysCount = 45,
                    rateChange = 5
                )
            )
        }
    }
}
