package com.example.habitz.core.database.interfaces

import com.example.habitz.core.database.entity.UserModel

interface IUserRepository {
    fun getUserDetails(): UserModel
}