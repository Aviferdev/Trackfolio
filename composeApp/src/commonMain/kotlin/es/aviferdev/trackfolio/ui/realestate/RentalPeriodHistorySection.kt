package es.aviferdev.trackfolio.ui.realestate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.RentalPeriod
import es.aviferdev.trackfolio.ui.common.SectionHeader
import es.aviferdev.trackfolio.ui.common.StatusTag
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RentalPeriodHistorySection(
    periods: List<RentalPeriod>,
    modifier: Modifier = Modifier
) {
    if (periods.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(label = "Historial de alquiler")
            Spacer(Modifier.height(8.dp))

            periods.forEachIndexed { index, period ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row {
                            Text("${period.monthlyRent.toInt()} €/mes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Spacer(Modifier.width(6.dp))
                            if (period.isActive) {
                                StatusTag(label = "Activo", color = IncomeGreen)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        val dateRange = "${formatDateShort(period.startDate)} — ${
                            period.endDate?.let { formatDateShort(it) } ?: "actualidad"
                        }"
                        Text(dateRange, fontSize = 11.sp, color = TextTertiary)
                        period.notes?.let {
                            Text(it, fontSize = 11.sp, color = TextTertiary)
                        }
                    }
                }
                if (index < periods.lastIndex) {
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun RentalPeriodHistorySectionPreview() {
    TrackfolioTheme {
        RentalPeriodHistorySection(
            periods = listOf(
                RentalPeriod(id = "1", propertyId = "p1", startDate = 1672531200000, endDate = 1704067200000, monthlyRent = 1200.0, notes = null),
                RentalPeriod(id = "2", propertyId = "p1", startDate = 1704067200000, endDate = null, monthlyRent = 1300.0, notes = "Actualizado 2026")
            )
        )
    }
}
