package es.aviferdev.trackfolio.ui.networth

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
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.NetWorthData
import es.aviferdev.trackfolio.domain.model.NetWorthHistoryPoint
import es.aviferdev.trackfolio.ui.annual.DonutChartCard
import es.aviferdev.trackfolio.ui.common.DonutSlice
import es.aviferdev.trackfolio.ui.common.LineChartCard
import es.aviferdev.trackfolio.ui.loan.AddEditLoanBottomSheet
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

@Composable
fun NetWorthScreen(
    onLoanClick: (String) -> Unit = {},
    viewModel: NetWorthViewModel = koinViewModel()
) {
    val uiState          by viewModel.uiState.collectAsState()
    val showAddLoanSheet by viewModel.showAddLoanSheet.collectAsState()

    if (showAddLoanSheet) {
        AddEditLoanBottomSheet(onDismiss = { viewModel.closeAddLoanSheet() })
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
            onLoanClick       = onLoanClick,
            onAddLoan         = { viewModel.openAddLoanSheet() }
        )
    }
}

@Composable
private fun NetWorthContent(
    data: NetWorthData,
    netWorthHistory: List<NetWorthHistoryPoint>,
    assetDistribution: List<DonutSlice>,
    onLoanClick: (String) -> Unit,
    onAddLoan: () -> Unit
) {
    val balancesHidden = LocalBalanceHidden.current

    LazyColumn(
        modifier        = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Título ────────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Patrimonio",
                fontSize      = 20.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-0.3).sp
            )
        }

        // ── Hero patrimonio neto ──────────────────────────────────────────────
        item { NetWorthHeroCard(data = data, balancesHidden = balancesHidden) }

        // ── Gráfico evolución ─────────────────────────────────────────────────
        if (netWorthHistory.size >= 2) {
            item {
                LineChartCard(
                    title          = "Evolución del patrimonio",
                    subtitle       = "Patrimonio neto mensual",
                    points         = netWorthHistory.map { point ->
                        val parts   = point.yearMonth.split("-")
                        val year    = parts[0].toInt()
                        val month   = parts[1].toInt()
                        val lastDay = when (month) {
                            2        -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                            4,6,9,11 -> 30
                            else     -> 31
                        }
                        LocalDateTime(year, month, lastDay, 23, 59, 59)
                            .toInstant(TimeZone.currentSystemDefault())
                            .toEpochMilliseconds() to point.netWorth
                    },
                    lineColor      = PrimaryDark,
                    currencyCode   = "EUR",
                    balancesHidden = balancesHidden
                )
            }
        }

        // ── Sección activos ───────────────────────────────────────────────────
        item { SectionLabel("Activos") }

        if (assetDistribution.isNotEmpty()) {
            item {
                DonutChartCard(
                    title          = "Distribución de activos",
                    subtitle       = "Composición del patrimonio",
                    slices         = assetDistribution,
                    totalAmount    = data.totalAssets,
                    currencyCode   = "EUR",
                    balancesHidden = balancesHidden
                )
            }
        }

        item { AssetsSummaryCard(data = data, balancesHidden = balancesHidden) }

        // ── Sección pasivos ───────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                SectionLabel("Pasivos")
                IconButton(
                    onClick  = onAddLoan,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(PrimaryAlpha)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Añadir préstamo",
                        tint               = PrimaryDark,
                        modifier           = Modifier.size(17.dp)
                    )
                }
            }
        }

        if (data.loans.isEmpty() && data.totalDebtsOwing <= 0.0) {
            item {
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

        if (data.totalDebtsOwing > 0.0) {
            item { EverydayDebtsRow(amount = data.totalDebtsOwing, balancesHidden = balancesHidden) }
        }

        items(data.loans, key = { it.id }) { loan ->
            LoanCard(loan = loan, onClick = { onLoanClick(loan.id) })
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─── Hero card ────────────────────────────────────────────────────────────────
@Composable
private fun NetWorthHeroCard(data: NetWorthData, balancesHidden: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Patrimonio neto",
                fontSize = 12.sp,
                color    = Color.White.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                maskAmount(formatCurrency(data.netWorth), balancesHidden),
                fontSize      = 32.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetWorthMetric(
                    label          = "Activos",
                    value          = maskAmount(formatCurrency(data.totalAssets), balancesHidden),
                    color          = Color(0xFF86EFAC)
                )
                Box(
                    Modifier
                        .width(0.5.dp)
                        .height(36.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                        .align(Alignment.CenterVertically)
                )
                NetWorthMetric(
                    label          = "Pasivos",
                    value          = maskAmount(formatCurrency(data.totalLiabilities), balancesHidden),
                    color          = Color(0xFFFCA5A5)
                )
            }
        }
    }
}

@Composable
private fun NetWorthMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
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
            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 12.dp),
                color     = BorderGray2,
                thickness = 0.5.dp
            )
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

// ─── Loan card ────────────────────────────────────────────────────────────────
@Composable
private fun LoanCard(loan: Loan, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(loan.type.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            loan.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = TextPrimary
                        )
                        loan.lenderName?.let {
                            Text(it, fontSize = 11.sp, color = TextTertiary)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "−${formatCurrency(loan.outstandingPrincipal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        color      = ExpenseRed
                    )
                    Text(
                        "de ${formatCurrency(loan.totalAmount)}",
                        fontSize = 10.sp,
                        color    = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress     = { loan.progressPercent },
                modifier     = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color        = PrimaryDark,
                trackColor   = BorderGray2
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${loan.paidInstallments}/${loan.totalInstallments} cuotas",
                    fontSize = 10.sp,
                    color    = TextTertiary
                )
                Text(
                    "${formatCurrency(loan.monthlyPayment)}/mes",
                    fontSize = 10.sp,
                    color    = TextTertiary
                )
                Text(
                    "${loan.currentInterestRate}%",
                    fontSize = 10.sp,
                    color    = TextTertiary
                )
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = TextTertiary,
        letterSpacing = 0.7.sp
    )
}

private fun formatCurrency(amount: Double): String {
    val absVal  = abs(amount)
    val prefix  = if (amount < 0) "-" else ""
    return "$prefix${formatAmount(absVal)} €"
}
