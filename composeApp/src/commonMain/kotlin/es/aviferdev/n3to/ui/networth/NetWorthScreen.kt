package es.aviferdev.n3to.ui.networth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.NetWorthData
import es.aviferdev.n3to.domain.model.NetWorthHistoryPoint
import es.aviferdev.n3to.ui.annual.DonutChartCard
import es.aviferdev.n3to.ui.common.*
import es.aviferdev.n3to.ui.common.LineChartWithTimeRange
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.loan.AddEditLoanBottomSheet
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
import n3to.composeapp.generated.resources.networth_accounts_label
import n3to.composeapp.generated.resources.networth_add_loan_cd
import n3to.composeapp.generated.resources.networth_add_property_cd
import n3to.composeapp.generated.resources.networth_assets_label
import n3to.composeapp.generated.resources.networth_assets_label_alt
import n3to.composeapp.generated.resources.networth_debts_label
import n3to.composeapp.generated.resources.networth_evolution_title
import n3to.composeapp.generated.resources.networth_fixedincome_label
import n3to.composeapp.generated.resources.networth_installments_format
import n3to.composeapp.generated.resources.networth_investments_label
import n3to.composeapp.generated.resources.networth_liabilities_label
import n3to.composeapp.generated.resources.networth_liabilities_label_alt
import n3to.composeapp.generated.resources.networth_monthly_format
import n3to.composeapp.generated.resources.networth_monthly_title
import n3to.composeapp.generated.resources.networth_no_data
import n3to.composeapp.generated.resources.networth_no_liabilities
import n3to.composeapp.generated.resources.networth_no_properties
import n3to.composeapp.generated.resources.networth_of_format
import n3to.composeapp.generated.resources.networth_portfolio_label
import n3to.composeapp.generated.resources.networth_realestate_label
import n3to.composeapp.generated.resources.networth_title
import n3to.composeapp.generated.resources.networth_total_label
import n3to.composeapp.generated.resources.networth_total_assets_label
import n3to.composeapp.generated.resources.realestate_detail_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.time.ExperimentalTime

@Composable
fun NetWorthScreen(
    onLoanClick: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onValuableClick: (String) -> Unit = {},
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
            Modifier.fillMaxSize().background(NavyDeep),
            contentAlignment = Alignment.Center
        ) { SplashLoader() }

        is NetWorthUiState.Error -> Box(
            Modifier.fillMaxSize().background(NavyDeep),
            contentAlignment = Alignment.Center
        ) { Text(state.message, color = ExpenseRed) }

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
            .background(NavyDeep)
    ) {
        // ── Cabecera ──────────────────────────────────────────────────────────
        TopBarApp(
            title = stringResource(Res.string.networth_title),
            navigateBack = null
        )

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero patrimonio neto ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = heroVisible,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    NetWorthHeroCard(data = data, balancesHidden = balancesHidden)
                }
            }

            // ── Gráfico evolución ─────────────────────────────────────────────
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
                            lineColor      = CyanAccent,
                            balancesHidden = balancesHidden
                        )
                    }
                }
            }

            // ── Sección: Activos ──────────────────────────────────────────────
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

            // ── Sub-sección: Inmuebles ────────────────────────────────────────
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
                                .background(NavySelected)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Add,
                                contentDescription = stringResource(Res.string.networth_add_property_cd),
                                tint               = CyanAccent,
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
                                .background(NavySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.networth_no_properties), color = TextTertiary, fontSize = 13.sp)
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

            // ── Sub-sección: Bienes (Valuable) ──────────────────────────────
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
                        N3toLabel(text = "Bienes")
                        IconButton(
                            onClick  = onAddValuable,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySelected)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Añadir bien",
                                tint        = CyanAccent,
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
                                .background(NavySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay bienes registrados", color = TextTertiary, fontSize = 13.sp)
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

            // ── Sección: Pasivos ──────────────────────────────────────────────
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
                                .background(NavySelected)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Add,
                                contentDescription = stringResource(Res.string.networth_add_loan_cd),
                                tint               = CyanAccent,
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
                                .background(NavySurface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.networth_no_liabilities), color = TextTertiary, fontSize = 13.sp)
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

// ─── Hero card ────────────────────────────────────────────────────────────────
@Composable
private fun NetWorthHeroCard(data: NetWorthData, balancesHidden: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(NavySurface, NavySurfaceLight),
                        start  = Offset(0f, 0f),
                        end    = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 40.dp.toPx()
                val cy = 40.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            N3toLabel(text = stringResource(Res.string.networth_total_label), color = Color.White.copy(alpha = 0.60f))
            Spacer(Modifier.height(6.dp))
            Text(
                maskAmount(formatCurrency(data.netWorth), balancesHidden),
                fontSize      = 28.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetWorthMetric(
                    label = stringResource(Res.string.networth_assets_label_alt),
                    value = "+${maskAmount(formatCurrency(data.totalAssets), balancesHidden)}",
                    color = PnLPositiveSoft
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                        .align(Alignment.CenterVertically)
                )
                NetWorthMetric(
                    label = stringResource(Res.string.networth_liabilities_label_alt),
                    value = "−${maskAmount(formatCurrency(data.totalLiabilities), balancesHidden)}",
                    color = PnLNegativeSoft
                )
            }
    }
}

@Composable
private fun NetWorthMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─── Assets summary card ──────────────────────────────────────────────────────
@Composable
private fun AssetsSummaryCard(data: NetWorthData, balancesHidden: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AssetRow(stringResource(Res.string.networth_accounts_label), data.totalAccountBalance, balancesHidden)
            if (data.totalPortfolioValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = NavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow(stringResource(Res.string.networth_portfolio_label), data.totalPortfolioValue, balancesHidden)
            }
            if (data.totalFixedIncomeValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = NavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow(stringResource(Res.string.networth_fixedincome_label), data.totalFixedIncomeValue, balancesHidden)
            }
            if (data.totalValuablesValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = NavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow("Bienes", data.totalValuablesValue, balancesHidden)
            }
            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 12.dp),
                color     = NavyBorder,
                thickness = 0.5.dp
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.networth_total_assets_label),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = CyanAccent
                )
                Text(
                    maskAmount(formatCurrency(data.totalAssets), balancesHidden),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = CyanAccent
                )
            }
        }
    }
}

@Composable
private fun AssetRow(label: String, amount: Double, balancesHidden: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(
            maskAmount(formatCurrency(amount), balancesHidden),
            fontSize   = 13.sp,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Everyday debts row ───────────────────────────────────────────────────────
@Composable
private fun EverydayDebtsRow(amount: Double, balancesHidden: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(stringResource(Res.string.networth_debts_label), fontSize = 13.sp, color = TextSecondary)
            Text(
                "−${maskAmount(formatCurrency(amount), balancesHidden)}",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = ExpenseRed
            )
        }
    }
}

// ─── Loan card ────────────────────────────────────────────────────────────────
@Composable
private fun LoanCard(loan: Loan, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        loan.type.toMaterialIcon(),
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = CyanAccent
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(loan.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                        loan.lenderName?.let { Text(it, fontSize = 11.sp, color = TextTertiary) }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "−${formatCurrency(loan.outstandingPrincipal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        color      = ExpenseRed
                    )
                    Text(stringResource(Res.string.networth_of_format, formatCurrency(loan.totalAmount)), fontSize = 10.sp, color = TextTertiary)
                }
            }

            Spacer(Modifier.height(12.dp))

            ProgressBar(
                progress = loan.progressPercent,
                color    = CyanAccent,
                height   = 4.dp
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(Res.string.networth_installments_format, loan.paidInstallments, loan.totalInstallments), fontSize = 10.sp, color = TextTertiary)
                Text(stringResource(Res.string.networth_monthly_format, formatCurrency(loan.monthlyPayment)), fontSize = 10.sp, color = TextTertiary)
                Text("${loan.currentInterestRate}%", fontSize = 10.sp, color = TextTertiary)
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
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

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun formatCurrency(amount: Double): String {
    val absVal = abs(amount)
    val prefix = if (amount < 0) "-" else ""
    return "$prefix${formatAmount(absVal)} €"
}
