package es.aviferdev.n3to.ui.portfolio

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.CyanGlow
import es.aviferdev.n3to.ui.theme.CyanSubtle
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.NavySurfaceLight
import es.aviferdev.n3to.ui.theme.PnLNegative
import es.aviferdev.n3to.ui.theme.PnLPositive
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_summary_invested
import n3to.composeapp.generated.resources.portfolio_summary_positions_many
import n3to.composeapp.generated.resources.portfolio_summary_positions_one
import n3to.composeapp.generated.resources.portfolio_summary_realized
import n3to.composeapp.generated.resources.portfolio_summary_total_pnl
import n3to.composeapp.generated.resources.portfolio_summary_total_value
import n3to.composeapp.generated.resources.portfolio_summary_unrealized
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(NavySurface, NavySurfaceLight),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 110.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(stringResource(Res.string.portfolio_summary_total_value), fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                maskAmount(formatAmount(totalCurrentValue), balancesHidden),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                lineHeight = 36.sp
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
                label = stringResource(Res.string.portfolio_summary_invested),
                primary = "${maskAmount(formatAmount(totalInvested), balancesHidden)} €",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier
                    .width(0.5.dp)
                    .height(44.dp)
                    .drawBehind {
                        drawRect(Color.White.copy(alpha = 0.12f))
                    }
            )
            PortfolioMetric(
                label = stringResource(Res.string.portfolio_summary_total_pnl),
                primary = if (totalPnL == 0.0) "—"
                else "${if (totalPnL >= 0) "+" else "−"} ${
                    maskAmount(formatAmount(abs(totalPnL)), balancesHidden)
                } €",
                secondary = if (totalPnL == 0.0) null
                else "${if (totalPnLPercent >= 0) "+" else "−"}${formatPercent(abs(totalPnLPercent))}%",
                color = when {
                    totalPnL > 0 -> PnLPositive
                    totalPnL < 0 -> PnLNegative
                    else -> Color.White
                },
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }
        if (totalRealizedPnL != 0.0 && totalUnrealizedPnL != 0.0) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                PnLChip(stringResource(Res.string.portfolio_summary_realized), totalRealizedPnL, balancesHidden)
                PnLChip(stringResource(Res.string.portfolio_summary_unrealized), totalUnrealizedPnL, balancesHidden)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (positionsCount == 1) stringResource(Res.string.portfolio_summary_positions_one, positionsCount)
            else stringResource(Res.string.portfolio_summary_positions_many, positionsCount),
            fontSize = 10.sp,
            color = CyanSubtle.copy(alpha = 0.6f)
        )
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
        amount > 0 -> PnLPositive
        amount < 0 -> PnLNegative
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
