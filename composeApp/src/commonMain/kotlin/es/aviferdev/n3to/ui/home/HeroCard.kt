package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HeroCard(
    balance: HomeBalance,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val accountLabel = balance.selectedAccount?.name ?: "Sin cuenta"
    val netWithDebts = balance.selectedAccountBalance + balance.totalOwed - balance.totalOwing

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7470FF),
            Color(0xFF4440D4)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(horizontal = 22.dp, vertical = 22.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = accountLabel.uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "${maskAmount(formatAmount(balance.selectedAccountBalance), balancesHidden)} €",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-2).sp,
                lineHeight = 42.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Saldo en cuenta",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.50f),
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DebtStatCard(
                    label = "Me deben",
                    amount = "${maskAmount(formatAmount(balance.totalOwed), balancesHidden)} €",
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                DebtStatCard(
                    label = "Debo yo",
                    amount = "${maskAmount(formatAmount(balance.totalOwing), balancesHidden)} €",
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neto con deudas",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.48f)
                )
                Text(
                    text = "${maskAmount(formatAmount(netWithDebts), balancesHidden)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Composable
private fun DebtStatCard(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, CircleShape)
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = amount,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
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
