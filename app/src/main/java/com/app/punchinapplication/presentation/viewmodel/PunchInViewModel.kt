package com.app.punchinapplication.presentation.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.punchinapplication.data.repository.PunchInRepository
import com.app.punchinapplication.util.PunchInTimer
import com.app.punchinapplication.util.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * ViewModel for Punch-In screen
 * Manages location tracking and punch-in timer
 */
class PunchInViewModel(
    private val punchInRepository: PunchInRepository,
    private val context: android.content.Context,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val punchInTimer = PunchInTimer()
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _isPunchingIn = MutableStateFlow<Boolean>(false)
    val isPunchingIn: StateFlow<Boolean> = _isPunchingIn.asStateFlow()
    
    private val _punchInSuccess = MutableStateFlow<Boolean>(false)
    val punchInSuccess: StateFlow<Boolean> = _punchInSuccess.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    val timeRemaining: StateFlow<Long> = punchInTimer.timeRemaining
    val isWarning: StateFlow<Boolean> = punchInTimer.isWarning
    val isOverdue: StateFlow<Boolean> = punchInTimer.isOverdue
    
    init {
        startTimerUpdates()
        viewModelScope.launch {
            updateLastPunchInTime()
        }
    }
    
    /**
     * Start periodic timer updates
     */
    private fun startTimerUpdates() {
        viewModelScope.launch {
            while (true) {
                updateLastPunchInTime()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Update timer based on last punch-in
     */
    private suspend fun updateLastPunchInTime() {
        val username = sessionManager.getUsername()
        if (username.isNotBlank()) {
            val lastPunchIn = punchInRepository.getLastPunchIn(username)
            punchInTimer.updateTimer(lastPunchIn?.timestamp)
        }
    }
    
    /**
     * Get current location
     */
    fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            _errorMessage.value = "Location permission not granted"
            return
        }
        
        viewModelScope.launch {
            try {
                // Check permission again before accessing location
                if (!hasLocationPermission()) {
                    _errorMessage.value = "Location permission not granted"
                    return@launch
                }
                
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    try {
                        if (location != null) {
                            _currentLocation.value = location
                        } else {
                            _errorMessage.value = "Unable to get location"
                        }
                    } catch (e: SecurityException) {
                        _errorMessage.value = "Location permission denied: ${e.message}"
                    }
                }.addOnFailureListener { e ->
                    _errorMessage.value = "Location error: ${e.message}"
                }
            } catch (e: SecurityException) {
                _errorMessage.value = "Location permission denied: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Location error: ${e.message}"
            }
        }
    }
    
    /**
     * Perform punch-in
     */
    fun punchIn() {
        val location = _currentLocation.value
        if (location == null) {
            _errorMessage.value = "Please wait for location to be available"
            getCurrentLocation()
            return
        }
        
        viewModelScope.launch {
            _isPunchingIn.value = true
            _errorMessage.value = null
            
            try {
                val username = sessionManager.getUsername()
                if (username.isBlank()) {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }
                
                val id = punchInRepository.insertPunchIn(
                    username = username,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                
                if (id > 0) {
                    punchInTimer.reset()
                    _punchInSuccess.value = true
                    updateLastPunchInTime()
                } else {
                    _errorMessage.value = "Failed to save punch-in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Punch-in failed: ${e.message}"
            } finally {
                _isPunchingIn.value = false
            }
        }
    }
    
    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _punchInSuccess.value = false
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
    }
}

