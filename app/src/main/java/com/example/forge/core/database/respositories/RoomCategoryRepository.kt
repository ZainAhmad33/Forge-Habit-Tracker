package com.example.forge.core.database.respositories

import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.interfaces.ICategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryRepository @Inject constructor(
    private val habitDao: HabitDao
) : ICategoryRepository {
    override fun getCategories(): Flow<List<HabitCategory>> {
        return habitDao.getHabits().map { habits ->
            habits.map { it.category }.distinct()
        }
    }
}
