package com.offlineledger.data.db

import androidx.room.*
import com.offlineledger.data.model.ReminderLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Query("SELECT * FROM reminder_logs ORDER BY sentAt DESC")
    fun getAllLogs(): Flow<List<ReminderLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ReminderLog): Long
}
