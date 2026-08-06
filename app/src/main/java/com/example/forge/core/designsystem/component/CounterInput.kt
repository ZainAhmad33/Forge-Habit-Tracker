package com.example.forge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
public fun CounterInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueMin: Int = 0,
    valueMax: Int = 99999999
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(max(valueMin, value - 1))
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
            }

            BasicTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    if (newValue.isEmpty()) {
                        onValueChange(0)
                    } else {
                        newValue.toIntOrNull()?.let { onValueChange(it) }
                    }
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(min(valueMax, value + 1))
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase")
            }
        }
    }
}