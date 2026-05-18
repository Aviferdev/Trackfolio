package es.aviferdev.n3to.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.WarnAmber
import androidx.compose.ui.tooling.preview.Preview

/**
 * Contextual alert banner with icon, label, optional action button, and dismiss.
 * Matches the `Banner` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * AlertBanner(
 *     icon      = "↻",
 *     label     = "3 activos sin precio actualizado",
 *     actionLabel = "Actualizar",
 *     onAction  = { updatePrices() },
 *     onDismiss = { dismiss() },
 *     color     = WarnAmber
 * )
 * ```
 */
@Composable
fun AlertBanner(
    icon: String,
    label: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    color: Color = WarnAmber,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = icon,
            fontSize   = 16.sp,
            color      = color,
            modifier   = Modifier.padding(end = 10.dp)
        )
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            color      = color,
            modifier   = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text       = actionLabel,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                modifier   = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        if (onDismiss != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                text       = "×",
                fontSize   = 14.sp,
                color      = color,
                modifier   = Modifier.clickable { onDismiss() }
            )
        }
    }
}

@Preview
@Composable
private fun AlertBannerPreview() {
    N3toTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlertBanner(
                icon = "↻",
                label = "3 activos sin precio actualizado",
                actionLabel = "Actualizar",
                onAction = {},
                onDismiss = {},
                color = WarnAmber
            )
            AlertBanner(
                icon = "⚠️",
                label = "Error de conexión",
                color = ExpenseRed
            )
        }
    }
}
