package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_category_current_format
import n3to.composeapp.generated.resources.portfolio_category_invested_format
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun CategoryGroupHeader(
    group: CategoryGroup,
    balancesHidden: Boolean
) {
    val pnlColor = when {
        group.totalPnL > 0 -> IncomeGreen
        group.totalPnL < 0 -> ExpenseRed
        else -> MaterialTheme.appColors.textSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.displayIcon, fontSize = 17.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    group.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
                Spacer(Modifier.width(6.dp))
                Text("(${group.rowCount})", fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
            }
            if (group.totalPnL != 0.0) {
                DeltaIndicator(
                    value = "${if (group.totalPnLPercent >= 0) "+" else "−"}${formatPercent(abs(group.totalPnLPercent))}%",
                    isPositive = group.totalPnLPercent >= 0
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(
                stringResource(Res.string.portfolio_category_invested_format, maskAmount(formatAmount(group.totalInvested), balancesHidden)),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary
            )
            Text(
                stringResource(Res.string.portfolio_category_current_format, maskAmount(formatAmount(group.totalCurrentValue), balancesHidden)),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary
            )
        }
    }
}

@Preview
@Composable
private fun CategoryGroupHeaderPreview() {
    N3toTheme {
        CategoryGroupHeader(
            group = CategoryGroup(
                category = null,
                rows = emptyList(),
                totalInvested = 5000.0,
                totalCurrentValue = 5500.0,
                totalUnrealizedPnL = 500.0,
                totalRealizedPnL = 0.0,
                totalPnL = 500.0,
                totalPnLPercent = 10.0
            ),
            balancesHidden = false
        )
    }
}
