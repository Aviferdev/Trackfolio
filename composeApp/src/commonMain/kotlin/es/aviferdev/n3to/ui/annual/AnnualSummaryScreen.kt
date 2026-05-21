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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.annual.components.CategoryExpenseList
import es.aviferdev.n3to.ui.annual.components.MonthlyBarChart
import es.aviferdev.n3to.ui.annual.components.YearTotalsCard
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_expense_categories_title
import n3to.composeapp.generated.resources.annual_income_types_title
import n3to.composeapp.generated.resources.annual_no_data_subtitle
import n3to.composeapp.generated.resources.annual_no_data_title
import n3to.composeapp.generated.resources.annual_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnnualSummaryScreen(
    navigateBack: () -> Unit = {},
    onNavigateToExpenseSettings: () -> Unit = {},
    viewModel: AnnualViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

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
            currentValue = uiState.displayLabel,
            canGoBack = uiState.canGoBack,
            canGoForward = uiState.canGoForward,
            onPrevious = { viewModel.previousPeriod() },
            onNext = { viewModel.nextPeriod() },
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
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
                        CategoryExpenseList(
                            title = stringResource(Res.string.annual_expense_categories_title),
                            comparisons = uiState.categoryComparisons,
                            isExpense = true,
                            balancesHidden = balancesHidden,
                            budgetStatus = uiState.budgetStatus,
                            onConfigureBudgets = onNavigateToExpenseSettings
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
                            balancesHidden = balancesHidden,
                            budgetStatus = uiState.budgetStatus,
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
    onToggle: () -> Unit
) {
    Surface(
        color = MaterialTheme.appColors.navySurface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.appColors.navyDeep)
                    .padding(2.dp)
            ) {
                ModeChip(
                    label = "Año",
                    selected = viewMode == SummaryViewMode.ANNUAL,
                    onClick = { if (viewMode != SummaryViewMode.ANNUAL) onToggle() }
                )
                ModeChip(
                    label = "Mes",
                    selected = viewMode == SummaryViewMode.MONTHLY,
                    onClick = { if (viewMode != SummaryViewMode.MONTHLY) onToggle() }
                )
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.appColors.navySurface else androidx.compose.ui.graphics.Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
    }
}
