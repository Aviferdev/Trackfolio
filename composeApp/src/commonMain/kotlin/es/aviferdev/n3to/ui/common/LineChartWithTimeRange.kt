package es.aviferdev.n3to.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.common.component.TimeRangeChipRow

@Composable
fun LineChartWithTimeRange(
    title: String,
    subtitle: String,
    points: List<Pair<Long, Double>>,
    lineColor: Color,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier,
    rotateXLabels: Boolean = true,
    initialTimeRange: TimeRange = TimeRange.ALL_TIME
) {
    var selectedTimeRange by remember { mutableStateOf(initialTimeRange) }
    val nowMillis = remember { nowMillis() }

    val filteredPoints = remember(points, selectedTimeRange) {
        if (selectedTimeRange == TimeRange.ALL_TIME) {
            points
        } else {
            val cutoff = nowMillis - selectedTimeRange.windowDays * 86_400_000L
            points.filter { it.first >= cutoff }
        }
    }

    LineChartCard(
        title = title,
        subtitle = subtitle,
        points = filteredPoints,
        lineColor = lineColor,
        balancesHidden = balancesHidden,
        rotateXLabels = rotateXLabels,
        timeRangeSelector = {
            TimeRangeChipRow(
                selected = selectedTimeRange,
                onSelect = { selectedTimeRange = it }
            )
        },
        modifier = modifier
    )
}
