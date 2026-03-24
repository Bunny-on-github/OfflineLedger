package com.offlineledger.ui.home

import androidx.lifecycle.*
import com.offlineledger.data.model.PersonWithBalance
import com.offlineledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeViewModel(private val repo: LedgerRepository) : ViewModel() {

    /** 0 = all, 1 = they owe you (balance > 0), 2 = you owe (balance < 0) */
    private val _filter = MutableStateFlow(0)
    val filter: StateFlow<Int> = _filter.asStateFlow()

    private val _hideZeroBalance = MutableStateFlow(false)
    val hideZeroBalance: StateFlow<Boolean> = _hideZeroBalance.asStateFlow()

    val personsWithBalance: StateFlow<List<PersonWithBalance>> =
        repo.personsWithBalance
            .combine(_filter) { list, f ->
                when (f) {
                    1 -> list.filter { it.balance > 0 }
                    2 -> list.filter { it.balance < 0 }
                    else -> list
                }
            }
            .combine(_hideZeroBalance) { list, hide ->
                if (hide) list.filter { it.balance != 0.0 } else list
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val netBalance: StateFlow<Double> = personsWithBalance
        .map { it.sumOf { p -> p.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalReceivable: StateFlow<Double> = repo.personsWithBalance
        .map { list -> list.filter { it.balance > 0 }.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalOwed: StateFlow<Double> = repo.personsWithBalance
        .map { list -> list.filter { it.balance < 0 }.sumOf { abs(it.balance) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun setFilter(f: Int) { _filter.value = f }

    fun setHideZeroBalance(hide: Boolean) { _hideZeroBalance.value = hide }

    fun addPerson(name: String, mobileNumber: String = "") =
        viewModelScope.launch { repo.addPerson(name, mobileNumber) }

    fun deletePerson(pwb: PersonWithBalance) =
        viewModelScope.launch { repo.deletePerson(pwb.person) }

    fun toggleBlacklist(pwb: PersonWithBalance) =
        viewModelScope.launch {
            repo.toggleBlacklist(pwb.person.id, !pwb.person.isBlacklisted)
        }
}

class HomeViewModelFactory(private val repo: LedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repo) as T
}
