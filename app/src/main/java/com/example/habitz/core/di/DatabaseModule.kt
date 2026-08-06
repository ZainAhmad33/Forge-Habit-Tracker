package com.example.habitz.core.di

import android.content.Context
import androidx.room.Room
import com.example.habitz.core.database.HabitzDatabase
import com.example.habitz.core.database.dao.HabitActivityDao
import com.example.habitz.core.database.dao.HabitDao
import com.example.habitz.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitzDatabase {
        return Room.databaseBuilder(
            context,
            HabitzDatabase::class.java,
            HabitzDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideHabitDao(database: HabitzDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitActivityDao(database: HabitzDatabase): HabitActivityDao = database.habitActivityDao()

    @Provides
    fun provideUserDao(database: HabitzDatabase): UserDao = database.userDao()
}
