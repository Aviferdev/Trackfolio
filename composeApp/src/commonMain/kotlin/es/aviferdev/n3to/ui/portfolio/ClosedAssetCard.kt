package es.aviferdev.n3to.ui.portfolio

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun ClosedAssetCard(
    row: AssetRow,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset = row.asset
    val pos = row.position
    val pnlColor = when {
        pos.realizedPnL > 0 -> IncomeGreen
        pos.realizedPnL < 0 -> ExpenseRed
        else -> TextSecondary
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    asset.ticker.take(3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text("Cerrada", fontSize = 10.sp, color = TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Realizado", fontSize = 10.sp, color = TextTertiary)
                Text(
                    if (pos.realizedPnL == 0.0) "—"
                    else "${if (pos.realizedPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(pos.realizedPnL)), balancesHidden)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun ClosedAssetCardPreview() {
    N3toTheme {
        ClosedAssetCard(
            row = AssetRow(
                asset = es.aviferdev.n3to.domain.model.Asset(
                    id = "1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.",
                    notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0
                ),
                position = es.aviferdev.n3to.domain.portfolio.AssetPosition(
                    netQuantity = 0.0, averageCostOfRemaining = 100.0, totalInvestedRemaining = 1000.0,
                    realizedPnL = 250.0, currentValue = 0.0, unrealizedPnL = 0.0, unrealizedPnLPercent = 0.0,
                    totalPnL = 250.0, totalPnLPercent = 25.0, hasCurrentPrice = false
                )
            ),
            balancesHidden = false,
            onClick = {}
        )
    }
}
