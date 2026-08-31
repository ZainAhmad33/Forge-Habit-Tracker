package com.example.forge.core.uiEntities

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a single metric for the HeroStatCard.
 */
data class HeroStatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val delta: Int? = null
)
