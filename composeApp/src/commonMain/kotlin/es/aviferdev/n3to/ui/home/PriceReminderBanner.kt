package es.aviferdev.n3to.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.TextDisabled
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.WarnAmber
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                imageVector        = Icons.AutoMirrored.Outlined.ShowChart,
                contentDescription = null,
                tint               = WarnAmber,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = "$outdatedCount ${if (outdatedCount == 1) "activo sin" else "activos sin"} precio actualizado (>7 días)",
                fontSize = 12.sp,
                color    = WarnAmber,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarnAmber.copy(alpha = 0.15f))
                    .clickable { onUpdateNow() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "Actualizar",
                    fontSize   = 11.sp,
                    color      = WarnAmber,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRemindLater() }
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("×", fontSize = 16.sp, color = WarnAmber.copy(alpha = 0.6f))
            }
        }
    }
}

@Preview
@Composable
private fun PriceReminderBannerVisiblePreview() {
    N3toTheme {
        PriceReminderBanner(
            outdatedCount = 3,
            visible = true,
            onUpdateNow = {},
            onRemindLater = {}
        )
    }
}

@Preview
@Composable
private fun PriceReminderBannerHiddenPreview() {
    N3toTheme {
        PriceReminderBanner(
            outdatedCount = 0,
            visible = false,
            onUpdateNow = {},
            onRemindLater = {}
        )
    }
}
