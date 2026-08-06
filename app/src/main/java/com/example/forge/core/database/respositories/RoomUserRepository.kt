package com.example.forge.core.database.respositories

import com.example.forge.core.database.dao.UserDao
import com.example.forge.core.database.entity.User
import com.example.forge.core.database.interfaces.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomUserRepository @Inject constructor(
    private val userDao: UserDao
) : IUserRepository {

    override fun getUserDetails(): Flow<User?> {
        return userDao.getUser()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }
}
