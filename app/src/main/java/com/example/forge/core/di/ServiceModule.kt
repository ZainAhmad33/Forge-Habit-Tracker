package com.example.forge.core.di

import com.example.forge.core.services.implementations.HabitActivityService
import com.example.forge.core.services.implementations.HabitsService
import com.example.forge.core.services.implementations.HomeService
import com.example.forge.core.services.implementations.HabitStatsService
import com.example.forge.core.services.implementations.TimeService
import com.example.forge.core.services.interfaces.IHabitActivityService
import com.example.forge.core.services.interfaces.IHabitsService
import com.example.forge.core.services.interfaces.IHabitStatsService
import com.example.forge.core.services.interfaces.IHomeService
import com.example.forge.core.services.interfaces.ITimeService
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

    @Binds
    @Singleton
    abstract fun bindHabitStatsService(
        impl: HabitStatsService
    ): IHabitStatsService

    @Binds
    @Singleton
    abstract fun bindTimeService(
        impl: TimeService
    ): ITimeService
}
