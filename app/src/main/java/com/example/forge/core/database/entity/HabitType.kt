package com.example.forge.core.database.entity

enum class HabitType(val label: String, val subLabel: String) {
    YesNo("Yes / No", "Go for a walk"),
    Quantity("Quantity", "e.g. Drink 3L water"),
    Count("Count", "e.g. Learn 10 words")
}