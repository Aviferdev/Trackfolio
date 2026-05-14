package es.aviferdev.trackfolio.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.ui.common.ProgressBar
import es.aviferdev.trackfolio.ui.common.component.EmptyStateView
import es.aviferdev.trackfolio.ui.common.navigation.TimeStepperHeader
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

import es.aviferdev.trackfolio.ui.theme.MONTH_LABELS

enum class AnnualTab {
    RESUMEN, GASTOS, INGRESOS, INVERSIONES
}

@Composable
fun AnnualSummaryScreen(
    navigateBack: () -> Unit = {},
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
        TopBarApp(title = "Resumen anual", navigateBack = navigateBack)

        TimeStepperHeader(
            currentValue = uiState.year,
            canGoBack = uiState.canGoBack,
            onPrevious = { viewModel.previousYear() },
            onNext = { viewModel.nextYear() },
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
                    if (uiState.summary != null) {
                        ResumenTab(
                            summary        = uiState.summary!!,
                            breakdown      = uiState.monthlyBreakdown,
                            balancesHidden = balancesHidden
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Outlined.BarChart,
                            title = "Sin datos para este año",
                            subtitle = "Añade movimientos para ver el resumen"
                        )
                    }
                }
                AnnualTab.GASTOS -> {
                    GastosTab(
                        breakdown      = uiState.monthlyBreakdown,
                        comparisons    = uiState.categoryComparisons,
                        year           = uiState.year,
                        balancesHidden = balancesHidden
                    )
                }
                AnnualTab.INGRESOS -> {
                    IngresosTab(
                        breakdown      = uiState.monthlyBreakdown,
                        comparisons    = uiState.incomeComparisons,
                        year           = uiState.year,
                        balancesHidden = balancesHidden
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

// ─── Pestaña RESUMEN ────────────────────────────────────────────────────────────
@Composable
private fun ResumenTab(
    summary: AnnualSummary,
    breakdown: List<MonthlyTotals>,
    balancesHidden: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        YearTotalsCard(summary = summary, balancesHidden = balancesHidden)
        MonthlyBarChart(
            breakdown    = breakdown,
            year         = summary.year,
            showIncome   = true,
            showExpense  = true
        )
    }
}

// ─── Pestaña GASTOS ─────────────────────────────────────────────────────────────
@Composable
private fun GastosTab(
    breakdown: List<MonthlyTotals>,
    comparisons: List<CategoryExpenseComparison>,
    year: String,
    balancesHidden: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MonthlyBarChart(
            breakdown    = breakdown,
            year         = year,
            showIncome   = false,
            showExpense  = true
        )
        CategoryExpenseList(
            title          = "Categorías de gasto",
            comparisons    = comparisons,
            isExpense      = true,
            balancesHidden = balancesHidden
        )
    }
}

// ─── Pestaña INGRESOS ───────────────────────────────────────────────────────────
@Composable
private fun IngresosTab(
    breakdown: List<MonthlyTotals>,
    comparisons: List<CategoryExpenseComparison>,
    year: String,
    balancesHidden: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MonthlyBarChart(
            breakdown    = breakdown,
            year         = year,
            showIncome   = true,
            showExpense  = false
        )
        CategoryExpenseList(
            title          = "Tipos de ingreso",
            comparisons    = comparisons,
            isExpense      = false,
            balancesHidden = balancesHidden
        )
    }
}

// ─── Tabs ───────────────────────────────────────────────────────────────────────
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

// ─── Year totals card (3-column grid) ───────────────────────────────────────────
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

// ─── Lista de categorías con comparativa interanual ─────────────────────────────
@Composable
private fun CategoryExpenseList(
    title: String,
    comparisons: List<CategoryExpenseComparison>,
    isExpense: Boolean,
    balancesHidden: Boolean
) {
    if (comparisons.isEmpty()) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(14.dp),
            colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp),
            border    = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.BarChart, contentDescription = null, modifier = Modifier.size(32.dp), tint = PrimaryDark)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sin datos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "No hay movimientos registrados",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        return
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(14.dp))

            comparisons.forEach { comp ->
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nombre
                        Text(
                            comp.name,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        // Importe
                        Text(
                            "${maskAmount(formatAmount(comp.currentAmount), balancesHidden)} €",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        // Badge de variación
                        VariationBadge(
                            changePercent = comp.changePercent,
                            previousAmount = comp.previousAmount,
                            isExpense = isExpense
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    ProgressBar(
                        progress = (comp.currentPercent / 100.0).toFloat(),
                        color = comp.color
                    )
                }
            }
        }
    }
}

@Composable
private fun VariationBadge(
    changePercent: Double?,
    previousAmount: Double?,
    isExpense: Boolean
) {
    if (changePercent != null) {
        val isPositive = changePercent >= 0
        // Para gastos: ▲ aumento es malo (rojo), ▼ disminución es bueno (verde)
        // Para ingresos: ▲ aumento es bueno (verde), ▼ disminución es malo (rojo)
        val isGood = if (isExpense) !isPositive else isPositive
        val varColor = if (isGood) IncomeGreen else ExpenseRed
        val arrow = if (isPositive) "▲" else "▼"
        val sign = if (isPositive && changePercent > 0) "+" else ""

        Text(
            "$arrow $sign${formatPercent(changePercent)}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = varColor
        )
    } else if (previousAmount == null) {
        // Categoría nueva (no existía el año anterior)
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = BackgroundGray
        ) {
            Text(
                " Nuevo ",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = TextTertiary
            )
        }
    }
}

// ─── Gráfico de barras por mes ─────────────────────────────────────────────────
@Composable
private fun MonthlyBarChart(
    breakdown: List<MonthlyTotals>,
    year: String,
    showIncome: Boolean = true,
    showExpense: Boolean = true
) {
    // Construimos un mapa completo para los 12 meses (rellena con 0 los que no tienen datos)
    val dataMap = breakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val maxValue = (1..12).maxOf { m ->
        val row = dataMap[m]
        val incomeVal = if (showIncome) row?.totalIncome ?: 0.0 else 0.0
        val expenseVal = if (showExpense) row?.totalExpense ?: 0.0 else 0.0
        maxOf(incomeVal, expenseVal)
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
                if (showIncome) {
                    LegendItem(color = IncomeGreen, label = "Ingresos")
                }
                if (showExpense) {
                    LegendItem(color = ExpenseRed, label = "Gastos")
                }
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
                        monthLabel   = MONTH_LABELS[monthNum - 1],
                        income       = income,
                        expense      = expense,
                        maxValue     = maxValue,
                        showIncome   = showIncome,
                        showExpense  = showExpense,
                        modifier     = Modifier.weight(1f)
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
    showIncome: Boolean = true,
    showExpense: Boolean = true,
    modifier: Modifier = Modifier
) {
    val incomeRatio  = if (showIncome) (income / maxValue).toFloat().coerceIn(0f, 1f) else 0f
    val expenseRatio = if (showExpense) (expense / maxValue).toFloat().coerceIn(0f, 1f) else 0f
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
            if (showIncome) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(incomeRatio)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(IncomeGreen)
                )
            }
            // Barra gasto
            if (showExpense) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(expenseRatio)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(ExpenseRed)
                )
            }
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

// EmptyYearState reemplazado por EmptyStateView de ui.common.component
// formatAmount y formatPercent se importan de ui.theme
