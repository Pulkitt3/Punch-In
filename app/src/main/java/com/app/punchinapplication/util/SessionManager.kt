package com.app.punchinapplication.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Session Manager
 * Manages user login state persistence
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
    }
    
    /**
     * Save login state
     */
    fun saveLoginState(username: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            apply()
        }
    }
    
    /**
     * Clear login state
     */
    fun clearLoginState() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_USERNAME, "")
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Get logged in username
     */
    fun getUsername(): String {
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }
}

