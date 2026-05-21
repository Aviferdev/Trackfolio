package es.aviferdev.n3to.ui.annual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.usecase.assettransaction.GetMonthlyInvestmentsUseCase
import es.aviferdev.n3to.domain.usecase.budget.GetCategoryBudgetStatusUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetYearlyGoalProgressUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetAnnualSummaryUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetExpensesByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetIncomeByTypeUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.platform.nowLocalDateTime
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.DonutSlice
import es.aviferdev.n3to.ui.theme.CategoryPalette
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AnnualUiState(
    val summary: AnnualSummary? = null,
    val monthlyBreakdown: List<MonthlyTotals> = emptyList(),
    val year: String = "",
    val isLoading: Boolean = true,
    val canGoBack: Boolean = true,
    // Nuevos datos para gráficos
    val expensesByCategory: List<DonutSlice> = emptyList(),
    val incomeByType: List<DonutSlice> = emptyList(),
    val monthlyInvestments: List<MonthlyInvestment> = emptyList(),
    // Comparativas interanuales
    val categoryComparisons: List<CategoryExpenseComparison> = emptyList(),
    val incomeComparisons: List<CategoryExpenseComparison> = emptyList(),
    // Progreso de objetivos anuales
    val goalProgress: List<MonthlyGoalProgress> = emptyList(),
    // Estado de presupuestos por categoría
    val budgetStatus: List<CategoryBudgetStatus> = emptyList()
)

/**
 * Contenedor intermedio para los flows principales del combine.
 */
private data class MainData(
    val summary: AnnualSummary?,
    val breakdown: List<MonthlyTotals>,
    val expenses: List<CategoryBreakdown>,
    val incomes: List<IncomeTypeBreakdown>,
    val investments: List<MonthlyInvestment>,
    val goalProgress: List<MonthlyGoalProgress>,
    val budgetStatus: List<CategoryBudgetStatus>
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnnualViewModel(
    private val getAnnualSummary: GetAnnualSummaryUseCase,
    private val getMonthlyBreakdown: GetMonthlyBreakdownUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val getExpensesByCategory: GetExpensesByCategoryUseCase,
    private val getIncomeByType: GetIncomeByTypeUseCase,
    private val getMonthlyInvestments: GetMonthlyInvestmentsUseCase,
    private val getYearlyGoalProgress: GetYearlyGoalProgressUseCase? = null,
    private val getCategoryBudgetStatus: GetCategoryBudgetStatusUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val now = nowLocalDateTime()
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

                // Flujo principal con 5 fuentes de datos
                val mainFlow = combine(
                    getAnnualSummary(accountId, year),
                    getMonthlyBreakdown(accountId, year),
                    getExpensesByCategory(accountId, year),
                    getIncomeByType(accountId, year),
                    getMonthlyInvestments(accountId, year)
                ) { summary, breakdown, expenses, income, investments ->
                    MainData(summary, breakdown, expenses, income, investments, emptyList(), emptyList())
                }

                // Estado de presupuestos (flow separado)
                val budgetFlow = getCategoryBudgetStatus(accountId, year)

                // Progreso de objetivos anuales (flow separado)
                val goalFlow = if (getYearlyGoalProgress != null) {
                    getYearlyGoalProgress(accountId, year)
                } else {
                    flowOf(emptyList())
                }

                combine(
                    mainFlow,
                    goalFlow,
                    budgetFlow,
                    prevExpensesFlow,
                    prevIncomeFlow
                ) { main, goals, budget, prevExpenses, prevIncomes ->
                    val totalExpense = main.summary?.totalExpense ?: 0.0
                    val totalIncome = main.summary?.totalIncome ?: 0.0

                    val expenseSlices = main.expenses.mapIndexed { idx, item ->
                        DonutSlice(
                            name = item.categoryName,
                            icon = "💰",
                            amount = item.amount,
                            percent = if (totalExpense > 0) (item.amount / totalExpense) * 100 else 0.0,
                            color = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    val incomeSlices = main.incomes.mapIndexed { idx, item ->
                        DonutSlice(
                            name = item.label,
                            icon = item.emoji,
                            amount = item.amount,
                            percent = if (totalIncome > 0) (item.amount / totalIncome) * 100 else 0.0,
                            color = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }

                    // Construir comparativas de gastos (año actual vs anterior)
                    val prevExpenseMap = prevExpenses.associateBy { it.categoryName }
                    val categoryComparisons = main.expenses.mapIndexed { idx, curr ->
                        val prev = prevExpenseMap[curr.categoryName]
                        CategoryExpenseComparison(
                            name = curr.categoryName,
                            icon = "💰",
                            currentAmount = curr.amount,
                            currentPercent = if (totalExpense > 0) (curr.amount / totalExpense) * 100 else 0.0,
                            previousAmount = prev?.amount,
                            changePercent = if (prev != null && prev.amount > 0)
                                ((curr.amount - prev.amount) / prev.amount) * 100 else null,
                            color = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }.sortedByDescending { it.currentAmount }

                    // Construir comparativas de ingresos (año actual vs anterior)
                    val prevIncomeMap = prevIncomes.associateBy { it.incomeType }
                    val incomeComparisons = main.incomes.mapIndexed { idx, curr ->
                        val prev = prevIncomeMap[curr.incomeType]
                        CategoryExpenseComparison(
                            name = curr.label,
                            icon = curr.emoji,
                            currentAmount = curr.amount,
                            currentPercent = if (totalIncome > 0) (curr.amount / totalIncome) * 100 else 0.0,
                            previousAmount = prev?.amount,
                            changePercent = if (prev != null && prev.amount > 0)
                                ((curr.amount - prev.amount) / prev.amount) * 100 else null,
                            color = CategoryPalette[idx % CategoryPalette.size]
                        )
                    }.sortedByDescending { it.currentAmount }

                    AnnualUiState(
                        summary = main.summary,
                        monthlyBreakdown = main.breakdown,
                        year = year,
                        isLoading = false,
                        canGoBack = canGoBack,
                        expensesByCategory = expenseSlices,
                        incomeByType = incomeSlices,
                        monthlyInvestments = main.investments,
                        categoryComparisons = categoryComparisons,
                        incomeComparisons = incomeComparisons,
                        goalProgress = goals,
                        budgetStatus = budget
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnnualUiState(year = _year.value)
        )

    fun previousYear() {
        val target = _year.value.toInt() - 1
        val oldest = _oldestYear.value
        if (oldest != null && target < oldest) return
        _year.value = target.toString()
    }

    fun nextYear() {
        val nowYear = nowYear()
        if (_year.value.toInt() >= nowYear) return
        _year.value = (_year.value.toInt() + 1).toString()
    }
}
