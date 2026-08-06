package com.example.forge.core.database.entity

enum class HabitType(val label: String, val subLabel: String) {
    YesNo("Yes / No", "Once a day"),
    Quantity("Quantity", "e.g. 3 L water"),
    Count("Count", "e.g. Pray 5x")
}