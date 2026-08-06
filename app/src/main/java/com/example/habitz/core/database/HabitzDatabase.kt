package com.example.habitz.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habitz.core.database.dao.HabitActivityDao
import com.example.habitz.core.database.dao.HabitDao
import com.example.habitz.core.database.dao.UserDao
import com.example.habitz.core.database.entity.Habit
import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.entity.User

@Database(
    entities = [Habit::class, HabitActivity::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitzDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitActivityDao(): HabitActivityDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "habitz_db"
    }
}
