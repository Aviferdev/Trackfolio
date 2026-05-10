package es.aviferdev.trackfolio.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TextDisabled
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.WarnAmber

@Composable
fun PriceReminderBanner(
    outdatedCount: Int,
    visible: Boolean,
    onUpdateNow: () -> Unit,
    onRemindLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = visible,
        enter    = expandVertically() + fadeIn(),
        exit     = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WarnAmber.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint               = WarnAmber,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = "$outdatedCount ${if (outdatedCount == 1) "activo sin" else "activos sin"} precio actualizado",
                fontSize = 12.sp,
                color    = WarnAmber,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick          = onUpdateNow,
                contentPadding   = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text       = "Actualizar",
                    fontSize   = 11.sp,
                    color      = WarnAmber,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick        = onRemindLater,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("×", fontSize = 16.sp, color = WarnAmber.copy(alpha = 0.6f))
            }
        }
    }
}
