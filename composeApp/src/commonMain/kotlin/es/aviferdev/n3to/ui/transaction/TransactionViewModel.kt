package es.aviferdev.n3to.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totals: MonthlyTotals? = null,
    val categoryNames: Map<String, String> = emptyMap(),
    val year: String = "",
    val month: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val canGoBack: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getMonthlyTotals: GetMonthlyTotalsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAllCategoriesIncludingArchived: GetAllCategoriesIncludingArchivedUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _selectedPeriod = MutableStateFlow(
        Pair(now.year.toString(), now.monthNumber.toString().padStart(2, '0'))
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /** Año y mes de la transacción más antigua (límite inferior de navegación). */
    private val _oldestYearMonth = MutableStateFlow<Pair<Int, Int>?>(null)

    init {
        // Reaccionar al cambio de cuenta para recalcular la fecha más antigua
        viewModelScope.launch {
            session.selectedAccountId.flatMapLatest { accountId ->
                if (accountId == null) flowOf(null)
                else getOldestDate(accountId)
            }.collect { epochMillis ->
                _oldestYearMonth.value = epochMillis?.let {
                    val ld = kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    ld.year to ld.monthNumber
                }
            }
        }
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        session.selectedAccountId,
        _selectedPeriod,
        _searchQuery,
        _oldestYearMonth
    ) { accountId, period, query, oldest ->
        data class Params(
            val accountId: String?,
            val period: Pair<String, String>,
            val query: String,
            val oldest: Pair<Int, Int>?
        )
        Params(accountId, period, query, oldest)
    }.flatMapLatest { params ->
        val (year, month) = params.period
        val query = params.query
        val oldest = params.oldest
        // Calcular si se puede retroceder
        val prevMonth = if (month.toInt() == 1) Pair(year.toInt() - 1, 12) else Pair(
            year.toInt(),
            month.toInt() - 1
        )
        val canGoBack =
            oldest == null || prevMonth.first > oldest.first || (prevMonth.first == oldest.first && prevMonth.second >= oldest.second)

        val accountId = params.accountId
        if (accountId == null) {
            getAllCategoriesIncludingArchived("").map { all ->
                val categoryNames = all.associate { it.id to it.name }
                TransactionListUiState(
                    categoryNames = categoryNames,
                    year = year,
                    month = month,
                    searchQuery = query,
                    isLoading = false,
                    canGoBack = canGoBack
                )
            }
        } else {
            combine(
                getTransactionsByMonth(accountId, year, month),
                getMonthlyTotals(accountId, year, month),
                getAllCategoriesIncludingArchived(accountId).map { all -> all.associate { it.id to it.name } }
            ) { transactions, totals, categoryNames ->
                val filtered = if (query.isBlank()) transactions
                else transactions.filter { t ->
                    val q = query.lowercase()
                    val label = resolveLabel(t, categoryNames).lowercase()
                    val note = t.notes?.lowercase() ?: ""
                    val issuer = t.issuerName?.lowercase() ?: ""
                    val amountFormatted = formatAmountSearch(t.amount)
                    label.contains(q) || note.contains(q) || issuer.contains(q) || amountFormatted.contains(
                        q
                    )
                }
                TransactionListUiState(
                    transactions = transactions,
                    filteredTransactions = filtered,
                    totals = totals,
                    categoryNames = categoryNames,
                    year = year,
                    month = month,
                    searchQuery = query,
                    isLoading = false,
                    canGoBack = canGoBack
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(
            year = _selectedPeriod.value.first,
            month = _selectedPeriod.value.second
        )
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun previousMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt();
        val year = y.toInt()
        val target = if (month == 1) Pair((year - 1).toString(), "12")
        else Pair(y, (month - 1).toString().padStart(2, '0'))
        // Limitar al mes más antiguo con datos
        val oldest = _oldestYearMonth.value
        if (oldest != null) {
            val (oYear, oMonth) = oldest
            val tYear = target.first.toInt();
            val tMonth = target.second.toInt()
            if (tYear < oYear || (tYear == oYear && tMonth < oMonth)) return
        }
        _selectedPeriod.value = target
    }

    fun nextMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt();
        val year = y.toInt()
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (year == nowDate.year && month == nowDate.monthNumber) return
        _selectedPeriod.value = if (month == 12) Pair((year + 1).toString(), "01")
        else Pair(y, (month + 1).toString().padStart(2, '0'))
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { deleteTransactionUseCase(id) }
    }

    /** Formatea el importe en varios formatos para búsqueda. */
    private fun formatAmountSearch(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        val intPart = abs.toLong()
        val frac = ((abs - intPart) * 100 + 0.5).toLong()
        val intStr = intPart.toString()
        val fracStr = frac.toString().padStart(2, '0')
        return "$intStr$fracStr,$intStr.$fracStr,$intStr,$fracStr,$intStr"
    }

    companion object {
        fun resolveLabel(transaction: Transaction, categoryNames: Map<String, String>): String {
            if (transaction.isLinkedToAsset) {
                return transaction.notes ?: "Inversión"
            }
            return if (transaction.isIncome) {
                transaction.incomeType?.label ?: "Ingreso"
            } else {
                transaction.categoryId?.let { categoryNames[it] } ?: "Gasto"
            }
        }
    }
}
