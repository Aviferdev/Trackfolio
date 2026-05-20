package es.aviferdev.n3to.ui.portfolio

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
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.ui.portfolio.home.AssetRow

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_closed_badge
import n3to.composeapp.generated.resources.portfolio_summary_realized
import org.jetbrains.compose.resources.stringResource
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
        pos.realizedPnL > 0 -> MaterialTheme.appColors.income
        pos.realizedPnL < 0 -> MaterialTheme.appColors.expense
        else -> MaterialTheme.appColors.textSecondary
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface.copy(alpha = 0.8f)),
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
                    .background(MaterialTheme.appColors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    asset.ticker.take(3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary,
                    maxLines = 1
                )
                Text(stringResource(Res.string.portfolio_closed_badge), fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(Res.string.portfolio_summary_realized), fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
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
                asset = Asset(
                    id = "1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.",
                    notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0
                ),
                position = AssetPosition(
                    netQuantity = 0.0,
                    averageCostOfRemaining = 100.0,
                    totalInvestedRemaining = 1000.0,
                    realizedPnL = 250.0,
                    currentValue = 0.0,
                    unrealizedPnL = 0.0,
                    unrealizedPnLPercent = 0.0,
                    totalPnL = 250.0,
                    totalPnLPercent = 25.0,
                    hasCurrentPrice = false
                )
            ),
            balancesHidden = false,
            onClick = {}
        )
    }
}
