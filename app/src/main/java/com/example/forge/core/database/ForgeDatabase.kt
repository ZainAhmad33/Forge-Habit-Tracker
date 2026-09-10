package com.example.forge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.dao.UserDao
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.entity.User

@Database(
    entities = [Habit::class, HabitActivity::class, User::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ForgeDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitActivityDao(): HabitActivityDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "forge_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN avatarColor INTEGER NOT NULL DEFAULT 4284969124")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite doesn't support DROP COLUMN, so we recreate the table
                database.execSQL("CREATE TABLE IF NOT EXISTS `users_new` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `dob` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                database.execSQL("INSERT INTO `users_new` (`id`, `firstName`, `lastName`, `dob`) SELECT `id`, `firstName`, `lastName`, `dob` FROM `users`")
                database.execSQL("DROP TABLE `users`")
                database.execSQL("ALTER TABLE `users_new` RENAME TO `users`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()
                database.execSQL("ALTER TABLE users ADD COLUMN joinedDate INTEGER NOT NULL DEFAULT $currentTime")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN isLockingEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
