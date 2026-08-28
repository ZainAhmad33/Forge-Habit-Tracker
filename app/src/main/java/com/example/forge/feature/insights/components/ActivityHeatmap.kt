package com.example.forge.feature.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.services.interfaces.HeatmapCell
import java.time.LocalDate

@Composable
fun ActivityHeatmap(
    cells: List<HeatmapCell>,
    modifier: Modifier = Modifier
) {
    val weeks = cells.chunked(7)
    
    Column(modifier = modifier) {
        Text(
            text = "Activity",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(weeks) { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = getIntensityColor(cell.intensity),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Less", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(getIntensityColor(it), shape = RoundedCornerShape(1.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "More", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun getIntensityColor(intensity: Int): Color {
    val base = MaterialTheme.colorScheme.primary
    return when (intensity) {
        0 -> MaterialTheme.colorScheme.surfaceVariant
        1 -> base.copy(alpha = 0.2f)
        2 -> base.copy(alpha = 0.4f)
        3 -> base.copy(alpha = 0.7f)
        else -> base
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityHeatmapPreview() {
    val sampleCells = (0..30).map { i ->
        HeatmapCell(LocalDate.now().minusDays(i.toLong()), (0..4).random(), emptyList())
    }
    ForgeTheme {
        ActivityHeatmap(cells = sampleCells, modifier = Modifier.padding(16.dp))
    }
}
