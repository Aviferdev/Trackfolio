package es.aviferdev.n3to.ui.loan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatPercent
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun LoanRateHistorySection(
    rateChanges: List<LoanRateChange>
) {
    rateChanges.forEach { change ->
        RateChangeRow(change)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.appColors.textTertiary,
        letterSpacing = .7.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun RateChangeRow(change: LoanRateChange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    formatDate(change.effectiveDate),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Text(
                    "Δ ${if (change.newRate > change.previousRate) "+" else ""}${
                        formatPercent(change.newRate - change.previousRate)
                    }%",
                    fontSize = 10.sp,
                    color = if (change.newRate > change.previousRate) MaterialTheme.appColors.expense else MaterialTheme.appColors.income
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "${formatPercent(change.previousRate)}%",
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Text("→", fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
                Text(
                    "${formatPercent(change.newRate)}%",
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.dayOfMonth.toString().padStart(2, '0')}/${
        dt.monthNumber.toString().padStart(2, '0')
    }/${dt.year}"
}
