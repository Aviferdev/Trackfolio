package es.aviferdev.n3to.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.annual.components.BudgetStatusCard
import es.aviferdev.n3to.ui.annual.components.CategoryExpenseList
import es.aviferdev.n3to.ui.annual.components.MonthlyBarChart
import es.aviferdev.n3to.ui.annual.components.YearTotalsCard
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.localizedMonthNames
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_expense_categories_title
import n3to.composeapp.generated.resources.annual_income_types_title
import n3to.composeapp.generated.resources.annual_no_data_subtitle
import n3to.composeapp.generated.resources.annual_no_data_title
import n3to.composeapp.generated.resources.annual_tab_month
import n3to.composeapp.generated.resources.annual_tab_year
import n3to.composeapp.generated.resources.annual_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnnualSummaryScreen(
    navigateBack: () -> Unit = {},
    onNavigateToExpenseSettings: () -> Unit = {},
    viewModel: AnnualViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    val monthNames = localizedMonthNames().map { it.replaceFirstChar { c -> c.uppercase() } }
    val displayLabel = if (uiState.viewMode == SummaryViewMode.MONTHLY) {
        "${monthNames.getOrElse(uiState.month.toIntOrNull()?.minus(1) ?: 0) { uiState.month }} ${uiState.year}"
    } else {
        uiState.year
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.annual_title),
            navigateBack = navigateBack
        )
        TimeStepperHeader(
            currentValue = displayLabel,
            canGoBack = uiState.canGoBack,
            canGoForward = uiState.canGoForward,
            onPrevious = { viewModel.previousPeriod() },
            onNext = { viewModel.nextPeriod() },
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navySurface
        )
        ViewModeToggle(
            viewMode = uiState.viewMode,
            onToggle = { viewModel.toggleViewMode() }
        )
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else if (uiState.viewMode == SummaryViewMode.ANNUAL && uiState.summary == null) {
            EmptyStateView(
                icon = Icons.Outlined.BarChart,
                title = stringResource(Res.string.annual_no_data_title),
                subtitle = stringResource(Res.string.annual_no_data_subtitle)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (uiState.viewMode) {
                    SummaryViewMode.ANNUAL -> {
                        val summary = uiState.summary!!
                        YearTotalsCard(
                            totalIncome = summary.totalIncome,
                            totalExpense = summary.totalExpense,
                            balancesHidden = balancesHidden
                        )
                        MonthlyBarChart(
                            breakdown = uiState.monthlyBreakdown,
                            year = uiState.year
                        )
                        GoalSummaryCard(goalProgress = uiState.goalProgress)
                        BudgetStatusCard(
                            budgetStatus = uiState.budgetStatus,
                            balancesHidden = balancesHidden,
                            onConfigureBudgets = onNavigateToExpenseSettings
                        )
                        CategoryExpenseList(
                            title = stringResource(Res.string.annual_expense_categories_title),
                            comparisons = uiState.categoryComparisons,
                            isExpense = true,
                            balancesHidden = balancesHidden
                        )
                        CategoryExpenseList(
                            title = stringResource(Res.string.annual_income_types_title),
                            comparisons = uiState.incomeComparisons,
                            isExpense = false,
                            balancesHidden = balancesHidden
                        )
                        InvestmentBarChart(
                            investments = uiState.monthlyInvestments,
                            year = uiState.year,
                            balancesHidden = balancesHidden
                        )
                    }

                    SummaryViewMode.MONTHLY -> {
                        val totals = uiState.monthlyTotals
                        YearTotalsCard(
                            totalIncome = totals?.totalIncome ?: 0.0,
                            totalExpense = totals?.totalExpense ?: 0.0,
                            balancesHidden = balancesHidden
                        )
                        CategoryExpenseList(
                            title = stringResource(Res.string.annual_expense_categories_title),
                            comparisons = uiState.categoryComparisons,
                            isExpense = true,
                            balancesHidden = balancesHidden
                        )
                        BudgetStatusCard(
                            budgetStatus = uiState.budgetStatus,
                            balancesHidden = balancesHidden,
                            onConfigureBudgets = onNavigateToExpenseSettings
                        )
                        CategoryExpenseList(
                            title = stringResource(Res.string.annual_income_types_title),
                            comparisons = uiState.incomeComparisons,
                            isExpense = false,
                            balancesHidden = balancesHidden
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(
    viewMode: SummaryViewMode,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.appColors.navySurface)
            .padding(horizontal = 8.dp)
    ) {
        Surface(
            onClick = { if (viewMode != SummaryViewMode.ANNUAL) onToggle() },
            shape = RoundedCornerShape(6.dp),
            color = if (viewMode == SummaryViewMode.ANNUAL) {
                MaterialTheme.appColors.navyBorder
            } else {
                Color.Transparent
            },
            shadowElevation = if (viewMode == SummaryViewMode.ANNUAL) {
                2.dp
            } else {
                0.dp
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.annual_tab_year),
                fontSize = 13.sp,
                fontWeight = if (viewMode == SummaryViewMode.ANNUAL) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                color = if (viewMode == SummaryViewMode.ANNUAL) {
                    MaterialTheme.appColors.primary
                } else {
                    MaterialTheme.appColors.textSecondary
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        Surface(
            onClick = { if (viewMode != SummaryViewMode.MONTHLY) onToggle() },
            shape = RoundedCornerShape(6.dp),
            color = if (viewMode == SummaryViewMode.MONTHLY) {
                MaterialTheme.appColors.navyBorder
            } else {
                Color.Transparent
            },
            shadowElevation = if (viewMode == SummaryViewMode.MONTHLY) {
                2.dp
            } else {
                0.dp
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.annual_tab_month),
                fontSize = 13.sp,
                fontWeight = if (viewMode == SummaryViewMode.MONTHLY) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                color = if (viewMode == SummaryViewMode.MONTHLY) {
                    MaterialTheme.appColors.primary
                } else {
                    MaterialTheme.appColors.textSecondary
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
