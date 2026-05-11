package es.aviferdev.trackfolio.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Section label — 10sp, uppercase, semi-bold, tertiary color.
 * Matches the `Lbl` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * TrackfolioLabel("Últimos movimientos")
 * TrackfolioLabel("Activos", color = PrimaryDark, modifier = Modifier.padding(start = 4.dp))
 * ```
 */
@Composable
fun TrackfolioLabel(
    text: String,
    color: Color = TextTertiary,
    modifier: Modifier = Modifier
) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = color,
        letterSpacing = 0.7.sp,
        modifier      = modifier
    )
}

@Preview
@Composable
private fun TrackfolioLabelPreview() {
    TrackfolioTheme {
        TrackfolioLabel("Últimos movimientos")
    }
}

@Preview
@Composable
private fun TrackfolioLabelCustomColorPreview() {
    TrackfolioTheme {
        TrackfolioLabel("Activos", color = PrimaryDark)
    }
}
