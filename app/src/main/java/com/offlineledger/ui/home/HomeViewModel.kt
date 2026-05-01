package com.offlineledger.ui.home

import androidx.lifecycle.*
import com.offlineledger.data.model.PersonWithBalance
import com.offlineledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.util.Locale

class HomeViewModel(private val repo: LedgerRepository) : ViewModel() {

    /** 0 = all, 1 = they owe you (balance > 0), 2 = you owe (balance < 0) */
    private val _filter = MutableStateFlow(0)
    val filter: StateFlow<Int> = _filter.asStateFlow()

    private val _hideZeroBalance = MutableStateFlow(false)
    val hideZeroBalance: StateFlow<Boolean> = _hideZeroBalance.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** true = highest balance first, false = alphabetical */
    private val _sortByAmount = MutableStateFlow(true)
    val sortByAmount: StateFlow<Boolean> = _sortByAmount.asStateFlow()

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
            .combine(_searchQuery) { list, q ->
                val query = q.trim().lowercase(Locale.getDefault())
                if (query.isBlank()) list else list.filter {
                    it.person.name.lowercase(Locale.getDefault()).contains(query) ||
                        it.person.mobileNumber.contains(query)
                }
            }
            .combine(_sortByAmount) { list, sortAmount ->
                if (sortAmount) list.sortedByDescending { kotlin.math.abs(it.balance) }
                else list.sortedBy { it.person.name.lowercase(Locale.getDefault()) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val netBalance: StateFlow<Double> = repo.personsWithBalance
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

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun toggleSortMode() { _sortByAmount.value = !_sortByAmount.value }

    fun addPerson(name: String, mobileNumber: String = "") =
        viewModelScope.launch { repo.addPerson(name, mobileNumber) }

    fun deletePerson(pwb: PersonWithBalance) =
        viewModelScope.launch { repo.deletePerson(pwb.person) }

    fun updatePerson(pwb: PersonWithBalance, newName: String, newMobile: String) =
        viewModelScope.launch {
            repo.updatePerson(pwb.person.copy(name = newName, mobileNumber = newMobile))
        }

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
