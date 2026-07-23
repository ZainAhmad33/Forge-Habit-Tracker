package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.datastore.dummyDatabase

class InMemoryCategoryRepository: ICategoryRepository {
    override fun getCategories(): List<HabitCategory> {
        var categories = dummyDatabase.habitsById.values.map {
            it.category
        }
        return categories
    }
}