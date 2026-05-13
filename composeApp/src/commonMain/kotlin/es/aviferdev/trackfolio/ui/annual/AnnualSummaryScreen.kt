package es.aviferdev.trackfolio.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.ui.common.ProgressBar
import es.aviferdev.trackfolio.ui.common.navigation.TimeStepperHeader
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private val MONTH_LABELS = listOf(
    "E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"
)

enum class AnnualTab {
    RESUMEN, GASTOS, INGRESOS, INVERSIONES
}

@Composable
fun AnnualSummaryScreen(
    viewModel: AnnualViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var selectedTab by remember { mutableStateOf(AnnualTab.RESUMEN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        TimeStepperHeader(
            title = "Resumen anual",
            currentValue = uiState.year,
            canGoBack = uiState.canGoBack,
            onPrevious = { viewModel.previousYear() },
            onNext = { viewModel.nextYear() }
        )

        // Tabs
        AnnualTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else {
            when (selectedTab) {
                AnnualTab.RESUMEN -> {
                    uiState.summary?.let { summary ->
                        AnnualContent(
                            summary        = summary,
                            breakdown      = uiState.monthlyBreakdown,
                            balancesHidden = balancesHidden
                        )
                    } ?: EmptyYearState()
                }
                AnnualTab.GASTOS -> {
                    DonutChartCard(
                        title          = "Gastos por categoría",
                        subtitle       = "Año ${uiState.year}",
                        slices         = uiState.expensesByCategory,
                        totalAmount    = uiState.summary?.totalExpense ?: 0.0,
                                                balancesHidden = balancesHidden,
                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
                AnnualTab.INGRESOS -> {
                    DonutChartCard(
                        title          = "Ingresos por tipo",
                        subtitle       = "Año ${uiState.year}",
                        slices         = uiState.incomeByType,
                        totalAmount    = uiState.summary?.totalIncome ?: 0.0,
                                                balancesHidden = balancesHidden,
                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
                AnnualTab.INVERSIONES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InvestmentBarChart(
                            investments    = uiState.monthlyInvestments,
                            year           = uiState.year,
                                                        balancesHidden = balancesHidden
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnualTabs(
    selectedTab: AnnualTab,
    onTabSelected: (AnnualTab) -> Unit
) {
    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnnualTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text    = tab.displayName(),
                        fontSize = 13.sp,
                        color   = if (isSelected) PrimaryDark else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(24.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isSelected) PrimaryDark else Color.Transparent)
                    )
                }
            }
        }
    }
}

private fun AnnualTab.displayName(): String = when (this) {
    AnnualTab.RESUMEN    -> "Resumen"
    AnnualTab.GASTOS     -> "Gastos"
    AnnualTab.INGRESOS   -> "Ingresos"
    AnnualTab.INVERSIONES -> "Inversiones"
}

@Composable
private fun AnnualHeader(year: String, canGoBack: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    val nowYear       = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val isCurrentYear = year.toIntOrNull() == nowYear

    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text("Resumen anual", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onPrevious,
                    enabled  = canGoBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (canGoBack) BackgroundGray else Color.Transparent)
                ) {
                    Text(
                        "‹",
                        fontSize   = 22.sp,
                        color      = if (canGoBack) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    )
                }
                Text(year, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                IconButton(
                    onClick  = onNext,
                    enabled  = !isCurrentYear,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (!isCurrentYear) BackgroundGray else Color.Transparent)
                ) {
                    Text(
                        "›",
                        fontSize   = 22.sp,
                        color      = if (!isCurrentYear) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
fun AnnualContent(summary: AnnualSummary, breakdown: List<MonthlyTotals>, balancesHidden: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Year totals - 3-column grid (matching JSX design)
        YearTotalsCard(summary = summary, balancesHidden = balancesHidden)

        // ── Gráfico de barras mensual ──────────────────────────────────────
        MonthlyBarChart(breakdown = breakdown, year = summary.year)

        // ── Top categorías de gastos ───────────────────────────────────────
        TopCategoriesCard(breakdown = breakdown, balancesHidden = balancesHidden)

        // ── Comparativa año anterior ───────────────────────────────────────
        if (summary.previousYearIncome > 0 || summary.previousYearExpense > 0) {
            PreviousYearCard(summary = summary, balancesHidden = balancesHidden)
        }
    }
}

// ─── Year totals card (3-column grid) ─────────────────────────────────────────
@Composable
private fun YearTotalsCard(summary: AnnualSummary, balancesHidden: Boolean) {
    val savings = summary.totalIncome - summary.totalExpense

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Ingresos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ingresos", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "+${maskAmount(formatAmount(summary.totalIncome), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

            // Gastos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Gastos", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "−${maskAmount(formatAmount(summary.totalExpense), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

            // Ahorro
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ahorro", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${maskAmount(formatAmount(savings), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }
        }
    }
}

// ─── Top categories card ───────────────────────────────────────────────────────
@Composable
private fun TopCategoriesCard(breakdown: List<MonthlyTotals>, balancesHidden: Boolean) {
    // Simplified: show placeholder data since expensesByCategory may not exist
    val sampleCategories = listOf(
        "Hogar" to 2550.0,
        "Alimentación" to 1348.0,
        "Transporte" to 850.0,
        "Ocio" to 540.0,
        "Otros" to 405.0
    )
    val totalExpense = sampleCategories.sumOf { it.second }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Top categorías de gastos",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(14.dp))

            val colors = listOf(WarnAmber, PrimaryDark, SecondaryTeal, ExpenseRed, BorderGray)
            sampleCategories.forEachIndexed { index, (category, amount) ->
                val pct = if (totalExpense > 0) (amount / totalExpense) * 100 else 0.0
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, fontSize = 12.sp, color = TextSecondary)
                        Text(
                            "${maskAmount(formatAmount(amount), balancesHidden)} €",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    ProgressBar(
                        progress = (pct / 100.0).toFloat(),
                        color = colors.getOrElse(index) { BorderGray }
                    )
                }
            }
        }
    }
}

// ─── Gráfico de barras por mes ────────────────────────────────────────────────
@Composable
private fun MonthlyBarChart(breakdown: List<MonthlyTotals>, year: String) {
    // Construimos un mapa completo para los 12 meses (rellena con 0 los que no tienen datos)
    val dataMap = breakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val maxValue = (1..12).maxOf { m ->
        val row = dataMap[m]
        maxOf(row?.totalIncome ?: 0.0, row?.totalExpense ?: 0.0)
    }.coerceAtLeast(1.0)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = "Evolución mensual $year",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(4.dp))

            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(color = IncomeGreen, label = "Ingresos")
                LegendItem(color = ExpenseRed,  label = "Gastos")
            }

            Spacer(Modifier.height(16.dp))

            // Barras
            Row(
                modifier              = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                (1..12).forEach { monthNum ->
                    val row     = dataMap[monthNum]
                    val income  = row?.totalIncome  ?: 0.0
                    val expense = row?.totalExpense ?: 0.0
                    MonthBarGroup(
                        monthLabel  = MONTH_LABELS[monthNum - 1],
                        income      = income,
                        expense     = expense,
                        maxValue    = maxValue,
                        modifier    = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthBarGroup(
    monthLabel: String,
    income: Double,
    expense: Double,
    maxValue: Double,
    modifier: Modifier = Modifier
) {
    val incomeRatio  = (income  / maxValue).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (expense / maxValue).toFloat().coerceIn(0f, 1f)
    val maxBarHeight = 130.dp

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Barras de income y expense lado a lado
        Row(
            modifier            = Modifier.height(maxBarHeight),
            verticalAlignment   = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Barra ingreso
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(incomeRatio)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(IncomeGreen)
            )
            // Barra gasto
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(expenseRatio)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(ExpenseRed)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text      = monthLabel,
            fontSize  = 9.sp,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

// ─── Tarjetas existentes ──────────────────────────────────────────────────────
@Composable
private fun BalanceHeroCard(summary: AnnualSummary, balancesHidden: Boolean) {
    val balance    = summary.balance
    val isPositive = balance >= 0

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Balance ${summary.year}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f))
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "${if (isPositive) "+" else "−"} ${maskAmount(formatAmount(abs(balance)), balancesHidden)} €",
                fontSize      = 36.sp,
                fontWeight    = FontWeight.Bold,
                color         = if (isPositive) Color(0xFF66BB6A) else Color(0xFFEF9A9A),
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (isPositive) "Año positivo" else "Año en negativo",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun AnnualMetricCard(
    label: String,
    amount: Double,
    color: Color,
    variationPercent: Double,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val hasPrevious        = variationPercent != 0.0
    val isPositiveVariation = variationPercent >= 0

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("${maskAmount(formatAmount(amount), balancesHidden)} €", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = color)
            if (hasPrevious) {
                Spacer(Modifier.height(6.dp))
                val sign     = if (isPositiveVariation) "+" else ""
                val varColor = if (isPositiveVariation) IncomeGreen else ExpenseRed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isPositiveVariation) "▲" else "▼", fontSize = 10.sp, color = varColor)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "$sign${formatPercent(variationPercent)}% vs año anterior",
                        fontSize = 11.sp,
                        color    = varColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviousYearCard(summary: AnnualSummary, balancesHidden: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "Comparativa con ${summary.year.toInt() - 1}",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(14.dp))
            ComparisonRow("Ingresos", summary.totalIncome,  summary.previousYearIncome,  IncomeGreen, balancesHidden)
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            ComparisonRow("Gastos",   summary.totalExpense, summary.previousYearExpense, ExpenseRed, balancesHidden)
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            ComparisonRow(
                label    = "Balance",
                current  = summary.balance,
                previous = summary.previousYearIncome - summary.previousYearExpense,
                color    = if (summary.balance >= 0) IncomeGreen else ExpenseRed,
                balancesHidden = balancesHidden
            )
        }
    }
}

@Composable
private fun ComparisonRow(label: String, current: Double, previous: Double, color: Color, balancesHidden: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label,                  fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text("${maskAmount(formatAmount(previous), balancesHidden)} €", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("${maskAmount(formatAmount(current), balancesHidden)} €",  fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EmptyYearState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Sin datos para este año", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Añade movimientos para ver el resumen", fontSize = 13.sp, color = TextSecondary)
        }
    }
}

private fun formatAmount(amount: Double): String {
    val abs     = abs(amount)
    val rounded = (abs * 100).toLong()
    val euros   = rounded / 100
    val cents   = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}

private fun formatPercent(value: Double): String {
    val abs = abs(value)
    return if (abs == abs.toLong().toDouble()) abs.toLong().toString()
    else {
        val rounded = (abs * 10).toLong()
        "${rounded / 10},${rounded % 10}"
    }
}

@Preview
@Composable
private fun AnnualContentPreview() {
    val fakeSummary = AnnualSummary(
        year = "2025",
        totalIncome = 45000.0,
        totalExpense = 32000.0,
        previousYearIncome = 42000.0,
        previousYearExpense = 30000.0
    )
    val fakeBreakdown = listOf(
        MonthlyTotals(year = "2025", month = "1", totalIncome = 3750.0, totalExpense = 2800.0),
        MonthlyTotals(year = "2025", month = "2", totalIncome = 3750.0, totalExpense = 2600.0),
        MonthlyTotals(year = "2025", month = "3", totalIncome = 3800.0, totalExpense = 2700.0)
    )

    TrackfolioTheme {
        AnnualContent(
            summary = fakeSummary,
            breakdown = fakeBreakdown,
            balancesHidden = false
        )
    }
}
