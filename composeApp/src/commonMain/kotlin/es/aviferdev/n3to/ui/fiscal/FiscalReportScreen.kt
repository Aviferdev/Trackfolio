package es.aviferdev.n3to.ui.fiscal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetPosition
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private val MONTH_NAMES = listOf(
    "Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
)

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FiscalReportScreen(
    onBack: () -> Unit,
    viewModel: FiscalReportViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    state.successMessage?.let { LaunchedEffect(it) { viewModel.clearMessages() } }

    FiscalReportContent(
        state = state,
        onBack = onBack,
        onPreviousYear = { viewModel.previousYear() },
        onNextYear = { viewModel.nextYear() },
        onGeneratePdf = { viewModel.generatePdf() }
    )

    if (state.showPasswordSheet) {
        PdfPasswordSheet(
            onConfirm = { viewModel.confirmGeneratePdf(it) },
            onDismiss = { viewModel.cancelPasswordSheet() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FiscalReportContent(
    state: FiscalReportUiState,
    onBack: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onGeneratePdf: () -> Unit,
    modifier: Modifier = Modifier,
    taxProfile: TaxProfileSnapshot? = state.activeTaxProfile
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        TopBarApp(
            title = "Informe Fiscal",
            subtitle = state.selectedYear,
            navigateBack = onBack,
            actions = {
                YearStepper(
                    year = state.selectedYear,
                    onPrevious = onPreviousYear,
                    onNext = onNextYear
                )
            }
        )

        if (state.hasNetOnlyIncomes && !state.isLoading) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = WarnAmber.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint    = WarnAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Hay ingresos registrados solo con el importe neto. " +
                                "El desglose fiscal puede estar incompleto.",
                        fontSize = 11.sp,
                        color = WarnAmber,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val report = state.reportData
                    if (report != null) {
                        if (taxProfile != null) TaxProfileBadge(taxProfile)
                        AnnualSummaryCard(report)
                        if (report.incomeTaxBreakdown.isNotEmpty()) IncomeTaxBreakdownCard(report)
                        MonthlyBreakdownCard(report)
                        if (report.activeDebts.isNotEmpty()) DebtsCard(report)
                        if (report.assetPositions.any { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }) {
                            PortfolioCard(report)
                        }
                    } else {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Assignment, contentDescription = null, modifier = Modifier.size(44.dp), tint = PrimaryDark)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No hay datos para ${state.selectedYear}",
                                    fontSize  = 15.sp,
                                    color     = TextTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = BorderGray, thickness = .5.dp, modifier = Modifier.padding(bottom = 8.dp))
                        state.errorMessage?.let { err ->
                            Text(err, fontSize = 11.sp, color = ExpenseRed, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        Button(
                            onClick  = onGeneratePdf,
                            enabled  = state.reportData != null && !state.isGenerating,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                        ) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Text("📄", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                if (state.isGenerating) "Generando PDF…" else "Generar y compartir PDF",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun FiscalReportContentPreview() {
    N3toTheme {
        FiscalReportContent(
            state = FiscalReportUiState(
                isLoading = false,
                selectedYear = "2026",
                reportData = FiscalReportData(
                    accountName = "Cuenta Principal",
                    year = "2026",
                    generatedAt = 1700000000000,
                    annualSummary = es.aviferdev.n3to.domain.model.AnnualSummary(
                        year = "2026",
                        totalIncome = 45000.0,
                        totalExpense = 32000.0,
                        previousYearIncome = 42000.0,
                        previousYearExpense = 30000.0
                    ),
                    monthlyBreakdown = listOf(
                        es.aviferdev.n3to.domain.model.MonthlyTotals("2026", "01", 3800.0, 2500.0),
                        es.aviferdev.n3to.domain.model.MonthlyTotals("2026", "02", 3750.0, 2700.0),
                        es.aviferdev.n3to.domain.model.MonthlyTotals("2026", "03", 4000.0, 2600.0)
                    ),
                    activeDebts = emptyList(),
                    assetPositions = emptyList(),
                    incomeTaxBreakdown = emptyList()
                ),
                isGenerating = false,
                showPasswordSheet = false
            ),
            onBack = {},
            onPreviousYear = {},
            onNextYear = {},
            onGeneratePdf = {}
        )
    }
}

// ─── Year stepper ─────────────────────────────────────────────────────────────
@Composable
private fun YearStepper(year: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    val nowYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val isMax   = year.toIntOrNull() == nowYear
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick  = onPrevious,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(SurfaceElevated)
        ) {
            Text("‹", fontSize = 20.sp, color = TextPrimary, fontWeight = FontWeight.Light)
        }
        Text(year, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(
            onClick  = onNext,
            enabled  = !isMax,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(if (!isMax) SurfaceElevated else Color.Transparent)
        ) {
            Text("›", fontSize = 20.sp, color = if (!isMax) TextPrimary else TextTertiary, fontWeight = FontWeight.Light)
        }
    }
}

// ─── Report cards ─────────────────────────────────────────────────────────────
@Composable
private fun AnnualSummaryCard(report: FiscalReportData) {
    val s = report.annualSummary
    ReportCard("Ejercicio ${report.year} · Resumen") {
        if (s == null) { Text("Sin movimientos registrados.", fontSize = 13.sp, color = TextTertiary); return@ReportCard }

        // 2x2 grid (matching JSX design)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Ingresos totales
                FiscalMetricCell(
                    "Ingresos totales",
                    s.totalIncome,
                    IncomeGreen,
                    Modifier.weight(1f)
                )
                // Retenciones fiscales
                FiscalMetricCell(
                    "Retenciones fiscales",
                    report.incomeTaxBreakdown.sumOf { it.irpfTotal },
                    ExpenseRed,
                    Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Ganancias capital (from portfolio)
                val capitalGains = report.assetPositions.sumOf { it.realizedPnl }
                FiscalMetricCell(
                    "Ganancias capital",
                    capitalGains,
                    if (capitalGains >= 0.0) IncomeGreen else ExpenseRed,
                    Modifier.weight(1f)
                )
                // Base imponible
                val baseImponible = s.totalIncome + capitalGains
                FiscalMetricCell(
                    "Base imponible est.",
                    baseImponible,
                    PrimaryDark,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FiscalMetricCell(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(9.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 10.sp, color = TextTertiary)
            Spacer(Modifier.height(3.dp))
            Text(
                "${if (amount >= 0) "" else "−"}${formatAmt(kotlin.math.abs(amount))} €",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun IncomeTaxBreakdownCard(report: FiscalReportData) {
    val bk = report.incomeTaxBreakdown
    val totalGross = bk.sumOf { it.grossTotal }
    val totalIrpf  = bk.sumOf { it.irpfTotal }
    val totalNet   = bk.sumOf { it.netTotal }
    val totalSS    = bk.sumOf { it.socialSecurityTotal }
    val totalComm  = bk.sumOf { it.commissionTotal }

    ReportCard("🏛️ Desglose fiscal ${report.year}") {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            MetricCell("Bruto total",       totalGross, TextPrimary, Modifier.weight(1f))
            MetricCell("Retención fiscal",  totalIrpf,  ExpenseRed,  Modifier.weight(1f))
            MetricCell("Neto total",        totalNet,   IncomeGreen, Modifier.weight(1f))
        }
        if (totalSS > 0 || totalComm > 0) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                if (totalSS   > 0) MetricCell("Cotizaciones",  totalSS,   WarnAmber, Modifier.weight(1f))
                if (totalComm > 0) MetricCell("Comisiones",    totalComm, WarnAmber, Modifier.weight(1f))
                if (totalSS > 0 && totalComm == 0.0) Spacer(Modifier.weight(1f))
                if (totalSS == 0.0 && totalComm > 0) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = BorderGray, thickness = .5.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Tipo de ingreso", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(3f))
            Text("Bruto",  fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Retención", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Neto",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("%",      fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(5.dp))
        bk.forEachIndexed { i, item ->
            if (i > 0) HorizontalDivider(color = BorderGray, thickness = .3.dp)
            TaxBreakdownRow(item)
        }
        Spacer(Modifier.height(8.dp))
        Text("Solo incluye ingresos con información fiscal introducida.", fontSize = 10.sp, color = TextTertiary)
    }
}

@Composable
private fun TaxBreakdownRow(item: FiscalIncomeTaxBreakdown) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(item.incomeType.toMaterialIcon(), contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
            Column {
                Text(item.incomeType.label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 13.sp)
                Text("${item.count} ingreso${if (item.count != 1) "s" else ""}", fontSize = 9.sp, color = TextTertiary)
            }
        }
        Text(formatAmt(item.grossTotal), fontSize = 11.sp, color = TextPrimary,  modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.irpfTotal),  fontSize = 11.sp, color = ExpenseRed,   fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.netTotal),   fontSize = 11.sp, color = IncomeGreen,  fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatPct(item.avgIrpfPercent),       fontSize = 11.sp, color = TextSecondary,modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun MonthlyBreakdownCard(report: FiscalReportData) {
    val byMonth = report.monthlyBreakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val hasData = (1..12).any { byMonth[it] != null }
    ReportCard("📅 Desglose mensual") {
        if (!hasData) { Text("Sin movimientos en ${report.year}.", fontSize = 13.sp, color = TextTertiary); return@ReportCard }
        Row(Modifier.fillMaxWidth()) {
            Text("Mes",      fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
            Text("Ingresos", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Gastos",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Balance",  fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = BorderGray, thickness = .5.dp)
        for (m in 1..12) {
            val row = byMonth[m] ?: continue
            HorizontalDivider(color = BorderGray, thickness = .3.dp)
            MonthlyRow(m, row)
        }
    }
}

@Composable
private fun MonthlyRow(month: Int, data: MonthlyTotals) {
    val balance = data.balance
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(MONTH_NAMES.getOrElse(month - 1) { month.toString() }, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(2f))
        Text(formatAmt(data.totalIncome),  fontSize = 11.sp, color = IncomeGreen, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(data.totalExpense), fontSize = 11.sp, color = ExpenseRed,  modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(
            "${if (balance >= 0) "+" else ""}${formatAmt(balance)}",
            fontSize   = 11.sp,
            color      = if (balance >= 0) IncomeGreen else ExpenseRed,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(2f),
            textAlign  = TextAlign.End
        )
    }
}

@Composable
private fun DebtsCard(report: FiscalReportData) {
    ReportCard("💳 Deudas activas") {
        report.activeDebts.forEachIndexed { i, debt ->
            if (i > 0) HorizontalDivider(color = BorderGray, thickness = .3.dp)
            val isIOwe   = debt.direction == DebtDirection.I_OWE
            val color    = if (isIOwe) ExpenseRed else IncomeGreen
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.personName, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(if (isIOwe) "Les debo" else "Me deben", fontSize = 11.sp, color = color)
                    debt.notes?.let { Text(it, fontSize = 10.sp, color = TextTertiary) }
                }
                Text(formatAmt(debt.amount), fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PortfolioCard(report: FiscalReportData) {
    val positions    = report.assetPositions.filter { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }
    val totalInvested = positions.sumOf { it.totalCost }
    val totalValue    = positions.mapNotNull { it.currentValue }.sum()
    val totalRealized = positions.sumOf { it.realizedPnl }

    ReportCard("📈 Cartera de inversión") {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), Arrangement.spacedBy(8.dp)) {
            MetricCell("Invertido",    totalInvested, TextPrimary,  Modifier.weight(1f))
            MetricCell("Valor actual", totalValue,    if (totalValue >= totalInvested) IncomeGreen else ExpenseRed, Modifier.weight(1f))
            MetricCell("P&L Real.",    totalRealized, if (totalRealized >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
        }
        HorizontalDivider(color = BorderGray, thickness = .5.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Activo",    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
            Text("Unidades",  fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text("P.Medio",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text("P&L Total", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        positions.forEachIndexed { i, pos ->
            if (i > 0) HorizontalDivider(color = BorderGray, thickness = .3.dp)
            val totalPnl = (pos.unrealizedPnl ?: 0.0) + pos.realizedPnl
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(2f)) {
                    Text(pos.ticker, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(pos.categoryName ?: "Sin categoría", fontSize = 9.sp, color = TextTertiary)
                }
                Text(formatQty(pos.netQuantity),          fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(formatAmt(pos.avgCostBasis), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(formatAmt(totalPnl), fontSize = 11.sp, color = if (totalPnl >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            }
        }

        val yearActive = positions.filter { it.totalBought > 0 || it.totalSold > 0 }
        if (yearActive.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = .5.dp)
            Spacer(Modifier.height(8.dp))
            Text("Actividad en ${report.year}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Activo",    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
                Text("Compras",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text("Ventas",    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text("P&L Real.", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            }
            yearActive.forEachIndexed { i, pos ->
                if (i > 0) HorizontalDivider(color = BorderGray, thickness = .3.dp)
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(pos.ticker, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                    Text(formatAmt(pos.totalBought), fontSize = 11.sp, color = IncomeGreen, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text(formatAmt(pos.totalSold),   fontSize = 11.sp, color = ExpenseRed,  modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text(formatAmt(pos.realizedPnl), fontSize = 11.sp, color = if (pos.realizedPnl >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

// ─── Tax profile badge ────────────────────────────────────────────────────────
@Composable
private fun TaxProfileBadge(snapshot: TaxProfileSnapshot) {
    val flag = when (snapshot.profile.countryCode) {
        "ES" -> "🇪🇸"; "GB" -> "🇬🇧"; "US" -> "🇺🇸"; "DE" -> "🇩🇪"; else -> "🌐"
    }
    val label = when (snapshot.profile.countryCode) {
        "ES" -> "España"; "GB" -> "Reino Unido"; "US" -> "EE.UU."; "DE" -> "Alemania"
        else -> "Personalizado"
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PrimaryDark.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(flag, fontSize = 18.sp)
            Column {
                Text(
                    "Perfil fiscal activo: $label",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDark
                )
                Text(
                    "${snapshot.profile.currency}  ·  vigente desde ${snapshot.effectiveFrom}",
                    fontSize = 10.sp,
                    color = PrimaryDark.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ─── Primitives ───────────────────────────────────────────────────────────────
@Composable
private fun ReportCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MetricCell(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label.uppercase(), fontSize = 9.sp, color = TextTertiary, fontWeight = FontWeight.Bold, letterSpacing = .4.sp)
        Spacer(Modifier.height(3.dp))
        Text(formatAmt(value), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
    }
}

// ─── PDF password sheet ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfPasswordSheet(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error           by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("🔒 Proteger informe", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Introduce una contraseña para proteger el PDF. Déjala vacía si no quieres protegerlo.", fontSize = 12.sp, color = TextTertiary)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it; error = null },
                label                = { Text("Contraseña") },
                placeholder          = { Text("Opcional") },
                singleLine           = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                colors   = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray)
            )
            if (password.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value                = confirmPassword,
                    onValueChange        = { confirmPassword = it; error = null },
                    label                = { Text("Confirmar contraseña") },
                    singleLine           = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError              = error != null,
                    modifier             = Modifier.fillMaxWidth(),
                    shape                = RoundedCornerShape(10.dp),
                    colors               = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray)
                )
            }
            error?.let { Text(it, fontSize = 11.sp, color = ExpenseRed, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (password.isNotEmpty() && password != confirmPassword) { error = "Las contraseñas no coinciden"; return@Button }
                    if (password.isNotEmpty() && password.length < 4) { error = "Mínimo 4 caracteres"; return@Button }
                    onConfirm(password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(if (password.isEmpty()) "Generar sin contraseña" else "Generar con contraseña", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ─── Format helpers ───────────────────────────────────────────────────────────
private fun formatAmt(value: Double): String {
    val sign   = if (value < 0) "-" else ""
    val absVal = abs(value)
    val euros  = absVal.toLong()
    val cents  = ((absVal - euros) * 100 + .5).toLong().coerceIn(0, 99)
    val eurosStr = euros.toString().reversed()
        .chunked(3).joinToString(".").reversed()
    return "$sign$eurosStr,${cents.toString().padStart(2,'0')} €"
}

private fun formatPct(value: Double): String {
    val i = value.toLong()
    val f = ((value - i) * 10 + .5).toLong().coerceIn(0, 9)
    return "$i,${f}%"
}

// formatQty se importa de ui.theme
