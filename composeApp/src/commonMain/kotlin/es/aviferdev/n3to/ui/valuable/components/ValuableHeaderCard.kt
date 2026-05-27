package es.aviferdev.n3to.ui.valuable.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.ValuableSummary
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.formatPercentSigned
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.valuable_balance_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ValuableHeaderCard(summary: ValuableSummary) {
    val valuable = summary.valuable
    val currency = LocalCurrencySymbol.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.valuable_balance_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.appColors.textPrimary
                )
                if (valuable.isSold) {
                    val profit = summary.realizedProfit
                    Text(
                        text = if (profit != null) formatAmountEuro(profit, currency) else "-",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (profit != null && profit >= 0) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                    )
                } else {
                    Text(
                        text = formatAmountEuro(valuable.currentValue, currency),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
            }
            val profitPct = summary.realizedProfitPercent
            if (profitPct != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DeltaIndicator(
                        value = formatPercentSigned(profitPct),
                        isPositive = (summary.realizedProfit ?: 0.0) >= 0
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.appColors.border, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem("Compra", formatAmountEuro(valuable.purchasePrice, currency))
                if (valuable.isSold) {
                    MetricItem("Venta", formatAmountEuro(valuable.salePrice ?: 0.0, currency))
                }
                MetricItem("Gastos", formatAmountEuro(summary.totalExpenses, currency))
            }
        }
    }
}
