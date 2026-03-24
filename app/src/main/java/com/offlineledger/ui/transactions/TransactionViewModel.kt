package com.offlineledger.ui.transactions

import androidx.lifecycle.*
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.Transaction
import com.offlineledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: LedgerRepository,
    val person: Person
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> =
        repo.getTransactionsForPerson(person.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Double> =
        repo.getBalanceForPerson(person.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun addTransaction(
        amount: Double,
        label: String,
        details: String,
        timestamp: Long
    ) = viewModelScope.launch {
        repo.addTransaction(
            Transaction(
                personId = person.id,
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
        repo.setReminderEnabled(person.id, enabled)
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
