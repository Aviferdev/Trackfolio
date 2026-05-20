package es.aviferdev.n3to.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.WarnAmber
import org.jetbrains.compose.ui.tooling.preview.Preview
import es.aviferdev.n3to.ui.theme.appColors

/**
 * Icon inside a colored rounded badge.
 * Matches the `Bdg` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * IconBadge(icon = Icons.Outlined.AccountBalance, color = MaterialTheme.appColors.income, size = 32.dp)
 * ```
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    color: Color = MaterialTheme.appColors.primary,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint        = color,
            modifier    = Modifier.size(size * 0.54f)
        )
    }
}

@Preview
@Composable
private fun IconBadgePreview() {
    N3toTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(icon = Icons.Outlined.AccountBalance, color = MaterialTheme.appColors.income)
            IconBadge(icon = Icons.Outlined.AccountBalance, color = MaterialTheme.appColors.warnAmber)
            IconBadge(icon = Icons.AutoMirrored.Outlined.ShowChart, color = MaterialTheme.appColors.primary, size = 48.dp)
        }
    }
}
