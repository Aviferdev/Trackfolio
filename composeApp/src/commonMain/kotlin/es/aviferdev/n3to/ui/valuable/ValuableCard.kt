package es.aviferdev.n3to.ui.valuable

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import trackfolio.composeapp.generated.resources.Res
import trackfolio.composeapp.generated.resources.valuable_in_stock
import trackfolio.composeapp.generated.resources.valuable_purchase_format

@Composable
fun ValuableCard(
    valuable: Valuable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = valuable.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (valuable.description.isNotBlank()) {
                    Text(
                        text = valuable.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.valuable_purchase_format, formatAmountEuro(valuable.purchasePrice)),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (valuable.isSold) {
                    Text(
                        text = formatAmountEuro(valuable.salePrice ?: 0.0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.income
                    )
                    valuable.grossProfitPercent?.let { pct ->
                        DeltaIndicator(
                            value = formatPercentSigned(pct),
                            isPositive = pct >= 0
                        )
                    }
                } else {
                    Text(
                        text = formatAmountEuro(valuable.currentValue),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Text(
                        text = stringResource(Res.string.valuable_in_stock),
                        fontSize = 11.sp,
                        color = SecondaryTeal
                    )
                }
            }
        }
    }
}
