package es.aviferdev.n3to.ui.common.component

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import androidx.compose.ui.tooling.preview.Preview

/**
 * Pill seleccionable (estilo toggle). Usado para seleccionar entre opciones
 * como tipo de transacción (gasto/ingreso/ajuste).
 *
 * @param label Texto del pill.
 * @param selected Si está seleccionado.
 * @param selectedColor Color de fondo cuando está seleccionado.
 * @param onClick Callback al hacer click.
 * @param modifier Modifier para personalizar.
 */
@Composable
fun SelectablePill(
    label: String,
    selected: Boolean,
    selectedColor: Color = PrimaryDark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedColor else MaterialTheme.appColors.textTertiary
        )
    }
}

@Preview
@Composable
private fun SelectablePillRowPreview() {
    N3toTheme {
        androidx.compose.foundation.layout.Row {
            SelectablePill(
                label = "Gasto",
                selected = true,
                selectedColor = ExpenseRed,
                onClick = {}
            )
            SelectablePill(
                label = "Ingreso",
                selected = false,
                selectedColor = IncomeGreen,
                onClick = {}
            )
        }
    }
}
