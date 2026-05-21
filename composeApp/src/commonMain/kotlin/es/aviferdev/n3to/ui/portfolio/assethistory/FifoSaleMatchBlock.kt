package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.portfolio.FifoSaleMatch
import es.aviferdev.n3to.ui.portfolio.formatShortDate
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatQty
import es.aviferdev.n3to.ui.theme.maskAmount
import kotlin.math.abs

@Composable
fun FifoSaleMatchBlock(sale: FifoSaleMatch, masked: Boolean) {
    val pnlColor = when {
        sale.realizedPnL > 0 -> MaterialTheme.appColors.income
        sale.realizedPnL < 0 -> MaterialTheme.appColors.expense
        else -> MaterialTheme.appColors.textSecondary
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.appColors.surfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "↘",
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Venta de ${formatQty(sale.saleQuantity)} u.",
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "${formatShortDate(sale.saleDate)}  ·  ${
                        maskAmount(
                            formatAmount(sale.salePrice),
                            masked
                        )
                    } €/u.", fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("P&L", fontSize = 9.sp, color = MaterialTheme.appColors.textTertiary)
                Text(
                    if (sale.realizedPnL == 0.0) "—" else "${if (sale.realizedPnL >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(abs(sale.realizedPnL)),
                            masked
                        )
                    } €",
                    fontSize = 12.sp, color = pnlColor, fontWeight = FontWeight.Bold
                )
            }
        }
        if (sale.consumed.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            sale.consumed.forEach { c ->
                val cColor = when {
                    c.pnl > 0 -> MaterialTheme.appColors.income; c.pnl < 0 -> MaterialTheme.appColors.expense; else -> MaterialTheme.appColors.textSecondary
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 14.dp, top = 2.dp, bottom = 2.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "↳ ${formatQty(c.quantityConsumed)} u. del lote del ${formatShortDate(c.purchaseDate)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Text(
                            "compra a ${
                                maskAmount(
                                    formatAmount(c.purchasePrice),
                                    masked
                                )
                            } €/u.", fontSize = 9.sp, color = MaterialTheme.appColors.textTertiary
                        )
                    }
                    Text(
                        if (c.pnl == 0.0) "—" else "${if (c.pnl >= 0) "+" else "−"} ${
                            maskAmount(
                                formatAmount(abs(c.pnl)),
                                masked
                            )
                        } €",
                        fontSize = 10.sp, color = cColor, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
