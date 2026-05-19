package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
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
import es.aviferdev.n3to.ui.common.component.IconActionButton

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import androidx.compose.ui.tooling.preview.Preview
import es.aviferdev.n3to.ui.theme.formatQty
import kotlin.math.abs

@Composable
fun AssetCard(
    row: AssetRow,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    onUpdatePrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset = row.asset
    val pos = row.position
    val pnlColor = when {
        pos.totalPnL > 0 -> IncomeGreen
        pos.totalPnL < 0 -> ExpenseRed
        else -> MaterialTheme.appColors.textSecondary
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.appColors.navySelected)
                    .border(1.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    asset.ticker.take(4),
                    fontSize = if (asset.ticker.length > 4) 8.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.cyanAccent,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${formatQty(pos.netQuantity)} × ${maskAmount(formatAmount(pos.averageCostOfRemaining), balancesHidden)} €",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (pos.hasCurrentPrice) "${maskAmount(formatAmount(pos.currentValue), balancesHidden)} €" else "—",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
                if (pos.hasCurrentPrice) {
                    Text(
                        "${if (pos.totalPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(pos.totalPnL)), balancesHidden)} €",
                        fontSize = 11.sp,
                        color = pnlColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconActionButton(
                onClick = onUpdatePrice,
                icon = Icons.Outlined.Refresh,
                label = "Actualizar precio",
                iconTint = MaterialTheme.appColors.cyanAccent
            )
        }
    }
}

@Preview
@Composable
private fun AssetCardPreview() {
    N3toTheme {
        AssetCard(
            row = AssetRow(
                asset = Asset(id = "1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0),
                position = AssetPosition(netQuantity = 10.0, averageCostOfRemaining = 100.0, totalInvestedRemaining = 1000.0, realizedPnL = 0.0, currentValue = 1500.0, unrealizedPnL = 500.0, unrealizedPnLPercent = 50.0, totalPnL = 500.0, totalPnLPercent = 50.0, hasCurrentPrice = true)
            ),
            balancesHidden = false,
            onClick = {},
            onUpdatePrice = {}
        )
    }
}
