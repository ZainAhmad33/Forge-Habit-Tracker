package com.example.forge.core.uiEntities

import java.time.LocalDate

/**
 * Represents the intensity of activity for a specific day.
 */
enum class ActivityLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH;

    companion object {
        /**
         * Maps a completion percentage (0-100+) to an [ActivityLevel].
         */
        fun fromPercentage(percentage: Int): ActivityLevel {
            return when {
                percentage <= 0 -> NONE
                percentage <= 25 -> LOW
                percentage <= 50 -> MEDIUM
                percentage <= 75 -> HIGH
                else -> VERY_HIGH
            }
        }

        /**
         * Maps a completion ratio (0.0-1.0+) to an [ActivityLevel].
         */
        fun fromRatio(ratio: Float): ActivityLevel {
            return fromPercentage((ratio * 100).toInt())
        }
    }
}

/**
 * Data class representing activity for a single day.
 *
 * @property date The date of the activity.
 * @property percentage Completion percentage (0 to 100+).
 */
data class ActivityData(
    val date: LocalDate,
    val percentage: Int
) {
    /**
     * Returns the intensity level for this activity based on percentage.
     */
    val level: ActivityLevel get() = ActivityLevel.fromPercentage(percentage)

    /**
     * Returns the intensity ratio (0.0 to 1.0+) for this activity.
     */
    val intensity: Float get() = (percentage / 100f).coerceIn(0f, 1f)
}
