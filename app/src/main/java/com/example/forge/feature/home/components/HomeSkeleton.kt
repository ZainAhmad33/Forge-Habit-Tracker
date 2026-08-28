package com.example.forge.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.component.shimmer
import com.example.forge.core.designsystem.theme.ForgeTheme

@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 20.dp, vertical = 18.dp)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Header Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.width(120.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            Box(modifier = Modifier.width(180.dp).height(32.dp).clip(RoundedCornerShape(4.dp)).shimmer())
        }

        // Summary Card Skeleton
        HomeSummarySkeleton()

        // Category Chips Skeleton
        CategoryChipsSkeleton()

        // Section Header Skeleton
        Box(modifier = Modifier.width(140.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmer())

        // Habit Grid Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HabitCardSkeleton(modifier = Modifier.weight(1f))
                HabitCardSkeleton(modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HabitCardSkeleton(modifier = Modifier.weight(1f))
                HabitCardSkeleton(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HomeSummarySkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.width(150.dp).height(28.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).shimmer())
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(200.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Box(modifier = Modifier.width(160.dp).height(32.dp).clip(CircleShape).shimmer())
            }
        }
    }
}

@Composable
fun CategoryChipsSkeleton() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
        }
    }
}

@Composable
fun HabitCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Ring Placeholder
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).shimmer())
            
            // Title Placeholder
            Box(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            
            // Subtitle Placeholder
            Box(modifier = Modifier.width(60.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmer())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenSkeletonPreview() {
    ForgeTheme {
        HomeScreenSkeleton()
    }
}
