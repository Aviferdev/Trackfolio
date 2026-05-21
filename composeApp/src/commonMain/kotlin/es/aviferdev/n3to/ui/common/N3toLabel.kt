package es.aviferdev.n3to.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Section label — 10sp, uppercase, semi-bold, tertiary color.
 * Matches the `Lbl` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * N3toLabel("Últimos movimientos")
 * N3toLabel("Activos", color = MaterialTheme.appColors.primary, modifier = Modifier.padding(start = 4.dp))
 * ```
 */
@Composable
fun N3toLabel(
    text: String,
    color: Color = MaterialTheme.appColors.textTertiary,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        letterSpacing = 0.7.sp,
        modifier = modifier
    )
}

@Preview
@Composable
private fun N3toLabelPreview() {
    N3toTheme {
        N3toLabel("Últimos movimientos")
    }
}

@Preview
@Composable
private fun N3toLabelCustomColorPreview() {
    N3toTheme {
        N3toLabel("Activos", color = MaterialTheme.appColors.primary)
    }
}
