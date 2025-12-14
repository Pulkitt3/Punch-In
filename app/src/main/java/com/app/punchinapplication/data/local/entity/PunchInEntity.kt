package com.app.punchinapplication.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing a punch-in record in the Room database
 * Stores location data (latitude, longitude) and timestamp for each punch-in
 * Associated with a specific user via username
 */
@Entity(
    tableName = "punch_ins",
    indices = [Index(value = ["username"])]
)
data class PunchInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String, // Username of the user who created this punch-in
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date())
)

