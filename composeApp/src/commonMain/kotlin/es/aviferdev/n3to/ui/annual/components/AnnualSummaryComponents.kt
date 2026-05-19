package es.aviferdev.n3to.ui.annual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.ui.annual.AnnualTab
import es.aviferdev.n3to.ui.annual.CategoryExpenseComparison
import es.aviferdev.n3to.ui.annual.GoalSummaryCard
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.MONTH_LABELS
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_expense_categories_title
import n3to.composeapp.generated.resources.annual_expense_legend
import n3to.composeapp.generated.resources.annual_expenses_label
import n3to.composeapp.generated.resources.annual_income_label
import n3to.composeapp.generated.resources.annual_income_legend
import n3to.composeapp.generated.resources.annual_income_types_title
import n3to.composeapp.generated.resources.annual_monthly_evolution
import n3to.composeapp.generated.resources.annual_new_badge
import n3to.composeapp.generated.resources.annual_no_data_text
import n3to.composeapp.generated.resources.annual_no_movements
import n3to.composeapp.generated.resources.annual_savings_label
import n3to.composeapp.generated.resources.annual_tab_expenses
import n3to.composeapp.generated.resources.annual_tab_income
import n3to.composeapp.generated.resources.annual_tab_investments
import n3to.composeapp.generated.resources.annual_tab_summary
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResumenTab(
    summary: AnnualSummary,
    breakdown: List<MonthlyTotals>,
    balancesHidden: Boolean,
    goalProgress: List<MonthlyGoalProgress> = emptyList()
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
        GoalSummaryCard(
            goalProgress = goalProgress,
            modifier = Modifier.fillMaxWidth()
        )
        MonthlyBarChart(
            breakdown    = breakdown,
            year         = summary.year,
            showIncome   = true,
            showExpense  = true
        )
    }
}

@Composable
internal fun GastosTab(
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
            title          = stringResource(Res.string.annual_expense_categories_title),
            comparisons    = comparisons,
            isExpense      = true,
            balancesHidden = balancesHidden
        )
    }
}

@Composable
internal fun IngresosTab(
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
            title          = stringResource(Res.string.annual_income_types_title),
            comparisons    = comparisons,
            isExpense      = false,
            balancesHidden = balancesHidden
        )
    }
}

@Composable
internal fun AnnualTabs(
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

@Composable
internal fun AnnualTab.displayName(): String = when (this) {
    AnnualTab.RESUMEN    -> stringResource(Res.string.annual_tab_summary)
    AnnualTab.GASTOS     -> stringResource(Res.string.annual_tab_expenses)
    AnnualTab.INGRESOS   -> stringResource(Res.string.annual_tab_income)
    AnnualTab.INVERSIONES -> stringResource(Res.string.annual_tab_investments)
}

@Composable
internal fun YearTotalsCard(summary: AnnualSummary, balancesHidden: Boolean) {
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.annual_income_label), fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "+${maskAmount(formatAmount(summary.totalIncome), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
            }

            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.annual_expenses_label), fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "−${maskAmount(formatAmount(summary.totalExpense), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }

            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.annual_savings_label), fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
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

@Composable
internal fun CategoryExpenseList(
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
                    stringResource(Res.string.annual_no_data_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.annual_no_movements),
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
                        Text(
                            comp.name,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${maskAmount(formatAmount(comp.currentAmount), balancesHidden)} €",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
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
internal fun VariationBadge(
    changePercent: Double?,
    previousAmount: Double?,
    isExpense: Boolean
) {
    if (changePercent != null) {
        val isPositive = changePercent >= 0
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
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = BackgroundGray
        ) {
            Text(
                stringResource(Res.string.annual_new_badge),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = TextTertiary
            )
        }
    }
}

@Composable
internal fun MonthlyBarChart(
    breakdown: List<MonthlyTotals>,
    year: String,
    showIncome: Boolean = true,
    showExpense: Boolean = true
) {
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
                text       = stringResource(Res.string.annual_monthly_evolution, year),
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (showIncome) {
                    LegendItem(color = IncomeGreen, label = stringResource(Res.string.annual_income_legend))
                }
                if (showExpense) {
                    LegendItem(color = ExpenseRed, label = stringResource(Res.string.annual_expense_legend))
                }
            }

            Spacer(Modifier.height(16.dp))

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
internal fun MonthBarGroup(
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
        Row(
            modifier            = Modifier.height(maxBarHeight),
            verticalAlignment   = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (showIncome) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(incomeRatio)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(IncomeGreen)
                )
            }
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
internal fun LegendItem(color: Color, label: String) {
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
