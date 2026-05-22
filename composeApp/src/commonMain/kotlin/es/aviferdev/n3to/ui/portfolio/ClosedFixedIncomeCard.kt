package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.FixedIncomeType
import es.aviferdev.n3to.domain.model.InterestFrequency

import es.aviferdev.n3to.ui.theme.ExpenseRed

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import kotlin.math.abs

import n3to.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ClosedFixedIncomeCard(
    row: FixedIncomeRow,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val position = row.position
    val pnlColor = when {
        row.totalProfit > 0 -> MaterialTheme.appColors.pnlPositive
        row.totalProfit < 0 -> MaterialTheme.appColors.pnlNegative
        else -> MaterialTheme.appColors.textTertiary
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.appColors.navySurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.cyanSubtle,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    position.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary,
                    maxLines = 1
                )
                Text(
                    stringResource(Res.string.portfolio_closed_badge),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textDisabled
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(Res.string.portfolio_summary_realized),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textDisabled
                )
                Text(
                    if (row.totalProfit == 0.0) "—"
                    else "${if (row.totalProfit >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(
                                abs(
                                    row.totalProfit
                                )
                            ), balancesHidden
                        )
                    } €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun ClosedFixedIncomeCardPreview() {
    val now = nowMillis()
    val day = 24L * 60L * 60L * 1000L
    N3toTheme {
        ClosedFixedIncomeCard(
            row = FixedIncomeRow(
                position = FixedIncomePosition(
                    id = "fi-1", accountId = "acc1", name = "Depósito 3M", ticker = "DEP-3M",
                    type = FixedIncomeType.DEPOSIT, notes = null, principal = 10000.0,
                    quantity = 1.0, nominalPerUnit = 10000.0, interestRate = 3.5,
                    interestFrequency = InterestFrequency.AT_MATURITY,
                    startDate = now - 180 * day, maturityDate = now - 30 * day,
                    platformId = "", issuerId = null, autoRenew = false,
                    archived = false, closedAt = now, closeType = null,
                    feeNote = null, createdAt = now - 180 * day
                ),
                collectedInterest = 0.0, currentValue = 10000.0,
                totalProfit = 87.5, totalProfitPercent = 0.875
            ),
            balancesHidden = false,
            onClick = {}
        )
    }
}
