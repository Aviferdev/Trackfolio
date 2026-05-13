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
    // Comparativas interanuales
    val categoryComparisons: List<CategoryExpenseComparison> = emptyList(),
    val incomeComparisons: List<CategoryExpenseComparison> = emptyList()
)

/**
 * Contenedor intermedio para los 5 flows principales del combine.
 * Necesario porque combine() solo soporta hasta 5 parámetros.
 */
private data class MainData(
    val summary: AnnualSummary?,
    val breakdown: List<MonthlyTotals>,
    val expenses: List<CategoryBreakdown>,
    val incomes: List<IncomeTypeBreakdown>,
    val investments: List<MonthlyInvestment>
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
                val prevYear = (year.toInt() - 1).toString()
                val prevExpensesFlow = if (canGoBack) getExpensesByCategory(accountId, prevYear)
                                       else flowOf(emptyList())
                val prevIncomeFlow = if (canGoBack) getIncomeByType(accountId, prevYear)
                                     else flowOf(emptyList())

                // Combinamos los 5 flows principales en uno intermedio,
                // porque combine() solo soporta hasta 5 flows.
                val mainFlow = combine(
                    getAnnualSummary(accountId, year),
                    getMonthlyBreakdown(accountId, year),
                    getExpensesByCategory(accountId, year),
                    getIncomeByType(accountId, year),
                    getMonthlyInvestments(accountId, year)
                ) { summary, breakdown, expenses, income, investments ->
                    MainData(summary, breakdown, expenses, income, investments)
                }

                combine(
                    mainFlow,
                    prevExpensesFlow,
                    prevIncomeFlow
                ) { main, prevExpenses, prevIncomes ->
                    val totalExpense = main.summary?.totalExpense ?: 0.0
                    val totalIncome = main.summary?.totalIncome ?: 0.0

                    val expenseSlices = main.expenses.mapIndexed { idx, item ->
                        DonutSlice(
                            name   = item.categoryName,
                            icon   = "💰",
                            amount = item.amount,
                            percent = if (totalExpense > 0) (item.amount / totalExpense) * 100 else 0.0,
                            color  = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    val incomeSlices = main.incomes.mapIndexed { idx, item ->
                        DonutSlice(
                            name   = item.label,
                            icon   = item.emoji,
                            amount = item.amount,
                            percent = if (totalIncome > 0) (item.amount / totalIncome) * 100 else 0.0,
                            color  = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    // Construir comparativas de gastos (año actual vs anterior)
                    val prevExpenseMap = prevExpenses.associateBy { it.categoryName }
                    val categoryComparisons = main.expenses.mapIndexed { idx, curr ->
                        val prev = prevExpenseMap[curr.categoryName]
                        CategoryExpenseComparison(
                            name           = curr.categoryName,
                            icon           = "💰",
                            currentAmount  = curr.amount,
                            currentPercent = if (totalExpense > 0) (curr.amount / totalExpense) * 100 else 0.0,
                            previousAmount = prev?.amount,
                            changePercent  = if (prev != null && prev.amount > 0)
                                ((curr.amount - prev.amount) / prev.amount) * 100 else null,
                            color          = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }.sortedByDescending { it.currentAmount }

                    // Construir comparativas de ingresos (año actual vs anterior)
                    val prevIncomeMap = prevIncomes.associateBy { it.incomeType }
                    val incomeComparisons = main.incomes.mapIndexed { idx, curr ->
                        val prev = prevIncomeMap[curr.incomeType]
                        CategoryExpenseComparison(
                            name           = curr.label,
                            icon           = curr.emoji,
                            currentAmount  = curr.amount,
                            currentPercent = if (totalIncome > 0) (curr.amount / totalIncome) * 100 else 0.0,
                            previousAmount = prev?.amount,
                            changePercent  = if (prev != null && prev.amount > 0)
                                ((curr.amount - prev.amount) / prev.amount) * 100 else null,
                            color          = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }.sortedByDescending { it.currentAmount }

                    AnnualUiState(
                        summary            = main.summary,
                        monthlyBreakdown   = main.breakdown,
                        year               = year,
                        isLoading          = false,
                        canGoBack          = canGoBack,
                        expensesByCategory = expenseSlices,
                        incomeByType       = incomeSlices,
                        monthlyInvestments = main.investments,
                        categoryComparisons = categoryComparisons,
                        incomeComparisons   = incomeComparisons
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
