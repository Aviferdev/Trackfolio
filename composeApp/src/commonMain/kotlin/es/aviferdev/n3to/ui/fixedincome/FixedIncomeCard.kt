package es.aviferdev.n3to.ui.fixedincome

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
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs

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
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(0.5.dp, NavyBorder)
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
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Renta Fija",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    if (summary.nearMaturityCount > 0) {
                        Badge(
                            count = summary.nearMaturityCount,
                            color = WarnAmber
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Capital", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                        Text(
                            text = "${maskAmount(formatAmount(summary.totalPrincipal), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Valor actual", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                        Text(
                            text = "${maskAmount(formatAmount(summary.totalCurrentValue), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
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
                        Text("Cobrado", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                        Text(
                            text = "+${maskAmount(formatAmount(summary.totalCollectedInterest), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.totalCollectedInterest >= 0) PnLPositive else Color.White.copy(alpha = 0.55f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rendimiento", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                        val sign = if (summary.totalNetProfit >= 0) "+" else ""
                        Text(
                            text = "$sign${maskAmount(formatAmount(summary.totalNetProfit), balancesHidden)} € (${formatPercent(summary.totalNetProfitPercent)}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                summary.totalNetProfit > 0 -> PnLPositive
                                summary.totalNetProfit < 0 -> PnLNegative
                                else -> Color.White
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
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
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
                    .background(NavySurfaceLight, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = position.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                val frequencyLabel = if (position.hasPeriodicCoupons) {
                    position.interestFrequency.label
                } else {
                    "Al vencimiento"
                }
                Text(
                    text = "Vence ${formatDateShort(position.maturityDate)} · $frequencyLabel · ${maskAmount(formatAmount(position.principal), balancesHidden)} €",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusTag(
                    label = if (position.isOpen) "ACTIVO" else "CERRADO",
                    color = if (position.isOpen) PnLPositive else Color.White.copy(alpha = 0.3f)
                )
                if (position.isOpen) {
                    val interestToShow = if (row.collectedInterest > 0) row.collectedInterest else position.accruedInterestToDate
                    val interestLabel = if (row.collectedInterest > 0) "Cobrado" else "Devengado"
                    if (interestToShow > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$interestLabel: +${maskAmount(formatAmount(interestToShow), balancesHidden)} €",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PnLPositive
                        )
                    }
                    if (position.hasPeriodicCoupons && onRegisterCoupon != null) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onRegisterCoupon,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Registrar",
                                fontSize = 10.sp,
                                color = CyanAccent
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
            .background(NavyBorder)
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
    val bgColor = if (isMatured) ExpenseRed.copy(alpha = 0.15f) else WarnAmber.copy(alpha = 0.15f)
    val textColor = if (isMatured) ExpenseRed else WarnAmber
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
