package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getUserDetails(): Flow<User?>
    suspend fun saveUser(user: User)
}
