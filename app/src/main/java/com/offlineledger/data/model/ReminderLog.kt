package com.offlineledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_logs")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val mobileNumber: String,
    val message: String,
    val sentAt: Long = System.currentTimeMillis()
)
