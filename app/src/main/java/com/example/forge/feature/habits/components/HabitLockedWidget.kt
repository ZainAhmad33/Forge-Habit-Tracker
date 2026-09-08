package com.example.forge.feature.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.designsystem.theme.ForgeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitLockedWidget(
    lockedAt: LocalDate,
    today: LocalDate,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    var showInfoText by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val daysServed = currentStreak.coerceIn(0, 30)
    val daysRemaining = 30 - daysServed
    val unlockDate = today.plusDays(daysRemaining.toLong())
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habit Locked",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight(700)
            )
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showInfoText = !showInfoText
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = "Show info icon",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialShapes.Cookie12Sided.toShape(),
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.LockClock,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Locked until ${unlockDate.format(dateFormatter)}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight(700)
                        )
                        Text(
                            text = "$daysRemaining days remaining",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                
                Spacer(Modifier.size(1.dp))
                
                LinearWavyProgressIndicator(
                    progress = { daysServed / 30f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f),
                    stroke = Stroke(12f),
                    amplitude = { 1f }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$daysServed / 30 days served",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "$daysRemaining days to go",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                AnimatedVisibility(
                    visible = showInfoText,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "You missed your goal on ${lockedAt.format(dateFormatter)} with 0 skip days banked. Rebuild your streak to 30 days to unlock this habit.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitLockedWidgetPreview() {
    ForgeTheme {
        HabitLockedWidget(
            lockedAt = LocalDate.now().minusDays(12),
            today = LocalDate.now(),
            currentStreak = 15,
            modifier = Modifier.padding(16.dp)
        )
    }
}
