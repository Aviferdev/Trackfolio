package es.aviferdev.trackfolio.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.PrimaryDark

/**
 * Icon inside a colored rounded badge.
 * Matches the `Bdg` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * IconBadge(icon = "📄", color = IncomeGreen, size = 32.dp)
 * IconBadge(icon = "🏦", color = WarnAmber)
 * ```
 */
@Composable
fun IconBadge(
    icon: String,
    color: Color = PrimaryDark,
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = icon,
            fontSize   = (size.value * 0.46f).sp,
            color      = color,
            fontWeight = FontWeight.Bold
        )
    }
}
