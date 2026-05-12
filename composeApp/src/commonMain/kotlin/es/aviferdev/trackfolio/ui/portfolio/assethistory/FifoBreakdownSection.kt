package es.aviferdev.trackfolio.ui.portfolio.assethistory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.portfolio.FifoBreakdown
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.currencySymbol

// ─── FIFO breakdown ───────────────────────────────────────────────────────────
@Composable
fun FifoBreakdownSection(
    breakdown: FifoBreakdown,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val symbol = currencySymbol(currencyCode)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧾", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Desglose FIFO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        val subtitle = buildString {
                            val o = breakdown.openLots.size;
                            val s = breakdown.saleMatches.size
                            if (o > 0) append("$o ${if (o == 1) "lote en cartera" else "lotes en cartera"}")
                            if (o > 0 && s > 0) append("  ·  ")
                            if (s > 0) append("$s ${if (s == 1) "cierre" else "cierres"}")
                        }
                        Text(subtitle, fontSize = 10.sp, color = TextTertiary)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    HorizontalDivider(color = BorderGray, thickness = .5.dp)
                    if (breakdown.openLots.isNotEmpty()) {
                        FifoSubHeader("Lotes en cartera")
                        breakdown.openLots.forEachIndexed { i, lot ->
                            FifoOpenLotRow(
                                i + 1,
                                lot,
                                symbol,
                                balancesHidden
                            )
                        }
                    }
                    if (breakdown.saleMatches.isNotEmpty()) {
                        if (breakdown.openLots.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = BorderGray,
                                thickness = .5.dp
                            )
                        }
                        FifoSubHeader("Cierres FIFO")
                        breakdown.saleMatches.forEach {
                            FifoSaleMatchBlock(
                                it,
                                symbol,
                                balancesHidden
                            )
                        }
                    }
                }
            }
        }
    }
}

