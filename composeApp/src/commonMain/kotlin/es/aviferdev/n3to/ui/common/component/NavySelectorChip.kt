package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
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
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Chip de selección del sistema de diseño Navy/Fintech.
 *
 * Patrón compartido por los selectores de cartera, filtros de gráfico y
 * cualquier elección tipo pill dentro de un contexto navy profundo.
 */
@Composable
fun NavySelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (selected) CyanAccent else NavyBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) CyanAccent else TextSecondary
        )
    }
}

@Preview
@Composable
private fun NavySelectorChipPreview() {
    N3toTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavySelectorChip(label = "Todas", selected = true, onClick = {})
            NavySelectorChip(label = "Acciones", selected = false, onClick = {})
            NavySelectorChip(label = "Cripto", selected = false, onClick = {})
        }
    }
}
