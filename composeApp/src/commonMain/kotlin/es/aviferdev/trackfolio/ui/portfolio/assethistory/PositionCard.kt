package es.aviferdev.trackfolio.ui.portfolio.assethistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.ui.portfolio.formatQty
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatPercent
import es.aviferdev.trackfolio.ui.theme.maskAmount
import kotlin.math.abs


// ─── Position card ────────────────────────────────────────────────────────────
@Composable
fun PositionCard(
    position: AssetPosition,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val isOpen = position.netQuantity > 0.0
    val pnlColor = when {
        position.totalPnL > 0 -> Color(0xFF86EFAC)
        position.totalPnL < 0 -> Color(0xFFFCA5A5)
        else -> Color.White
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                PositionMetric(
                    "Cantidad",
                    if (isOpen) formatQty(position.netQuantity) else "—",
                    Color.White
                )
                PositionMetric(
                    "Coste medio",
                    if (isOpen) "${
                        maskAmount(
                            formatAmount(position.averageCostOfRemaining),
                            balancesHidden
                        )
                    } €" else "—",
                    Color.White,
                    alignEnd = true
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                PositionMetric(
                    "Invertido",
                    if (isOpen) "${
                        maskAmount(
                            formatAmount(position.totalInvestedRemaining),
                            balancesHidden
                        )
                    } €" else "—",
                    Color.White.copy(.8f)
                )
                PositionMetric(
                    "Valor actual",
                    when {
                        !isOpen -> "—"
                        position.hasCurrentPrice -> "${
                            maskAmount(
                                formatAmount(position.currentValue),
                                balancesHidden
                            )
                        } €"

                        else -> "Sin precio"
                    },
                    Color.White,
                    alignEnd = true
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(12.dp))

            Text("Beneficio total", fontSize = 10.sp, color = Color.White.copy(.5f))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (position.totalPnL == 0.0) "—"
                    else "${if (position.totalPnL >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(
                                abs(
                                    position.totalPnL
                                )
                            ), balancesHidden
                        )
                    } €",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
                if (position.totalPnL != 0.0 && (isOpen || position.realizedPnL != 0.0)) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${if (position.totalPnLPercent >= 0) "+" else "−"}${
                            formatPercent(
                                abs(position.totalPnLPercent)
                            )
                        }%",
                        fontSize = 12.sp,
                        color = pnlColor.copy(.8f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            if (position.realizedPnL != 0.0 || position.unrealizedPnL != 0.0 || position.dividendIncome != 0.0) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    PnLChip("Realizado", position.realizedPnL, balancesHidden)
                    if (position.dividendIncome != 0.0) PnLChip(
                        "Dividendos",
                        position.dividendIncome,
                        balancesHidden
                    )
                    PnLChip(
                        "Latente",
                        position.unrealizedPnL,
                        balancesHidden,
                        unavailable = !position.hasCurrentPrice && isOpen
                    )
                }
            }
        }
    }
}
