package com.example.habitz.core.di

import com.example.habitz.core.services.implementations.HabitActivityService
import com.example.habitz.core.services.implementations.HabitsService
import com.example.habitz.core.services.implementations.HomeService
import com.example.habitz.core.services.interfaces.IHabitActivityService
import com.example.habitz.core.services.interfaces.IHabitsService
import com.example.habitz.core.services.interfaces.IHomeService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindHomeService(
        impl: HomeService
    ): IHomeService

    @Binds
    @Singleton
    abstract fun bindHabitsService(
        impl: HabitsService
    ): IHabitsService

    @Binds
    @Singleton
    abstract fun bindHabitActivityService(
        impl: HabitActivityService
    ): IHabitActivityService
}
