package es.aviferdev.trackfolio.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TransactionListUiState(
    val transactions: List<Transaction>         = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totals: MonthlyTotals?                  = null,
    val categoryNames: Map<String, String>      = emptyMap(),
    val year: String                            = "",
    val month: String                           = "",
    val searchQuery: String                     = "",
    val isLoading: Boolean                      = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getMonthlyTotals: GetMonthlyTotalsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAllCategoriesIncludingArchived: GetAllCategoriesIncludingArchivedUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _selectedPeriod = MutableStateFlow(
        Pair(now.year.toString(), now.monthNumber.toString().padStart(2, '0'))
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Incluye categorías archivadas para que las transacciones existentes
    // sigan mostrando el nombre original aunque la categoría haya sido eliminada.
    private val categoryNamesFlow = getAllCategoriesIncludingArchived()
        .map { all -> all.associate { it.id to it.name } }

    val uiState: StateFlow<TransactionListUiState> = combine(
        session.selectedAccountId,
        _selectedPeriod,
        _searchQuery
    ) { accountId, period, query ->
        Triple(accountId, period, query)
    }.flatMapLatest { (accountId, period, query) ->
        val (year, month) = period
        if (accountId == null) {
            categoryNamesFlow.map { categoryNames ->
                TransactionListUiState(
                    categoryNames = categoryNames,
                    year          = year,
                    month         = month,
                    searchQuery   = query,
                    isLoading     = false
                )
            }
        } else {
            combine(
                getTransactionsByMonth(accountId, year, month),
                getMonthlyTotals(accountId, year, month),
                categoryNamesFlow
            ) { transactions, totals, categoryNames ->
                val filtered = if (query.isBlank()) transactions
                else transactions.filter { t ->
                    val catName = categoryNames[t.categoryId]?.lowercase() ?: ""
                    val note    = t.notes?.lowercase() ?: ""
                    val q       = query.lowercase()
                    catName.contains(q) || note.contains(q)
                }
                TransactionListUiState(
                    transactions         = transactions,
                    filteredTransactions = filtered,
                    totals               = totals,
                    categoryNames        = categoryNames,
                    year                 = year,
                    month                = month,
                    searchQuery          = query,
                    isLoading            = false
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(
            year  = _selectedPeriod.value.first,
            month = _selectedPeriod.value.second
        )
    )

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun previousMonth() {
        val (y, m) = _selectedPeriod.value
        val month  = m.toInt(); val year = y.toInt()
        _selectedPeriod.value = if (month == 1) Pair((year - 1).toString(), "12")
        else Pair(y, (month - 1).toString().padStart(2, '0'))
    }

    fun nextMonth() {
        val (y, m) = _selectedPeriod.value
        val month  = m.toInt(); val year = y.toInt()
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (year == nowDate.year && month == nowDate.monthNumber) return
        _selectedPeriod.value = if (month == 12) Pair((year + 1).toString(), "01")
        else Pair(y, (month + 1).toString().padStart(2, '0'))
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { deleteTransactionUseCase(id) }
    }
}
