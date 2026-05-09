package es.aviferdev.trackfolio.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import es.aviferdev.trackfolio.domain.model.AmortizationEntry
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.LoanRateChange
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun LoanDetailScreen(
    loanId: String,
    onBack: () -> Unit,
    viewModel: LoanDetailViewModel = koinViewModel(parameters = { parametersOf(loanId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    var showArchiveConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.loan?.name ?: "Detalle préstamo", maxLines = 1, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver", tint = PrimaryDark)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openEditSheet() }) {
                        Icon(Icons.Outlined.Edit, "Editar", tint = PrimaryDark)
                    }
                    IconButton(onClick = { viewModel.openRateSheet() }) {
                        Icon(Icons.Outlined.Edit, "Cambiar tipo", tint = PrimaryDark)
                    }
                    IconButton(onClick = { showArchiveConfirmation = true }) {
                        Icon(Icons.Outlined.Delete, "Archivar", tint = Color(0xFFEF5350))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
            return@Scaffold
        }

        val loan = uiState.loan
        if (loan == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Préstamo no encontrado", color = TextSecondary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Resumen ──────────────────────────────────────────────────────
            item { LoanSummarySection(loan) }

            // ── Historial de cambios de tipo ─────────────────────────────────
            if (uiState.rateChanges.isNotEmpty()) {
                item {
                    Text(
                        "Historial de tipo de interés",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryDark
                    )
                }
                items(uiState.rateChanges, key = { it.id }) { change ->
                    RateChangeRow(change)
                }
            }

            // ── Cuadro de amortización ───────────────────────────────────────
            item {
                Text(
                    "Cuadro de amortización",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Cabecera de tabla
            item { AmortizationHeader() }

            items(uiState.schedule, key = { it.installmentNumber }) { entry ->
                AmortizationRow(entry, loan.paidInstallments)
            }
        }
    }

    // ── Diálogo de confirmación de archivado ─────────────────────────────────
    if (showArchiveConfirmation && uiState.loan != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmation = false },
            containerColor = SurfaceWhite,
            icon = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text(
                    "Archivar préstamo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres archivar \"${uiState.loan!!.name}\"? El préstamo desaparecerá de la pantalla principal pero sus datos se mantendrán en el histórico.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archive()
                    showArchiveConfirmation = false
                    onBack()
                }) {
                    Text("Archivar", color = Color(0xFFEF5350), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Sheet de edición de préstamo ────────────────────────────────────────
    if (uiState.showEditSheet && uiState.loan != null) {
        AddEditLoanBottomSheet(
            loan = uiState.loan,
            onDismiss = { viewModel.closeEditSheet() }
        )
    }
}

@Composable
private fun LoanSummarySection(loan: Loan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${loan.type.emoji} ${loan.type.label}", fontSize = 13.sp, color = TextSecondary)
                loan.lenderName?.let {
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Capital pendiente", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${formatAmount(loan.outstandingPrincipal)} €",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cuota mensual", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${formatAmount(loan.monthlyPayment)} €",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailRow("Total", "${formatAmount(loan.totalAmount)} €")
                DetailRow("Interés", "${formatPercent(loan.currentInterestRate)}%")
                DetailRow("Plazo", "${loan.totalInstallments} meses")
            }

            Spacer(Modifier.height(12.dp))

            // Progreso
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progreso", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${loan.paidInstallments}/${loan.totalInstallments} cuotas (${(loan.progressPercent * 100).toInt()}%)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { loan.progressPercent },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = PrimaryDark,
                    trackColor = BorderGray
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RateChangeRow(change: LoanRateChange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(formatDate(change.effectiveDate), fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${formatCurrency(change.previousRate - change.newRate)}%",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${formatPercent(change.previousRate)}%", fontSize = 12.sp, color = TextSecondary)
                Text("→", fontSize = 12.sp, color = TextSecondary)
                Text("${formatPercent(change.newRate)}%", fontSize = 12.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun AmortizationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("#", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
        Text("Fecha", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Text("Cuota", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
        Text("Interés", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
        Text("Capital", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
        Text("Pendiente", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
private fun AmortizationRow(entry: AmortizationEntry, paidInstallments: Int) {
    val isPast = entry.installmentNumber <= paidInstallments
    val textColor = if (isPast) TextPrimary else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (entry.installmentNumber == paidInstallments + 1) SurfaceElevated else Color.Transparent)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            entry.installmentNumber.toString(),
            fontSize = 11.sp,
            color = textColor,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            formatDateShort(entry.date),
            fontSize = 11.sp,
            color = textColor,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Text(formatCurrencyShort(entry.monthlyPayment), Modifier.weight(1.3f), fontSize = 11.sp, color = textColor, textAlign = TextAlign.End)
        Text(formatCurrencyShort(entry.interestPortion), Modifier.weight(1.3f), fontSize = 11.sp, color = textColor, textAlign = TextAlign.End)
        Text(formatCurrencyShort(entry.principalPortion), Modifier.weight(1.3f), fontSize = 11.sp, color = textColor, textAlign = TextAlign.End)
        Text(formatCurrencyShort(entry.outstandingBalance), Modifier.weight(1.5f), fontSize = 11.sp, color = PrimaryDark, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }

    HorizontalDivider(color = TextSecondary.copy(alpha = 0.08f))
}

private fun formatCurrency(amount: Double): String {
    val formatted = formatAmount(kotlin.math.abs(amount))
    val prefix = if (amount < 0) "-" else ""
    return "$prefix$formatted €"
}

private fun formatCurrencyShort(amount: Double): String =
    formatAmount(amount)

private fun formatDate(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
}

private fun formatDateShort(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
}

private fun formatPercent(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    val intPart = rounded.toLong()
    val decPart = ((rounded - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')}"
}