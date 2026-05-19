package es.aviferdev.n3to.ui.fixedincome.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.portfolio.MaturitySimulation
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_coupons_received
import n3to.composeapp.generated.resources.fixedincome_estimated_commissions
import n3to.composeapp.generated.resources.fixedincome_estimated_irpf
import n3to.composeapp.generated.resources.fixedincome_gross_interest
import n3to.composeapp.generated.resources.fixedincome_invested_label
import n3to.composeapp.generated.resources.fixedincome_maturity_simulation
import n3to.composeapp.generated.resources.fixedincome_net_maturity
import n3to.composeapp.generated.resources.fixedincome_net_profit_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaturitySimulatorCard(
    simulation: MaturitySimulation,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_maturity_simulation),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(12.dp))

            SimulatorRow(
                label = stringResource(Res.string.fixedincome_invested_label),
                value = maskAmount(formatAmount(simulation.capitalInvested), balancesHidden)
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_gross_interest),
                value = "+ ${maskAmount(formatAmount(simulation.grossInterest), balancesHidden)}",
                valueColor = MaterialTheme.appColors.pnlPositive
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_coupons_received),
                value = "- ${maskAmount(formatAmount(simulation.collectedCoupons), balancesHidden)}",
                valueColor = MaterialTheme.appColors.textSecondary
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_estimated_irpf, "19"),
                value = "- ${maskAmount(formatAmount(simulation.estimatedIrpf), balancesHidden)}",
                valueColor = MaterialTheme.appColors.pnlNegative
            )
            if (simulation.estimatedCommission > 0) {
                SimulatorRow(
                    label = stringResource(Res.string.fixedincome_estimated_commissions),
                    value = "- ${maskAmount(formatAmount(simulation.estimatedCommission), balancesHidden)}",
                    valueColor = MaterialTheme.appColors.pnlNegative
                )
            }

            HorizontalDivider(color = MaterialTheme.appColors.navyBorder, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.fixedincome_net_maturity),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
                Text(
                    text = "${maskAmount(formatAmount(simulation.netAtMaturity), balancesHidden)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.cyanAccent
                )
            }

            Spacer(Modifier.height(6.dp))

            val sign = if (simulation.netProfit >= 0) "+" else ""
            Text(
                text = "${stringResource(Res.string.fixedincome_net_profit_label)}: $sign${maskAmount(formatAmount(simulation.netProfit), balancesHidden)} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (simulation.netProfit >= 0) MaterialTheme.appColors.pnlPositive else MaterialTheme.appColors.pnlNegative
            )
        }
    }
}

@Composable
internal fun SimulatorRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.appColors.textPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
        Text(text = "$value €", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
