package com.example.habitz.feature.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.Reward
import com.example.habitz.core.designsystem.theme.HabitzTheme

@Composable
fun RewardsSection(rewards: List<Reward>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(rewards) { reward ->
            RewardCard(reward)
        }
    }
}

@Composable
fun RewardCard(reward: Reward) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isUnlocked) MaterialTheme.colorScheme.secondaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = reward.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = reward.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${reward.requiredStreak}d",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardsSectionPreview() {
    HabitzTheme {
        val rewards = listOf(
            Reward(title = "Starter", description = "3 day streak", emoji = "🥉", requiredStreak = 3, isUnlocked = true),
            Reward(title = "Consistent", description = "7 day streak", emoji = "🥈", requiredStreak = 7, isUnlocked = true),
            Reward(title = "Dedicated", description = "15 day streak", emoji = "🥇", requiredStreak = 15, isUnlocked = false),
            Reward(title = "Master", description = "30 day streak", emoji = "💎", requiredStreak = 30, isUnlocked = false)
        )
        RewardsSection(rewards = rewards)
    }
}
