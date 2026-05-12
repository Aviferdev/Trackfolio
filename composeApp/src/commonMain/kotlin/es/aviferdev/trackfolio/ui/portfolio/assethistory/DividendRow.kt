package es.aviferdev.trackfolio.ui.portfolio.assethistory

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.ui.portfolio.formatShortDate
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.currencySymbol
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.maskAmount

// ─── Dividend row ─────────────────────────────────────────────────────────────
@Composable
fun DividendRow(
    dividend: Transaction,
    currencyCode: String,
    balancesHidden: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val gross = dividend.grossAmount ?: dividend.amount
    val irpf = if (dividend.grossAmount != null && dividend.irpfPercent != null)
        dividend.grossAmount * dividend.irpfPercent / 100.0 else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(IncomeGreen.copy(.14f)),
                contentAlignment = Alignment.Center
            ) { Text("📈", fontSize = 16.sp) }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Dividendo",
                    fontSize = 12.sp,
                    color = IncomeGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(formatShortDate(dividend.date), fontSize = 10.sp, color = TextTertiary)
                if (irpf > 0) {
                    Text(
                        "Bruto: ${
                            maskAmount(
                                formatAmount(gross),
                                balancesHidden
                            )
                        } $symbol  ·  IRPF: ${
                            maskAmount(
                                formatAmount(irpf),
                                balancesHidden
                            )
                        } $symbol",
                        fontSize = 9.sp, color = TextTertiary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+ ${maskAmount(formatAmount(dividend.amount), balancesHidden)} $symbol",
                    fontSize = 12.sp, color = IncomeGreen, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        modifier = Modifier.size(13.dp),
                        tint = ExpenseRed
                    )
                }
            }
        }
    }
}