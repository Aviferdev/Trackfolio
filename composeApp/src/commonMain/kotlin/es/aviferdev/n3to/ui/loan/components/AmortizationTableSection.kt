package es.aviferdev.n3to.ui.loan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AmortizationEntry
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AmortizationTableSection(
    schedule: List<AmortizationEntry>,
    paidInstallments: Int
) {
    AmortizationHeader()
    schedule.forEach { entry ->
        AmortizationRow(entry = entry, paidInstallments = paidInstallments)
    }
}

@Composable
private fun AmortizationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf(
            "#" to .4f,
            "Fecha" to 1.4f,
            "Cuota" to 1.2f,
            "Interés" to 1.2f,
            "Capital" to 1.2f,
            "Pendiente" to 1.4f
        ).forEach { (h, w) ->
            Text(
                h,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textTertiary,
                letterSpacing = .4.sp,
                modifier = Modifier.weight(w),
                textAlign = if (h == "#" || h == "Fecha") TextAlign.Start else TextAlign.End
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.appColors.border, thickness = .5.dp)
}

@Composable
private fun AmortizationRow(entry: AmortizationEntry, paidInstallments: Int) {
    val isPaid = entry.installmentNumber <= paidInstallments
    val isNext = entry.installmentNumber == paidInstallments + 1
    val txtColor =
        if (isPaid) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isNext) PrimaryAlpha else Color.Transparent)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            entry.installmentNumber.toString(),
            fontSize = 10.sp,
            color = txtColor,
            modifier = Modifier.weight(.4f)
        )
        Text(
            formatDateShort(entry.date),
            fontSize = 10.sp,
            color = txtColor,
            modifier = Modifier.weight(1.4f)
        )
        Text(
            formatCurrencyShort(entry.monthlyPayment),
            fontSize = 10.sp,
            color = txtColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            formatCurrencyShort(entry.interestPortion),
            fontSize = 10.sp,
            color = MaterialTheme.appColors.expense.copy(if (isPaid) 1f else .5f),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            formatCurrencyShort(entry.principalPortion),
            fontSize = 10.sp,
            color = MaterialTheme.appColors.income.copy(if (isPaid) 1f else .5f),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            formatCurrencyShort(entry.outstandingBalance),
            fontSize = 10.sp,
            color = if (isPaid) MaterialTheme.appColors.primary else MaterialTheme.appColors.textTertiary,
            fontWeight = if (isPaid) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
    HorizontalDivider(color = MaterialTheme.appColors.border, thickness = .3.dp)
}

private fun formatCurrencyShort(amount: Double): String = formatAmount(amount)

private fun formatDateShort(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
}
