package es.aviferdev.n3to.ui.portfolio

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun PortfolioSummaryCard(
    totalInvested: Double,
    totalCurrentValue: Double,
    totalPnL: Double,
    totalPnLPercent: Double,
    totalRealizedPnL: Double,
    totalUnrealizedPnL: Double,
    positionsCount: Int,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text("Valor total", fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    maskAmount(formatAmount(totalCurrentValue), balancesHidden),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "€",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                PortfolioMetric(
                    label = "Invertido",
                    primary = "${maskAmount(formatAmount(totalInvested), balancesHidden)} €",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(0.5.dp)
                        .height(44.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
                PortfolioMetric(
                    label = "Beneficio total",
                    primary = if (totalPnL == 0.0) "—"
                    else "${if (totalPnL >= 0) "+" else "−"} ${
                        maskAmount(formatAmount(abs(totalPnL)), balancesHidden)
                    } €",
                    secondary = if (totalPnL == 0.0) null
                    else "${if (totalPnLPercent >= 0) "+" else "−"}${formatPercent(abs(totalPnLPercent))}%",
                    color = when {
                        totalPnL > 0 -> Color(0xFF86EFAC)
                        totalPnL < 0 -> Color(0xFFFCA5A5)
                        else -> Color.White
                    },
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            if (totalRealizedPnL != 0.0 && totalUnrealizedPnL != 0.0) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    PnLChip("Realizado", totalRealizedPnL, balancesHidden)
                    PnLChip("Latente", totalUnrealizedPnL, balancesHidden)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "$positionsCount ${if (positionsCount == 1) "posición abierta" else "posiciones abiertas"}",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
fun PortfolioMetric(
    label: String,
    primary: String,
    color: Color,
    secondary: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))
        Text(primary, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        if (secondary != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                secondary,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PnLChip(label: String, amount: Double, masked: Boolean) {
    val color = when {
        amount > 0 -> Color(0xFF86EFAC)
        amount < 0 -> Color(0xFFFCA5A5)
        else -> Color.White.copy(alpha = 0.5f)
    }
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
        Spacer(Modifier.height(2.dp))
        Text(
            if (amount == 0.0) "—"
            else "${if (amount >= 0) "+" else "−"} ${
                maskAmount(formatAmount(abs(amount)), masked)
            } €",
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun PortfolioSummaryCardPreview() {
    N3toTheme {
        PortfolioSummaryCard(
            totalInvested = 10000.0,
            totalCurrentValue = 12500.0,
            totalPnL = 2500.0,
            totalPnLPercent = 25.0,
            totalRealizedPnL = 500.0,
            totalUnrealizedPnL = 2000.0,
            positionsCount = 5,
            balancesHidden = false
        )
    }
}
