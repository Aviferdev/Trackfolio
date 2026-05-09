package es.aviferdev.trackfolio.ui.annual

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.CategoryBreakdown
import es.aviferdev.trackfolio.domain.model.IncomeTypeBreakdown
import es.aviferdev.trackfolio.domain.model.MonthlyInvestment
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetMonthlyInvestmentsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetAnnualSummaryUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetExpensesByCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetIncomeByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.theme.CategoryPalette
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

data class AnnualUiState(
    val summary: AnnualSummary?          = null,
    val monthlyBreakdown: List<MonthlyTotals> = emptyList(),
    val year: String                     = "",
    val isLoading: Boolean               = true,
    val canGoBack: Boolean               = true,
    // Nuevos datos para gráficos
    val expensesByCategory: List<DonutSlice> = emptyList(),
    val incomeByType: List<DonutSlice>       = emptyList(),
    val monthlyInvestments: List<MonthlyInvestment> = emptyList(),
    val currencyCode: String              = "EUR"
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnnualViewModel(
    private val getAnnualSummary: GetAnnualSummaryUseCase,
    private val getMonthlyBreakdown: GetMonthlyBreakdownUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val getExpensesByCategory: GetExpensesByCategoryUseCase,
    private val getIncomeByType: GetIncomeByTypeUseCase,
    private val getMonthlyInvestments: GetMonthlyInvestmentsUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val _year = MutableStateFlow(now.year.toString())
    val year: StateFlow<String> = _year

    /** Año de la transacción más antigua (límite inferior de navegación). */
    private val _oldestYear = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            session.selectedAccountId.flatMapLatest { accountId ->
                if (accountId == null) flowOf(null)
                else getOldestDate(accountId)
            }.collect { epochMillis ->
                _oldestYear.value = epochMillis?.let {
                    kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).year
                }
            }
        }
    }

    val uiState: StateFlow<AnnualUiState> = combine(
        session.selectedAccountId,
        _year,
        _oldestYear
    ) { accountId, year, oldest -> Triple(accountId, year, oldest) }
        .flatMapLatest { (accountId, year, oldest) ->
            val canGoBack = oldest == null || (year.toInt() - 1) >= oldest
            if (accountId == null) {
                flowOf(AnnualUiState(year = year, isLoading = false, canGoBack = canGoBack))
            } else {
                combine(
                    getAnnualSummary(accountId, year),
                    getMonthlyBreakdown(accountId, year),
                    getExpensesByCategory(accountId, year),
                    getIncomeByType(accountId, year),
                    getMonthlyInvestments(accountId, year)
                ) { summary, breakdown, expenses, income, investments ->
                    val totalExpense = summary?.totalExpense ?: 0.0
                    val totalIncome = summary?.totalIncome ?: 0.0

                    val expenseSlices = expenses.mapIndexed { idx, item ->
                        DonutSlice(
                            name   = item.categoryName,
                            icon   = "💰",
                            amount = item.amount,
                            percent = if (totalExpense > 0) (item.amount / totalExpense) * 100 else 0.0,
                            color  = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    val incomeSlices = income.mapIndexed { idx, item ->
                        DonutSlice(
                            name   = item.label,
                            icon   = item.emoji,
                            amount = item.amount,
                            percent = if (totalIncome > 0) (item.amount / totalIncome) * 100 else 0.0,
                            color  = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    AnnualUiState(
                        summary            = summary,
                        monthlyBreakdown   = breakdown,
                        year               = year,
                        isLoading          = false,
                        canGoBack          = canGoBack,
                        expensesByCategory = expenseSlices,
                        incomeByType       = incomeSlices,
                        monthlyInvestments = investments,
                        currencyCode       = "EUR"
                    )
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnnualUiState(year = _year.value)
        )

    fun previousYear() {
        val target = _year.value.toInt() - 1
        val oldest = _oldestYear.value
        if (oldest != null && target < oldest) return
        _year.value = target.toString()
    }

    fun nextYear() {
        val nowYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
        if (_year.value.toInt() >= nowYear) return
        _year.value = (_year.value.toInt() + 1).toString()
    }
}
