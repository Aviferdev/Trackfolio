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
import androidx.compose.material.icons.filled.Edit
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
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.portfolio.formatQty
import es.aviferdev.trackfolio.ui.portfolio.formatShortDate
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.currencySymbol
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.maskAmount

@Composable
fun TxRow(
    tx: AssetTransaction,
    platform: Platform?,
    currencyCode: String,
    balancesHidden: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isBuy = tx.type == AssetTransactionType.BUY || tx.type == AssetTransactionType.TRANSFER_IN
    val sideColor = if (isBuy) IncomeGreen else ExpenseRed
    val sideLabel = when (tx.type) {
        AssetTransactionType.BUY -> "Compra"
        AssetTransactionType.SELL -> "Venta"
        AssetTransactionType.TRANSFER_OUT -> "Traspaso salida"
        AssetTransactionType.TRANSFER_IN -> "Traspaso entrada"
    }
    val sideIcon = when (tx.type) {
        AssetTransactionType.BUY -> "↗"
        AssetTransactionType.SELL -> "↘"
        AssetTransactionType.TRANSFER_OUT -> "→"
        AssetTransactionType.TRANSFER_IN -> "←"
    }

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
                    .background(sideColor.copy(.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(sideIcon, fontSize = 16.sp, color = sideColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        sideLabel,
                        fontSize = 12.sp,
                        color = sideColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${formatQty(tx.quantity)} × ${
                            maskAmount(
                                formatAmount(tx.pricePerUnit),
                                balancesHidden
                            )
                        } $symbol", fontSize = 11.sp, color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatShortDate(tx.date), fontSize = 10.sp, color = TextTertiary)
                    if (platform != null) {
                        Text("  ·  ", fontSize = 10.sp, color = TextTertiary)
                        Text(platform.icon, fontSize = 11.sp)
                        Spacer(Modifier.width(2.dp))
                        Text(platform.name, fontSize = 10.sp, color = TextTertiary)
                    }
                }
                if (!tx.feeNote.isNullOrBlank()) Text(
                    "Com: ${tx.feeNote}",
                    fontSize = 9.sp,
                    color = TextTertiary
                )
                if (!tx.notes.isNullOrBlank()) Text(
                    tx.notes,
                    fontSize = 9.sp,
                    color = TextTertiary,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isBuy) "+" else "−"} ${
                        maskAmount(
                            formatAmount(tx.grossAmount),
                            balancesHidden
                        )
                    } $symbol",
                    fontSize = 12.sp, color = sideColor, fontWeight = FontWeight.Bold
                )
                Row {
                    if (!tx.isTransfer) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                modifier = Modifier.size(13.dp),
                                tint = TextSecondary
                            )
                        }
                    }
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
}