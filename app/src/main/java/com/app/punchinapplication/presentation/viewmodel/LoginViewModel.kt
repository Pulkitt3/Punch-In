package com.app.punchinapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.punchinapplication.data.repository.UserRepository
import com.app.punchinapplication.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Login/Logout screen
 * Manages user authentication state
 */
class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _isLoggedIn = MutableStateFlow<Boolean>(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _username = MutableStateFlow<String>(sessionManager.getUsername())
    val username: StateFlow<String> = _username.asStateFlow()
    
    init {
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            _isLoggedIn.value = true
            _username.value = sessionManager.getUsername()
        }
    }
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Login user with credentials
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter username and password"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val success = userRepository.login(username, password)
                if (success) {
                    _username.value = username
                    _isLoggedIn.value = true
                    // Save login state
                    sessionManager.saveLoginState(username)
                } else {
                    _errorMessage.value = "Invalid username or password"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Register new user
     */
    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter username and password"
            return
        }
        
        if (password.length < 4) {
            _errorMessage.value = "Password must be at least 4 characters"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val success = userRepository.register(username, password)
                if (success) {
                    _username.value = username
                    _isLoggedIn.value = true
                    // Save login state
                    sessionManager.saveLoginState(username)
                } else {
                    _errorMessage.value = "Username already exists"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Registration failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        _isLoggedIn.value = false
        _username.value = ""
        _errorMessage.value = null
        // Clear login state
        sessionManager.clearLoginState()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

