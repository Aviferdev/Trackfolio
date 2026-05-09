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
import es.aviferdev.trackfolio.ui.loan.AddEditLoanBottomSheet
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetWorthScreen(
    onLoanClick: (String) -> Unit = {},
    viewModel: NetWorthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddLoanSheet by viewModel.showAddLoanSheet.collectAsState()

    if (showAddLoanSheet) {
        AddEditLoanBottomSheet(
            onDismiss = { viewModel.closeAddLoanSheet() }
        )
    }

    when (val state = uiState) {
        is NetWorthUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        }
        is NetWorthUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = Color.Red)
            }
        }
        is NetWorthUiState.Success -> {
            NetWorthContent(
                data        = state.data,
                onLoanClick = onLoanClick,
                onAddLoan   = { viewModel.openAddLoanSheet() }
            )
        }
    }
}

@Composable
private fun NetWorthContent(
    data: NetWorthData,
    onLoanClick: (String) -> Unit,
    onAddLoan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Tarjeta de patrimonio neto ────────────────────────────────────────
        item {
            Text(
                text = "Patrimonio",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
        }

        item {
            NetWorthSummaryCard(data)
        }

        // ── Sección Activos ──────────────────────────────────────────────────
        item {
            Text(
                text = "Activos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AssetsSummaryCard(data)
        }

        // ── Sección Pasivos ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pasivos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryDark
                )
                IconButton(onClick = onAddLoan) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Añadir préstamo",
                        tint = PrimaryDark
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
                        .background(SurfaceElevated)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin pasivos registrados",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Deudas cotidianas
        if (data.totalDebtsOwing > 0.0) {
            item {
                DebtsSummaryRow(data.totalDebtsOwing)
            }
        }

        // Préstamos
        items(data.loans, key = { it.id }) { loan ->
            LoanCard(
                loan = loan,
                onClick = { onLoanClick(loan.id) }
            )
        }
    }
}

@Composable
private fun NetWorthSummaryCard(data: NetWorthData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Patrimonio neto",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatCurrency(data.netWorth),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Activos", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        formatCurrency(data.totalAssets),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pasivos", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        formatCurrency(data.totalLiabilities),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetsSummaryCard(data: NetWorthData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SummaryRow("Balance cuentas", data.totalAccountBalance)
            if (data.totalPortfolioValue > 0) {
                Spacer(Modifier.height(8.dp))
                SummaryRow("Portfolio inversiones", data.totalPortfolioValue)
            }
            if (data.totalFixedIncomeValue > 0) {
                Spacer(Modifier.height(8.dp))
                SummaryRow("Renta fija", data.totalFixedIncomeValue)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = TextSecondary.copy(alpha = 0.2f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total activos", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PrimaryDark)
                Text(formatCurrency(data.totalAssets), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PrimaryDark)
            }
        }
    }
}

@Composable
private fun DebtsSummaryRow(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Deudas cotidianas", fontSize = 14.sp, color = TextSecondary)
            Text(
                formatCurrency(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFEF5350)
            )
        }
    }
}

@Composable
private fun LoanCard(
    loan: Loan,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(loan.type.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            loan.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PrimaryDark
                        )
                        loan.lenderName?.let {
                            Text(it, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatCurrency(loan.outstandingPrincipal),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFFEF5350)
                    )
                    Text(
                        "de ${formatCurrency(loan.totalAmount)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { loan.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryDark,
                trackColor = TextSecondary.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${loan.paidInstallments}/${loan.totalInstallments} cuotas",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    "Cuota: ${formatCurrency(loan.monthlyPayment)}/mes",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    "${loan.currentInterestRate}%",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(formatCurrency(amount), fontSize = 13.sp, color = PrimaryDark)
    }
}

private fun formatCurrency(amount: Double): String {
    val absVal = abs(amount)
    val formatted = formatAmount(absVal)
    val prefix = if (amount < 0) "-" else ""
    return "$prefix$formatted €"
}
