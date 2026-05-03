package es.aviferdev.trackfolio.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionsByMonthUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val totals: MonthlyTotals? = null,
    val year: String = "",
    val month: String = "",
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getMonthlyTotals: GetMonthlyTotalsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _selectedPeriod = MutableStateFlow(
        Pair(now.year.toString(), now.monthNumber.toString().padStart(2, '0'))
    )

    val uiState: StateFlow<TransactionListUiState> = _selectedPeriod
        .flatMapLatest { (year, month) ->
            combine(
                getTransactionsByMonth(year, month),
                getMonthlyTotals(year, month)
            ) { transactions, totals ->
                TransactionListUiState(
                    transactions = transactions,
                    totals = totals,
                    year = year,
                    month = month,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState(
                year = _selectedPeriod.value.first,
                month = _selectedPeriod.value.second
            )
        )

    fun previousMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt()
        val year = y.toInt()
        if (month == 1) {
            _selectedPeriod.value = Pair((year - 1).toString(), "12")
        } else {
            _selectedPeriod.value = Pair(y, (month - 1).toString().padStart(2, '0'))
        }
    }

    fun nextMonth() {
        val (y, m) = _selectedPeriod.value
        val month = m.toInt()
        val year = y.toInt()
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (year == nowDate.year && month == nowDate.monthNumber) return
        if (month == 12) {
            _selectedPeriod.value = Pair((year + 1).toString(), "01")
        } else {
            _selectedPeriod.value = Pair(y, (month + 1).toString().padStart(2, '0'))
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
        }
    }
}
