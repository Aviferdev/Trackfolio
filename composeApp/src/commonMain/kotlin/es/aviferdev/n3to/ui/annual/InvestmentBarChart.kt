package es.aviferdev.n3to.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.annual_investment_monthly_format
import n3to.composeapp.generated.resources.annual_no_investments
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val MONTH_LABELS = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

/**
 * Gráfico de barras para mostrar inversión mensual (compras de activos).
 */
@Composable
fun InvestmentBarChart(
    investments: List<MonthlyInvestment>,
    year: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    if (investments.isEmpty()) {
        EmptyInvestmentState(year = year)
        return
    }

    // Construir mapa de meses
    val dataMap = investments.associateBy { it.month.trimStart('0').ifEmpty { "0" }.toInt() }
    val maxValue = (1..12).maxOfOrNull { m -> dataMap[m]?.amount ?: 0.0 } ?: 1.0
    val totalInvested = investments.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.annual_investment_monthly_format, year),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))

            // Total invertido
            Text(
                text = "Total invertido: ${
                    maskAmount(
                        formatAmountEuro(totalInvested),
                        balancesHidden
                    )
                }",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // Barras
            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                (1..12).forEach { monthNum ->
                    val investment = dataMap[monthNum]
                    val amount = investment?.amount ?: 0.0
                    InvestmentBar(
                        monthLabel = MONTH_LABELS[monthNum - 1],
                        amount = amount,
                        maxValue = maxValue,
                        balancesHidden = balancesHidden,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InvestmentBar(
    monthLabel: String,
    amount: Double,
    maxValue: Double,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val ratio = (amount / maxValue).toFloat().coerceIn(0f, 1f)
    val maxBarHeight = 110.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Barra de inversión
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(maxBarHeight)
                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(ratio)
                    .background(MaterialTheme.appColors.primary)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = monthLabel,
            fontSize = 9.sp,
            color = MaterialTheme.appColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyInvestmentState(year: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.annual_investment_monthly_format, year),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(16.dp))
            Icon(
                Icons.AutoMirrored.Outlined.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.appColors.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.annual_no_investments),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun createMockInvestments(): List<MonthlyInvestment> {
    return listOf(
        MonthlyInvestment(year = "2024", month = "01", amount = 1500.0),
        MonthlyInvestment(year = "2024", month = "02", amount = 2200.0),
        MonthlyInvestment(year = "2024", month = "03", amount = 800.0),
        MonthlyInvestment(year = "2024", month = "04", amount = 3000.0),
        MonthlyInvestment(year = "2024", month = "05", amount = 1200.0),
        MonthlyInvestment(year = "2024", month = "06", amount = 1800.0),
        MonthlyInvestment(year = "2024", month = "07", amount = 2500.0),
        MonthlyInvestment(year = "2024", month = "08", amount = 900.0),
        MonthlyInvestment(year = "2024", month = "09", amount = 1500.0),
        MonthlyInvestment(year = "2024", month = "10", amount = 2000.0),
        MonthlyInvestment(year = "2024", month = "11", amount = 1100.0),
        MonthlyInvestment(year = "2024", month = "12", amount = 3500.0)
    )
}

@Preview
@Composable
private fun InvestmentBarChartPreview() {
    N3toTheme {
        InvestmentBarChart(
            investments = createMockInvestments(),
            year = "2024",
            balancesHidden = false
        )
    }
}

@Preview
@Composable
private fun InvestmentBarChartEmptyPreview() {
    N3toTheme {
        InvestmentBarChart(
            investments = emptyList(),
            year = "2025",
            balancesHidden = false
        )
    }
}