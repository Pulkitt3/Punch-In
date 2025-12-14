package com.app.punchinapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a user in the Room database
 * Simple authentication with username and password
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val password: String
)

