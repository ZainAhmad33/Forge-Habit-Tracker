package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.User
import com.example.forge.core.database.interfaces.IHabitActivityRepository
import com.example.forge.core.database.interfaces.IHabitRepository
import com.example.forge.core.database.interfaces.IUserRepository
import com.example.forge.core.services.interfaces.IInsightsService
import com.example.forge.core.services.interfaces.IUserService
import com.example.forge.core.services.interfaces.InsightPeriod
import com.example.forge.core.services.interfaces.ProfileStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val userRepository: IUserRepository,
    private val habitRepository: IHabitRepository,
    private val activityRepository: IHabitActivityRepository,
    private val insightsService: IInsightsService
) : IUserService {
    override fun getUser(): Flow<User?> = userRepository.getUserDetails()

    override suspend fun saveUser(user: User) {
        userRepository.saveUser(user)
    }

    override fun getProfileStats(): Flow<ProfileStats> {
        return combine(
            getUser(),
            habitRepository.getHabits(),
            activityRepository.getAllActivities(),
            insightsService.getGlobalStats(InsightPeriod.AllTime)
        ) { user, habits, activities, globalStats ->
            val totalHabits = habits.size
            val joinedDate = user?.joinedDate ?: Date()
            val totalCompletions = activities.size
            
            val avgRate = (globalStats.completionRate * 100).toInt().coerceIn(0, 100)

            ProfileStats(
                totalHabits = totalHabits,
                joinedDate = joinedDate,
                totalCompletions = totalCompletions,
                averageCompletionRate = avgRate
            )
        }
    }
}
