package com.example.habitz.core.database.respositories

import com.example.habitz.core.database.entity.UserModel
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.datastore.dummyDatabase

class InMemoryUserRepository: IUserRepository {
    override fun getUserDetails(): UserModel {
        return dummyDatabase.userInformation
    }
}