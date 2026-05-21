package es.aviferdev.n3to.ui.common.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun TimeRangeChipRow(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    NavyTabRow(
        items = TimeRange.entries,
        selected = selected,
        onSelect = onSelect,
        modifier = modifier,
        content = { timeRange ->
            Text(
                text = timeRange.label,
                fontSize = 12.sp,
                fontWeight = if (timeRange == selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (timeRange == selected) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
            )
        }
    )
}
