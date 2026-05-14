package es.aviferdev.trackfolio.ui.common.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.aviferdev.trackfolio.ui.common.chart.TimeRange
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Fila horizontal de chips seleccionables para elegir el rango temporal
 * del gráfico de evolución.
 *
 * @param selected Rango actualmente seleccionado.
 * @param onSelect Callback al seleccionar un rango.
 * @param modifier Modifier para personalizar el contenedor.
 */
@Composable
fun TimeRangeChipRow(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimeRange.entries.forEach { range ->
            SelectableChip(
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
    TrackfolioTheme {
        TimeRangeChipRow(
            selected = TimeRange.ALL_TIME,
            onSelect = {}
        )
    }
}
