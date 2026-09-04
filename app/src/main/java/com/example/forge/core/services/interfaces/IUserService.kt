package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.User
import kotlinx.coroutines.flow.Flow
import java.util.Date

data class ProfileStats(
    val totalHabits: Int,
    val joinedDate: Date,
    val totalCompletions: Int,
    val averageCompletionRate: Int
)

interface IUserService {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    fun getProfileStats(): Flow<ProfileStats>
}
