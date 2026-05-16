package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import es.aviferdev.n3to.ui.theme.BorderGray2
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Chip seleccionable con borde. Similar a [SelectablePill] pero con borde
 * en lugar de fondo coloreado.
 *
 * @param label Texto del chip.
 * @param selected Si está seleccionado.
 * @param onClick Callback al hacer click.
 * @param modifier Modifier para personalizar.
 */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) PrimaryAlpha else Color.Transparent)
            .border(1.dp, if (selected) PrimaryDark else BorderGray2, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) PrimaryDark else TextTertiary
        )
    }
}

@Preview
@Composable
private fun SelectableChipRowPreview() {
    N3toTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            SelectableChip(label = "Categoría", selected = true, onClick = {})
            SelectableChip(label = "Composición", selected = false, onClick = {})
            SelectableChip(label = "Región", selected = false, onClick = {})
        }
    }
}
