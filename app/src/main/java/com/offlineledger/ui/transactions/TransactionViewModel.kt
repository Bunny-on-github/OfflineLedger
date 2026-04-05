package com.offlineledger.ui.transactions

import androidx.lifecycle.*
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.Transaction
import com.offlineledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: LedgerRepository,
    person: Person
) : ViewModel() {

    private val _person = MutableStateFlow(person)
    val person: StateFlow<Person> = _person.asStateFlow()

    val transactions: StateFlow<List<Transaction>> =
        repo.getTransactionsForPerson(_person.value.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Double> =
        repo.getBalanceForPerson(_person.value.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun addTransaction(
        amount: Double,
        label: String,
        details: String,
        timestamp: Long
    ) = viewModelScope.launch {
        repo.addTransaction(
            Transaction(
                personId = _person.value.id,
                amount = amount,
                label = label.trim(),
                details = details.trim(),
                timestamp = timestamp
            )
        )
    }

    fun updateTransaction(t: Transaction) = viewModelScope.launch { repo.updateTransaction(t) }

    fun deleteTransaction(t: Transaction) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        val updated = _person.value.copy(reminderEnabled = enabled)
        repo.setReminderEnabled(updated.id, enabled)
        _person.value = updated
    }

    fun updateReminderConfig(
        enabled: Boolean,
        frequency: String,
        prefix: String,
        suffix: String
    ) = viewModelScope.launch {
        val updated = _person.value.copy(
            reminderEnabled = enabled,
            reminderFrequency = frequency,
            reminderMessagePrefix = prefix,
            reminderMessageSuffix = suffix
        )
        repo.updateReminderConfig(
            personId = updated.id,
            enabled = enabled,
            frequency = frequency,
            prefix = prefix,
            suffix = suffix
        )
        _person.value = updated
    }
}

class TransactionViewModelFactory(
    private val repo: LedgerRepository,
    private val person: Person
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TransactionViewModel(repo, person) as T
}
