package com.app.punchinapplication.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages screen locking state
 * When punch-in is overdue, all screens except Punch-In are locked
 */
object ScreenLockManager {
    private val _isLocked = MutableStateFlow<Boolean>(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    
    /**
     * Lock all screens except Punch-In
     */
    fun lock() {
        _isLocked.value = true
    }
    
    /**
     * Unlock all screens
     */
    fun unlock() {
        _isLocked.value = false
    }
}

