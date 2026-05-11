package es.aviferdev.trackfolio.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Delta indicator — arrow + formatted value, green for positive, red for negative.
 * Matches the `Delta` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * DeltaIndicator(value = "+10,8%", isPositive = true)
 * DeltaIndicator(value = "−7,1%", isPositive = false)
 * ```
 */
@Composable
fun DeltaIndicator(
    value: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) IncomeGreen else ExpenseRed
    val arrow = if (isPositive) "↑" else "↓"

    Row(
        modifier        = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = arrow,
            fontSize   = 10.sp,
            color      = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text       = value,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

@Preview
@Composable
private fun DeltaIndicatorPreview() {
    TrackfolioTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeltaIndicator(value = "+10,8%", isPositive = true)
            DeltaIndicator(value = "-7,1%", isPositive = false)
        }
    }
}
