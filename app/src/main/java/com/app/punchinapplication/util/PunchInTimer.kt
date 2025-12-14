package com.app.punchinapplication.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Timer utility for managing punch-in intervals
 * Tracks time since last punch-in and provides warnings
 */
class PunchInTimer {
    
    private val _timeRemaining = MutableStateFlow<Long>(0)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()
    
    private val _isWarning = MutableStateFlow<Boolean>(false)
    val isWarning: StateFlow<Boolean> = _isWarning.asStateFlow()
    
    private val _isOverdue = MutableStateFlow<Boolean>(false)
    val isOverdue: StateFlow<Boolean> = _isOverdue.asStateFlow()
    
    companion object {
        const val PUNCH_IN_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
        const val WARNING_TIME_MS = 1 * 60 * 1000L // 1 minute (warning when 1 minute left)
    }
    
    /**
     * Update timer based on last punch-in timestamp
     */
    fun updateTimer(lastPunchInTime: Long?) {
        if (lastPunchInTime == null) {
            _timeRemaining.value = PUNCH_IN_INTERVAL_MS
            _isWarning.value = false
            _isOverdue.value = false
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastPunchInTime
        val remaining = PUNCH_IN_INTERVAL_MS - elapsed
        
        _timeRemaining.value = if (remaining > 0) remaining else 0
        _isWarning.value = remaining > 0 && remaining <= WARNING_TIME_MS
        _isOverdue.value = remaining <= 0
    }
    
    /**
     * Reset timer after successful punch-in
     */
    fun reset() {
        _timeRemaining.value = PUNCH_IN_INTERVAL_MS
        _isWarning.value = false
        _isOverdue.value = false
    }
    
    /**
     * Format milliseconds to MM:SS string
     */
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

