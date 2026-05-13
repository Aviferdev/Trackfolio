package es.aviferdev.trackfolio.ui.fixedincome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.FixedIncomeRow
import es.aviferdev.trackfolio.domain.model.FixedIncomeSummary
import es.aviferdev.trackfolio.domain.model.FixedIncomeType
import es.aviferdev.trackfolio.domain.model.InterestFrequency
import es.aviferdev.trackfolio.ui.common.StatusTag
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlinx.datetime.Clock
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
                        Text("🏦", fontSize = 20.sp)
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
                            color = Color(0xFFFF9800)
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
                            text = "$sign${maskAmount(formatAmount(summary.totalNetProfit), balancesHidden)} € (${formatPercent1(summary.totalNetProfitPercent)})",
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
                    text = "Vence ${formatDate(position.maturityDate)} · $frequencyLabel · ${maskAmount(formatAmount(position.principal), balancesHidden)} €",
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
                                text = "💰 Registrar",
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
            .background(Color(0xFFE0E0E0))
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
        Triple(Color(0xFFFFEBEE), Color(0xFFE53935), "Vencido")
    } else {
        Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), "$remainingDays días")
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

fun formatAmount(amount: Double): String {
    val negative = amount < 0
    val abs = if (negative) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    val formatted = "$eurosStr,${cents.toString().padStart(2, '0')}"
    return if (negative) "-$formatted" else formatted
}

fun maskAmount(amount: String, hidden: Boolean): String =
    if (hidden) "••••" else amount

fun formatPercent1(value: Double): String {
    val absVal = if (value < 0) -value else value
    val intPart = absVal.toLong()
    val decPart = ((absVal - intPart) * 10).toInt()
    val sign = if (value < 0) "-" else ""
    return "$sign$intPart.$decPart"
}

fun formatDate(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
    val dateTime = instant.toLocalDateTime(tz)
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
}

private fun createMockSummary(): FixedIncomeSummary {
    val now = Clock.System.now().toEpochMilliseconds()
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
    TrackfolioTheme {
        FixedIncomeSection(
            summary = createMockSummary(),
            onPositionClick = {},
            onRegisterCoupon = {},
            balancesHidden = false
        )
    }
}