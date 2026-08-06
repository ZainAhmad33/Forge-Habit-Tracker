package com.example.forge.core.di

import com.example.forge.core.database.interfaces.ICategoryRepository
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.database.interfaces.IUserRepository
import com.example.forge.core.database.respositories.RoomCategoryRepository
import com.example.forge.core.database.respositories.RoomHabitActivityRepository
import com.example.forge.core.database.respositories.RoomHabitRepository
import com.example.forge.core.database.respositories.RoomUserRepository
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
        impl: RoomHabitRepository
    ): IHabitRepository

    @Binds
    @Singleton
    abstract fun bindHabitActivityRepository(
        impl: RoomHabitActivityRepository
    ): IHabitActivityRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: RoomCategoryRepository
    ): ICategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: RoomUserRepository
    ): IUserRepository
}
