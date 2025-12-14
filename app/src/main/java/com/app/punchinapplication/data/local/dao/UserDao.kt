package com.app.punchinapplication.data.local.dao

import androidx.room.*
import com.app.punchinapplication.data.local.entity.UserEntity

/**
 * Data Access Object for UserEntity
 * Handles user authentication data
 */
@Dao
interface UserDao {
    
    /**
     * Insert a new user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Get user by username
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    /**
     * Check if user exists
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun userExists(username: String): Int
}

