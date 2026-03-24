package com.offlineledger.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    /** Positive = incoming (they owe you), Negative = outgoing (you owe them) */
    val amount: Double,
    val label: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
