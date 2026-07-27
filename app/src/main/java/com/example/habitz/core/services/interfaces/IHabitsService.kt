package com.example.habitz.core.services.interfaces

import com.example.habitz.core.uiEntities.CategoryPill

interface IHabitsService {
    fun getAllowedCategories(): List<CategoryPill>
}
