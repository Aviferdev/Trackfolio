package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import kotlin.math.abs

@Composable
fun PnLChip(
    label: String,
    amount: Double,
    masked: Boolean,
    unavailable: Boolean = false
) {
    val currency = LocalCurrencySymbol.current
    val text = when {
        unavailable -> "Sin precio"
        amount == 0.0 -> "—"
        else -> "${if (amount >= 0) "+" else "−"} ${
            maskAmount(
                formatAmount(abs(amount)),
                masked
            )
        } $currency"
    }
    val color = when {
        unavailable -> MaterialTheme.appColors.textTertiary
        amount > 0 -> MaterialTheme.appColors.pnlPositive
        amount < 0 -> MaterialTheme.appColors.pnlNegative
        else -> MaterialTheme.appColors.textSecondary
    }
    Column {
        Text(label, fontSize = 9.sp, color = MaterialTheme.appColors.textTertiary)
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}