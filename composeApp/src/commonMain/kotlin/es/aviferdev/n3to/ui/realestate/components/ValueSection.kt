package es.aviferdev.n3to.ui.realestate.components

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.theme.formatPercent
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_capital_gain_label
import n3to.composeapp.generated.resources.realestate_effective_value_label
import n3to.composeapp.generated.resources.realestate_estimated_value_label
import n3to.composeapp.generated.resources.realestate_ownership_percent_label
import n3to.composeapp.generated.resources.realestate_purchase_value_label
import n3to.composeapp.generated.resources.realestate_sale_price_label
import n3to.composeapp.generated.resources.realestate_update_label
import n3to.composeapp.generated.resources.realestate_value_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun ValueSection(property: RealEstateProperty, onUpdateValue: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.realestate_value_label),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textPrimary
                )
                if (!property.isSold) {
                    TextButton(onClick = onUpdateValue) {
                        Text(
                            stringResource(Res.string.realestate_update_label),
                            color = MaterialTheme.appColors.primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            DataRow(
                stringResource(Res.string.realestate_estimated_value_label),
                formatAmountEuro(property.currentEstimatedValue)
            )
            DataRow(
                stringResource(Res.string.realestate_purchase_value_label),
                formatAmountEuro(property.purchaseValue)
            )
            DataRow(
                stringResource(Res.string.realestate_ownership_percent_label),
                "${formatPercent(property.ownershipPercentage)}%"
            )

            if (property.isSold && property.saleValue != null) {
                HorizontalDivider(
                    color = MaterialTheme.appColors.border2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                DataRow(
                    stringResource(Res.string.realestate_sale_price_label),
                    formatAmountEuro(property.saleValue)
                )
                val gain = property.realizedGain ?: 0.0
                val pct = property.realizedGainPercent ?: 0.0
                DataRow(
                    stringResource(Res.string.realestate_capital_gain_label),
                    "${if (gain >= 0) "+" else ""}${formatAmountEuro(gain)} (${
                        if (pct >= 0) "+" else ""
                    }${formatPercent(pct)}%)"
                )
            }

            HorizontalDivider(
                color = MaterialTheme.appColors.border2,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DataRow(
                stringResource(Res.string.realestate_effective_value_label),
                formatAmountEuro(property.effectiveValue)
            )
        }
    }
}
