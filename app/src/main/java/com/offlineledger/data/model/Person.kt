package com.offlineledger.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobileNumber: String = "",
    val isBlacklisted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
