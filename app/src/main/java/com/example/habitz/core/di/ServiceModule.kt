package com.example.habitz.core.di

import com.example.habitz.core.services.implementations.HomeService
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
}
