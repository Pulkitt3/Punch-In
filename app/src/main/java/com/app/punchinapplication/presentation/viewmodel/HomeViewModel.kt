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
import java.util.Calendar

/**
 * ViewModel for Home screen
 * Manages weekly punch-in data for donut charts
 */
class HomeViewModel(
    private val punchInRepository: PunchInRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _weeklyPunchIns = MutableStateFlow<List<PunchInEntity>>(emptyList())
    val weeklyPunchIns: StateFlow<List<PunchInEntity>> = _weeklyPunchIns.asStateFlow()
    
    private val _allPunchIns = MutableStateFlow<List<PunchInEntity>>(emptyList())
    val allPunchIns: StateFlow<List<PunchInEntity>> = _allPunchIns.asStateFlow()
    
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadWeeklyPunchIns()
        loadAllPunchIns()
    }
    
    /**
     * Load punch-ins for the current week
     */
    private fun loadWeeklyPunchIns() {
        viewModelScope.launch {
            val username = sessionManager.getUsername()
            if (username.isNotBlank()) {
                punchInRepository.getPunchInsForWeek(username)
                    .catch { e ->
                        // Handle error
                    }
                    .collect { punchIns ->
                        _weeklyPunchIns.value = punchIns
                    }
            }
        }
    }
    
    /**
     * Load all punch-ins for the user (for recent activity display)
     */
    private fun loadAllPunchIns() {
        viewModelScope.launch {
            val username = sessionManager.getUsername()
            if (username.isNotBlank()) {
                punchInRepository.getAllPunchIns(username)
                    .catch { e ->
                        // Handle error
                    }
                    .collect { punchIns ->
                        _allPunchIns.value = punchIns
                    }
            }
        }
    }
    
    /**
     * Get punch-ins grouped by day of week
     */
    fun getPunchInsByDay(): Map<Int, Int> {
        return getPunchInsByDayFromList(_weeklyPunchIns.value)
    }
    
    /**
     * Get punch-ins grouped by day of week from a given list
     */
    fun getPunchInsByDayFromList(punchIns: List<PunchInEntity>): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val dayCounts = mutableMapOf<Int, Int>()
        
        // Initialize all days with 0
        for (i in Calendar.MONDAY..Calendar.SUNDAY) {
            dayCounts[i] = 0
        }
        
        punchIns.forEach { punchIn ->
            calendar.timeInMillis = punchIn.timestamp
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayCounts[dayOfWeek] = (dayCounts[dayOfWeek] ?: 0) + 1
        }
        
        return dayCounts
    }
    
    /**
     * Refresh weekly data
     */
    fun refresh() {
        loadWeeklyPunchIns()
        loadAllPunchIns()
    }
}

