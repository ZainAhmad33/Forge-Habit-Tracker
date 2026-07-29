package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.entity.User
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.datastore.dummyDatabase
import javax.inject.Inject

class InMemoryUserRepository @Inject constructor(): IUserRepository {
    override fun getUserDetails(): User {
        return dummyDatabase.userInformation
    }
}