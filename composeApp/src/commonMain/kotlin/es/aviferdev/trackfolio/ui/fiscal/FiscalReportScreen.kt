package es.aviferdev.trackfolio.ui.fiscal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import es.aviferdev.trackfolio.domain.model.AssetPosition
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.trackfolio.domain.model.FiscalReportData
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@Composable
fun FiscalReportScreen(onBack: () -> Unit, viewModel: FiscalReportViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    state.successMessage?.let { LaunchedEffect(it) { viewModel.clearMessages() } }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary) }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Informe Fiscal", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(state.selectedYear, fontSize = 12.sp, color = TextSecondary)
                }
                YearStepper(year = state.selectedYear, onPrevious = { viewModel.previousYear() }, onNext = { viewModel.nextYear() })
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryDark) }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val report = state.reportData
                    if (report != null) {
                        AnnualSummaryCard(report)
                        if (report.incomeTaxBreakdown.isNotEmpty()) IncomeTaxBreakdownCard(report)
                        MonthlyBreakdownCard(report)
                        if (report.activeDebts.isNotEmpty()) DebtsCard(report)
                        if (report.assetPositions.any { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }) PortfolioCard(report)
                    } else {
                        Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No hay datos para ${state.selectedYear}", fontSize = 16.sp, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Surface(color = SurfaceWhite, shadowElevation = 4.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.errorMessage?.let { err ->
                            Text(err, fontSize = 12.sp, color = ExpenseRed, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        Button(
                            onClick  = { viewModel.generatePdf() },
                            enabled  = state.reportData != null && !state.isGenerating,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                        ) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                            } else {
                                Text("📄", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                            }
                            Text(if (state.isGenerating) "Generando PDF…" else "Generar y compartir PDF", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearStepper(year: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    val nowYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val isMax   = year.toIntOrNull() == nowYear
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp).clip(CircleShape).background(BackgroundGray)) {
            Text("‹", fontSize = 20.sp, color = TextPrimary, fontWeight = FontWeight.Light)
        }
        Text(year, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onNext, enabled = !isMax, modifier = Modifier.size(32.dp).clip(CircleShape).background(if (!isMax) BackgroundGray else Color.Transparent)) {
            Text("›", fontSize = 20.sp, color = if (!isMax) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun AnnualSummaryCard(report: FiscalReportData) {
    val s = report.annualSummary
    ReportCard(title = "📊 Resumen del ejercicio ${report.year}") {
        if (s == null) { Text("Sin movimientos registrados.", fontSize = 13.sp, color = TextSecondary); return@ReportCard }
        val balance = s.balance
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricItem("Ingresos", s.totalIncome,  report.currency, IncomeGreen, Modifier.weight(1f))
            MetricItem("Gastos",   s.totalExpense, report.currency, ExpenseRed,  Modifier.weight(1f))
            MetricItem("Balance",  balance,        report.currency, if (balance >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
        }
    }
}

@Composable
private fun IncomeTaxBreakdownCard(report: FiscalReportData) {
    val breakdown  = report.incomeTaxBreakdown
    val totalGross = breakdown.sumOf { it.grossTotal }
    val totalIrpf  = breakdown.sumOf { it.irpfTotal }
    val totalNet   = breakdown.sumOf { it.netTotal }
    val totalSS    = breakdown.sumOf { it.socialSecurityTotal }
    val totalComm  = breakdown.sumOf { it.commissionTotal }

    ReportCard(title = "🏛️ Desglose IRPF ${report.year}") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricItem("Bruto total",   totalGross, report.currency, TextPrimary, Modifier.weight(1f))
            MetricItem("IRPF retenido", totalIrpf,  report.currency, ExpenseRed,  Modifier.weight(1f))
            MetricItem("Neto total",    totalNet,   report.currency, IncomeGreen, Modifier.weight(1f))
        }
        if (totalSS > 0 || totalComm > 0) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (totalSS > 0) MetricItem("Seg. Social", totalSS, report.currency, Color(0xFFFF9800), Modifier.weight(1f))
                if (totalComm > 0) MetricItem("Comisiones", totalComm, report.currency, Color(0xFFFF9800), Modifier.weight(1f))
                // Rellenar si solo hay uno
                if (totalSS > 0 && totalComm == 0.0) Spacer(Modifier.weight(1f))
                if (totalSS == 0.0 && totalComm > 0) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tipo de ingreso", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(3f))
            Text("Bruto", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("IRPF",  fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Neto",  fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("%",     fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(6.dp))

        breakdown.forEachIndexed { idx, item ->
            if (idx > 0) HorizontalDivider(color = BorderGray.copy(alpha = 0.4f), thickness = 0.3.dp)
            TaxBreakdownRow(item, report.currency)
        }

        Spacer(Modifier.height(10.dp))
        Text("Solo incluye ingresos con información fiscal introducida.", fontSize = 10.sp, color = TextSecondary.copy(alpha = 0.7f))
    }
}

@Composable
private fun TaxBreakdownRow(item: FiscalIncomeTaxBreakdown, currency: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.incomeType.emoji, fontSize = 14.sp)
            Column {
                Text(item.incomeType.label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 13.sp)
                Text("${item.count} ingreso${if (item.count != 1) "s" else ""}", fontSize = 9.sp, color = TextSecondary)
            }
        }
        Text(formatAmt(item.grossTotal, currency), fontSize = 11.sp, color = TextPrimary,   modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.irpfTotal,  currency), fontSize = 11.sp, color = ExpenseRed,    fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.netTotal,   currency), fontSize = 11.sp, color = IncomeGreen,   fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatPct(item.avgIrpfPercent),       fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun MonthlyBreakdownCard(report: FiscalReportData) {
    val byMonth = report.monthlyBreakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val hasData = (1..12).any { byMonth[it] != null }
    ReportCard(title = "📅 Desglose mensual") {
        if (!hasData) { Text("Sin movimientos en ${report.year}.", fontSize = 13.sp, color = TextSecondary); return@ReportCard }
        Row(Modifier.fillMaxWidth()) {
            Text("Mes",      fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f))
            Text("Ingresos", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Gastos",   fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text("Balance",  fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
        for (m in 1..12) {
            val row = byMonth[m] ?: continue
            MonthlyRow(m, row, report.currency)
        }
    }
}

@Composable
private fun MonthlyRow(month: Int, data: MonthlyTotals, currency: String) {
    val balance = data.balance
    HorizontalDivider(color = BorderGray.copy(alpha = 0.4f), thickness = 0.3.dp)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(MONTH_NAMES.getOrElse(month - 1) { month.toString() }, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(2f))
        Text(formatAmt(data.totalIncome, currency),  fontSize = 12.sp, color = IncomeGreen, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(data.totalExpense, currency), fontSize = 12.sp, color = ExpenseRed,  modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text("${if (balance >= 0) "+" else ""}${formatAmt(balance, currency)}", fontSize = 12.sp, color = if (balance >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
    }
}

@Composable
private fun DebtsCard(report: FiscalReportData) {
    ReportCard(title = "💳 Deudas activas") {
        report.activeDebts.forEachIndexed { idx, debt ->
            if (idx > 0) HorizontalDivider(color = BorderGray.copy(alpha = 0.4f), thickness = 0.3.dp)
            DebtRow(debt, report.currency)
        }
    }
}

@Composable
private fun DebtRow(debt: Debt, currency: String) {
    val isIOwe = debt.direction == DebtDirection.I_OWE
    val dirColor = if (isIOwe) ExpenseRed else IncomeGreen
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(debt.personName, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(if (isIOwe) "Les debo" else "Me deben", fontSize = 11.sp, color = dirColor)
            debt.notes?.let { Text(it, fontSize = 11.sp, color = TextSecondary) }
        }
        Text(formatAmt(debt.amount, currency), fontSize = 14.sp, color = dirColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PortfolioCard(report: FiscalReportData) {
    val positions = report.assetPositions.filter { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }
    ReportCard(title = "📈 Cartera de inversión") {
        val totalInvested = positions.sumOf { it.totalCost }
        val totalValue    = positions.mapNotNull { it.currentValue }.sum()
        val totalRealized = positions.sumOf { it.realizedPnl }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricItem("Invertido",    totalInvested, report.currency, TextPrimary,  Modifier.weight(1f))
            MetricItem("Valor actual", totalValue,    report.currency, if (totalValue >= totalInvested) IncomeGreen else ExpenseRed, Modifier.weight(1f))
            MetricItem("P&L Real.",    totalRealized, report.currency, if (totalRealized >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
        }
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Activo",    fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f))
            Text("Unidades",  fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text("P.Medio",   fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text("P&L Total", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        positions.forEachIndexed { idx, pos ->
            if (idx > 0) HorizontalDivider(color = BorderGray.copy(alpha = 0.4f), thickness = 0.3.dp)
            AssetPositionRow(pos, report.currency)
        }
        val yearActive = positions.filter { it.totalBought > 0 || it.totalSold > 0 }
        if (yearActive.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            Text("Actividad en ${report.year}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Activo",    fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(2f))
                Text("Compras",   fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text("Ventas",    fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text("P&L Real.", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(4.dp))
            yearActive.forEachIndexed { idx, pos ->
                if (idx > 0) HorizontalDivider(color = BorderGray.copy(alpha = 0.4f), thickness = 0.3.dp)
                YearActivityRow(pos, report.currency)
            }
        }
    }
}

@Composable
private fun AssetPositionRow(pos: AssetPosition, currency: String) {
    val totalPnl = (pos.unrealizedPnl ?: 0.0) + pos.realizedPnl
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(2f)) {
            Text(pos.ticker, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(pos.categoryName ?: "Sin categoría", fontSize = 10.sp, color = TextSecondary)
        }
        Text(formatQty(pos.netQuantity),          fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text(formatAmt(pos.avgCostBasis, currency), fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text(formatAmt(totalPnl, currency),        fontSize = 12.sp, color = if (totalPnl >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
private fun YearActivityRow(pos: AssetPosition, currency: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(pos.ticker, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
        Text(formatAmt(pos.totalBought, currency), fontSize = 12.sp, color = IncomeGreen, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text(formatAmt(pos.totalSold, currency),   fontSize = 12.sp, color = ExpenseRed,  modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text(formatAmt(pos.realizedPnl, currency), fontSize = 12.sp, color = if (pos.realizedPnl >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
private fun ReportCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MetricItem(label: String, value: Double, currency: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(formatAmt(value, currency), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
    }
}

private fun formatAmt(value: Double, currency: String): String {
    val sign   = if (value < 0) "-" else ""
    val absVal = abs(value)
    val euros  = absVal.toLong()
    val cents  = ((absVal - euros) * 100 + 0.5).toLong().coerceIn(0, 99)
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$sign$eurosStr,${cents.toString().padStart(2, '0')} $currency"
}

private fun formatPct(value: Double): String {
    val intPart  = value.toLong()
    val fracPart = ((value - intPart) * 10 + 0.5).toLong().coerceIn(0, 9)
    return "$intPart,${fracPart}%"
}

private fun formatQty(value: Double): String {
    if (value == value.toLong().toDouble()) return value.toLong().toString()
    val sign    = if (value < 0) "-" else ""
    val absVal  = abs(value)
    val intPart = absVal.toLong()
    val fracRaw = ((absVal - intPart) * 1_000_000 + 0.5).toLong()
    val fracStr = fracRaw.toString().padStart(6, '0').trimEnd('0')
    return "$sign$intPart${if (fracStr.isNotEmpty()) ",$fracStr" else ""}"
}
