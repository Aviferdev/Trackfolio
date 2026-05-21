package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors

// ─── Empty card ───────────────────────────────────────────────────────────────
@Composable
fun EmptyCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.appColors.surface)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.appColors.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sin movimientos",
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pulsa + para registrar tu primera compra",
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}