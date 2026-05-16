package es.aviferdev.n3to.ui.reconciliation

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
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.SecondaryTeal
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ReconciliationReminderBanner(
    visible: Boolean,
    onReconcileNow: () -> Unit,
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
                .background(SecondaryTeal.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Outlined.Balance,
                contentDescription = null,
                tint               = SecondaryTeal,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text       = "Hace tiempo que no reconcilias el saldo de efectivo",
                fontSize   = 12.sp,
                color      = SecondaryTeal,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryTeal.copy(alpha = 0.15f))
                    .clickable { onReconcileNow() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "Ajustar",
                    fontSize   = 11.sp,
                    color      = SecondaryTeal,
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
                Text("×", fontSize = 16.sp, color = SecondaryTeal.copy(alpha = 0.6f))
            }
        }
    }
}

@Preview
@Composable
private fun ReconciliationReminderBannerVisiblePreview() {
    N3toTheme {
        ReconciliationReminderBanner(
            visible = true,
            onReconcileNow = {},
            onRemindLater = {}
        )
    }
}

@Preview
@Composable
private fun ReconciliationReminderBannerHiddenPreview() {
    N3toTheme {
        ReconciliationReminderBanner(
            visible = false,
            onReconcileNow = {},
            onRemindLater = {}
        )
    }
}
