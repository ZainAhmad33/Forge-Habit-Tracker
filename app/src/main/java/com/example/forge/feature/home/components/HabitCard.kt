package com.example.forge.feature.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.component.CustomShapeProgress
import com.example.forge.core.designsystem.theme.ForgeTheme
import com.example.forge.core.uiEntities.HomeHabit
import com.example.forge.core.uiEntities.ProgressShape
import com.example.forge.core.uiEntities.ProgressShapeAngleResolver
import com.example.forge.core.uiEntities.ProgressShapeEnumResolver

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitCard(
    habit: HomeHabit,
    modifier: Modifier = Modifier,
    onDetailsClick: () -> Unit = {},

    onHabitCardClick: (habit: HomeHabit) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy, // less floaty than HighBouncy
            stiffness = Spring.StiffnessLow              // ~1500, reaches target fast
        ),
        label = "habit_card_scale"
    )

    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val progressShape = ProgressShapeEnumResolver[habit.progressShape] ?: MaterialShapes.Cookie12Sided.toShape()
    val shapeAngle = ProgressShapeAngleResolver[habit.progressShape] ?: 0f
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHabitCardClick(habit)
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp, 16.dp, 0.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // 1. Centered Icon with Status Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                CustomShapeProgress(
                    habit.progressPercent/100f,
                    shape = progressShape,
                    startAngle = shapeAngle,
                    label = habit.image,
                    strokeWidth = 7.dp,
                    showCheckMark = true
                )
            }

            // 2. Centered Title & Schedule
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = habit.targetLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            // 3. Horizontal Divider
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 4. Bottom Actions: Details Link (Left) & Streak Pill (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Details Link
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDetailsClick()
                    },
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                // Streak Pill
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = habit.streakDays.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
private data class HabitAccentColors(
    val container: Color,
    val content: Color,
)


@Preview(showBackground = true)
@Composable
private fun HabitCardPreview() {
    ForgeTheme {
        HabitCard(
            habit = HomeHabit(
                id = "1",
                title = "Drink Water",
                category = HabitCategory.Work, // Adjust based on your enum values
                targetLabel = "Every Day",
                streakDays = 12,
                progressPercent = 100,
                isCompletedToday = false,
                image = "💧",
                progressShape = ProgressShape.Puffy,
                habitType = HabitType.YesNo,
                isScheduledForToday = true,
                quantityLoggedToday = 2500
            ),
            onDetailsClick = {  },
            onHabitCardClick = {  }
        )
    }
}


