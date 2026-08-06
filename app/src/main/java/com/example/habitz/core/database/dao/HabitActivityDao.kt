package com.example.habitz.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habitz.core.database.entity.HabitActivity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface HabitActivityDao {
    @Query("SELECT * FROM habit_activities WHERE habitId = :habitId")
    fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>>

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
