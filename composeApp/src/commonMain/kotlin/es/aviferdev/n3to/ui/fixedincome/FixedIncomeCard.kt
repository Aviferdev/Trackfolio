package es.aviferdev.n3to.ui.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.animation.animateColorAsState
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
import es.aviferdev.n3to.ui.theme.DividerLight
import es.aviferdev.n3to.ui.theme.ErrorBgLight
import es.aviferdev.n3to.ui.theme.ErrorDark
import es.aviferdev.n3to.ui.theme.WarnBgLight
import es.aviferdev.n3to.ui.theme.WarnOrange
import org.jetbrains.compose.ui.tooling.preview.Preview
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
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Renta Fija",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    if (summary.nearMaturityCount > 0) {
                        Badge(
                            count = summary.nearMaturityCount,
                            color = WarnOrange
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Capital", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${maskAmount(formatAmount(summary.totalPrincipal), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Valor actual", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${maskAmount(formatAmount(summary.totalCurrentValue), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cobrado", fontSize = 11.sp, color = TextSecondary)
                        val collectedColor = if (summary.totalCollectedInterest >= 0) PositiveGreen else TextSecondary
                        Text(
                            text = "+${maskAmount(formatAmount(summary.totalCollectedInterest), balancesHidden)} €",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = collectedColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rendimiento", fontSize = 11.sp, color = TextSecondary)
                        val pnlColor = when {
                            summary.totalNetProfit > 0 -> PositiveGreen
                            summary.totalNetProfit < 0 -> NegativeRed
                            else -> TextPrimary
                        }
                        val sign = if (summary.totalNetProfit >= 0) "+" else ""
                        Text(
                            text = "$sign${maskAmount(formatAmount(summary.totalNetProfit), balancesHidden)} € (${formatPercent(summary.totalNetProfitPercent)}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = pnlColor
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

    // JSX design: badge icon + name + "Vence date · frequency · amount" + "ACTIVO" tag + collected interest + register button
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon (bond icon in warn color)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WarnAmber.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = WarnAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name + "Vence date · frequency · amount"
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = position.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
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
                    color = TextTertiary
                )
            }

            // "ACTIVO" tag + collected interest + register button
            Column(horizontalAlignment = Alignment.End) {
                StatusTag(
                    label = if (position.isOpen) "ACTIVO" else "CERRADO",
                    color = if (position.isOpen) PositiveGreen else TextTertiary
                )
                if (position.isOpen) {
                    // Mostrar intereses cobrados si hay, o devengados si no hay cobrados aún
                    val interestToShow = if (row.collectedInterest > 0) row.collectedInterest else position.accruedInterestToDate
                    val interestLabel = if (row.collectedInterest > 0) "Cobrado" else "Devengado"
                    if (interestToShow > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$interestLabel: +${maskAmount(formatAmount(interestToShow), balancesHidden)} €",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PositiveGreen
                        )
                    }
                    // Botón rápido para registrar cupón (solo si tiene cupones periódicos)
                    if (position.hasPeriodicCoupons && onRegisterCoupon != null) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onRegisterCoupon,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Registrar",
                                fontSize = 10.sp,
                                color = PrimaryDark
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
            .background(DividerLight)
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
    val (bgColor, textColor, label) = if (isMatured) {
        Triple(ErrorBgLight, ErrorDark, "Vencido")
    } else {
        Triple(WarnBgLight, WarnOrange, "$remainingDays días")
    }

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
        color = color,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// Nota: formatAmount, maskAmount, formatPercent, formatDate se importan desde ui.theme

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