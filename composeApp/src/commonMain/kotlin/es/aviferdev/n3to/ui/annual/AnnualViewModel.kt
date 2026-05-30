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
import es.aviferdev.n3to.domain.usecase.transaction.GetExpensesByCategoryByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetExpensesByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetIncomeByTypeByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetIncomeByTypeUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.platform.nowLocalDateTime
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.account.AccountSession
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

enum class SummaryViewMode { ANNUAL, MONTHLY }

data class AnnualUiState(
    val viewMode: SummaryViewMode = SummaryViewMode.ANNUAL,
    val summary: AnnualSummary? = null,
    val monthlyTotals: MonthlyTotals? = null,
    val monthlyBreakdown: List<MonthlyTotals> = emptyList(),
    val year: String = "",
    val month: String = "",
    val isLoading: Boolean = true,
    val canGoBack: Boolean = true,
    val canGoForward: Boolean = false,
    val expensesByCategory: List<CategoryBreakdown> = emptyList(),
    val incomeByType: List<IncomeTypeBreakdown> = emptyList(),
    val monthlyInvestments: List<MonthlyInvestment> = emptyList(),
    val categoryComparisons: List<CategoryExpenseComparison> = emptyList(),
    val incomeComparisons: List<CategoryExpenseComparison> = emptyList(),
    val goalProgress: List<MonthlyGoalProgress> = emptyList(),
    val budgetStatus: List<CategoryBudgetStatus> = emptyList()
)

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
    private val getMonthlyTotals: GetMonthlyTotalsUseCase,
    private val getOldestDate: GetOldestTransactionDateUseCase,
    private val getExpensesByCategory: GetExpensesByCategoryUseCase,
    private val getExpensesByCategoryByMonth: GetExpensesByCategoryByMonthUseCase,
    private val getIncomeByType: GetIncomeByTypeUseCase,
    private val getIncomeByTypeByMonth: GetIncomeByTypeByMonthUseCase,
    private val getMonthlyInvestments: GetMonthlyInvestmentsUseCase,
    private val getYearlyGoalProgress: GetYearlyGoalProgressUseCase? = null,
    private val getCategoryBudgetStatus: GetCategoryBudgetStatusUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val now = nowLocalDateTime()
    private val _viewMode = MutableStateFlow(SummaryViewMode.ANNUAL)
    private val _year = MutableStateFlow(now.year.toString())
    private val _month = MutableStateFlow(now.monthNumber.toString().padStart(2, '0'))
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
        _month,
        _viewMode,
        _oldestYear
    ) { accountId, year, month, viewMode, oldest ->
        arrayOf<Any?>(accountId, year, month, viewMode, oldest)
    }.flatMapLatest { arr ->
        val accountId = arr[0] as String?
        val year = arr[1] as String
        val month = arr[2] as String
        val viewMode = arr[3] as SummaryViewMode
        val oldest = arr[4] as Int?

        val nowY = nowYear()
        val nowM = now.monthNumber

        val canGoBack: Boolean
        val canGoForward: Boolean
        if (viewMode == SummaryViewMode.ANNUAL) {
            canGoBack = oldest == null || (year.toInt() - 1) >= oldest
            canGoForward = year.toInt() < nowY
        } else {
            val yearInt = year.toInt()
            val monthInt = month.toInt()
            canGoBack = oldest == null || yearInt > oldest || (yearInt == oldest && monthInt > 1)
            canGoForward = yearInt < nowY || (yearInt == nowY && monthInt < nowM)
        }

        if (accountId == null) {
            flowOf(
                AnnualUiState(
                    viewMode = viewMode, year = year, month = month,
                    isLoading = false, canGoBack = canGoBack, canGoForward = canGoForward
                )
            )
        } else if (viewMode == SummaryViewMode.MONTHLY) {
            buildMonthlyFlow(accountId, year, month, canGoBack, canGoForward)
        } else {
            buildAnnualFlow(accountId, year, oldest, canGoBack, canGoForward)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnnualUiState(year = _year.value, month = _month.value)
    )

    private fun buildMonthlyFlow(
        accountId: String,
        year: String,
        month: String,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) = combine(
        getMonthlyTotals(accountId, year, month),
        getExpensesByCategoryByMonth(accountId, year, month),
        getIncomeByTypeByMonth(accountId, year, month),
        getCategoryBudgetStatus(accountId, year)
    ) { totals, expenses, incomes, budget ->
        val totalExpense = totals.totalExpense
        val totalIncome = totals.totalIncome

        val expenseComparisons = expenses.mapIndexed { _, item ->
            CategoryExpenseComparison(
                name = item.categoryName,
                icon = "💰",
                currentAmount = item.amount,
                currentPercent = if (totalExpense > 0) (item.amount / totalExpense) * 100 else 0.0,
                previousAmount = null,
                changePercent = null
            )
        }.sortedByDescending { it.currentAmount }

        val incomeComparisons = incomes.mapIndexed { _, item ->
            CategoryExpenseComparison(
                name = item.label,
                icon = item.emoji,
                currentAmount = item.amount,
                currentPercent = if (totalIncome > 0) (item.amount / totalIncome) * 100 else 0.0,
                previousAmount = null,
                changePercent = null
            )
        }.sortedByDescending { it.currentAmount }

        AnnualUiState(
            viewMode = SummaryViewMode.MONTHLY,
            monthlyTotals = totals,
            year = year,
            month = month,
            isLoading = false,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            categoryComparisons = expenseComparisons,
            incomeComparisons = incomeComparisons,
            budgetStatus = budget
        )
    }

    private fun buildAnnualFlow(
        accountId: String,
        year: String,
        oldest: Int?,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) = run {
        val prevYear = (year.toInt() - 1).toString()
        val canFetchPrev = oldest == null || (year.toInt() - 1) >= oldest
        val prevExpensesFlow = if (canFetchPrev) getExpensesByCategory(accountId, prevYear)
        else flowOf(emptyList())
        val prevIncomeFlow = if (canFetchPrev) getIncomeByType(accountId, prevYear)
        else flowOf(emptyList())

        val mainFlow = combine(
            getAnnualSummary(accountId, year),
            getMonthlyBreakdown(accountId, year),
            getExpensesByCategory(accountId, year),
            getIncomeByType(accountId, year),
            getMonthlyInvestments(accountId, year)
        ) { summary, breakdown, expenses, income, investments ->
            MainData(summary, breakdown, expenses, income, investments, emptyList(), emptyList())
        }

        val budgetFlow = getCategoryBudgetStatus(accountId, year)
        val goalFlow = if (getYearlyGoalProgress != null) {
            val y = year.toIntOrNull()
            if (y != null) getYearlyGoalProgress(accountId, y) else flowOf(emptyList())
        } else {
            flowOf(emptyList())
        }

        combine(mainFlow, goalFlow, budgetFlow, prevExpensesFlow, prevIncomeFlow) { main, goals, budget, prevExpenses, prevIncomes ->
            val totalExpense = main.summary?.totalExpense ?: 0.0
            val totalIncome = main.summary?.totalIncome ?: 0.0

            val prevExpenseMap = prevExpenses.associateBy { it.categoryName }
            val categoryComparisons = main.expenses.mapIndexed { _, curr ->
                val prev = prevExpenseMap[curr.categoryName]
                CategoryExpenseComparison(
                    name = curr.categoryName, icon = "💰",
                    currentAmount = curr.amount,
                    currentPercent = if (totalExpense > 0) (curr.amount / totalExpense) * 100 else 0.0,
                    previousAmount = prev?.amount,
                    changePercent = if (prev != null && prev.amount > 0)
                        ((curr.amount - prev.amount) / prev.amount) * 100 else null
                )
            }.sortedByDescending { it.currentAmount }

            val prevIncomeMap = prevIncomes.associateBy { it.incomeType }
            val incomeComparisons = main.incomes.mapIndexed { _, curr ->
                val prev = prevIncomeMap[curr.incomeType]
                CategoryExpenseComparison(
                    name = curr.label, icon = curr.emoji,
                    currentAmount = curr.amount,
                    currentPercent = if (totalIncome > 0) (curr.amount / totalIncome) * 100 else 0.0,
                    previousAmount = prev?.amount,
                    changePercent = if (prev != null && prev.amount > 0)
                        ((curr.amount - prev.amount) / prev.amount) * 100 else null
                )
            }.sortedByDescending { it.currentAmount }

            AnnualUiState(
                viewMode = SummaryViewMode.ANNUAL,
                summary = main.summary,
                monthlyBreakdown = main.breakdown,
                year = year,
                month = "",
                isLoading = false,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                expensesByCategory = main.expenses,
                incomeByType = main.incomes,
                monthlyInvestments = main.investments,
                categoryComparisons = categoryComparisons,
                incomeComparisons = incomeComparisons,
                goalProgress = goals,
                budgetStatus = budget
            )
        }
    }

    fun toggleViewMode() {
        val now = nowLocalDateTime()
        _viewMode.value = when (_viewMode.value) {
            SummaryViewMode.ANNUAL -> {
                _month.value = now.monthNumber.toString().padStart(2, '0')
                _year.value = now.year.toString()
                SummaryViewMode.MONTHLY
            }
            SummaryViewMode.MONTHLY -> {
                _year.value = now.year.toString()
                SummaryViewMode.ANNUAL
            }
        }
    }

    fun previousPeriod() {
        if (_viewMode.value == SummaryViewMode.ANNUAL) {
            val target = _year.value.toInt() - 1
            val oldest = _oldestYear.value
            if (oldest != null && target < oldest) return
            _year.value = target.toString()
        } else {
            val y = _year.value.toInt()
            val m = _month.value.toInt()
            val oldest = _oldestYear.value
            val (newY, newM) = if (m == 1) Pair(y - 1, 12) else Pair(y, m - 1)
            if (oldest != null && newY < oldest) return
            _year.value = newY.toString()
            _month.value = newM.toString().padStart(2, '0')
        }
    }

    fun nextPeriod() {
        val now = nowLocalDateTime()
        if (_viewMode.value == SummaryViewMode.ANNUAL) {
            if (_year.value.toInt() >= nowYear()) return
            _year.value = (_year.value.toInt() + 1).toString()
        } else {
            val y = _year.value.toInt()
            val m = _month.value.toInt()
            if (y > now.year || (y == now.year && m >= now.monthNumber)) return
            val (newY, newM) = if (m == 12) Pair(y + 1, 1) else Pair(y, m + 1)
            _year.value = newY.toString()
            _month.value = newM.toString().padStart(2, '0')
        }
    }
}
