package es.aviferdev.n3to.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AppCurrency
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Standard row layout inside cards: left slot → fluid mid → right slot.
 * Optionally shows a bottom divider unless [isLast] is true.
 * Matches the `Row` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * InfoRow(
 *     left    = { InitialsAvatar("A", MaterialTheme.appColors.income) },
 *     mid     = { Column { Text("Label"); Text("Subtitle") } },
 *     right   = { Text("+123 €") },
 *     isLast  = false,
 *     onClick = { navigate() }
 * )
 * ```
 */
@Composable
fun InfoRow(
    left: @Composable () -> Unit,
    mid: @Composable () -> Unit,
    right: @Composable () -> Unit,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            left()
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) { mid() }
            Spacer(Modifier.width(8.dp))
            right()
        }
        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.appColors.border,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun InfoRowPreview() {
    N3toTheme {
        Column {
            InfoRow(
                left = { InitialsAvatar("A", bgColor = MaterialTheme.appColors.income) },
                mid = {
                    Column {
                        Text(
                            "Apple Inc.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Text(
                            "10 acciones",
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    }
                },
                right = {
                    Text(
                        "+1.234,56 ${AppCurrency.EUR.symbol}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.income
                    )
                },
                isLast = false,
                onClick = {}
            )
            InfoRow(
                left = { InitialsAvatar("G", bgColor = MaterialTheme.appColors.expense) },
                mid = {
                    Column {
                        Text(
                            "Gas Natural",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Text(
                            "5 acciones",
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    }
                },
                right = {
                    Text(
                        "-456,78 ${AppCurrency.EUR.symbol}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.expense
                    )
                },
                isLast = true,
                onClick = {}
            )
        }
    }
}
