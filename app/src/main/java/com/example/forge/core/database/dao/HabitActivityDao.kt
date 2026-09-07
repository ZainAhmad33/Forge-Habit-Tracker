package com.example.forge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.pojo.DailyHabitQuantity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface HabitActivityDao {
    @Query("SELECT * FROM habit_activities WHERE habitId = :habitId")
    fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>>

    @Query("SELECT habitId, date(createdAt / 1000, 'unixepoch', 'localtime') as day, SUM(quantity) as totalQuantity FROM habit_activities WHERE habitId = :habitId GROUP BY day")
    fun getDailyQuantitiesForHabit(habitId: UUID): Flow<List<DailyHabitQuantity>>

    @Query("SELECT habitId, date(createdAt / 1000, 'unixepoch', 'localtime') as day, SUM(quantity) as totalQuantity FROM habit_activities WHERE habitId = :habitId GROUP BY day")
    suspend fun getDailyQuantitiesForHabitSync(habitId: UUID): List<DailyHabitQuantity>

    @Query("SELECT habitId, date(createdAt / 1000, 'unixepoch', 'localtime') as day, SUM(quantity) as totalQuantity FROM habit_activities GROUP BY habitId, day")
    fun getAllDailyQuantities(): Flow<List<DailyHabitQuantity>>

    @Query("SELECT habitId, date(createdAt / 1000, 'unixepoch', 'localtime') as day, SUM(quantity) as totalQuantity FROM habit_activities WHERE habitId = :habitId AND createdAt BETWEEN :from AND :to GROUP BY day")
    suspend fun getDailyQuantitiesByRange(habitId: UUID, from: Long, to: Long): List<DailyHabitQuantity>

    @Query("SELECT * FROM habit_activities WHERE habitId IN (:habitIds) AND createdAt BETWEEN :from AND :to")
    fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>>

    @Query("SELECT * FROM habit_activities")
    fun getAllActivities(): Flow<List<HabitActivity>>

    @Query("SELECT * FROM habit_activities WHERE habitId = :habitId AND createdAt BETWEEN :from AND :to")
    suspend fun getActivitiesByRange(habitId: UUID, from: Date, to: Date): List<HabitActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: HabitActivity)

    @Query("DELETE FROM habit_activities WHERE id = :activityId")
    suspend fun deleteActivityById(activityId: UUID)

    @Query("DELETE FROM habit_activities WHERE habitId = :habitId")
    suspend fun deleteActivitiesForHabit(habitId: UUID)
}
