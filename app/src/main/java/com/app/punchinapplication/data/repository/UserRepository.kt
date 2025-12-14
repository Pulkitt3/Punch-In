package com.app.punchinapplication.data.repository

import com.app.punchinapplication.data.local.dao.UserDao
import com.app.punchinapplication.data.local.entity.UserEntity

/**
 * Repository for managing user authentication
 */
class UserRepository(
    private val userDao: UserDao
) {
    
    /**
     * Login user with username and password
     */
    suspend fun login(username: String, password: String): Boolean {
        val user = userDao.getUserByUsername(username)
        return user?.password == password
    }
    
    /**
     * Register a new user
     */
    suspend fun register(username: String, password: String): Boolean {
        return try {
            if (userDao.userExists(username) > 0) {
                false // User already exists
            } else {
                userDao.insertUser(UserEntity(username, password))
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if user exists
     */
    suspend fun userExists(username: String): Boolean = 
        userDao.userExists(username) > 0
}

