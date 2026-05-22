package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.usecase.realestate.PropertyFinancialSummary
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_yield_current
import n3to.composeapp.generated.resources.fixedincome_yield_gross
import n3to.composeapp.generated.resources.realestate_cashflow_label
import n3to.composeapp.generated.resources.realestate_expenses_ledger
import n3to.composeapp.generated.resources.realestate_income_ledger
import n3to.composeapp.generated.resources.realestate_profitability_label
import n3to.composeapp.generated.resources.realestate_purchase_expenses
import n3to.composeapp.generated.resources.realestate_sale_expenses
import n3to.composeapp.generated.resources.realestate_total_return_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun PropertyFinancialSummaryCard(
    summary: PropertyFinancialSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(label = stringResource(Res.string.realestate_profitability_label))
            Spacer(Modifier.height(8.dp))

            if (summary.grossYieldOnPurchase > 0 || summary.grossYieldOnCurrent > 0) {
                DataRowLabel(
                    stringResource(Res.string.fixedincome_yield_gross),
                    "${formatPercent(summary.grossYieldOnPurchase)}%"
                )
                DataRowLabel(
                    stringResource(Res.string.fixedincome_yield_current),
                    "${formatPercent(summary.grossYieldOnCurrent)}%"
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.appColors.border, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
            }

            DataRowLabel(stringResource(Res.string.realestate_income_ledger), formatAmountEuro(summary.totalIncome))
            DataRowLabel(stringResource(Res.string.realestate_expenses_ledger), formatAmountEuro(summary.totalExpenses))

            // Gastos de compra/venta
            if (summary.totalPurchaseExpenses > 0) {
                DataRowLabel(
                    stringResource(Res.string.realestate_purchase_expenses),
                    formatAmountEuro(summary.totalPurchaseExpenses)
                )
            }
            if (summary.totalSaleExpenses > 0) {
                DataRowLabel(
                    stringResource(Res.string.realestate_sale_expenses),
                    formatAmountEuro(summary.totalSaleExpenses)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.appColors.border2,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Cashflow neto
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.realestate_cashflow_label),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textPrimary
                )
                Text(
                    if (summary.netCashflow >= 0) formatAmountEuro(summary.netCashflow) else "-${
                        formatAmountEuro(
                            -summary.netCashflow
                        )
                    }",
                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = if (summary.netCashflow >= 0) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                )
            }

            // Retorno total (solo si vendida)
            if (summary.totalReturn != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.appColors.primary.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(Res.string.realestate_total_return_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text(
                            if (summary.totalReturn >= 0) "+${formatAmountEuro(summary.totalReturn)}" else "-${
                                formatAmountEuro(
                                    -summary.totalReturn
                                )
                            }",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = if (summary.totalReturn >= 0) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                        )
                        if (summary.totalReturnPercent != null) {
                            Text(
                                "${if (summary.totalReturnPercent >= 0) "+" else ""}${
                                    formatPercent(
                                        summary.totalReturnPercent
                                    )
                                }%",
                                fontSize = 11.sp,
                                color = if (summary.totalReturnPercent >= 0) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRowLabel(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary)
        Text(
            value,
            fontSize = 13.sp,
            color = MaterialTheme.appColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun PropertyFinancialSummaryCardPreview() {
    N3toTheme {
        PropertyFinancialSummaryCard(
            summary = PropertyFinancialSummary(
                grossYieldOnPurchase = 5.76, grossYieldOnCurrent = 5.54,
                totalIncome = 14400.0, totalExpenses = 3600.0, netCashflow = 10800.0,
                totalPurchaseExpenses = 3500.0, totalSaleExpenses = 0.0,
                totalReturn = null, totalReturnPercent = null
            )
        )
    }
}
