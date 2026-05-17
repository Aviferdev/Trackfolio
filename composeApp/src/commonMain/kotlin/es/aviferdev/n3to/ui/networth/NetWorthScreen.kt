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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.NetWorthData
import es.aviferdev.n3to.domain.model.NetWorthHistoryPoint
import es.aviferdev.n3to.ui.annual.DonutChartCard
import es.aviferdev.n3to.ui.common.*
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.common.component.TimeRangeChipRow
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.loan.AddEditLoanBottomSheet
import es.aviferdev.n3to.ui.realestate.AddEditPropertyBottomSheet
import es.aviferdev.n3to.ui.realestate.PropertyCard
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

@Composable
fun NetWorthScreen(
    onLoanClick: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    viewModel: NetWorthViewModel = koinViewModel()
) {
    val uiState              by viewModel.uiState.collectAsState()
    val showAddLoanSheet     by viewModel.showAddLoanSheet.collectAsState()
    val showAddPropertySheet by viewModel.showAddPropertySheet.collectAsState()
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
                accountId      = state.data.loans.firstOrNull()?.accountId ?: "",
                existingProperty = null,
                availableLoans = state.data.loans,
                onDismiss      = { viewModel.closeAddPropertySheet() },
                onSave         = { _, _ -> viewModel.closeAddPropertySheet() }
            )
        }
    }

    when (val state = uiState) {
        is NetWorthUiState.Loading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = PrimaryDark) }

        is NetWorthUiState.Error -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(state.message, color = ExpenseRed) }

        is NetWorthUiState.Success -> NetWorthContent(
            data              = state.data,
            netWorthHistory   = state.netWorthHistory,
            assetDistribution = state.assetDistribution,
            balancesHidden    = balancesHidden,
            onLoanClick       = onLoanClick,
            onPropertyClick   = onPropertyClick,
            onAddLoan         = { viewModel.openAddLoanSheet() },
            onAddProperty     = { viewModel.openAddPropertySheet() },
            heroVisible       = heroVisible,
            chartVisible      = chartVisible,
            assetsVisible     = assetsVisible,
            liabilitiesVisible = liabilitiesVisible
        )
    }
}

@Composable
fun NetWorthContent(
    data: NetWorthData,
    netWorthHistory: List<NetWorthHistoryPoint>,
    assetDistribution: List<DonutSlice>,
    balancesHidden: Boolean,
    onLoanClick: (String) -> Unit,
    onPropertyClick: (String) -> Unit = {},
    onAddLoan: () -> Unit,
    onAddProperty: () -> Unit = {},
    heroVisible: Boolean = true,
    chartVisible: Boolean = true,
    assetsVisible: Boolean = true,
    liabilitiesVisible: Boolean = true,
    modifier: Modifier = Modifier
) {

    var selectedTimeRange by remember { mutableStateOf(TimeRange.ALL_TIME) }

    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }

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

    val filteredHistory = remember(historyPoints, selectedTimeRange) {
        if (selectedTimeRange == TimeRange.ALL_TIME) {
            historyPoints
        } else {
            val cutoff = nowMillis - selectedTimeRange.windowDays * 86_400_000L
            historyPoints.filter { it.first >= cutoff }
        }
    }

    Column(modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(title = "Patrimonio")

        LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        // ── Hero patrimonio neto (animated) ────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = heroVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
            ) {
                NetWorthHeroCard(data = data, balancesHidden = balancesHidden)
            }
        }

        // ── Gráfico evolución (animated) ───────────────────────────────────────
        if (netWorthHistory.size >= 2) {
            item {
                AnimatedVisibility(
                    visible = chartVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Column {
                        TimeRangeChipRow(
                            selected = selectedTimeRange,
                            onSelect = { selectedTimeRange = it }
                        )
                        Spacer(Modifier.height(8.dp))
                        LineChartCard(
                            title          = "Evolución del patrimonio",
                            subtitle       = "Patrimonio neto mensual",
                            points         = filteredHistory,
                            lineColor      = PrimaryDark,
                            balancesHidden = balancesHidden,
                            rotateXLabels  = true,
                            timeRangeLabel = if (selectedTimeRange != TimeRange.ALL_TIME) {
                                when (selectedTimeRange) {
                                    TimeRange.LAST_MONTH -> "Último mes"
                                    TimeRange.LAST_YEAR -> "Último año"
                                    else -> null
                                }
                            } else null
                        )
                    }
                }
            }
        }

        // ── Sección activos (animated) ─────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = assetsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
            ) {
                Column {
                    N3toLabel(text = "Activos", modifier = Modifier.padding(bottom = 8.dp))

                    if (assetDistribution.isNotEmpty()) {
                        DonutChartCard(
                            title          = "Distribución · Activos",
                            subtitle       = "",
                            slices         = assetDistribution,
                            totalAmount    = data.totalAssets,
                                                        balancesHidden = balancesHidden
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    AssetsSummaryCard(data = data, balancesHidden = balancesHidden)

                    // ── Sección Inmuebles (dentro de Activos) ─────────────────
                    if (data.properties.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        N3toLabel(text = "Inmuebles", modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
        }

        // ── Cards de propiedades (dentro de Activos, fuera del AnimatedVisibility de activos) ─
        items(data.properties, key = { it.id }) { property ->
            AnimatedVisibility(
                visible = assetsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
            ) {
                val linkedLoan = data.loans.find { it.id == property.linkedLoanId }
                PropertyCard(
                    property   = property,
                    linkedLoan = linkedLoan,
                    onClick    = { onPropertyClick(property.id) }
                )
            }
        }

        // Botón añadir propiedad (dentro de Activos)
        if (data.properties.isNotEmpty() || true) {
            item {
                AnimatedVisibility(
                    visible = assetsVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = onAddProperty,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryAlpha)
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Añadir propiedad",
                                tint               = PrimaryDark,
                                modifier           = Modifier.size(15.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Añadir propiedad",
                            fontSize = 13.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }

        // ── Sección pasivos (animated) ─────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = liabilitiesVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    N3toLabel(text = "Pasivos")
                    IconButton(
                        onClick  = onAddLoan,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryAlpha)
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Añadir préstamo",
                            tint               = PrimaryDark,
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
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin pasivos registrados", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            }
        }

        if (data.totalDebtsOwing > 0.0) {
            item {
                AnimatedVisibility(
                    visible = liabilitiesVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
                ) {
                    EverydayDebtsRow(amount = data.totalDebtsOwing, balancesHidden = balancesHidden)
                }
            }
        }

        items(data.loans, key = { it.id }) { loan ->
            AnimatedVisibility(
                visible = liabilitiesVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
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
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            N3toLabel(text = "Patrimonio neto", color = Color.White.copy(alpha = 0.60f))
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
                    label          = "Activos",
                    value          = "+${maskAmount(formatCurrency(data.totalAssets), balancesHidden)}",
                    color          = Color(0xFFB4FFB4)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                        .align(Alignment.CenterVertically)
                )
                NetWorthMetric(
                    label          = "Pasivos",
                    value          = "−${maskAmount(formatCurrency(data.totalLiabilities), balancesHidden)}",
                    color          = Color(0xFFFFB4B4)
                )
            }
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
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AssetRow("Balance cuentas", data.totalAccountBalance, balancesHidden)
            if (data.totalPortfolioValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow("Portfolio inversiones", data.totalPortfolioValue, balancesHidden)
            }
            if (data.totalFixedIncomeValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow("Renta fija", data.totalFixedIncomeValue, balancesHidden)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGray2, thickness = 0.5.dp)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total activos", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryDark)
                Text(
                    maskAmount(formatCurrency(data.totalAssets), balancesHidden),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = PrimaryDark
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
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Deudas cotidianas", fontSize = 13.sp, color = TextSecondary)
            Text(
                "−${maskAmount(formatCurrency(amount), balancesHidden)}",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = ExpenseRed
            )
        }
    }
}

// ─── Loan card (con progress bar del paquete común) ─────────────────────────
@Composable
private fun LoanCard(loan: Loan, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(loan.type.toMaterialIcon(), contentDescription = null, modifier = Modifier.size(22.dp), tint = PrimaryDark)
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
                    Text("de ${formatCurrency(loan.totalAmount)}", fontSize = 10.sp, color = TextTertiary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Usar ProgressBar del paquete común
            ProgressBar(
                progress = loan.progressPercent,
                color    = PrimaryDark,
                height   = 4.dp
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${loan.paidInstallments}/${loan.totalInstallments} cuotas", fontSize = 10.sp, color = TextTertiary)
                Text("${formatCurrency(loan.monthlyPayment)}/mes", fontSize = 10.sp, color = TextTertiary)
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
                totalAccountBalance = 25000.0,
                totalPortfolioValue = 75000.0,
                totalFixedIncomeValue = 15000.0,
                totalRealEstateValue = 250000.0,
                totalLoansOutstanding = 30000.0,
                totalDebtsOwing = 2000.0,
                loans = emptyList(),
                properties = emptyList()
            ),
            netWorthHistory = listOf(
                NetWorthHistoryPoint("2026-01", 75000.0, 110000.0, 35000.0),
                NetWorthHistoryPoint("2026-02", 80000.0, 115000.0, 35000.0),
                NetWorthHistoryPoint("2026-03", 83000.0, 118000.0, 35000.0)
            ),
            assetDistribution = listOf(
                DonutSlice("Cuentas", "🏦", 25000.0, 21.74, Color(0xFF4CAF50)),
                DonutSlice("Inversiones", "📈", 75000.0, 65.22, Color(0xFF2196F3)),
                DonutSlice("Renta fija", "🏛️", 15000.0, 13.04, Color(0xFFFF9800)),
                DonutSlice("Inmuebles", "🏠", 250000.0, 68.49, Color(0xFF8D6E63))
            ),
            balancesHidden = false,
            onLoanClick = {},
            onPropertyClick = {},
            onAddLoan = {},
            onAddProperty = {}
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun formatCurrency(amount: Double): String {
    val absVal  = abs(amount)
    val prefix  = if (amount < 0) "-" else ""
    return "$prefix${formatAmount(absVal)} €"
}