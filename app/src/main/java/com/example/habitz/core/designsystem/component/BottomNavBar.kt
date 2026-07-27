package com.example.habitz.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.StackedLineChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.HomeHabit
import com.example.habitz.feature.home.screen.HomeRoute
import com.example.habitz.feature.upserthabit.screen.NewHabitRoute

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNavBar(
    scrollBehavior: FloatingToolbarScrollBehavior,
    onAddHabitClick: () -> Unit
) {
    var selected by remember { mutableStateOf("Home") }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
            floatingActionButton = { CreateHabitFab(onClick = onAddHabitClick) },
            scrollBehavior = scrollBehavior,
            modifier = Modifier
                .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                .align(Alignment.BottomCenter),
        ) {
            SlidingTabRow(
                selectedTab = selected,
                onTabSelected = { selected = it }
            )
        }
    }
}

@Composable
private fun SlidingTabRow(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val tabs = listOf("Home", "Insights")
    val selectedIndex = tabs.indexOf(selectedTab)

    // Store widths and offsets for dynamic sliding measurement
    var tabWidths by remember { mutableStateOf(mapOf<Int, Dp>()) }
    var tabOffsets by remember { mutableStateOf(mapOf<Int, Dp>()) }

    val density = LocalDensity.current

    // Animate target offset and width of the background pill
    val animatedOffset by animateDpAsState(
        targetValue = tabOffsets[selectedIndex] ?: 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TabOffsetAnimation"
    )

    val animatedWidth by animateDpAsState(
        targetValue = tabWidths[selectedIndex] ?: 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TabWidthAnimation"
    )

    Box(contentAlignment = Alignment.CenterStart) {
        // 1. Sliding Pill Background Indicator
        if (animatedWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(animatedWidth)
                    .height(40.dp) // Standard button height match
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }

        // 2. Tab Buttons Stacked Horizontally
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == label

                // Text color animation
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "TextColorAnimation"
                )

                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(label) },
                    colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        with(density) {
                            tabWidths = tabWidths + (index to coordinates.size.width.toDp())
                            tabOffsets = tabOffsets + (index to coordinates.positionInParent().x.toDp())
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (label == "Home") Icons.Rounded.Home else Icons.Rounded.StackedLineChart,
                            contentDescription = label
                        )
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    if (selected) {
        // Highlighted state with a pill background container
        FilledTonalButton(
            onClick = onClick,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label)
                Text(text = label)
            }
        }
    } else {
        // Standard unselected transparent button
        TextButton(
            onClick = onClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label)
                Text(text = label)
            }
        }
    }
}
@Composable
fun CreateHabitFab(
    onClick: () -> Unit
){
    val haptic = LocalHapticFeedback.current
    FloatingActionButton(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }) {
        Icon(
            imageVector = Icons.Rounded.AddCircleOutline,
            contentDescription = "Create Habit Icon"
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun PreviewBottomNav(){
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )
    BottomNavBar(scrollBehavior, onAddHabitClick = {})
}