package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.HabitCategory

interface CategoryRepository {
    fun getCategories(): List<HabitCategory>
}