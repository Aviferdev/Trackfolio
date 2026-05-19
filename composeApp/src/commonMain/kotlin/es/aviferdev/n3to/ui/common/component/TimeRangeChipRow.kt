package es.aviferdev.n3to.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

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
        label = { it.label },
        modifier = modifier
    )
}

@Preview
@Composable
private fun TimeRangeChipRowPreview() {
    N3toTheme {
        TimeRangeChipRow(
            selected = TimeRange.ALL_TIME,
            onSelect = {}
        )
    }
}
