package es.aviferdev.trackfolio.ui.portfolio.assethistory

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
import es.aviferdev.trackfolio.ui.portfolio.formatFullDate
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import kotlinx.datetime.Clock

// ─── Maturity date card ───────────────────────────────────────────────────────
@Composable
fun MaturityDateCard(maturityDate: Long, modifier: Modifier = Modifier) {
    val isExpired = maturityDate < Clock.System.now().toEpochMilliseconds()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) ExpenseRed.copy(.08f) else SurfaceWhite
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
                Text("Vencimiento", fontSize = 10.sp, color = TextTertiary)
                Spacer(Modifier.height(2.dp))
                Text(
                    formatFullDate(maturityDate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isExpired) ExpenseRed else TextPrimary
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