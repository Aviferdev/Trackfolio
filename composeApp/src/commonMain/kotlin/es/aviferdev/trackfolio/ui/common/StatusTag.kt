package es.aviferdev.trackfolio.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Status badge with semi-transparent background.
 * Matches the `Tag` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * StatusTag("ACTIVO", color = IncomeGreen)
 * StatusTag("BUY", color = PrimaryDark)
 * ```
 */
@Composable
fun StatusTag(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text          = label,
        fontSize      = 9.sp,
        fontWeight    = FontWeight.ExtraBold,
        color         = color,
        letterSpacing = 0.4.sp,
        modifier      = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

@Preview
@Composable
private fun StatusTagPreview() {
    TrackfolioTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            StatusTag("ACTIVO", color = IncomeGreen)
            StatusTag("BUY", color = PrimaryDark)
            StatusTag("VENDIDO", color = ExpenseRed)
        }
    }
}
