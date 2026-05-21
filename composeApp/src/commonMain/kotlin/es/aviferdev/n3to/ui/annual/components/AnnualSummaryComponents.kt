package es.aviferdev.n3to.ui.annual.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.ui.annual.CategoryExpenseComparison
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.MONTH_LABELS
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_budget_configure
import n3to.composeapp.generated.resources.annual_budget_edit
import n3to.composeapp.generated.resources.annual_budget_no_limits
import n3to.composeapp.generated.resources.annual_budget_title
import n3to.composeapp.generated.resources.annual_expense_legend
import n3to.composeapp.generated.resources.annual_expenses_label
import n3to.composeapp.generated.resources.annual_income_label
import n3to.composeapp.generated.resources.annual_income_legend
import n3to.composeapp.generated.resources.annual_monthly_evolution
import n3to.composeapp.generated.resources.annual_new_badge
import n3to.composeapp.generated.resources.annual_no_data_text
import n3to.composeapp.generated.resources.annual_no_movements
import n3to.composeapp.generated.resources.annual_savings_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun YearTotalsCard(
    totalIncome: Double,
    totalExpense: Double,
    balancesHidden: Boolean
) {
    val savings = totalIncome - totalExpense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.annual_income_label),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "+${maskAmount(formatAmount(totalIncome), balancesHidden)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.income
                )
            }

            Box(
                modifier = Modifier.width(1.dp).fillMaxHeight(0.5f).align(Alignment.CenterVertically)
                    .background(MaterialTheme.appColors.navyBorder)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.annual_expenses_label),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "−${maskAmount(formatAmount(totalExpense), balancesHidden)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.expense
                )
            }

            Box(
                modifier = Modifier.width(1.dp).fillMaxHeight(0.5f).align(Alignment.CenterVertically)
                    .background(MaterialTheme.appColors.navyBorder)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.annual_savings_label),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${maskAmount(formatAmount(savings), balancesHidden)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.primary
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.appColors.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.annual_no_data_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.annual_no_movements),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
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
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${maskAmount(formatAmount(comp.currentAmount), balancesHidden)} €",
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary,
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
internal fun BudgetStatusCard(
    budgetStatus: List<CategoryBudgetStatus>,
    balancesHidden: Boolean,
    onConfigureBudgets: () -> Unit = {}
) {
    val itemsWithLimit = budgetStatus.filter { it.effectiveLimit > 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.annual_budget_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onConfigureBudgets,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        stringResource(Res.string.annual_budget_edit),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.cyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (itemsWithLimit.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(Res.string.annual_budget_no_limits),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(Res.string.annual_budget_configure),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(Modifier.height(14.dp))
                itemsWithLimit.forEach { budget ->
                    val barColor = when {
                        budget.isOverBudget -> MaterialTheme.appColors.expense
                        budget.isNearLimit -> MaterialTheme.appColors.warnAmber
                        else -> MaterialTheme.appColors.income
                    }
                    val budgetProgress = budget.progress.coerceIn(0f, 1f)

                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                budget.categoryName,
                                fontSize = 12.sp,
                                color = MaterialTheme.appColors.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${maskAmount(formatAmount(budget.spent), balancesHidden)} / ${
                                    maskAmount(formatAmount(budget.effectiveLimit), balancesHidden)
                                } €",
                                fontSize = 11.sp,
                                color = barColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        ProgressBar(
                            progress = budgetProgress,
                            color = barColor
                        )
                    }
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
        val varColor =
            if (isGood) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
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
            color = MaterialTheme.appColors.navySurfaceLight
        ) {
            Text(
                stringResource(Res.string.annual_new_badge),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textTertiary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.annual_monthly_evolution, year),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (showIncome) {
                    LegendItem(
                        color = MaterialTheme.appColors.income,
                        label = stringResource(Res.string.annual_income_legend)
                    )
                }
                if (showExpense) {
                    LegendItem(
                        color = MaterialTheme.appColors.expense,
                        label = stringResource(Res.string.annual_expense_legend)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                (1..12).forEach { monthNum ->
                    val row = dataMap[monthNum]
                    val income = row?.totalIncome ?: 0.0
                    val expense = row?.totalExpense ?: 0.0
                    MonthBarGroup(
                        monthLabel = MONTH_LABELS[monthNum - 1],
                        income = income,
                        expense = expense,
                        maxValue = maxValue,
                        showIncome = showIncome,
                        showExpense = showExpense,
                        modifier = Modifier.weight(1f)
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
    val incomeRatio = if (showIncome) (income / maxValue).toFloat().coerceIn(0f, 1f) else 0f
    val expenseRatio = if (showExpense) (expense / maxValue).toFloat().coerceIn(0f, 1f) else 0f
    val maxBarHeight = 130.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.height(maxBarHeight),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (showIncome) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(incomeRatio)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(MaterialTheme.appColors.income)
                )
            }
            if (showExpense) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(expenseRatio)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(MaterialTheme.appColors.expense)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = monthLabel,
            fontSize = 9.sp,
            color = MaterialTheme.appColors.textSecondary,
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
        Text(label, fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
    }
}
