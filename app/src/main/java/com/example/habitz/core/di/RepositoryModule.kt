package com.example.habitz.core.di

import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.database.interfaces.IHabitActivityRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.database.respositories.InMemoryCategoryRepository
import com.example.habitz.core.database.respositories.InMemoryHabitActivityRepository
import com.example.habitz.core.database.respositories.InMemoryIHabitRepository
import com.example.habitz.core.database.respositories.InMemoryUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        impl: InMemoryIHabitRepository
    ): IHabitRepository

    @Binds
    @Singleton
    abstract fun bindHabitActivityRepository(
        impl: InMemoryHabitActivityRepository
    ): IHabitActivityRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: InMemoryCategoryRepository
    ): ICategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: InMemoryUserRepository
    ): IUserRepository
}
