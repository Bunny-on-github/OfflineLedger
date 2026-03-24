package com.offlineledger.data.db

import androidx.room.*
import com.offlineledger.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY timestamp DESC")
    fun getTransactionsForPerson(personId: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE personId = :personId")
    fun getBalanceForPerson(personId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE personId = :personId")
    suspend fun getBalanceForPersonSync(personId: Long): Double

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
