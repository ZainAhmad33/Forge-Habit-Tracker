package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie12Sided
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.designsystem.component.CustomShapeProgress
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.core.services.interfaces.MonthlyRate
import com.example.habitz.core.uiEntities.ProgressShape
import com.example.habitz.core.uiEntities.ProgressShapeEnumResolver

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuarterlyProgressCards(rates: List<MonthlyRate>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rates.forEach { rate ->
            Card(
                modifier = Modifier
                    .weight(1f, fill = false) // fill = false lets it be smaller than available weight space
                    .widthIn(max = 120.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
//                    modifier = Modifier
//                        .padding(0.dp, 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.size(8.dp))
                    Text(text = rate.monthName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight(700))
                    val shape =  ProgressShapeEnumResolver[ProgressShape.getRandom()] ?: Cookie12Sided.toShape()
                    val successColor = HabitzTheme.colors.success
                    val errorColor = MaterialTheme.colorScheme.error
                    val color = if (rate.rate >= 0.90f) successColor else errorColor
                    CustomShapeProgress(
                        progress = rate.rate,
                        shape = shape,
                        progressColor = color,
                        strokeWidth = 8.dp
                    )
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
            MonthlyRate("May", 0.96f),
            MonthlyRate("June", 0.90f),
//            MonthlyRate("July", 0.60f)
        )
        QuarterlyProgressCards(rates = rates)
    }
}
