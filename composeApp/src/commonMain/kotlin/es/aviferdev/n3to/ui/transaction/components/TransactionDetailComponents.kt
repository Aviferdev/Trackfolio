package es.aviferdev.n3to.ui.transaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DetailSectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.appColors.textTertiary,
        letterSpacing = 0.7.sp
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isLast: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.width(100.dp)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 14.dp),
                color = MaterialTheme.appColors.border,
                thickness = 0.5.dp
            )
        }
    }
}

/** Determina si la transacción tiene campos de ingreso que mostrar. */
fun shouldShowIncomeDetails(transaction: Transaction): Boolean {
    if (!transaction.isIncome) return false
    return transaction.grossAmount != null
            || transaction.taxLines.isNotEmpty()
            || transaction.commissionAmount != null
            || !transaction.issuerName.isNullOrBlank()
}

/** Formatea el valor absoluto sin signo: "1.500,00" */
fun formatAmountAbs(amount: Double): String {
    val rounded = (amount * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = euros.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}

/** Formatea fecha larga: "12 de mayo de 2026" */
fun formatDetailDate(epochMillis: Long): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val ld = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
