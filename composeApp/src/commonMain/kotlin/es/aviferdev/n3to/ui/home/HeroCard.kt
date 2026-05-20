package es.aviferdev.n3to.ui.home

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.ui.theme.BrandGreen

import es.aviferdev.n3to.ui.theme.N3toTheme

import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_hero_balance_label
import n3to.composeapp.generated.resources.home_hero_net_with_debts
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HeroCard(
    balance: HomeBalance,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val netWithDebts = balance.selectedAccountBalance + balance.totalOwed - balance.totalOwing
    // Capture before DrawScope — appColors is not accessible inside drawBehind
    val heroStart = MaterialTheme.appColors.heroCardStart
    val heroEnd = MaterialTheme.appColors.heroCardEnd
    val cyanGlowColor = MaterialTheme.appColors.cyanGlow
    val cyanSubtleColor = MaterialTheme.appColors.cyanSubtle
    val incomeGlowColor = MaterialTheme.appColors.income

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(heroStart, heroEnd),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 40.dp.toPx()
                val cy = 40.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyanGlowColor.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
                val orbGreen = 65.dp.toPx()
                val gx = 28.dp.toPx()
                val gy = size.height - 18.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(incomeGlowColor.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(gx, gy),
                        radius = orbGreen
                    ),
                    radius = orbGreen,
                    center = Offset(gx, gy)
                )
            }
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text(
            text = stringResource(Res.string.home_hero_balance_label),
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "${maskAmount(formatAmount(balance.selectedAccountBalance), balancesHidden)} €",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1.5).sp,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.home_hero_net_with_debts),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
            Text(
                text = "${maskAmount(formatAmount(netWithDebts), balancesHidden)} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = cyanSubtleColor
            )
        }
    }
}

@Preview
@Composable
private fun HeroCardPreview() {
    N3toTheme {
        HeroCard(
            balance = HomeBalance(
                selectedAccount = null,
                selectedAccountBalance = 3500.0,
                totalOwed = 500.0,
                totalOwing = 200.0,
                recentTransactions = emptyList()
            ),
            balancesHidden = false
        )
    }
}
