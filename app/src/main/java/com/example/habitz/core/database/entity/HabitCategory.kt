package com.example.habitz.core.database.entity

enum class HabitCategory(val label: String) {
    All("All"),
    Health("Health & Fitness"),
    Mindfulness("Mindfulness"),
    Work("Work"),
    Home("Home"),
    Nutrition("Nutrition"),
    Productivity("Productivity"),
    Learning("Learning"),
    Sleep("Sleep"),
    Finance("Finance"),
    Social("Social"),
    Creativity("Creativity"),
    Environment("Environment"),
    Spirituality("Spirituality"),
    DigitalWellbeing("Digital Wellbeing"),
}

val CategoryToImage = mapOf(
    HabitCategory.Health            to "🏃",
    HabitCategory.Nutrition         to "🥗",
    HabitCategory.Mindfulness       to "🧘",
    HabitCategory.Productivity      to "🎯",
    HabitCategory.Learning          to "📖",
    HabitCategory.Sleep             to "😴",
    HabitCategory.Finance           to "💰",
    HabitCategory.Social            to "👥",
    HabitCategory.Creativity        to "🎨",
    HabitCategory.Work              to "💼",
    HabitCategory.Home              to "🏠",
    HabitCategory.Environment       to "🌱",
    HabitCategory.Spirituality      to "🙏",
    HabitCategory.DigitalWellbeing  to "📵",
)