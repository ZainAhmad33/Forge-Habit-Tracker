package com.example.forge.core.database.interfaces

import com.example.forge.core.database.entity.HabitCategory
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    fun getCategories(): Flow<List<HabitCategory>>
}
