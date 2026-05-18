package es.aviferdev.n3to.ui.valuable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValuableDetailScreen(
    valuableId: String,
    onNavigateBack: () -> Unit,
    viewModel: ValuableDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    if (uiState.showEditSheet && summary != null) {
        AddEditValuableBottomSheet(
            existingValuable = summary.valuable,
            existingPurchaseExpenses = emptyList(), // simplified: would need to pass from state
            existingHoldingExpenses = emptyList(),
            accountId = summary.valuable.accountId,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { valuable, purchaseExpenses, holdingExpenses ->
                viewModel.saveValuable(valuable, purchaseExpenses, holdingExpenses)
            }
        )
    }

    if (uiState.showSellSheet && summary != null) {
        SellValuableBottomSheet(
            valuableId = summary.valuable.id,
            valuableName = summary.valuable.name,
            onDismiss = { viewModel.hideSellSheet() },
            onConfirm = { saleDate, salePrice, expenses ->
                viewModel.sellValuable(saleDate, salePrice, expenses)
            }
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteConfirmDialog(
            title = "Eliminar bien",
            message = "¿Estás seguro de eliminar este bien? También se eliminarán todas las transacciones vinculadas.",
            onConfirm = { viewModel.deleteValuable() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    // ── Loan picker dialog ───────────────────────────────────────────────
    if (uiState.showLoanPicker && summary != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLoanPicker() },
            containerColor = NavySurface,
            title = { Text("Vincular préstamo", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                if (uiState.availableLoans.isEmpty()) {
                    Text("No hay préstamos disponibles", color = TextTertiary, fontSize = 14.sp)
                } else {
                    Column {
                        uiState.availableLoans.forEach { loan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.linkLoan(loan.id)
                                        viewModel.hideLoanPicker()
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(loan.name, fontSize = 14.sp, color = TextPrimary)
                                Text(
                                    formatAmountEuro(loan.outstandingPrincipal),
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.hideLoanPicker() }) {
                    Text("Cerrar", color = TextTertiary)
                }
            }
        )
    }

    if (uiState.showValueDialog && summary != null) {
        var valueText by remember { mutableStateOf(summary.valuable.currentValue.toString()) }
        AlertDialog(
            onDismissRequest = { viewModel.hideValueDialog() },
            containerColor = NavySurface,
            title = { Text("Actualizar valor", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Nuevo valor estimado") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    valueText.toDoubleOrNull()?.let { viewModel.updateEstimatedValue(it) }
                    viewModel.hideValueDialog()
                }) { Text("Actualizar", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideValueDialog() }) { Text("Cancelar", color = TextTertiary) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(summary?.valuable?.name ?: "Detalle bien") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (summary != null) {
                        if (!summary.valuable.isSold) {
                            IconButton(onClick = { viewModel.showSellSheet() }) {
                                Icon(Icons.Outlined.Sell, contentDescription = "Vender")
                            }
                        }
                        IconButton(onClick = { viewModel.showEditSheet() }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray, titleContentColor = TextPrimary)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (summary == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
        } else {
            val valuable = summary.valuable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Hero card ──────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            if (valuable.isSold) {
                                val profit = summary.realizedProfit
                                val profitPct = summary.realizedProfitPercent
                                Text(
                                    text = if (profit != null) formatAmountEuro(profit) else "-",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (profit != null && profit >= 0) IncomeGreen else ExpenseRed
                                )
                            } else {
                                Text(
                                    text = formatAmountEuro(valuable.currentValue),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                        val profitPct = summary.realizedProfitPercent
                        if (profitPct != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                DeltaIndicator(
                                    value = formatPercentSigned(profitPct),
                                    isPositive = (summary.realizedProfit ?: 0.0) >= 0
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                        Spacer(Modifier.height(12.dp))

                        // Métricas
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MetricItem("Compra", formatAmountEuro(valuable.purchasePrice))
                            if (valuable.isSold) {
                                MetricItem("Venta", formatAmountEuro(valuable.salePrice ?: 0.0))
                            }
                            MetricItem("Gastos", formatAmountEuro(summary.totalExpenses))
                        }
                    }
                }

                // ── Información general ────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Información")
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Nombre", valuable.name)
                        if (valuable.description.isNotBlank()) {
                            DetailRow("Descripción", valuable.description)
                        }
                        DetailRow("Fecha compra", formatDate(valuable.purchaseDate))
                        DetailRow("Precio compra", formatAmountEuro(valuable.purchasePrice))
                        if (!valuable.isSold) {
                            DetailRow("Valor actual", formatAmountEuro(valuable.currentValue))
                        }
                        if (valuable.isSold) {
                            DetailRow("Fecha venta", formatDate(valuable.saleDate ?: 0L))
                            DetailRow("Precio venta", formatAmountEuro(valuable.salePrice ?: 0.0))
                        }
                        if (valuable.linkedLoanId != null) {
                            DetailRow("Préstamo vinculado", summary.linkedLoan?.name ?: "ID: ${valuable.linkedLoanId}")
                        }
                        if (valuable.notes != null) {
                            DetailRow("Notas", valuable.notes)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!valuable.isSold) {
                                OutlinedButton(
                                    onClick = { viewModel.showValueDialog() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                                ) {
                                    Text("Actualizar valor", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.showLoanPicker() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                                ) {
                                    Text(
                                        if (valuable.linkedLoanId != null) "Cambiar préstamo" else "Vincular préstamo",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Gastos ─────────────────────────────────────────────────
                if (summary.totalExpenses > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            SectionHeader("Gastos")
                            Spacer(Modifier.height(8.dp))
                            if (summary.purchaseExpenses > 0) {
                                DetailRow("Gastos compra", formatAmountEuro(summary.purchaseExpenses))
                            }
                            if (summary.holdingExpenses > 0) {
                                DetailRow("Gastos tenencia", formatAmountEuro(summary.holdingExpenses))
                            }
                            if (summary.saleExpenses > 0) {
                                DetailRow("Gastos venta", formatAmountEuro(summary.saleExpenses))
                            }
                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Total gastos", formatAmountEuro(summary.totalExpenses))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextTertiary)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun formatDate(millis: Long): String {
    if (millis <= 0) return "-"
    return try {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        val local = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        "${local.dayOfMonth.toString().padStart(2, '0')}/" +
                "${local.monthNumber.toString().padStart(2, '0')}/" +
                "${local.year}"
    } catch (_: Exception) { "-" }
}
