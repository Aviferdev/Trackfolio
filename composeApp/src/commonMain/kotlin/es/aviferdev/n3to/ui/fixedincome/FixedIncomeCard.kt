package es.aviferdev.n3to.ui.fixedincome

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.FixedIncomeSummary
import es.aviferdev.n3to.domain.model.FixedIncomeType
import es.aviferdev.n3to.domain.model.InterestFrequency
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDateShort
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_capital_short
import n3to.composeapp.generated.resources.fixedincome_coupon_paid
import n3to.composeapp.generated.resources.fixedincome_register_coupon_btn
import n3to.composeapp.generated.resources.fixedincome_title
import n3to.composeapp.generated.resources.fixedincome_yield_label
import n3to.composeapp.generated.resources.portfolio_update_price_current
import org.jetbrains.compose.resources.stringResource

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun FixedIncomeSection(
    summary: FixedIncomeSummary,
    onPositionClick: (String) -> Unit,
    onRegisterCoupon: ((String) -> Unit)? = null,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.fixedincome_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                    if (summary.nearMaturityCount > 0) {
                        Badge(
                            count = summary.nearMaturityCount,
                            color = MaterialTheme.appColors.warnAmber
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.fixedincome_capital_short),
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary
                        )
                        Text(
                            text = "${
                                maskAmount(
                                    formatAmount(summary.totalPrincipal),
                                    balancesHidden
                                )
                            } €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(Res.string.portfolio_update_price_current),
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary
                        )
                        Text(
                            text = "${
                                maskAmount(
                                    formatAmount(summary.totalCurrentValue),
                                    balancesHidden
                                )
                            } €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.fixedincome_coupon_paid),
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary
                        )
                        Text(
                            text = "+${
                                maskAmount(
                                    formatAmount(summary.totalCollectedInterest),
                                    balancesHidden
                                )
                            } €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.totalCollectedInterest >= 0) MaterialTheme.appColors.pnlPositive else MaterialTheme.appColors.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(Res.string.fixedincome_yield_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary
                        )
                        val sign = if (summary.totalNetProfit >= 0) "+" else ""
                        Text(
                            text = "$sign${
                                maskAmount(
                                    formatAmount(summary.totalNetProfit),
                                    balancesHidden
                                )
                            } € (${formatPercent(summary.totalNetProfitPercent)}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                summary.totalNetProfit > 0 -> MaterialTheme.appColors.pnlPositive
                                summary.totalNetProfit < 0 -> MaterialTheme.appColors.pnlNegative
                                else -> MaterialTheme.appColors.textPrimary
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        summary.positions.forEach { row ->
            FixedIncomePositionCard(
                row = row,
                balancesHidden = balancesHidden,
                onClick = { onPositionClick(row.position.id) },
                onRegisterCoupon = if (onRegisterCoupon != null && row.position.hasPeriodicCoupons) {
                    { onRegisterCoupon(row.position.id) }
                } else null,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun FixedIncomePositionCard(
    row: FixedIncomeRow,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    onRegisterCoupon: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val position = row.position

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.appColors.navySurfaceLight,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = position.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1
                )
                val frequencyLabel = if (position.hasPeriodicCoupons) {
                    position.interestFrequency.label
                } else {
                    "Al vencimiento"
                }
                Text(
                    text = "Vence ${formatDateShort(position.maturityDate)} · $frequencyLabel · ${
                        maskAmount(
                            formatAmount(position.principal),
                            balancesHidden
                        )
                    } €",
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusTag(
                    label = if (position.isOpen) "ACTIVO" else "CERRADO",
                    color = if (position.isOpen) MaterialTheme.appColors.pnlPositive else MaterialTheme.appColors.textDisabled
                )
                if (position.isOpen) {
                    val interestToShow =
                        if (row.collectedInterest > 0) row.collectedInterest else position.accruedInterestToDate
                    val interestLabel = if (row.collectedInterest > 0) "Cobrado" else "Devengado"
                    if (interestToShow > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$interestLabel: +${
                                maskAmount(
                                    formatAmount(interestToShow),
                                    balancesHidden
                                )
                            } €",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.pnlPositive
                        )
                    }
                    if (position.hasPeriodicCoupons && onRegisterCoupon != null) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onRegisterCoupon,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.fixedincome_register_coupon_btn),
                                fontSize = 10.sp,
                                color = MaterialTheme.appColors.cyanAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FixedIncomeProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.appColors.navyBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun NearMaturityBadge(
    remainingDays: Int,
    isMatured: Boolean
) {
    val bgColor =
        if (isMatured) MaterialTheme.appColors.expense.copy(alpha = 0.15f) else MaterialTheme.appColors.warnAmber.copy(
            alpha = 0.15f
        )
    val textColor =
        if (isMatured) MaterialTheme.appColors.expense else MaterialTheme.appColors.warnAmber
    val label = if (isMatured) "Vencido" else "$remainingDays días"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun Badge(count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun createMockSummary(): FixedIncomeSummary {
    val now = nowMillis()
    val dayInMillis = 24 * 60 * 60 * 1000L
    val position = FixedIncomePosition(
        id = "1",
        accountId = "acc1",
        name = "Bono Tesoro 2025",
        ticker = "ES0000000001",
        type = FixedIncomeType.BOND,
        notes = null,
        principal = 10000.0,
        quantity = 10.0,
        nominalPerUnit = 1000.0,
        interestRate = 3.5,
        interestFrequency = InterestFrequency.ANNUAL,
        startDate = now - (365 * dayInMillis),
        maturityDate = now + (180 * dayInMillis),
        platformId = "platform1",
        issuerId = null,
        autoRenew = false,
        archived = false,
        closedAt = null,
        closeType = null,
        feeNote = null,
        createdAt = now - (365 * dayInMillis)
    )
    val row = FixedIncomeRow(
        position = position,
        collectedInterest = 350.0,
        currentValue = 10350.0,
        totalProfit = 350.0,
        totalProfitPercent = 3.5
    )
    return FixedIncomeSummary(
        totalPrincipal = 10000.0,
        totalCurrentValue = 10350.0,
        totalAccruedInterest = 350.0,
        totalCollectedInterest = 350.0,
        totalNetProfit = 350.0,
        totalNetProfitPercent = 3.5,
        openPositionsCount = 1,
        nearMaturityCount = 0,
        positions = listOf(row),
        closedPositions = emptyList()
    )
}

@Preview
@Composable
private fun FixedIncomeSectionPreview() {
    N3toTheme {
        FixedIncomeSection(
            summary = createMockSummary(),
            onPositionClick = {},
            onRegisterCoupon = {},
            balancesHidden = false
        )
    }
}
