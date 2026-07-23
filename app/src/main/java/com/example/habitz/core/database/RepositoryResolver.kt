package com.example.habitz.core.database

import com.example.habitz.core.database.interfaces.CategoryRepository
import com.example.habitz.core.database.interfaces.HabitRepository
import com.example.habitz.core.database.respositories.InMemoryCategoryRepository
import com.example.habitz.core.database.respositories.InMemoryHabitRepository

class RepositoryResolver(private val habitRepository: HabitRepository, private val categoryRepository: CategoryRepository){

    public fun getHabitRepository(): HabitRepository{
        return habitRepository
    }
    public fun getCategoryRepository(): CategoryRepository{
        return categoryRepository
    }

}

object ServiceLocator {
    // Lazy initialization ensures it's created only when first accessed
    val repositoryResolver: RepositoryResolver by lazy {
        RepositoryResolver(provideHabitRepository(), provideCategoryRepository())
    }

    private fun provideHabitRepository(): HabitRepository {
        return InMemoryHabitRepository() // Or however you instantiate HabitRepository
    }

    private fun provideCategoryRepository(): CategoryRepository {
        return InMemoryCategoryRepository() // Or however you instantiate HabitRepository
    }
}