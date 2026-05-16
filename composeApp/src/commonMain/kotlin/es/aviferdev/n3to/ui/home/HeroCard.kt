package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 11.dp)
        ) {
            Text(
                text = accountLabel,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.60f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${maskAmount(formatAmount(balance.selectedAccountBalance), balancesHidden)} €",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-2).sp,
                lineHeight = 46.sp
            )
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Neto con deudas: ${maskAmount(formatAmount(netWithDebts), balancesHidden)} €",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DebtChip(
                    label  = "Me deben",
                    amount = "${maskAmount(formatAmount(balance.totalOwed), balancesHidden)} €",
                    color  = IncomeGreen
                )
                DebtChip(
                    label  = "Debo yo",
                    amount = "${maskAmount(formatAmount(balance.totalOwing), balancesHidden)} €",
                    color  = ExpenseRed,
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
fun DebtChip(
    label: String,
    amount: String,
    color: Color,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text     = amount,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color    = color
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = Color.White.copy(alpha = 0.50f),
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
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
