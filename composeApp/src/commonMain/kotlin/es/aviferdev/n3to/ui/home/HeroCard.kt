package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.ui.theme.CyanGlow
import es.aviferdev.n3to.ui.theme.CyanSubtle
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.NavySurfaceLight
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HeroCard(
    balance: HomeBalance,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val netWithDebts = balance.selectedAccountBalance + balance.totalOwed - balance.totalOwing

    val gradient = Brush.linearGradient(
        colors = listOf(
            NavySurface,
            NavySurfaceLight
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanGlow.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Text(
                text = "${maskAmount(formatAmount(balance.selectedAccountBalance), balancesHidden)} €",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                lineHeight = 36.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Saldo en cuenta",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f),
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neto con deudas",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
                Text(
                    text = "${maskAmount(formatAmount(netWithDebts), balancesHidden)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanSubtle
                )
            }
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
