package es.aviferdev.n3to.ui.fiscal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.FiscalIncomeTaxBreakdown
import es.aviferdev.n3to.domain.model.FiscalReportData
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fiscal_asset_header
import n3to.composeapp.generated.resources.fiscal_avg_price_header
import n3to.composeapp.generated.resources.fiscal_balance_header
import n3to.composeapp.generated.resources.fiscal_buys_header
import n3to.composeapp.generated.resources.fiscal_capital_gains_card
import n3to.composeapp.generated.resources.fiscal_commissions_short
import n3to.composeapp.generated.resources.fiscal_count_format
import n3to.composeapp.generated.resources.fiscal_current_value_label
import n3to.composeapp.generated.resources.fiscal_debts_title
import n3to.composeapp.generated.resources.fiscal_expenses_header
import n3to.composeapp.generated.resources.fiscal_gross_header
import n3to.composeapp.generated.resources.fiscal_gross_total
import n3to.composeapp.generated.resources.fiscal_i_owe
import n3to.composeapp.generated.resources.fiscal_income_header
import n3to.composeapp.generated.resources.fiscal_income_label
import n3to.composeapp.generated.resources.fiscal_income_type_header
import n3to.composeapp.generated.resources.fiscal_incomes_with_tax_info
import n3to.composeapp.generated.resources.fiscal_invested_label
import n3to.composeapp.generated.resources.fiscal_irpf_total
import n3to.composeapp.generated.resources.fiscal_month_header
import n3to.composeapp.generated.resources.fiscal_monthly_title
import n3to.composeapp.generated.resources.fiscal_net_header
import n3to.composeapp.generated.resources.fiscal_net_total
import n3to.composeapp.generated.resources.fiscal_no_category
import n3to.composeapp.generated.resources.fiscal_no_movements
import n3to.composeapp.generated.resources.fiscal_no_movements_year
import n3to.composeapp.generated.resources.fiscal_percent_header
import n3to.composeapp.generated.resources.fiscal_pnl_total_header
import n3to.composeapp.generated.resources.fiscal_portfolio_title
import n3to.composeapp.generated.resources.fiscal_realized_pnl_label
import n3to.composeapp.generated.resources.fiscal_sales_header
import n3to.composeapp.generated.resources.fiscal_social_security
import n3to.composeapp.generated.resources.fiscal_tax_breakdown_title
import n3to.composeapp.generated.resources.fiscal_taxable_base_est
import n3to.composeapp.generated.resources.fiscal_they_owe
import n3to.composeapp.generated.resources.fiscal_units_header
import n3to.composeapp.generated.resources.fiscal_withholding_label
import n3to.composeapp.generated.resources.fiscal_withholding_short
import n3to.composeapp.generated.resources.fiscal_year_activity
import n3to.composeapp.generated.resources.fiscal_year_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AnnualSummaryCard(report: FiscalReportData) {
    val s = report.annualSummary
    ReportCard(stringResource(Res.string.fiscal_year_title, report.year)) {
        if (s == null) {
            Text(stringResource(Res.string.fiscal_no_movements), fontSize = 13.sp, color = TextTertiary)
            return@ReportCard
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiscalMetricCell(
                    stringResource(Res.string.fiscal_income_label),
                    s.totalIncome,
                    IncomeGreen,
                    Modifier.weight(1f)
                )
                FiscalMetricCell(
                    stringResource(Res.string.fiscal_withholding_label),
                    report.incomeTaxBreakdown.sumOf { it.irpfTotal },
                    ExpenseRed,
                    Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val capitalGains = report.assetPositions.sumOf { it.realizedPnl }
                FiscalMetricCell(
                    stringResource(Res.string.fiscal_capital_gains_card),
                    capitalGains,
                    if (capitalGains >= 0.0) IncomeGreen else ExpenseRed,
                    Modifier.weight(1f)
                )
                val baseImponible = s.totalIncome + capitalGains
                FiscalMetricCell(
                    stringResource(Res.string.fiscal_taxable_base_est),
                    baseImponible,
                    CyanAccent,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun IncomeTaxBreakdownCard(report: FiscalReportData) {
    val bk        = report.incomeTaxBreakdown
    val totalGross = bk.sumOf { it.grossTotal }
    val totalIrpf  = bk.sumOf { it.irpfTotal }
    val totalNet   = bk.sumOf { it.netTotal }
    val totalSS    = bk.sumOf { it.socialSecurityTotal }
    val totalComm  = bk.sumOf { it.commissionTotal }

    ReportCard(stringResource(Res.string.fiscal_tax_breakdown_title, report.year)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            MetricCell(stringResource(Res.string.fiscal_gross_total), totalGross, TextPrimary,  Modifier.weight(1f))
            MetricCell(stringResource(Res.string.fiscal_irpf_total),  totalIrpf,  ExpenseRed,   Modifier.weight(1f))
            MetricCell(stringResource(Res.string.fiscal_net_total),   totalNet,   IncomeGreen,  Modifier.weight(1f))
        }
        if (totalSS > 0 || totalComm > 0) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                if (totalSS   > 0) MetricCell(stringResource(Res.string.fiscal_social_security),  totalSS,   WarnAmber, Modifier.weight(1f))
                if (totalComm > 0) MetricCell(stringResource(Res.string.fiscal_commissions_short), totalComm, WarnAmber, Modifier.weight(1f))
                if (totalSS > 0 && totalComm == 0.0)  Spacer(Modifier.weight(1f))
                if (totalSS == 0.0 && totalComm > 0)  Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = NavyBorder, thickness = .5.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.fiscal_income_type_header), fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(3f))
            Text(stringResource(Res.string.fiscal_gross_header),        fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_withholding_short),   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_net_header),          fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_percent_header),      fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(5.dp))
        bk.forEachIndexed { i, item ->
            if (i > 0) HorizontalDivider(color = NavyBorder, thickness = .3.dp)
            TaxBreakdownRow(item)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.fiscal_incomes_with_tax_info), fontSize = 10.sp, color = TextTertiary)
    }
}

@Composable
private fun TaxBreakdownRow(item: FiscalIncomeTaxBreakdown) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                item.incomeType.toMaterialIcon(),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = CyanAccent.copy(alpha = 0.8f)
            )
            Column {
                Text(item.incomeType.label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 13.sp)
                Text(stringResource(Res.string.fiscal_count_format, item.count, if (item.count != 1) "s" else ""), fontSize = 9.sp, color = TextTertiary)
            }
        }
        Text(formatAmt(item.grossTotal),    fontSize = 11.sp, color = TextPrimary,   modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.irpfTotal),     fontSize = 11.sp, color = ExpenseRed,    fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(item.netTotal),      fontSize = 11.sp, color = IncomeGreen,   fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatPct(item.avgIrpfPercent), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
internal fun MonthlyBreakdownCard(report: FiscalReportData) {
    val byMonth = report.monthlyBreakdown.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val hasData = (1..12).any { byMonth[it] != null }
    ReportCard(stringResource(Res.string.fiscal_monthly_title)) {
        if (!hasData) {
            Text(stringResource(Res.string.fiscal_no_movements_year, report.year), fontSize = 13.sp, color = TextTertiary)
            return@ReportCard
        }
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.fiscal_month_header),    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
            Text(stringResource(Res.string.fiscal_income_header),   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_expenses_header), fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_balance_header),  fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = NavyBorder, thickness = .5.dp)
        for (m in 1..12) {
            val row = byMonth[m] ?: continue
            HorizontalDivider(color = NavyBorder, thickness = .3.dp)
            MonthlyRow(m, row)
        }
    }
}

@Composable
private fun MonthlyRow(month: Int, data: MonthlyTotals) {
    val balance = data.balance
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(monthName(month), fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(2f))
        Text(formatAmt(data.totalIncome),  fontSize = 11.sp, color = IncomeGreen, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(formatAmt(data.totalExpense), fontSize = 11.sp, color = ExpenseRed,  modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        Text(
            "${if (balance >= 0) "+" else ""}${formatAmt(balance)}",
            fontSize = 11.sp,
            color = if (balance >= 0) IncomeGreen else ExpenseRed,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
internal fun DebtsCard(report: FiscalReportData) {
    ReportCard(stringResource(Res.string.fiscal_debts_title)) {
        report.activeDebts.forEachIndexed { i, debt ->
            if (i > 0) HorizontalDivider(color = NavyBorder, thickness = .3.dp)
            val isIOwe = debt.direction == DebtDirection.I_OWE
            val color  = if (isIOwe) ExpenseRed else IncomeGreen
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.personName, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(if (isIOwe) stringResource(Res.string.fiscal_i_owe) else stringResource(Res.string.fiscal_they_owe), fontSize = 11.sp, color = color)
                    debt.notes?.let { Text(it, fontSize = 10.sp, color = TextTertiary) }
                }
                Text(formatAmt(debt.amount), fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun PortfolioCard(report: FiscalReportData) {
    val positions     = report.assetPositions.filter { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }
    val totalInvested = positions.sumOf { it.totalCost }
    val totalValue    = positions.mapNotNull { it.currentValue }.sum()
    val totalRealized = positions.sumOf { it.realizedPnl }

    ReportCard(stringResource(Res.string.fiscal_portfolio_title)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), Arrangement.spacedBy(8.dp)) {
            MetricCell(stringResource(Res.string.fiscal_invested_label),      totalInvested, TextPrimary,  Modifier.weight(1f))
            MetricCell(stringResource(Res.string.fiscal_current_value_label), totalValue,    if (totalValue >= totalInvested) IncomeGreen else ExpenseRed, Modifier.weight(1f))
            MetricCell(stringResource(Res.string.fiscal_realized_pnl_label),  totalRealized, if (totalRealized >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
        }
        HorizontalDivider(color = NavyBorder, thickness = .5.dp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.fiscal_asset_header),    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
            Text(stringResource(Res.string.fiscal_units_header),    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_avg_price_header), fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text(stringResource(Res.string.fiscal_pnl_total_header), fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(4.dp))
        positions.forEachIndexed { i, pos ->
            if (i > 0) HorizontalDivider(color = NavyBorder, thickness = .3.dp)
            val totalPnl = (pos.unrealizedPnl ?: 0.0) + pos.realizedPnl
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(2f)) {
                    Text(pos.ticker, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(pos.categoryName ?: stringResource(Res.string.fiscal_no_category), fontSize = 9.sp, color = TextTertiary)
                }
                Text(formatQty(pos.netQuantity),  fontSize = 11.sp, color = TextPrimary,   modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(formatAmt(pos.avgCostBasis), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(
                    formatAmt(totalPnl),
                    fontSize = 11.sp,
                    color = if (totalPnl >= 0) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End
                )
            }
        }

        val yearActive = positions.filter { it.totalBought > 0 || it.totalSold > 0 }
        if (yearActive.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = NavyBorder, thickness = .5.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.fiscal_year_activity, report.year),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.fiscal_asset_header),       fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(2f))
                Text(stringResource(Res.string.fiscal_buys_header),        fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(stringResource(Res.string.fiscal_sales_header),       fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                Text(stringResource(Res.string.fiscal_realized_pnl_label), fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            }
            yearActive.forEachIndexed { i, pos ->
                if (i > 0) HorizontalDivider(color = NavyBorder, thickness = .3.dp)
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(pos.ticker,              fontSize = 12.sp, color = TextPrimary,  fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                    Text(formatAmt(pos.totalBought), fontSize = 11.sp, color = IncomeGreen, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text(formatAmt(pos.totalSold),   fontSize = 11.sp, color = ExpenseRed,  modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text(
                        formatAmt(pos.realizedPnl),
                        fontSize = 11.sp,
                        color = if (pos.realizedPnl >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
