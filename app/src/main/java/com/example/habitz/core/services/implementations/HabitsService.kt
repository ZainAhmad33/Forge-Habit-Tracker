package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.CategoryToImage
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.services.interfaces.IHabitsService
import com.example.habitz.core.uiEntities.CategoryPill
import javax.inject.Inject

class HabitsService @Inject constructor() : IHabitsService {
    override fun getAllowedCategories(): List<CategoryPill> {
        return HabitCategory.entries
            .filter { it != HabitCategory.All }
            .map { CategoryPill(it, CategoryToImage[it] ?: "❓") }
    }
}
