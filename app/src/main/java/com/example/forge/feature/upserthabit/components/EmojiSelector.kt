package com.example.forge.feature.upserthabit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forge.core.designsystem.theme.ForgeTheme
import dev.alexdametto.compose_emoji_picker.presentation.EmojiPicker
import dev.alexdametto.compose_emoji_picker.presentation.EmojiPickerDefaults

@Composable
fun EmojiPreviewCard(
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(100.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = 48.sp)
        }
    }
}

@Composable
fun EmojiSelectorBar(
    selectedEmoji: String,
    popularEmojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showEmojiPicker by remember{ mutableStateOf(false)}
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            popularEmojis.forEach { emoji ->
                EmojiItem(
                    emoji = emoji,
                    isSelected = emoji == selectedEmoji,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showEmojiPicker = false
                        onEmojiSelected(emoji)
                    }
                )
            }

            // "More" Grid button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showEmojiPicker = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = "More emojis",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        EmojiPickerKeyboard(
            isOpen = showEmojiPicker,
            onDismiss = {showEmojiPicker = false},
            onEmojiSelected = { emoji ->
                onEmojiSelected(emoji)
                showEmojiPicker = false
            }
        )
    }

}

@Composable
private fun EmojiItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

@Composable
fun EmojiPickerKeyboard(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    EmojiPicker(
        open = isOpen,
        onClose = onDismiss,
        colors = EmojiPickerDefaults.emojiPickerColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            searchBarBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
            searchBarIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            searchBarTextColor = MaterialTheme.colorScheme.onSurface,
            textColor = MaterialTheme.colorScheme.onSurface,
            activeCategoryTint = MaterialTheme.colorScheme.primary,
            inactiveCategoryTint = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onEmojiSelected = { emojiItem ->
            onEmojiSelected(emojiItem.emoji)
            onDismiss()
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun EmojiSelectorPreview() {
    ForgeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EmojiSelectorBar(
                selectedEmoji = "🧘",
                popularEmojis = listOf("💧", "🏃", "📖", "🧘", "🙏", "🍎", "😴"),
                onEmojiSelected = {},
                onMoreClick = {}
            )
        }
    }
}
