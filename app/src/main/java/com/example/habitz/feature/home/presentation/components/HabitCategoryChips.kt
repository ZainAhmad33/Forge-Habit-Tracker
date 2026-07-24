package com.example.habitz.feature.home.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.uiEntities.CategoryPill

@Composable
fun HabitCategoryChips(
    categories: List<CategoryPill>,
    selectedCategory: HabitCategory,
    onCategorySelected: (HabitCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = HabitCategory.All == selectedCategory,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onCategorySelected(HabitCategory.All) },
            label = { Text(HabitCategory.All.label) },
            leadingIcon = { Icon(Icons.Rounded.Apps, contentDescription = "All categories icon") },
            shape = FilterChipDefaults.shape
        )
        categories.forEach { category ->
            FilterChip(
                selected = category.name == selectedCategory,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onCategorySelected(category.name) },
                label = { Text(category.name.label) },
                leadingIcon = { Text(category.image) }
            )
        }
    }
}
