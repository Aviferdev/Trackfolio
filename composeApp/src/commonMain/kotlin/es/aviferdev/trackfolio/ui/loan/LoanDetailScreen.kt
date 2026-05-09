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
    viewModel: LoanDetailViewModel = koinViewModel { parametersOf(loanId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val showRateSheet by viewModel.showRateSheet.collectAsState()

    if (showRateSheet && uiState.loan != null) {
        UpdateLoanRateSheet(
            currentRate = uiState.loan!!.currentInterestRate,
            onDismiss   = { viewModel.closeRateSheet() },
            onConfirm   = { newRate, effectiveDate ->
                viewModel.updateRate(newRate, effectiveDate)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.loan?.name ?: "Detalle préstamo", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openRateSheet() }) {
                        Icon(Icons.Outlined.Edit, "Cambiar tipo", tint = PrimaryDark)
                    }
                    IconButton(onClick = {
                        viewModel.archive()
                        onBack()
                    }) {
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

            // Progreso
            LinearProgressIndicator(
                progress = { loan.progressPercent },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PrimaryDark,
                trackColor = TextSecondary.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "${loan.paidInstallments} de ${loan.totalInstallments} cuotas pagadas (${(loan.progressPercent * 100).toInt()}%)",
                fontSize = 12.sp,
                color = TextSecondary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TextSecondary.copy(alpha = 0.15f))

            DetailRow("Capital total", formatCurrency(loan.totalAmount))
            DetailRow("Capital pendiente", formatCurrency(loan.outstandingPrincipal))
            DetailRow("Cuota mensual", formatCurrency(loan.monthlyPayment))
            DetailRow("Tipo de interés", "${loan.currentInterestRate}%")
            DetailRow("Intereses estimados", formatCurrency(loan.totalEstimatedInterest))
            DetailRow("Inicio", formatDate(loan.startDate))
            DetailRow("Fin", formatDate(loan.endDate))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PrimaryDark)
    }
}

@Composable
private fun RateChangeRow(change: LoanRateChange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(formatDate(change.effectiveDate), fontSize = 12.sp, color = TextSecondary)
                Text(
                    "${change.previousRate}% → ${change.newRate}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryDark
                )
            }
            val diff = change.newRate - change.previousRate
            val color = if (diff > 0) Color(0xFFEF5350) else Color(0xFF4CAF50)
            val diffStr = formatDiff(diff)
            Text(
                "${if (diff > 0) "+" else ""}$diffStr%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun AmortizationHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryDark)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text("#", Modifier.weight(0.8f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text("Fecha", Modifier.weight(1.5f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text("Cuota", Modifier.weight(1.3f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
        Text("Capital", Modifier.weight(1.3f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
        Text("Intereses", Modifier.weight(1.3f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
        Text("Pendiente", Modifier.weight(1.5f), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}

@Composable
private fun AmortizationRow(entry: AmortizationEntry, paidInstallments: Int) {
    val isPaid = entry.installmentNumber <= paidInstallments
    val bgColor = if (isPaid) Color(0xFF4CAF50).copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            entry.installmentNumber.toString(),
            Modifier.weight(0.8f),
            fontSize = 11.sp,
            color = if (isPaid) Color(0xFF4CAF50) else TextSecondary,
            fontWeight = if (isPaid) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(formatDateShort(entry.date), Modifier.weight(1.5f), fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Text(formatCurrencyShort(entry.monthlyPayment), Modifier.weight(1.3f), fontSize = 11.sp, color = PrimaryDark, textAlign = TextAlign.End)
        Text(formatCurrencyShort(entry.principalPortion), Modifier.weight(1.3f), fontSize = 11.sp, color = PrimaryDark, textAlign = TextAlign.End)
        Text(formatCurrencyShort(entry.interestPortion), Modifier.weight(1.3f), fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.End)
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

private fun formatDiff(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    val intPart = rounded.toLong()
    val decPart = ((rounded - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')}"
}
