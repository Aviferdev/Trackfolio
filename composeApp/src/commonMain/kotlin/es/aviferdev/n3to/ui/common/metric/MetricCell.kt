package es.aviferdev.n3to.ui.common.metric

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Display de una métrica individual: label + valor.
 * Soporta formato vertical (label arriba, valor abajo) y horizontal (label a la izquierda, valor a la derecha).
 *
 * @param label Texto descriptivo de la métrica.
 * @param value Valor formateado a mostrar.
 * @param labelColor Color del label.
 * @param valueColor Color del valor.
 * @param horizontal Si true, dispone label y valor en una misma fila.
 * @param valueSize Tamaño de fuente del valor.
 * @param modifier Modifier para personalizar.
 */
@Composable
fun MetricCell(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.appColors.textTertiary,
    valueColor: Color = MaterialTheme.appColors.textPrimary,
    horizontal: Boolean = false,
    valueSize: Int = 14,
    modifier: Modifier = Modifier
) {
    if (horizontal) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = labelColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Text(
                value,
                fontSize = valueSize.sp,
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Column(modifier = modifier) {
            Text(
                label,
                fontSize = 11.sp,
                color = labelColor
            )
            Spacer(Modifier.height(3.dp))
            Text(
                value,
                fontSize = valueSize.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

/**
 * Grid de métricas de 2 columnas.
 */
@Composable
fun MetricGrid2(
    items: List<Pair<String, String>>,
    color: Color = MaterialTheme.appColors.textPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        items.forEachIndexed { index, (label, value) ->
            MetricCell(
                label = label,
                value = value,
                valueColor = color,
                modifier = Modifier.weight(1f).let { mod ->
                    if (index < items.lastIndex) mod.padding(end = 12.dp) else mod
                }
            )
        }
    }
}

@Preview
@Composable
private fun MetricGrid2Preview() {
    N3toTheme {
        MetricGrid2(
            items = listOf(
                "Ingresos" to "25.000,00 €",
                "Gastos" to "18.500,00 €"
            )
        )
    }
}

@Preview
@Composable
private fun MetricCellVerticalPreview() {
    N3toTheme {
        MetricCell(
            label = "Invertido",
            value = "10.000,00 €",
            valueColor = MaterialTheme.appColors.textPrimary
        )
    }
}

@Preview
@Composable
private fun MetricCellHorizontalPreview() {
    N3toTheme {
        MetricCell(
            label = "Total ingresos:",
            value = "25.000,00 €",
            horizontal = true
        )
    }
}
