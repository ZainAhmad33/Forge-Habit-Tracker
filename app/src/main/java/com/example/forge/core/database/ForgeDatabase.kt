package com.example.forge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.dao.UserDao
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.entity.User

@Database(
    entities = [Habit::class, HabitActivity::class, User::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ForgeDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitActivityDao(): HabitActivityDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "forge_db"
    }
}
