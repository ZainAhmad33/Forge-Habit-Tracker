package com.example.forge.core.designsystem.component

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

object ShowcaseManager {
    var currentActiveKey by mutableStateOf<String?>(null)
}

@Composable
fun ShowcaseHintOverlay(
    hintKey: String,
    message: String,
    modifier: Modifier = Modifier,
    dependsOnKey: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("forge_onboarding_hints", Context.MODE_PRIVATE) }
    
    var hasSeenThis by remember { mutableStateOf(true) }
    var satisfiesDependency by remember { mutableStateOf(false) }

    LaunchedEffect(hintKey, dependsOnKey) {
        hasSeenThis = prefs.getBoolean(hintKey, false)
        satisfiesDependency = dependsOnKey == null || prefs.getBoolean(dependsOnKey, false)
    }

    val isEligible = !hasSeenThis && satisfiesDependency

    LaunchedEffect(isEligible, ShowcaseManager.currentActiveKey, hintKey) {
        if (isEligible && ShowcaseManager.currentActiveKey == null) {
            ShowcaseManager.currentActiveKey = hintKey
        }
    }

    DisposableEffect(hintKey) {
        onDispose {
            if (ShowcaseManager.currentActiveKey == hintKey) {
                ShowcaseManager.currentActiveKey = null
            }
        }
    }

    val shouldShow = isEligible && ShowcaseManager.currentActiveKey == hintKey
    var targetBounds by remember { mutableStateOf<Rect?>(null) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Box(modifier = modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onGloballyPositioned { coordinates ->
            targetBounds = coordinates.boundsInWindow()
        }
    ) {
        content()
    }

    if (shouldShow && targetBounds != null) {
        ShowcaseHintPopup(
            targetBounds = targetBounds!!,
            message = message,
            onDismiss = {
                prefs.edit().putBoolean(hintKey, true).apply()
                hasSeenThis = true
                if (ShowcaseManager.currentActiveKey == hintKey) {
                    ShowcaseManager.currentActiveKey = null
                }
            }
        )
    }
}

@Composable
internal fun ShowcaseHintPopupContent(
    targetBounds: Rect,
    message: String,
    onDismiss: () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val arrowColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Invisible Clickable Dismiss Layer (No Dimming)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )

        // 2. Hint Card and arrow positioned next to the component
        Layout(
            content = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Got it", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Arrow Pointing Up (used when card is BELOW target)
                Canvas(modifier = Modifier.size(12.dp, 6.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path = path, color = arrowColor)
                }

                // Arrow Pointing Down (used when card is ABOVE target)
                Canvas(modifier = Modifier.size(12.dp, 6.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(path = path, color = arrowColor)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { measurables, constraints ->
            val cardPlaceable = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val arrowUpPlaceable = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val arrowDownPlaceable = measurables[2].measure(constraints.copy(minWidth = 0, minHeight = 0))
            
            val spaceBelow = constraints.maxHeight - targetBounds.bottom
            val spaceAbove = targetBounds.top
            val spacing = with(density) { 8.dp.toPx() }
            
            val cardX = ((constraints.maxWidth - cardPlaceable.width) / 2).coerceIn(0, constraints.maxWidth - cardPlaceable.width)
            
            val isPlacedBelow = spaceBelow >= cardPlaceable.height + spacing || spaceBelow > spaceAbove
            val cardY = if (isPlacedBelow) {
                (targetBounds.bottom + spacing).toInt().coerceIn(0, constraints.maxHeight - cardPlaceable.height)
            } else {
                (targetBounds.top - cardPlaceable.height - spacing).toInt().coerceIn(0, constraints.maxHeight - cardPlaceable.height)
            }
            
            val arrowX = (targetBounds.center.x - arrowUpPlaceable.width / 2f).toInt()
                .coerceIn(cardX + 24, cardX + cardPlaceable.width - arrowUpPlaceable.width - 24)
            
            layout(constraints.maxWidth, constraints.maxHeight) {
                cardPlaceable.placeRelative(cardX, cardY)
                if (isPlacedBelow) {
                    val arrowY = cardY - arrowUpPlaceable.height
                    arrowUpPlaceable.placeRelative(arrowX, arrowY)
                } else {
                    val arrowY = cardY + cardPlaceable.height
                    arrowDownPlaceable.placeRelative(arrowX, arrowY)
                }
            }
        }
    }
}

@Composable
internal fun ShowcaseHintPopup(
    targetBounds: Rect,
    message: String,
    onDismiss: () -> Unit
) {
    val positionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: androidx.compose.ui.unit.IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                return IntOffset(-anchorBounds.left, -anchorBounds.top)
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = false)
    ) {
        ShowcaseHintPopupContent(targetBounds, message, onDismiss)
    }
}



@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ShowcaseHintOverlayPreview() {
    com.example.forge.core.designsystem.theme.ForgeTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Habit List", style = MaterialTheme.typography.headlineMedium)
                repeat(3) {
                    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Habit Item $it")
                        }
                    }
                }
            }

            // Simulated popup content (Punch hole over the first item)
            ShowcaseHintPopupContent(
                targetBounds = Rect(Offset(42f, 179f), Size(996f, 263f)),
                message = "Tap the card anywhere to log your progress for today! 💧",
                onDismiss = {}
            )
        }
    }
}

