package com.app.punchinapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.punchinapplication.data.local.dao.PunchInDao
import com.app.punchinapplication.data.local.dao.UserDao
import com.app.punchinapplication.data.local.entity.PunchInEntity
import com.app.punchinapplication.data.local.entity.UserEntity

/**
 * Room Database class
 * Manages database creation and provides access to DAOs
 */
@Database(
    entities = [PunchInEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun punchInDao(): PunchInDao
    abstract fun userDao(): UserDao
    
    companion object {
        /**
         * Migration from version 1 to 2
         * Adds username column to punch_ins table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add username column with default empty string for existing records
                database.execSQL("ALTER TABLE punch_ins ADD COLUMN username TEXT NOT NULL DEFAULT ''")
                // Create index on username column
                database.execSQL("CREATE INDEX IF NOT EXISTS index_punch_ins_username ON punch_ins(username)")
            }
        }
    }
}

