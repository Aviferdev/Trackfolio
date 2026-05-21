package es.aviferdev.n3to.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.annual.components.AnnualTabs
import es.aviferdev.n3to.ui.annual.components.GastosTab
import es.aviferdev.n3to.ui.annual.components.IngresosTab
import es.aviferdev.n3to.ui.annual.components.ResumenTab
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_no_data_subtitle
import n3to.composeapp.generated.resources.annual_no_data_title
import n3to.composeapp.generated.resources.annual_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

enum class AnnualTab {
    RESUMEN, GASTOS, INGRESOS, INVERSIONES
}

@Composable
fun AnnualSummaryScreen(
    navigateBack: () -> Unit = {},
    onNavigateToExpenseSettings: () -> Unit = {},
    viewModel: AnnualViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var selectedTab by remember { mutableStateOf(AnnualTab.RESUMEN) }

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
            currentValue = uiState.year,
            canGoBack = uiState.canGoBack,
            onPrevious = { viewModel.previousYear() },
            onNext = { viewModel.nextYear() },
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
        )

        // Tabs
        AnnualTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else {
            when (selectedTab) {
                AnnualTab.RESUMEN -> {
                    if (uiState.summary != null) {
                        ResumenTab(
                            summary = uiState.summary!!,
                            breakdown = uiState.monthlyBreakdown,
                            balancesHidden = balancesHidden,
                            goalProgress = uiState.goalProgress
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Outlined.BarChart,
                            title = stringResource(Res.string.annual_no_data_title),
                            subtitle = stringResource(Res.string.annual_no_data_subtitle)
                        )
                    }
                }

                AnnualTab.GASTOS -> {
                    GastosTab(
                        breakdown = uiState.monthlyBreakdown,
                        comparisons = uiState.categoryComparisons,
                        year = uiState.year,
                        balancesHidden = balancesHidden,
                        budgetStatus = uiState.budgetStatus,
                        onConfigureBudgets = onNavigateToExpenseSettings
                    )
                }

                AnnualTab.INGRESOS -> {
                    IngresosTab(
                        breakdown = uiState.monthlyBreakdown,
                        comparisons = uiState.incomeComparisons,
                        year = uiState.year,
                        balancesHidden = balancesHidden
                    )
                }

                AnnualTab.INVERSIONES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        InvestmentBarChart(
                            investments = uiState.monthlyInvestments,
                            year = uiState.year,
                            balancesHidden = balancesHidden
                        )
                    }
                }
            }
        }
    }
}
