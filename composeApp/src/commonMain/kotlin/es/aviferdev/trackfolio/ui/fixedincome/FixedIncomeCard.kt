package es.aviferdev.trackfolio.ui.fixedincome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import es.aviferdev.trackfolio.ui.theme.*
import kotlin.math.abs
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun FixedIncomeSection(
    summary: FixedIncomeSummary,
    onPositionClick: (String) -> Unit,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)

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
                            text = "${maskAmount(formatAmount(summary.totalPrincipal), balancesHidden)} $symbol",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Valor actual", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${maskAmount(formatAmount(summary.totalCurrentValue), balancesHidden)} $symbol",
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
                        Text("Devengado", fontSize = 11.sp, color = TextSecondary)
                        val accruedColor = if (summary.totalAccruedInterest >= 0) PositiveGreen else TextSecondary
                        Text(
                            text = "+${maskAmount(formatAmount(summary.totalAccruedInterest), balancesHidden)} $symbol",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = accruedColor
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
                            text = "$sign${maskAmount(formatAmount(summary.totalNetProfit), balancesHidden)} $symbol (${formatPercent1(summary.totalNetProfitPercent)})",
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
                currencyCode = currencyCode,
                balancesHidden = balancesHidden,
                onClick = { onPositionClick(row.position.id) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun FixedIncomePositionCard(
    row: FixedIncomeRow,
    currencyCode: String,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val position = row.position
    val symbol = currencySymbol(currencyCode)

    val progressColor by animateColorAsState(
        targetValue = when {
            position.remainingDays <= 30 -> Color(0xFFE53935)
            position.progressPercent > 0.75f -> Color(0xFFFF9800)
            position.progressPercent > 0.5f -> Color(0xFFFFC107)
            else -> PositiveGreen
        },
        label = "progressColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(position.type.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = position.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        if (position.ticker.isNotBlank()) {
                            Text(
                                text = position.ticker,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = maskAmount(formatAmount(row.currentValue), balancesHidden),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$symbol",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            FixedIncomeProgressBar(
                progress = position.progressPercent,
                color = progressColor
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Capital", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${maskAmount(formatAmount(position.principal), balancesHidden)} $symbol",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TAE", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${formatPercent1(position.interestRate)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Devengado", fontSize = 11.sp, color = TextSecondary)
                    val accruedColor = if (position.accruedInterestToDate >= 0) PositiveGreen else NegativeRed
                    Text(
                        text = "+${maskAmount(formatAmount(position.accruedInterestToDate), balancesHidden)} $symbol",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = accruedColor
                    )
                }

                if (position.isNearMaturity || position.isMatured) {
                    NearMaturityBadge(remainingDays = position.remainingDays, isMatured = position.isMatured)
                } else {
                    Text(
                        text = "Vence: ${formatDate(position.maturityDate)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (position.remainingDays in 1..30) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Quedan ${position.remainingDays} días",
                    fontSize = 11.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }

            if (position.isMatured && position.isOpen) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "⚠️ Vencido - Liquidar",
                        fontSize = 11.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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

fun currencySymbol(code: String): String = when (code.uppercase()) {
    "EUR" -> "€"
    "USD" -> "$"
    "GBP" -> "£"
    else -> code
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