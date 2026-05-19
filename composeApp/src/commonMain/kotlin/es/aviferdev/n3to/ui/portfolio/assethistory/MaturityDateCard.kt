package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.portfolio.components.formatFullDate
import es.aviferdev.n3to.ui.theme.ExpenseRed

// ─── Maturity date card ───────────────────────────────────────────────────────
@Composable
fun MaturityDateCard(maturityDate: Long, modifier: Modifier = Modifier) {
    val isExpired = maturityDate < nowMillis()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) ExpenseRed.copy(.08f) else MaterialTheme.appColors.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isExpired) "⏰" else "📅", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Vencimiento", fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
                Spacer(Modifier.height(2.dp))
                Text(
                    formatFullDate(maturityDate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isExpired) ExpenseRed else MaterialTheme.appColors.textPrimary
                )
                if (isExpired) Text(
                    "Vencido",
                    fontSize = 11.sp,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}