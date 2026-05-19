package es.aviferdev.n3to.ui.networth

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.NetWorthData
import es.aviferdev.n3to.domain.model.NetWorthHistoryPoint
import es.aviferdev.n3to.ui.annual.DonutChartCard
import es.aviferdev.n3to.ui.common.*
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.LineChartWithTimeRange
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.loan.AddEditLoanBottomSheet
import es.aviferdev.n3to.ui.networth.components.*
import es.aviferdev.n3to.ui.realestate.AddEditPropertyBottomSheet
import es.aviferdev.n3to.ui.realestate.PropertyCard
import es.aviferdev.n3to.ui.splash.SplashLoader
import es.aviferdev.n3to.ui.valuable.AddEditValuableBottomSheet
import es.aviferdev.n3to.ui.valuable.ValuableCard
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.networth_add_loan_cd
import n3to.composeapp.generated.resources.networth_add_property_cd
import n3to.composeapp.generated.resources.networth_assets_label_alt
import n3to.composeapp.generated.resources.networth_evolution_title
import n3to.composeapp.generated.resources.networth_liabilities_label_alt
import n3to.composeapp.generated.resources.networth_monthly_title
import n3to.composeapp.generated.resources.networth_no_account_subtitle
import n3to.composeapp.generated.resources.networth_no_account_title
import n3to.composeapp.generated.resources.networth_no_liabilities
import n3to.composeapp.generated.resources.networth_no_properties
import n3to.composeapp.generated.resources.networth_no_valuables
import n3to.composeapp.generated.resources.networth_realestate_label
import n3to.composeapp.generated.resources.networth_title
import n3to.composeapp.generated.resources.networth_valuables_label
import n3to.composeapp.generated.resources.networth_add_valuable_cd
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun NetWorthScreen(
    onLoanClick: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onValuableClick: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: NetWorthViewModel = koinViewModel()
) {
    val uiState              by viewModel.uiState.collectAsState()
    val showAddLoanSheet     by viewModel.showAddLoanSheet.collectAsState()
    val showAddPropertySheet by viewModel.showAddPropertySheet.collectAsState()
    val showAddValuableSheet by viewModel.showAddValuableSheet.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

    var heroVisible by remember { mutableStateOf(false) }
    var chartVisible by remember { mutableStateOf(false) }
    var assetsVisible by remember { mutableStateOf(false) }
    var liabilitiesVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(40);  heroVisible = true
        delay(100); chartVisible = true
        delay(160); assetsVisible = true
        delay(220); liabilitiesVisible = true
    }

    if (showAddLoanSheet) {
        AddEditLoanBottomSheet(onDismiss = { viewModel.closeAddLoanSheet() })
    }

    if (showAddPropertySheet) {
        val state = uiState
        if (state is NetWorthUiState.Success) {
            AddEditPropertyBottomSheet(
                accountId        = state.data.loans.firstOrNull()?.accountId ?: "",
                existingProperty = null,
                availableLoans   = state.data.loans,
                onDismiss        = { viewModel.closeAddPropertySheet() },
                onSave           = { _, _ -> viewModel.closeAddPropertySheet() }
            )
        }
    }

    if (showAddValuableSheet) {
        val state = uiState
        if (state is NetWorthUiState.Success) {
            val accountId = state.data.loans.firstOrNull()?.accountId ?: ""
            AddEditValuableBottomSheet(
                accountId = accountId,
                onDismiss = { viewModel.closeAddValuableSheet() },
                onSave = { _, _, _ -> viewModel.closeAddValuableSheet() }
            )
        }
    }

    when (val state = uiState) {
        is NetWorthUiState.Loading -> Box(
            Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
            contentAlignment = Alignment.Center
        ) { SplashLoader() }

        is NetWorthUiState.Empty -> Box(
            Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateView(
                icon        = "\uD83C\uDFE6",
                title       = stringResource(Res.string.networth_no_account_title),
                subtitle    = stringResource(Res.string.networth_no_account_subtitle),
                actionLabel = "Ir a Ajustes",
                onAction    = onNavigateToSettings
            )
        }

        is NetWorthUiState.Error -> Box(
            Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
            contentAlignment = Alignment.Center
        ) { Text(state.message, color = MaterialTheme.appColors.expense) }

        is NetWorthUiState.Success -> NetWorthContent(
            data               = state.data,
            netWorthHistory    = state.netWorthHistory,
            assetDistribution  = state.assetDistribution,
            balancesHidden     = balancesHidden,
            onLoanClick        = onLoanClick,
            onPropertyClick    = onPropertyClick,
            onValuableClick    = onValuableClick,
            onAddLoan          = { viewModel.openAddLoanSheet() },
            onAddProperty      = { viewModel.openAddPropertySheet() },
            onAddValuable      = { viewModel.openAddValuableSheet() },
            heroVisible        = heroVisible,
            chartVisible       = chartVisible,
            assetsVisible      = assetsVisible,
            liabilitiesVisible = liabilitiesVisible
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun NetWorthContent(
    data: NetWorthData,
    netWorthHistory: List<NetWorthHistoryPoint>,
    assetDistribution: List<DonutSlice>,
    balancesHidden: Boolean,
    onLoanClick: (String) -> Unit,
    onPropertyClick: (String) -> Unit = {},
    onValuableClick: (String) -> Unit = {},
    onAddLoan: () -> Unit,
    onAddProperty: () -> Unit = {},
    onAddValuable: () -> Unit = {},
    heroVisible: Boolean = true,
    chartVisible: Boolean = true,
    assetsVisible: Boolean = true,
    liabilitiesVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val historyPoints = remember(netWorthHistory) {
        netWorthHistory.map { point ->
            val parts   = point.yearMonth.split("-")
            val year    = parts[0].toInt()
            val month   = parts[1].toInt()
            val lastDay = when (month) {
                2        -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                4,6,9,11 -> 30
                else     -> 31
            }
            val epoch = LocalDateTime(year, month, lastDay, 23, 59, 59)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
            epoch to point.netWorth
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarApp(
            title = stringResource(Res.string.networth_title),
            navigateBack = null
        )

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                AnimatedVisibility(
                    visible = heroVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    NetWorthHeroCard(data = data, balancesHidden = balancesHidden)
                }
            }

            if (netWorthHistory.size >= 2) {
                item {
                    AnimatedVisibility(
                        visible = chartVisible,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                    ) {
                        LineChartWithTimeRange(
                            title          = stringResource(Res.string.networth_evolution_title),
                            subtitle       = stringResource(Res.string.networth_monthly_title),
                            points         = historyPoints,
                            lineColor      = MaterialTheme.appColors.cyanAccent,
                            balancesHidden = balancesHidden
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Column {
                        N3toLabel(
                            text     = stringResource(Res.string.networth_assets_label_alt),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (assetDistribution.isNotEmpty()) {
                            DonutChartCard(
                                title          = stringResource(Res.string.networth_assets_label_alt),
                                subtitle       = "",
                                slices         = assetDistribution,
                                totalAmount    = data.totalAssets,
                                balancesHidden = balancesHidden
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        AssetsSummaryCard(data = data, balancesHidden = balancesHidden)
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        N3toLabel(text = stringResource(Res.string.networth_realestate_label))
                        IconButton(
                            onClick  = onAddProperty,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.appColors.navySelected)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Add,
                                contentDescription = stringResource(Res.string.networth_add_property_cd),
                                tint               = MaterialTheme.appColors.cyanAccent,
                                modifier           = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            if (data.properties.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = assetsVisible,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.appColors.navySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.networth_no_properties), color = MaterialTheme.appColors.textTertiary, fontSize = 13.sp)
                        }
                    }
                }
            }

            items(data.properties, key = { it.id }) { property ->
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    val linkedLoan = data.loans.find { it.id == property.linkedLoanId }
                    PropertyCard(
                        property   = property,
                        linkedLoan = linkedLoan,
                        onClick    = { onPropertyClick(property.id) }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        N3toLabel(text = stringResource(Res.string.networth_valuables_label))
                        IconButton(
                            onClick  = onAddValuable,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.appColors.navySelected)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(Res.string.networth_add_valuable_cd),
                                tint        = MaterialTheme.appColors.cyanAccent,
                                modifier    = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            if (data.valuables.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = assetsVisible,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.appColors.navySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.networth_no_valuables), color = MaterialTheme.appColors.textTertiary, fontSize = 13.sp)
                        }
                    }
                }
            }

            items(data.valuables, key = { it.id }) { valuable ->
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    ValuableCard(
                        valuable = valuable,
                        onClick  = { onValuableClick(valuable.id) }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = liabilitiesVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        N3toLabel(text = stringResource(Res.string.networth_liabilities_label_alt))
                        IconButton(
                            onClick  = onAddLoan,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.appColors.navySelected)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Add,
                                contentDescription = stringResource(Res.string.networth_add_loan_cd),
                                tint               = MaterialTheme.appColors.cyanAccent,
                                modifier           = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            if (data.loans.isEmpty() && data.totalDebtsOwing <= 0.0) {
                item {
                    AnimatedVisibility(
                        visible = liabilitiesVisible,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.appColors.navySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.networth_no_liabilities), color = MaterialTheme.appColors.textTertiary, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (data.totalDebtsOwing > 0.0) {
                item {
                    AnimatedVisibility(
                        visible = liabilitiesVisible,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                    ) {
                        EverydayDebtsRow(amount = data.totalDebtsOwing, balancesHidden = balancesHidden)
                    }
                }
            }

            items(data.loans, key = { it.id }) { loan ->
                AnimatedVisibility(
                    visible = liabilitiesVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    LoanCard(loan = loan, onClick = { onLoanClick(loan.id) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Preview
@Composable
fun NetWorthContentPreview() {
    N3toTheme {
        NetWorthContent(
            data = NetWorthData(
                totalAccountBalance   = 25000.0,
                totalPortfolioValue   = 75000.0,
                totalFixedIncomeValue = 15000.0,
                totalRealEstateValue  = 250000.0,
                totalLoansOutstanding = 30000.0,
                totalDebtsOwing       = 2000.0,
                loans                 = emptyList(),
                properties            = emptyList()
            ),
            netWorthHistory = listOf(
                NetWorthHistoryPoint("2026-01", 75000.0, 110000.0, 35000.0),
                NetWorthHistoryPoint("2026-02", 80000.0, 115000.0, 35000.0),
                NetWorthHistoryPoint("2026-03", 83000.0, 118000.0, 35000.0)
            ),
            assetDistribution = listOf(
                DonutSlice("Cuentas",     "🏦", 25000.0,  21.74, DonutAccounts),
                DonutSlice("Inversiones", "📈", 75000.0,  65.22, DonutInvestments),
                DonutSlice("Renta fija",  "🏛️", 15000.0,  13.04, WarnOrange),
                DonutSlice("Inmuebles",   "🏠", 250000.0, 68.49, DonutRealEstate)
            ),
            balancesHidden = false,
            onLoanClick    = {},
            onPropertyClick = {},
            onValuableClick = {},
            onAddLoan      = {},
            onAddProperty  = {},
            onAddValuable  = {}
        )
    }
}
