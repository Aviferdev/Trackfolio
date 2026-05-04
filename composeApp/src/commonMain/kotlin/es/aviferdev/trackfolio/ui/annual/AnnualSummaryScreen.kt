package es.aviferdev.trackfolio.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

@Composable
fun AnnualSummaryScreen(
    viewModel: AnnualViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AnnualHeader(
            year = uiState.year,
            onPrevious = { viewModel.previousYear() },
            onNext = { viewModel.nextYear() }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else {
            uiState.summary?.let { summary ->
                AnnualContent(summary = summary)
            }
        }
    }
}

@Composable
private fun AnnualHeader(
    year: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val nowYear = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).year
    val isCurrentYear = year.toIntOrNull() == nowYear

    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Resumen anual",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BackgroundGray)
                ) {
                    Text("‹", fontSize = 22.sp, color = TextPrimary, fontWeight = FontWeight.Light)
                }
                Text(
                    text = year,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onNext,
                    enabled = !isCurrentYear,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (!isCurrentYear) BackgroundGray else Color.Transparent)
                ) {
                    Text(
                        "›",
                        fontSize = 22.sp,
                        color = if (!isCurrentYear) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnualContent(summary: AnnualSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BalanceHeroCard(summary = summary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnnualMetricCard(
                label = "Ingresos totales",
                amount = summary.totalIncome,
                color = IncomeGreen,
                variationPercent = summary.incomeVariationPercent,
                modifier = Modifier.weight(1f)
            )
            AnnualMetricCard(
                label = "Gastos totales",
                amount = summary.totalExpense,
                color = ExpenseRed,
                variationPercent = -summary.expenseVariationPercent,
                modifier = Modifier.weight(1f)
            )
        }
        if (summary.previousYearIncome > 0 || summary.previousYearExpense > 0) {
            PreviousYearCard(summary = summary)
        }
    }
}

@Composable
private fun BalanceHeroCard(summary: AnnualSummary) {
    val balance = summary.balance
    val isPositive = balance >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Balance ${summary.year}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${if (isPositive) "+" else "−"} ${formatAmount(abs(balance))} €",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF66BB6A) else Color(0xFFEF9A9A),
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isPositive) "Año positivo" else "Año en negativo",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun AnnualMetricCard(
    label: String,
    amount: Double,
    color: Color,
    variationPercent: Double,
    modifier: Modifier = Modifier
) {
    val hasPreviousData = variationPercent != 0.0
    val isPositiveVariation = variationPercent >= 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${formatAmount(amount)} €",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            if (hasPreviousData) {
                Spacer(Modifier.height(6.dp))
                val sign = if (isPositiveVariation) "+" else ""
                val varColor = if (isPositiveVariation) IncomeGreen else ExpenseRed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPositiveVariation) "▲" else "▼",
                        fontSize = 10.sp,
                        color = varColor
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "$sign${formatPercent(variationPercent)}% vs año anterior",
                        fontSize = 11.sp,
                        color = varColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviousYearCard(summary: AnnualSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Comparativa con ${summary.year.toInt() - 1}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(14.dp))
            ComparisonRow(
                label = "Ingresos",
                current = summary.totalIncome,
                previous = summary.previousYearIncome,
                color = IncomeGreen
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            ComparisonRow(
                label = "Gastos",
                current = summary.totalExpense,
                previous = summary.previousYearExpense,
                color = ExpenseRed
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            ComparisonRow(
                label = "Balance",
                current = summary.balance,
                previous = summary.previousYearIncome - summary.previousYearExpense,
                color = if (summary.balance >= 0) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    current: Double,
    previous: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(
            text = "${formatAmount(previous)} €",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatAmount(current)} €",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatAmount(amount: Double): String {
    val abs = abs(amount)
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}

private fun formatPercent(value: Double): String {
    val abs = abs(value)
    return if (abs == abs.toLong().toDouble()) {
        abs.toLong().toString()
    } else {
        val rounded = (abs * 10).toLong()
        "${rounded / 10},${rounded % 10}"
    }
}
