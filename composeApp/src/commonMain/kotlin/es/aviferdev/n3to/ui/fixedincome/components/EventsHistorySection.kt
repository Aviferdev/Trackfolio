package es.aviferdev.n3to.ui.fixedincome.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_commission_label
import n3to.composeapp.generated.resources.fixedincome_event_history
import n3to.composeapp.generated.resources.fixedincome_gross_label
import n3to.composeapp.generated.resources.fixedincome_no_events
import n3to.composeapp.generated.resources.fixedincome_retention_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EventsHistorySection(
    events: List<FixedIncomeEvent>,
    balancesHidden: Boolean,
    onDeleteEvent: (FixedIncomeEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_event_history),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            if (events.isEmpty()) {
                Text(
                    text = stringResource(Res.string.fixedincome_no_events),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            } else {
                events.forEach { event ->
                    EventItem(
                        event = event,
                        balancesHidden = balancesHidden,
                        onDelete = { onDeleteEvent(event) }
                    )
                    if (events.last() != event) {
                        HorizontalDivider(
                            color = MaterialTheme.appColors.navyBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: FixedIncomeEvent,
    balancesHidden: Boolean,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = event.type.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = formatDate(event.date),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${maskAmount(formatAmount(event.netAmount), balancesHidden)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.netAmount >= 0) MaterialTheme.appColors.pnlPositive else MaterialTheme.appColors.pnlNegative
                )
                val eventGrossText = stringResource(Res.string.fixedincome_gross_label)
                if (event.irpfPercent > 0 || event.commissionAmount > 0) {
                    Text(
                        text = "$eventGrossText: ${maskAmount(formatAmount(event.grossAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }

        if (event.irpfPercent > 0 || event.commissionAmount > 0) {
            Spacer(Modifier.height(4.dp))
            Row {
                if (event.irpfPercent > 0) {
                    Text(
                        text = "${stringResource(Res.string.fixedincome_retention_label)} ${formatPercent(event.irpfPercent)}%",
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.pnlNegative
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (event.commissionAmount > 0) {
                    Text(
                        text = "${stringResource(Res.string.fixedincome_commission_label)}: ${maskAmount(formatAmount(event.commissionAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}
