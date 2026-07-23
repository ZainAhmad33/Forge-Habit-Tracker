package com.example.habitz.core.database

import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.respositories.InMemoryCategoryRepository
import com.example.habitz.core.database.respositories.InMemoryIHabitRepository

class RepositoryResolver(private val IHabitRepository: IHabitRepository, private val ICategoryRepository: ICategoryRepository){

    public fun getHabitRepository(): IHabitRepository{
        return IHabitRepository
    }
    public fun getCategoryRepository(): ICategoryRepository{
        return ICategoryRepository
    }

}

object ServiceLocator {
    // Lazy initialization ensures it's created only when first accessed
    val repositoryResolver: RepositoryResolver by lazy {
        RepositoryResolver(provideHabitRepository(), provideCategoryRepository())
    }

    private fun provideHabitRepository(): IHabitRepository {
        return InMemoryIHabitRepository() // Or however you instantiate HabitRepository
    }

    private fun provideCategoryRepository(): ICategoryRepository {
        return InMemoryCategoryRepository() // Or however you instantiate HabitRepository
    }
}