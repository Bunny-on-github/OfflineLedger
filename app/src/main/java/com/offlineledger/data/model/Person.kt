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
    val reminderFrequency: String = ReminderFrequency.WEEKLY.value,
    val reminderMessagePrefix: String = "You have a pending balance of",
    val reminderMessageSuffix: String = "Please pay at your earliest convenience.",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class ReminderFrequency(val value: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    TEN_DAYS("ten_days"),
    EVERY_THREE_DAYS_OF_MONTH("every_3rd_day_of_month");

    companion object {
        fun fromValue(value: String?): ReminderFrequency =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: WEEKLY
    }
}
