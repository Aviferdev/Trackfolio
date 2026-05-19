package es.aviferdev.n3to.ui.loan

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import es.aviferdev.n3to.domain.model.AmortizationEntry
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.fixedincome_interest_label
import n3to.composeapp.generated.resources.loan_amortization_title
import n3to.composeapp.generated.resources.loan_archive_title
import n3to.composeapp.generated.resources.loan_change_rate
import n3to.composeapp.generated.resources.loan_detail_title
import n3to.composeapp.generated.resources.loan_history_title
import n3to.composeapp.generated.resources.loan_monthly_payment
import n3to.composeapp.generated.resources.loan_not_found
import n3to.composeapp.generated.resources.loan_pending_capital
import n3to.composeapp.generated.resources.loan_progress
import n3to.composeapp.generated.resources.loan_term_label
import n3to.composeapp.generated.resources.settings_edit_cd
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    onBack: () -> Unit,
    viewModel: LoanDetailViewModel = koinViewModel(parameters = { parametersOf(loanId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    var showArchiveConfirm by remember { mutableStateOf(false) }

    LoanDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEditClick = { viewModel.openEditSheet() },
        onRateChangeClick = { viewModel.openRateSheet() },
        onArchiveClick = { showArchiveConfirm = true }
    )

    if (showArchiveConfirm && uiState.loan != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm = false },
            containerColor   = MaterialTheme.appColors.surface,
            icon             = { Text("⚠️", fontSize = 26.sp) },
            title = {
                Text(
                    stringResource(Res.string.loan_archive_title),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres archivar «${uiState.loan!!.name}»? Desaparecerá de la pantalla principal pero sus datos se mantendrán.",
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.archive(); showArchiveConfirm = false; onBack() }) {
                    Text(stringResource(Res.string.loan_archive_title), color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirm = false }) {
                    Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.primary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.showEditSheet && uiState.loan != null) {
        AddEditLoanBottomSheet(loan = uiState.loan, onDismiss = { viewModel.closeEditSheet() })
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailContent(
    uiState: LoanDetailUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onRateChangeClick: () -> Unit,
    onArchiveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)
    ) {
        TopBarApp(
            title = uiState.loan?.name ?: stringResource(Res.string.loan_detail_title),
            navigateBack = onBack,
            actions = {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Outlined.Edit, stringResource(Res.string.settings_edit_cd), tint = MaterialTheme.appColors.textSecondary)
                    Icon(Icons.Outlined.Edit, stringResource(Res.string.loan_change_rate), tint = MaterialTheme.appColors.textSecondary)
                    Icon(Icons.Outlined.Delete, stringResource(Res.string.loan_archive_title), tint = MaterialTheme.appColors.expense)
                }
            }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else if (uiState.loan == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.loan_not_found), color = MaterialTheme.appColors.textTertiary, fontSize = 13.sp)
            }
        } else {
            val loan = uiState.loan

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item { LoanHeroCard(loan = loan) }
            item { LoanDetailsGrid(loan = loan) }

            if (uiState.rateChanges.isNotEmpty()) {
                item { SectionLabel("Historial de tipo de interés") }
                items(uiState.rateChanges, key = { it.id }) { change ->
                    RateChangeRow(change)
                }
            }

            item { SectionLabel("Cuadro de amortización") }
            item { AmortizationHeader() }
            items(uiState.schedule, key = { it.installmentNumber }) { entry ->
                AmortizationRow(entry = entry, paidInstallments = loan.paidInstallments)
            }

            item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun LoanDetailContentPreview() {
    N3toTheme {
        LoanDetailContent(
            uiState = LoanDetailUiState(
                isLoading = false,
                loan = Loan(
                    id = "preview-1",
                    accountId = "acct-1",
                    name = "Préstamo Hipoteca",
                    type = es.aviferdev.n3to.domain.model.LoanType.MORTGAGE,
                    totalAmount = 120000.0,
                    outstandingPrincipal = 98000.0,
                    monthlyPayment = 850.0,
                    currentInterestRate = 2.75,
                    totalInstallments = 240,
                    paidInstallments = 36,
                    startDate = 1700000000000,
                    endDate = 1700000000000 + 240L * 30L * 24L * 3600L * 1000L,
                    lenderName = "Banco Ejemplo",
                    notes = null,
                    archived = false,
                    createdAt = 1700000000000
                ),
                rateChanges = emptyList(),
                schedule = emptyList()
            ),
            onBack = {},
            onEditClick = {},
            onRateChangeClick = {},
            onArchiveClick = {}
        )
    }
}

// ─── Hero card ────────────────────────────────────────────────────────────────
@Composable
private fun LoanHeroCard(loan: Loan) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.primary),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text("${loan.type.emoji} ${loan.type.label}", fontSize = 12.sp, color = Color.White.copy(.5f))
                loan.lenderName?.let { Text(it, fontSize = 12.sp, color = Color.White.copy(.5f)) }
            }
            Spacer(Modifier.height(10.dp))

            // Saldo pendiente
            Text(stringResource(Res.string.loan_pending_capital), fontSize = 11.sp, color = Color.White.copy(.5f))
            Text(
                "−${formatAmount(loan.outstandingPrincipal)} €",
                fontSize      = 30.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                HeroMetric(stringResource(Res.string.loan_monthly_payment), "${formatAmount(loan.monthlyPayment)} €")
                HeroMetric(stringResource(Res.string.fixedincome_interest_label), "${formatPercent(loan.currentInterestRate)}%")
                HeroMetric(stringResource(Res.string.loan_term_label), "${loan.totalInstallments} meses")
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(10.dp))

            // Progress bar
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.loan_progress), fontSize = 10.sp, color = Color.White.copy(.45f))
                Text(
                    "${loan.paidInstallments}/${loan.totalInstallments} cuotas · ${(loan.progressPercent * 100).toInt()}%",
                    fontSize = 10.sp, color = Color.White.copy(.45f)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { loan.progressPercent },
                modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color      = Color.White.copy(.9f),
                trackColor = Color.White.copy(.2f)
            )
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(.45f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// ─── Details grid ─────────────────────────────────────────────────────────────
@Composable
private fun LoanDetailsGrid(loan: Loan) {
    val items = listOf(
        Triple("Capital inicial", "${formatAmount(loan.totalAmount)} €",                ""),
        Triple("Tipo",            "${loan.type.emoji} ${loan.type.label}",              ""),
        Triple("Amortización",    "Francés",                                            ""),
        Triple("Entidad",         loan.lenderName ?: "—",                              ""),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { col ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                col.forEach { (label, value, _) ->
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                label.uppercase(),
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = MaterialTheme.appColors.textTertiary,
                                letterSpacing = .5.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ─── Rate change row ──────────────────────────────────────────────────────────
@Composable
private fun RateChangeRow(change: LoanRateChange) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(11.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(formatDate(change.effectiveDate), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary)
                Text(
                    "Δ ${if (change.newRate > change.previousRate) "+" else ""}${
                        formatPercent(change.newRate - change.previousRate)}%",
                    fontSize = 10.sp,
                    color    = if (change.newRate > change.previousRate) MaterialTheme.appColors.expense else MaterialTheme.appColors.income
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${formatPercent(change.previousRate)}%", fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary)
                Text("→", fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
                Text("${formatPercent(change.newRate)}%", fontSize = 13.sp, color = MaterialTheme.appColors.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Amortization table ───────────────────────────────────────────────────────
@Composable
private fun AmortizationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("#" to .4f, "Fecha" to 1.4f, "Cuota" to 1.2f, "Interés" to 1.2f, "Capital" to 1.2f, "Pendiente" to 1.4f).forEach { (h, w) ->
            Text(
                h,
                fontSize  = 9.sp,
                fontWeight = FontWeight.Bold,
                color     = MaterialTheme.appColors.textTertiary,
                letterSpacing = .4.sp,
                modifier  = Modifier.weight(w),
                textAlign = if (h == "#" || h == "Fecha") TextAlign.Start else TextAlign.End
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.appColors.border, thickness = .5.dp)
}

@Composable
private fun AmortizationRow(entry: AmortizationEntry, paidInstallments: Int) {
    val isPaid   = entry.installmentNumber <= paidInstallments
    val isNext   = entry.installmentNumber == paidInstallments + 1
    val txtColor = if (isPaid) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isNext) PrimaryAlpha else Color.Transparent)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(entry.installmentNumber.toString(), fontSize = 10.sp, color = txtColor, modifier = Modifier.weight(.4f))
        Text(formatDateShort(entry.date),        fontSize = 10.sp, color = txtColor, modifier = Modifier.weight(1.4f))
        Text(formatCurrencyShort(entry.monthlyPayment),  fontSize = 10.sp, color = txtColor,    textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
        Text(formatCurrencyShort(entry.interestPortion), fontSize = 10.sp, color = MaterialTheme.appColors.expense.copy(if (isPaid) 1f else .5f), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
        Text(formatCurrencyShort(entry.principalPortion),fontSize = 10.sp, color = MaterialTheme.appColors.income.copy(if (isPaid) 1f else .5f),textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
        Text(formatCurrencyShort(entry.outstandingBalance), fontSize = 10.sp, color = if (isPaid) MaterialTheme.appColors.primary else MaterialTheme.appColors.textTertiary, fontWeight = if (isPaid) FontWeight.SemiBold else FontWeight.Normal, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
    }
    HorizontalDivider(color = MaterialTheme.appColors.border, thickness = .3.dp)
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.appColors.textTertiary,
        letterSpacing = .7.sp,
        modifier      = Modifier.padding(top = 4.dp)
    )
}

private fun formatCurrencyShort(amount: Double): String = formatAmount(amount)

private fun formatDate(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
}

private fun formatDateShort(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
}

private fun formatPercent(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    val i = rounded.toLong()
    val d = ((rounded - i) * 100).toInt()
    return "$i,${d.toString().padStart(2,'0')}"
}
