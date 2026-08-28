package com.example.forge.feature.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.component.shimmer
import com.example.forge.core.designsystem.theme.ForgeTheme

@Composable
fun InsightsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Stats Placeholder
        Column {
            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(200.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                // Interior dividers or structure can be added if needed, but a shimmering card is usually enough
                Box(modifier = Modifier.fillMaxSize().shimmer())
            }
        }

        // Multiple Chart Placeholders
        repeat(3) {
            Column {
                Box(modifier = Modifier.width(140.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(240.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .shimmer()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsightsScreenSkeletonPreview() {
    ForgeTheme {
        InsightsScreenSkeleton()
    }
}
