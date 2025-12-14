package com.app.punchinapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.punchinapplication.data.local.entity.PunchInEntity
import com.app.punchinapplication.data.repository.PunchInRepository
import com.app.punchinapplication.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for Route plotting screen
 * Manages selected punch-ins and route data
 */
class RouteViewModel(
    private val punchInRepository: PunchInRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _allPunchIns = MutableStateFlow<List<PunchInEntity>>(emptyList())
    val allPunchIns: StateFlow<List<PunchInEntity>> = _allPunchIns.asStateFlow()
    
    private val _selectedPunchIns = MutableStateFlow<List<PunchInEntity>>(emptyList())
    val selectedPunchIns: StateFlow<List<PunchInEntity>> = _selectedPunchIns.asStateFlow()
    
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadAllPunchIns()
    }
    
    /**
     * Load all punch-ins for the current user
     */
    private fun loadAllPunchIns() {
        viewModelScope.launch {
            _isLoading.value = true
            val username = sessionManager.getUsername()
            if (username.isNotBlank()) {
                punchInRepository.getAllPunchIns(username)
                    .catch { e ->
                        // Handle error
                    }
                    .collect { punchIns ->
                        _allPunchIns.value = punchIns
                        _isLoading.value = false
                    }
            } else {
                _allPunchIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Toggle selection of a punch-in
     */
    fun togglePunchInSelection(punchIn: PunchInEntity) {
        val current = _selectedPunchIns.value.toMutableList()
        if (current.contains(punchIn)) {
            current.remove(punchIn)
        } else {
            current.add(punchIn)
        }
        _selectedPunchIns.value = current.sortedBy { it.timestamp }
    }
    
    /**
     * Clear all selections
     */
    fun clearSelections() {
        _selectedPunchIns.value = emptyList()
    }
    
    /**
     * Select all punch-ins
     */
    fun selectAll() {
        _selectedPunchIns.value = _allPunchIns.value.sortedBy { it.timestamp }
    }
    
    /**
     * Refresh punch-ins
     */
    fun refresh() {
        loadAllPunchIns()
    }
}

