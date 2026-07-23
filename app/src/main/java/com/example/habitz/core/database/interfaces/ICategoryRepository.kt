package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.HabitCategory

interface ICategoryRepository {
    fun getCategories(): List<HabitCategory>
}