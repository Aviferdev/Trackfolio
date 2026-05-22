package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.portfolio.formatShortDate
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_dividend_title_alt
import org.jetbrains.compose.resources.stringResource

// ─── Dividend row ─────────────────────────────────────────────────────────────
@Composable
fun DividendRow(
    dividend: Transaction,
    balancesHidden: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gross = dividend.grossAmount ?: dividend.amount
    val irpf =
        dividend.taxLines.firstOrNull { it.role == es.aviferdev.n3to.domain.model.TaxRole.INCOME_TAX }?.amount
            ?: 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.appColors.income.copy(.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.appColors.income
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.portfolio_dividend_title_alt),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.income,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatShortDate(dividend.date),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
                if (irpf > 0) {
                    Text(
                        "Bruto: ${
                            maskAmount(
                                formatAmount(gross),
                                balancesHidden
                            )
                        } €  ·  Retención: ${
                            maskAmount(
                                formatAmount(irpf),
                                balancesHidden
                            )
                        } €",
                        fontSize = 9.sp, color = MaterialTheme.appColors.textTertiary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+ ${maskAmount(formatAmount(dividend.amount), balancesHidden)} €",
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.income,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.appColors.expense
                    )
                }
            }
        }
    }
}