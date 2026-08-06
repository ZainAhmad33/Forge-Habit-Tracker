package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.HabitCategory
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    fun getCategories(): Flow<List<HabitCategory>>
}
