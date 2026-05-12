package es.aviferdev.trackfolio.ui.portfolio.assethistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.ui.portfolio.formatQty
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.currencySymbol
import es.aviferdev.trackfolio.ui.theme.formatAmount
import kotlin.math.abs


// ─── Summary card (price + position) ───────────────────────────────────────────
@Composable
fun AssetSummaryCard(
    ticker: String,
    currentPrice: Double?,
    currencyCode: String,
    position: AssetPosition?,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isOpen = position?.netQuantity ?: 0.0 > 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 3-column grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // P&L FIFO
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "P&L FIFO",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    val pnlColor = when {
                        (position?.totalPnL ?: 0.0) > 0 -> IncomeGreen
                        (position?.totalPnL ?: 0.0) < 0 -> ExpenseRed
                        else -> TextPrimary
                    }
                    Text(
                        if ((position?.totalPnL ?: 0.0) == 0.0) "—"
                        else "${if ((position?.totalPnL ?: 0.0) >= 0) "+" else "−"} ${
                            formatAmount(
                                kotlin.math.abs(position?.totalPnL ?: 0.0)
                            )
                        } €",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

                // Posición
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Posición",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isOpen) "${formatQty(position!!.netQuantity)} uds" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

                // Coste medio
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Coste medio",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isOpen && position!!.averageCostOfRemaining > 0) "${
                            formatAmount(
                                position.averageCostOfRemaining
                            )
                        } €" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            // Bottom row: Precio actual
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Precio actual", fontSize = 11.sp, color = TextTertiary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        currentPrice?.let { "${formatAmount(it)} €" } ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (currentPrice != null && position?.averageCostOfRemaining != null && position.averageCostOfRemaining > 0) {
                        val pctChange =
                            ((currentPrice - position.averageCostOfRemaining) / position.averageCostOfRemaining) * 100
                        Spacer(Modifier.width(8.dp))
                        val isPositive = pctChange >= 0
                        Text(
                            "${if (isPositive) "+" else "−"}${
                                formatPercent1(
                                    abs(
                                        pctChange
                                    )
                                )
                            }%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}


// ─── Helpers ──────────────────────────────────────────────────────────────────
fun formatPercent1(value: Double): String {
    val r = (value * 10).toLong()
    return "${r / 10},${r % 10}"
}