package com.offlineledger.data.db

import androidx.room.*
import com.offlineledger.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Long): Person?

    @Query("SELECT * FROM persons")
    suspend fun getAllPersonsSync(): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("UPDATE persons SET isBlacklisted = :blacklisted WHERE id = :personId")
    suspend fun setBlacklisted(personId: Long, blacklisted: Boolean)

    @Query("UPDATE persons SET reminderEnabled = :enabled WHERE id = :personId")
    suspend fun setReminderEnabled(personId: Long, enabled: Boolean)

    @Query("SELECT * FROM persons WHERE reminderEnabled = 1 AND mobileNumber != ''")
    suspend fun getReminderEnabledPersons(): List<Person>

    @Query("SELECT * FROM persons WHERE name = :name LIMIT 1")
    suspend fun getPersonByName(name: String): Person?
}
