package es.aviferdev.n3to.ui.settings.emergencyfund.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro

@Composable
fun EmergencyFundProgressSection(
    savedAmount: Double,
    targetAmount: Double,
    progressPercent: Float
) {
    NavySectionCard {
        SectionLabel(text = "Estado del fondo")
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Ahorrado",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
                Text(
                    formatAmountEuro(savedAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.cyanAccent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Objetivo",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
                Text(
                    formatAmountEuro(targetAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.appColors.navyBorder, thickness = 0.5.dp)
        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.appColors.cyanAccent,
            trackColor = MaterialTheme.appColors.navySurfaceLight
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${(progressPercent * 100).toInt()}% del objetivo",
            fontSize = 11.sp,
            color = MaterialTheme.appColors.textSecondary
        )
    }
}
