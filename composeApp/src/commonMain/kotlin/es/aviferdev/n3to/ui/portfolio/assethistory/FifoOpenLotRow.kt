package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.portfolio.FifoOpenLot
import es.aviferdev.n3to.ui.portfolio.formatShortDate
import es.aviferdev.n3to.ui.theme.IncomeGreen

import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatQty
import es.aviferdev.n3to.ui.theme.maskAmount

@Composable
fun FifoOpenLotRow(index: Int, lot: FifoOpenLot, masked: Boolean) {
    val partial = lot.remainingQuantity < lot.originalQuantity
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp))
                .background(IncomeGreen.copy(.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text("#$index", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${formatQty(lot.remainingQuantity)} u. × ${
                    maskAmount(
                        formatAmount(lot.pricePerUnit),
                        masked
                    )
                } €",
                fontSize = 12.sp, color = MaterialTheme.appColors.textPrimary, fontWeight = FontWeight.Medium
            )
            Text(
                "Comprado el ${formatShortDate(lot.purchaseDate)}${
                    if (partial) "  ·  ${
                        formatQty(
                            lot.remainingQuantity
                        )
                    } de ${formatQty(lot.originalQuantity)} restantes" else ""
                }",
                fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary
            )
        }
        Text(
            "${maskAmount(formatAmount(lot.remainingCost), masked)} €",
            fontSize = 11.sp,
            color = MaterialTheme.appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
