package com.example.forge.core.services.implementations

import com.example.forge.core.database.entity.User
import com.example.forge.core.database.interfaces.IUserRepository
import com.example.forge.core.services.interfaces.IUserService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val userRepository: IUserRepository
) : IUserService {
    override fun getUser(): Flow<User?> = userRepository.getUserDetails()

    override suspend fun saveUser(user: User) {
        userRepository.saveUser(user)
    }
}
