package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.User

interface IUserRepository {
    fun getUserDetails(): User
}