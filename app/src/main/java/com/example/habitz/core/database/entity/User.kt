package com.example.habitz.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "users")
data class User (
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val dob: Date,
    )