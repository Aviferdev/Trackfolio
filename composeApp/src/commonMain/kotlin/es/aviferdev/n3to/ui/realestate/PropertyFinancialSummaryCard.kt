package es.aviferdev.n3to.ui.realestate

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

@Composable
fun PropertyFinancialSummaryCard(
    summary: PropertyFinancialSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(label = "Rentabilidad")
            Spacer(Modifier.height(8.dp))

            if (summary.grossYieldOnPurchase > 0 || summary.grossYieldOnCurrent > 0) {
                DataRowLabel("Yield bruto s/ compra", "${formatPercent(summary.grossYieldOnPurchase)}%")
                DataRowLabel("Yield bruto s/ valor actual", "${formatPercent(summary.grossYieldOnCurrent)}%")
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
            }

            DataRowLabel("Ingresos (ledger)", formatAmountEuro(summary.totalIncome))
            DataRowLabel("Gastos (ledger)", formatAmountEuro(summary.totalExpenses))

            // Gastos de compra/venta
            if (summary.totalPurchaseExpenses > 0) {
                DataRowLabel("Gastos de compra", formatAmountEuro(summary.totalPurchaseExpenses))
            }
            if (summary.totalSaleExpenses > 0) {
                DataRowLabel("Gastos de venta", formatAmountEuro(summary.totalSaleExpenses))
            }

            HorizontalDivider(color = BorderGray2, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Cashflow neto
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cashflow neto", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Text(
                    if (summary.netCashflow >= 0) formatAmountEuro(summary.netCashflow) else "-${formatAmountEuro(-summary.netCashflow)}",
                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = if (summary.netCashflow >= 0) IncomeGreen else ExpenseRed
                )
            }

            // Retorno total (solo si vendida)
            if (summary.totalReturn != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = PrimaryDark.copy(alpha = 0.3f), thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Retorno total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text(
                            if (summary.totalReturn >= 0) "+${formatAmountEuro(summary.totalReturn)}" else "-${formatAmountEuro(-summary.totalReturn)}",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = if (summary.totalReturn >= 0) IncomeGreen else ExpenseRed
                        )
                        if (summary.totalReturnPercent != null) {
                            Text(
                                "${if (summary.totalReturnPercent >= 0) "+" else ""}${formatPercent(summary.totalReturnPercent)}%",
                                fontSize = 11.sp,
                                color = if (summary.totalReturnPercent >= 0) IncomeGreen else ExpenseRed
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
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
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
