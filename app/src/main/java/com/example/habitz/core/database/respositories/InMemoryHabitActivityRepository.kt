package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.entity.HabitActivity
import com.example.habitz.core.database.interfaces.IHabitActivityRepository
import com.example.habitz.core.datastore.dummyDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryHabitActivityRepository @Inject constructor() : IHabitActivityRepository {

    private val _activities = MutableStateFlow(dummyDatabase.activities)

    override fun logActivity(activity: HabitActivity) {
        dummyDatabase.activities = dummyDatabase.activities + activity
        _activities.value = dummyDatabase.activities
    }

    override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date): Flow<List<HabitActivity>> {
        return _activities.asStateFlow().map { list ->
            list.filter { activity ->
                activity.habitId in habitIds &&
                        activity.createdAt.after(from) &&
                        activity.createdAt.before(to)
            }
        }
    }

    override fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>> {
        return _activities.asStateFlow().map { list ->
            list.filter { it.habitId == habitId }
        }
    }

    override fun getAllActivities(): Flow<List<HabitActivity>> {
        return _activities.asStateFlow()
    }

    override fun deleteActivity(activityId: UUID) {
        dummyDatabase.activities = dummyDatabase.activities.filter { it.id != activityId }
        _activities.value = dummyDatabase.activities
    }
}
