package com.example.forge.core.services.interfaces

import com.example.forge.core.database.entity.User
import kotlinx.coroutines.flow.Flow

interface IUserService {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
}
