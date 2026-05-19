package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val text = when {
        unavailable -> "Sin precio"
        amount == 0.0 -> "—"
        else -> "${if (amount >= 0) "+" else "−"} ${
            maskAmount(
                formatAmount(abs(amount)),
                masked
            )
        } €"
    }
    val color = when {
        unavailable -> Color.White.copy(.45f)
        amount > 0 -> MaterialTheme.appColors.pnlPositive
        amount < 0 -> MaterialTheme.appColors.pnlNegative
        else -> Color.White.copy(.55f)
    }
    Column {
        Text(label, fontSize = 9.sp, color = Color.White.copy(.45f))
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}