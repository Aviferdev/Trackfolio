package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Fila horizontal de tabs para elegir el rango temporal del gráfico de evolución.
 * Usa [NavyTab] para coincidir visualmente con AccountSelectorBar.
 */
@Composable
fun TimeRangeChipRow(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Start
    ) {
        TimeRange.entries.forEach { range ->
            NavyTab(
                label = range.label,
                selected = selected == range,
                onClick = { onSelect(range) }
            )
        }
    }
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
