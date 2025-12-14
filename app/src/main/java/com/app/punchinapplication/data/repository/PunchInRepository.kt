package com.app.punchinapplication.data.repository

import com.app.punchinapplication.data.local.dao.PunchInDao
import com.app.punchinapplication.data.local.entity.PunchInEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository for managing punch-in data
 * Acts as a single source of truth for punch-in operations
 */
class PunchInRepository(
    private val punchInDao: PunchInDao
) {
    
    /**
     * Get all punch-ins for a specific user as a Flow
     */
    fun getAllPunchIns(username: String): Flow<List<PunchInEntity>> = 
        punchInDao.getAllPunchIns(username)
    
    /**
     * Get punch-ins for a specific date and user
     */
    fun getPunchInsByDate(username: String, date: String): Flow<List<PunchInEntity>> = 
        punchInDao.getPunchInsByDate(username, date)
    
    /**
     * Get punch-ins for the current week for a specific user
     */
    fun getPunchInsForWeek(username: String): Flow<List<PunchInEntity>> {
        val calendar = Calendar.getInstance()
        // Set to start of current week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) {
            6 // If Sunday, go back 6 days to Monday
        } else {
            dayOfWeek - Calendar.MONDAY
        }
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return punchInDao.getPunchInsForWeek(username, calendar.timeInMillis)
    }
    
    /**
     * Get the last punch-in for a specific user
     */
    suspend fun getLastPunchIn(username: String): PunchInEntity? = 
        punchInDao.getLastPunchIn(username)
    
    /**
     * Insert a new punch-in for a specific user
     */
    suspend fun insertPunchIn(username: String, latitude: Double, longitude: Double): Long {
        val punchIn = PunchInEntity(
            username = username,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        return punchInDao.insertPunchIn(punchIn)
    }
    
    /**
     * Get punch-ins by IDs for a specific user (for route plotting)
     */
    suspend fun getPunchInsByIds(username: String, ids: List<Long>): List<PunchInEntity> = 
        punchInDao.getPunchInsByIds(username, ids)
    
    /**
     * Delete a punch-in
     */
    suspend fun deletePunchIn(punchIn: PunchInEntity) = punchInDao.deletePunchIn(punchIn)
}

