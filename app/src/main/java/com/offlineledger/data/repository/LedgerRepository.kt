package com.offlineledger.data.repository

import com.offlineledger.data.db.PersonDao
import com.offlineledger.data.db.ReminderLogDao
import com.offlineledger.data.db.TransactionDao
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.PersonWithBalance
import com.offlineledger.data.model.ReminderLog
import com.offlineledger.data.model.Transaction
import kotlinx.coroutines.flow.*

class LedgerRepository(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val reminderLogDao: ReminderLogDao
) {
    val allPersons: Flow<List<Person>> = personDao.getAllPersons()

    fun getTransactionsForPerson(personId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForPerson(personId)

    fun getBalanceForPerson(personId: Long): Flow<Double> =
        transactionDao.getBalanceForPerson(personId)

    /** Emits a list of every person paired with their live balance, sorted by |balance| desc. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val personsWithBalance: Flow<List<PersonWithBalance>> =
        allPersons.flatMapLatest { persons ->
            if (persons.isEmpty()) return@flatMapLatest flowOf(emptyList())
            val flows = persons.map { p ->
                transactionDao.getBalanceForPerson(p.id).map { bal ->
                    PersonWithBalance(p, bal)
                }
            }
            combine(flows) { arr -> arr.sortedByDescending { kotlin.math.abs(it.balance) } }
        }

    // ── CRUD ────────────────────────────────────────────────────────────────

    suspend fun addPerson(name: String, mobileNumber: String = ""): Long =
        personDao.insert(Person(name = name.trim(), mobileNumber = mobileNumber.trim()))

    suspend fun updatePerson(person: Person) = personDao.update(person)

    suspend fun deletePerson(person: Person) = personDao.delete(person)

    suspend fun toggleBlacklist(personId: Long, blacklisted: Boolean) =
        personDao.setBlacklisted(personId, blacklisted)

    suspend fun setReminderEnabled(personId: Long, enabled: Boolean) =
        personDao.setReminderEnabled(personId, enabled)

    suspend fun addTransaction(t: Transaction): Long = transactionDao.insert(t)

    suspend fun updateTransaction(t: Transaction) = transactionDao.update(t)

    suspend fun deleteTransaction(t: Transaction) = transactionDao.delete(t)

    // ── Backup / restore helpers ─────────────────────────────────────────────

    suspend fun getAllPersonsSync(): List<Person> = personDao.getAllPersonsSync()

    suspend fun getAllTransactionsSync(): List<Transaction> =
        transactionDao.getAllTransactionsSync()

    suspend fun getBalanceForPersonSync(personId: Long): Double =
        transactionDao.getBalanceForPersonSync(personId)

    /** Insert a fully populated Person (used during XML import). */
    suspend fun insertPersonDirect(person: Person): Long = personDao.insert(person)

    /** Insert a fully populated Transaction (used during XML import). */
    suspend fun insertTransactionDirect(t: Transaction): Long = transactionDao.insert(t)

    /** Find a person by exact name (for duplicate detection during restore). */
    suspend fun getPersonByName(name: String): Person? = personDao.getPersonByName(name)

    // ── Reminder helpers ────────────────────────────────────────────────────

    suspend fun getReminderEnabledPersons(): List<Person> =
        personDao.getReminderEnabledPersons()

    // ── Reminder logs ───────────────────────────────────────────────────────

    fun getAllReminderLogs(): Flow<List<ReminderLog>> = reminderLogDao.getAllLogs()

    suspend fun insertReminderLog(log: ReminderLog): Long = reminderLogDao.insert(log)
}
