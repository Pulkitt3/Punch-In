package com.app.punchinapplication.data.local.dao

import androidx.room.*
import com.app.punchinapplication.data.local.entity.PunchInEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PunchInEntity
 * Provides methods to interact with punch-in data in the database
 */
@Dao
interface PunchInDao {
    
    /**
     * Insert a new punch-in record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunchIn(punchIn: PunchInEntity): Long
    
    /**
     * Get all punch-ins for a specific user as a Flow for reactive updates
     */
    @Query("SELECT * FROM punch_ins WHERE username = :username ORDER BY timestamp DESC")
    fun getAllPunchIns(username: String): Flow<List<PunchInEntity>>
    
    /**
     * Get punch-ins for a specific date and user
     */
    @Query("SELECT * FROM punch_ins WHERE username = :username AND date = :date ORDER BY timestamp DESC")
    fun getPunchInsByDate(username: String, date: String): Flow<List<PunchInEntity>>
    
    /**
     * Get punch-ins for the current week for a specific user
     */
    @Query("""
        SELECT * FROM punch_ins 
        WHERE username = :username AND timestamp >= :weekStartTimestamp 
        ORDER BY timestamp DESC
    """)
    fun getPunchInsForWeek(username: String, weekStartTimestamp: Long): Flow<List<PunchInEntity>>
    
    /**
     * Get the last punch-in for a specific user
     */
    @Query("SELECT * FROM punch_ins WHERE username = :username ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPunchIn(username: String): PunchInEntity?
    
    /**
     * Get punch-ins by IDs for a specific user (for route plotting)
     */
    @Query("SELECT * FROM punch_ins WHERE username = :username AND id IN (:ids) ORDER BY timestamp ASC")
    suspend fun getPunchInsByIds(username: String, ids: List<Long>): List<PunchInEntity>
    
    /**
     * Delete a punch-in record
     */
    @Delete
    suspend fun deletePunchIn(punchIn: PunchInEntity)
}

