package com.example.forge.core.di

import android.content.Context
import androidx.room.Room
import com.example.forge.core.database.ForgeDatabase
import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.dao.UserDao
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
    fun provideDatabase(@ApplicationContext context: Context): ForgeDatabase {
        return Room.databaseBuilder(
            context,
            ForgeDatabase::class.java,
            ForgeDatabase.DATABASE_NAME
        ).addMigrations(
            ForgeDatabase.MIGRATION_2_3,
            ForgeDatabase.MIGRATION_3_4,
            ForgeDatabase.MIGRATION_4_5
        ).build()
    }

    @Provides
    fun provideHabitDao(database: ForgeDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitActivityDao(database: ForgeDatabase): HabitActivityDao = database.habitActivityDao()

    @Provides
    fun provideUserDao(database: ForgeDatabase): UserDao = database.userDao()
}
