package es.aviferdev.n3to.ui.networth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import es.aviferdev.n3to.domain.model.AppCurrency
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.NetWorthScreenData
import es.aviferdev.n3to.ui.common.N3toLabel
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.networth_accounts_label
import n3to.composeapp.generated.resources.networth_assets_label_alt
import n3to.composeapp.generated.resources.networth_debts_label
import n3to.composeapp.generated.resources.networth_fixedincome_label
import n3to.composeapp.generated.resources.networth_installments_format
import n3to.composeapp.generated.resources.networth_liabilities_label_alt
import n3to.composeapp.generated.resources.networth_monthly_format
import n3to.composeapp.generated.resources.networth_of_format
import n3to.composeapp.generated.resources.networth_portfolio_label
import n3to.composeapp.generated.resources.networth_total_assets_label
import n3to.composeapp.generated.resources.networth_total_label
import n3to.composeapp.generated.resources.networth_valuables_label
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
internal fun NetWorthHeroCard(data: NetWorthScreenData, balancesHidden: Boolean) {
    val currency = LocalCurrencySymbol.current
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCPnlNegativeSoft = MaterialTheme.appColors.pnlNegativeSoft
    val appCPnlPositiveSoft = MaterialTheme.appColors.pnlPositiveSoft
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(heroCardBg1, heroCardBg2),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 40.dp.toPx()
                val cy = 40.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        N3toLabel(
            text = stringResource(Res.string.networth_total_label),
            color = Color.White.copy(alpha = 0.60f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            maskAmount(formatCurrency(data.netWorth, currency), balancesHidden),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NetWorthMetric(
                label = stringResource(Res.string.networth_assets_label_alt),
                value = "+${maskAmount(formatCurrency(data.totalAssets, currency), balancesHidden)}",
                color = appCPnlPositiveSoft
            )
            Box(
                Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.White.copy(alpha = 0.18f))
                    .align(Alignment.CenterVertically)
            )
            NetWorthMetric(
                label = stringResource(Res.string.networth_liabilities_label_alt),
                value = "−${maskAmount(formatCurrency(data.totalLiabilities, currency), balancesHidden)}",
                color = appCPnlNegativeSoft
            )
        }
    }
}

@Composable
private fun NetWorthMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
internal fun AssetsSummaryCard(data: NetWorthScreenData, balancesHidden: Boolean) {
    val currency = LocalCurrencySymbol.current
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = heroCardBg1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        val appCCyanAccent = MaterialTheme.appColors.cyanAccent
        val appCNavyBorder = MaterialTheme.appColors.navyBorder
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AssetRow(
                stringResource(Res.string.networth_accounts_label),
                data.totalAccountBalance,
                balancesHidden
            )
            if (data.totalPortfolioValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = appCNavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow(
                    stringResource(Res.string.networth_portfolio_label),
                    data.totalPortfolioValue,
                    balancesHidden
                )
            }
            if (data.totalFixedIncomeValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = appCNavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow(
                    stringResource(Res.string.networth_fixedincome_label),
                    data.totalFixedIncomeValue,
                    balancesHidden
                )
            }
            if (data.totalValuablesValue > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = appCNavyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                AssetRow(
                    stringResource(Res.string.networth_valuables_label),
                    data.totalValuablesValue,
                    balancesHidden
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = appCNavyBorder,
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.networth_total_assets_label),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = appCCyanAccent
                )
                Text(
                    maskAmount(formatCurrency(data.totalAssets, currency), balancesHidden),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = appCCyanAccent
                )
            }
        }
    }
}

@Composable
private fun AssetRow(label: String, amount: Double, balancesHidden: Boolean) {
    val currency = LocalCurrencySymbol.current
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val appCTextPrimary = MaterialTheme.appColors.textPrimary
        val appCTextSecondary = MaterialTheme.appColors.textSecondary
        Text(label, fontSize = 13.sp, color = appCTextSecondary)
        Text(
            maskAmount(formatCurrency(amount, currency), balancesHidden),
            fontSize = 13.sp,
            color = appCTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun EverydayDebtsRow(amount: Double, balancesHidden: Boolean) {
    val currency = LocalCurrencySymbol.current
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = heroCardBg1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        val appCTextSecondary = MaterialTheme.appColors.textSecondary
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.networth_debts_label),
                fontSize = 13.sp,
                color = appCTextSecondary
            )
            Text(
                "−${maskAmount(formatCurrency(amount, currency), balancesHidden)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.expense
            )
        }
    }
}

@Composable
internal fun LoanCard(loan: Loan, onClick: () -> Unit) {
    val currency = LocalCurrencySymbol.current
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = heroCardBg1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        loan.type.toMaterialIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = appCCyanAccent
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            loan.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = appCTextPrimary
                        )
                        loan.lenderName?.let {
                            Text(
                                it,
                                fontSize = 11.sp,
                                color = appCTextTertiary
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "−${formatCurrency(loan.outstandingPrincipal, currency)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.expense
                    )
                    Text(
                        stringResource(
                            Res.string.networth_of_format,
                            formatCurrency(loan.totalAmount, currency)
                        ), fontSize = 10.sp, color = appCTextTertiary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            ProgressBar(progress = loan.progressPercent, color = appCCyanAccent, height = 4.dp)

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(
                        Res.string.networth_installments_format,
                        loan.paidInstallments,
                        loan.totalInstallments
                    ), fontSize = 10.sp, color = appCTextTertiary
                )
                Text(
                    stringResource(
                        Res.string.networth_monthly_format,
                        formatCurrency(loan.monthlyPayment, currency)
                    ), fontSize = 10.sp, color = appCTextTertiary
                )
                Text("${loan.currentInterestRate}%", fontSize = 10.sp, color = appCTextTertiary)
            }
        }
    }
}

internal fun formatCurrency(amount: Double, currencySymbol: String = AppCurrency.EUR.symbol): String {
    val absVal = abs(amount)
    val prefix = if (amount < 0) "-" else ""
    return "$prefix${formatAmount(absVal)} $currencySymbol"
}
